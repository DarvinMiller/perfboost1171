package com.perfboost.client;

import org.lwjgl.opengl.GL11;

import java.util.regex.Pattern;

/** Оценка мощности ПК для стартового уровня «Рекомендуемого» (0 — мощный, 2 — слабый). Вызывать из render-потока. */
public final class HardwareProfile {
    private static final Pattern WEAK_GPU = Pattern.compile(
            "(?i).*(intel|uhd|hd graphics|vega|radeon r\\d|radeon hd|gt ?7\\d\\d|gt ?10\\d\\d|gtx ?10[456]\\d|gtx ?16[45]\\d|mx ?\\d{3}|mali|adreno|llvmpipe).*");

    private HardwareProfile() {}

    public static int baselineLevel() {
        int score = 0;
        if (Runtime.getRuntime().availableProcessors() <= 4) score++;
        if (Runtime.getRuntime().maxMemory() < 3500L * 1024 * 1024) score++;
        try {
            String gpu = GL11.glGetString(GL11.GL_RENDERER);
            if (gpu != null && WEAK_GPU.matcher(gpu).matches()) score++;
        } catch (Throwable ignored) {
        }
        return Math.min(2, score);
    }
}
