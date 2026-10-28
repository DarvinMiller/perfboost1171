package com.perfboost.client;

import com.perfboost.config.PerfConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class PerfBoostClient implements ClientModInitializer {
    private static long lastNs;
    private static int frames, lowStreak, highStreak, warmup;
    private static boolean inited;

    @Override
    public void onInitializeClient() {
        KeyBinding open = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.perfboost.config", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_F9, "category.perfboost"));
        WorldRenderEvents.START.register(ctx -> {
            frames++;
            Benchmark.onFrame();
        });
        StatsHud.register();
        ClientLifecycleEvents.CLIENT_STOPPING.register(GameSettingsTuner::restore);
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (open.wasPressed()) {
                client.setScreen(new ConfigScreen(client.currentScreen));
            }
            Benchmark.tick(client);
            GameSettingsTuner.tick(client);
            adapt(client);
        });
    }

    /** Авто-подбор: раз в 3 секунды игры сравниваем FPS с целью и сдвигаем уровень на 1 шаг. */
    private static void adapt(MinecraftClient client) {
        if (Benchmark.running) {
            lastNs = 0;
            return;
        }
        PerfConfig c = PerfConfig.get();
        if (!c.recommended) {
            inited = false;
            lastNs = 0;
            return;
        }
        if (!inited) {
            inited = true;
            if (c.recommendedLevel < 0) c.recommendedLevel = HardwareProfile.baselineLevel();
            c.applyLevel(c.recommendedLevel);
            PerfConfig.save();
        }
        if (client.world == null || client.currentScreen != null || client.isPaused()) {
            lastNs = 0;
            frames = 0;
            warmup = 0;
            return;
        }
        long now = System.nanoTime();
        if (lastNs == 0) {
            lastNs = now;
            frames = 0;
            return;
        }
        long dt = now - lastNs;
        if (dt < 3_000_000_000L) return;
        double fps = frames * 1.0e9 / dt;
        frames = 0;
        lastNs = now;
        if (warmup < 2) {
            warmup++;
            return;
        }
        if (fps < c.targetFps * 0.7) {
            highStreak = 0;
            if (++lowStreak >= 2 && c.recommendedLevel < 3) {
                c.applyLevel(c.recommendedLevel + 1);
                lowStreak = 0;
                PerfConfig.save();
            }
        } else if (fps > c.targetFps * 1.6) {
            lowStreak = 0;
            if (++highStreak >= 3 && c.recommendedLevel > 0) {
                c.applyLevel(c.recommendedLevel - 1);
                highStreak = 0;
                PerfConfig.save();
            }
        } else {
            lowStreak = 0;
            highStreak = 0;
        }
    }
}
