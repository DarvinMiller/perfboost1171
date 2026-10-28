package com.perfboost.client;

import com.perfboost.config.PerfConfig;
import com.perfboost.util.Stats;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;

/** Окно статистики: счётчики за секунду. Строки и StringBuilder переиспользуются, пересборка только при изменении чисел. */
public final class StatsHud {
    private static final int N = 9;
    private static final String[] lines = new String[N];
    private static final int[] shown = new int[N];
    private static final int[] cur = new int[N];
    private static final StringBuilder sb = new StringBuilder(64);
    private static final String[] LABELS = {
        "PerfBoost (за сек.)", "Частиц не добавлено: ", "Частиц заморожено: ", "Частиц не нарисовано: ",
        "Сущностей заморожено: ", "LOD мобов: ", "Сущностей скрыто: ", "Теней пропущено: ", "Блочных сущ. скрыто: "
    };
    private static long lastNs;
    private static int itemTicks = -1, itemTicksShown = -1;
    private static String itemLine = "";

    private StatsHud() {}

    public static void register() {
        for (int i = 0; i < N; i++) shown[i] = -1;
        HudRenderCallback.EVENT.register((matrices, tickDelta) -> {
            PerfConfig c = PerfConfig.get();
            MinecraftClient mc = MinecraftClient.getInstance();
            if (!c.showStats || mc.options.hudHidden || mc.options.debugEnabled) return;
            long now = System.nanoTime();
            if (now - lastNs > 1_000_000_000L) {
                lastNs = now;
                cur[1] = Stats.particlesNotSpawned; cur[2] = Stats.particlesFrozen; cur[3] = Stats.particlesNotDrawn;
                cur[4] = Stats.entitiesFrozen; cur[5] = Stats.entitiesLod; cur[6] = Stats.entitiesHidden;
                cur[7] = Stats.shadowsSkipped; cur[8] = Stats.blockEntitiesSkipped;
                itemTicks = Stats.itemTicksSkipped;
                Stats.particlesNotSpawned = Stats.particlesNotDrawn = Stats.particlesFrozen = 0;
                Stats.entitiesFrozen = Stats.entitiesLod = Stats.entitiesHidden = 0;
                Stats.shadowsSkipped = Stats.blockEntitiesSkipped = Stats.itemTicksSkipped = 0;
                if (lines[0] == null) lines[0] = LABELS[0];
                for (int i = 1; i < N; i++) {
                    if (cur[i] != shown[i] || lines[i] == null) {
                        shown[i] = cur[i];
                        sb.setLength(0);
                        sb.append(LABELS[i]).append(cur[i]);
                        lines[i] = sb.toString();
                    }
                }
                if (itemTicks != itemTicksShown) {
                    itemTicksShown = itemTicks;
                    sb.setLength(0);
                    sb.append("Тиков предметов пропущено: ").append(itemTicks);
                    itemLine = sb.toString();
                }
            }
            if (lines[0] == null) return;
            int y = 4;
            for (int i = 0; i < N; i++) {
                mc.textRenderer.drawWithShadow(matrices, lines[i], 4, y, 0xFFFFFF);
                y += 10;
            }
            mc.textRenderer.drawWithShadow(matrices, itemLine, 4, y, 0xFFFFFF);
        });
    }
}
