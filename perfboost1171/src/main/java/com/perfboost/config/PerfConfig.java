package com.perfboost.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Конфиг мода (config/perfboost.json). Дистанции в блоках; 128 = без ограничения. */
public final class PerfConfig {
    public static final int UNLIMITED = 128;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static PerfConfig instance;
    private static PerfConfig realInstance;

    public int armorDistance = 48;
    public int weaponDistance = 48;
    public int capeDistance = 48;
    public int skinLayerDistance = 32;
    public int glintDistance = 24;

    public boolean hideEntitiesOnPause = true;
    public boolean itemTickCap = true;
    public int itemNearDistance = 16;
    public int itemFarInterval = 5;
    public boolean gravitySleep = true;

    /** Отсечение/заморозка вне конуса обзора; частицы вне обзора и далеко не создаются вообще. */
    public boolean offscreenCulling = true;
    public int particleCullAngle = 100;
    public int layerCullAngle = 65;
    public int particleCollisionDistance = 32;
    public int entityFreezeDistance = 32;
    public int particleMaxDistance = 48;
    public int itemRenderDistance = 32;

    /** Тени, блочные сущности, рамки/стойки/картины, LOD анимаций, окно статистики. */
    public int shadowDistance = 12;
    public int blockEntityDistance = 48;
    public int frameDistance = 48;
    public boolean lodAnimation = true;
    public boolean showStats = false;

    /** Авто-настройки игры (дальность прорисовки/симуляции, масштаб сущностей, частицы) в режиме «Рекомендуемое». */
    public boolean autoGameSettings = true;
    /** Оригинальные значения игрока, пока мод их изменил (для восстановления после сбоя); -1 = нет. */
    public int origViewDistance = -1, origSimDistance = -1, origEntityPct = -1, origParticles = -1;

    /** «Рекомендуемое»: мод сам подбирает настройки под железо и текущий FPS. */
    public boolean recommended = true;
    public int recommendedLevel = -1;
    public int targetFps = 90;
    /** Массивы бэкапа ручных значений создаются один раз и переиспользуются (не обнуляются). */
    public int[] manualBackup;
    public boolean[] manualFlags;
    public boolean hasBackup;

    /** На время теста FPS запись конфига на диск приостановлена. */
    public static boolean suspendSave;

    // броня, оружие, плащи, скин, glint, колл. частиц, заморозка сущн., дальн. частиц, дальн. предметов,
    // угол частиц, угол слоёв, интервал тиков предметов, тени, блочные сущности, рамки
    private static final int[][] PRESETS = {
        {64, 64, 64, 48, 32, 48, 48, 64, 48, 110, 75, 3, 16, 64, 64},
        {48, 48, 48, 32, 24, 32, 32, 48, 32, 100, 65, 5, 12, 48, 48},
        {32, 32, 32, 20, 16, 24, 24, 32, 24, 90, 55, 8, 8, 32, 32},
        {20, 20, 20, 12, 8, 16, 16, 24, 16, 80, 45, 10, 6, 24, 24}
    };
    private static final int N_INT = 15, N_FLAG = 5;

    public static PerfConfig get() {
        if (instance == null) load();
        return instance;
    }

    private static Path path() {
        return FabricLoader.getInstance().getConfigDir().resolve("perfboost.json");
    }

    public static synchronized void load() {
        try {
            Path p = path();
            if (Files.exists(p)) instance = GSON.fromJson(Files.readString(p), PerfConfig.class);
        } catch (Exception ignored) {
        }
        if (instance == null) instance = new PerfConfig();
        realInstance = instance;
        save();
    }

    /** Настоящий конфиг игрока (не подменённый на время теста). */
    public static PerfConfig real() {
        if (realInstance == null) load();
        return realInstance;
    }

    public static synchronized void save() {
        if (suspendSave || instance == null) return;
        try {
            Files.writeString(path(), GSON.toJson(instance));
        } catch (IOException ignored) {
        }
    }

