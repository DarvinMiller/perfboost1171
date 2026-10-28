package com.perfboost.client;

import com.perfboost.config.PerfConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.LiteralText;

/**
 * Тест FPS: 4 фазы по (5 с прогрев + 10 с замер): С модом, БЕЗ мода, С модом, БЕЗ мода — порядок не влияет на итог.
 * Во время «без мода» сняты все ограничения и возвращены ваши настройки игры. Итог в чат.
 */
public final class Benchmark {
    private static final int SETTLE = 5, MEASURE = 10, PHASES = 4;
    private static final int[] secFps = new int[MEASURE];

    public static boolean running;
    private static PerfConfig saved, disabled;
    private static int phase, frames, lastFrames, secIdx;
    private static long phaseStart, secStart;
    private static boolean measuring;
    private static int onSum, offSum, onSecs, offSecs, onMin, offMin;

    private Benchmark() {}

    public static void onFrame() {
        if (running) frames++;
    }

    public static void start(MinecraftClient mc) {
        if (running) return;
        saved = PerfConfig.get();
        disabled = PerfConfig.disabledCopy(saved);
        PerfConfig.suspendSave = true;
        running = true;
        onSum = offSum = onSecs = offSecs = 0;
        onMin = offMin = Integer.MAX_VALUE;
        beginPhase(0);
        say(mc, "§e[PerfBoost] Тест FPS: не двигайтесь и не открывайте меню ~" + (PHASES * (SETTLE + MEASURE)) + " с.");
    }

    /** phase 0..3: чётные — с модом, нечётные — без. */
    private static void beginPhase(int p) {
        phase = p;
        measuring = false;
        phaseStart = System.nanoTime();
        frames = 0;
        lastFrames = 0;
        secIdx = 0;
        PerfConfig.swap(p % 2 == 0 ? saved : disabled);
        GameSettingsTuner.forceNext();
    }

    private static void finish(MinecraftClient mc, boolean ok) {
        PerfConfig.swap(saved);
        PerfConfig.suspendSave = false;
        running = false;
        GameSettingsTuner.forceNext();
        PerfConfig.save();
        if (!ok) {
            say(mc, "§c[PerfBoost] Тест прерван (открыто меню или вы вышли из мира). Настройки восстановлены.");
            return;
        }
        double on = onSecs > 0 ? onSum / (double) onSecs : 0, off = offSecs > 0 ? offSum / (double) offSecs : 0;
        double diff = off > 0 ? (on - off) / off * 100.0 : 0;
        say(mc, String.format("§a[PerfBoost] С модом: %.0f FPS (мин. %d) | Без мода: %.0f FPS (мин. %d) | Разница: %+.1f%%",
                on, onMin, off, offMin, diff));
    }

    public static void tick(MinecraftClient mc) {
        if (!running) return;
        if (mc.world == null || mc.currentScreen != null || mc.isPaused()) {
            finish(mc, false);
            return;
        }
        long now = System.nanoTime();
        if (!measuring) {
            if (now - phaseStart >= SETTLE * 1_000_000_000L) {
                measuring = true;
                frames = 0;
                lastFrames = 0;
                secIdx = 0;
                secStart = now;
            }
            return;
        }
        if (now - secStart >= 1_000_000_000L && secIdx < MEASURE) {
            secFps[secIdx++] = frames - lastFrames;
            lastFrames = frames;
            secStart += 1_000_000_000L;
        }
        if (secIdx < MEASURE) return;
        int sum = 0, min = Integer.MAX_VALUE;
        for (int i = 0; i < MEASURE; i++) {
            sum += secFps[i];
            if (secFps[i] < min) min = secFps[i];
        }
        if (phase % 2 == 0) {
            onSum += sum; onSecs += MEASURE;
            if (min < onMin) onMin = min;
        } else {
            offSum += sum; offSecs += MEASURE;
            if (min < offMin) offMin = min;
        }
        if (phase + 1 >= PHASES) finish(mc, true);
        else beginPhase(phase + 1);
    }

    private static void say(MinecraftClient mc, String msg) {
        if (mc.inGameHud != null) mc.inGameHud.getChatHud().addMessage(new LiteralText(msg));
    }
}