    public synchronized void applyLevel(int level) {
        level = Math.max(0, Math.min(PRESETS.length - 1, level));
        int[] p = PRESETS[level];
        armorDistance = p[0]; weaponDistance = p[1]; capeDistance = p[2]; skinLayerDistance = p[3];
        glintDistance = p[4]; particleCollisionDistance = p[5]; entityFreezeDistance = p[6];
        particleMaxDistance = p[7]; itemRenderDistance = p[8]; particleCullAngle = p[9];
        layerCullAngle = p[10]; itemFarInterval = p[11];
        shadowDistance = p[12]; blockEntityDistance = p[13]; frameDistance = p[14];
        hideEntitiesOnPause = true; itemTickCap = true; gravitySleep = true; offscreenCulling = true;
        lodAnimation = true;
        recommendedLevel = level;
    }

    private void snapshot() {
        if (manualBackup == null || manualBackup.length != N_INT) manualBackup = new int[N_INT];
        if (manualFlags == null || manualFlags.length != N_FLAG) manualFlags = new boolean[N_FLAG];
        int[] b = manualBackup;
        b[0] = armorDistance; b[1] = weaponDistance; b[2] = capeDistance; b[3] = skinLayerDistance;
        b[4] = glintDistance; b[5] = particleCollisionDistance; b[6] = entityFreezeDistance;
        b[7] = particleMaxDistance; b[8] = itemRenderDistance; b[9] = particleCullAngle;
        b[10] = layerCullAngle; b[11] = itemFarInterval; b[12] = shadowDistance;
        b[13] = blockEntityDistance; b[14] = frameDistance;
        boolean[] f = manualFlags;
        f[0] = hideEntitiesOnPause; f[1] = itemTickCap; f[2] = gravitySleep; f[3] = offscreenCulling; f[4] = lodAnimation;
        hasBackup = true;
    }

    private void restore() {
        int[] b = manualBackup;
        if (hasBackup && b != null && b.length == N_INT) {
            armorDistance = b[0]; weaponDistance = b[1]; capeDistance = b[2]; skinLayerDistance = b[3];
            glintDistance = b[4]; particleCollisionDistance = b[5]; entityFreezeDistance = b[6];
            particleMaxDistance = b[7]; itemRenderDistance = b[8]; particleCullAngle = b[9];
            layerCullAngle = b[10]; itemFarInterval = b[11];
            shadowDistance = b[12]; blockEntityDistance = b[13]; frameDistance = b[14];
        }
        boolean[] f = manualFlags;
        if (hasBackup && f != null && f.length == N_FLAG) {
            hideEntitiesOnPause = f[0]; itemTickCap = f[1]; gravitySleep = f[2]; offscreenCulling = f[3];
            lodAnimation = f[4];
        }
    }

    public synchronized void enableRecommended(int baselineLevel) {
        if (!recommended || !hasBackup) snapshot();
        recommended = true;
        if (recommendedLevel < 0) recommendedLevel = baselineLevel;
        applyLevel(recommendedLevel);
        save();
    }

    public synchronized void disableRecommended() {
        if (recommended) restore();
        recommended = false;
        hasBackup = false;
        save();
    }

    /** Ручная правка: авто-режим выключается, значения остаются как есть. */
    public synchronized void touchManual() {
        recommended = false;
        hasBackup = false;
    }

    /** Замена активного конфига (для теста FPS); mixin'ы читают PerfConfig.get() каждый раз. */
    public static synchronized void swap(PerfConfig c) {
        instance = c;
    }

    /** Конфиг «мод выключен»: без ограничений дальности и без отсечений. */
    public static PerfConfig disabledCopy(PerfConfig base) {
        PerfConfig o = new PerfConfig();
        o.armorDistance = o.weaponDistance = o.capeDistance = o.skinLayerDistance = o.glintDistance = UNLIMITED;
        o.particleCollisionDistance = o.entityFreezeDistance = o.particleMaxDistance = UNLIMITED;
        o.itemRenderDistance = o.shadowDistance = o.blockEntityDistance = o.frameDistance = UNLIMITED;
        o.hideEntitiesOnPause = o.itemTickCap = o.gravitySleep = o.offscreenCulling = o.lodAnimation = false;
        o.recommended = false;
        o.autoGameSettings = false;
        o.showStats = base.showStats;
        return o;
    }

    public static double limitSq(int distance) {
        return distance >= UNLIMITED ? Double.MAX_VALUE : (double) distance * distance;
    }
}
