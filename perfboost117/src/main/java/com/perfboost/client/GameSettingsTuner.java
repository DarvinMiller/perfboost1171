package com.perfboost.client;

import com.perfboost.config.PerfConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.ParticlesMode;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;

/**
 * Авто-подбор настроек самой игры в режиме «Рекомендуемое»: дальность прорисовки и симуляции, масштаб дальности
 * сущностей, режим частиц. Под землёй (нет неба над игроком) дальность ещё меньше. Значения игрока запоминаются
 * (и в конфиге — на случай сбоя) и возвращаются при выключении, выходе из мира и закрытии игры.
 * Опции только уменьшаются относительно ваших: выше вашего значения мод ничего не ставит.
 */
public final class GameSettingsTuner {
    private static final int MAX = Integer.MAX_VALUE;
    private static final int[] VIEW_CAP = {MAX, 12, 10, 8};
    private static final int[] VIEW_UG = {10, 8, 7, 6};
    private static final int[] SIM_CAP = {MAX, 10, 8, 6};
    private static final int[] SIM_UG = {10, 8, 6, 5};
    private static final int[] ENT_CAP = {MAX, 100, 75, 50};
    private static final int[] PART_MIN = {0, 0, 1, 2};

    private static int origView = -1, origSim = -1, origEnt = -1, origPart = -1;
    private static int lastView, lastSim, lastEnt, lastPart;
    private static boolean applied, force, underground, startupChecked;
    private static int ugCount, surfCount, cooldown, tickCounter;

    private GameSettingsTuner() {}

    /** Следующий вызов tick сработает сразу, без пауз и задержек (смена режима, тест FPS). */
    public static void forceNext() {
        force = true;
    }

    public static void tick(MinecraftClient c) {
        if (c.options == null) return;
        if (!startupChecked) {
            startupChecked = true;
            PerfConfig real = PerfConfig.real();
            if (real.origViewDistance >= 0 || real.origSimDistance >= 0
                    || real.origEntityPct >= 0 || real.origParticles >= 0) {
                origView = real.origViewDistance; origSim = real.origSimDistance;
                origEnt = real.origEntityPct; origPart = real.origParticles;
                restore(c);
            }
        }
        if (!force && ++tickCounter < 20) return;
        tickCounter = 0;
        boolean f = force;
        force = false;

        PerfConfig cfg = PerfConfig.get();
        boolean active = cfg.recommended && cfg.autoGameSettings && c.world != null && c.player != null;
        if (!active) {
            if (applied) restore(c);
            ugCount = surfCount = 0;
            underground = false;
            return;
        }
        GameOptions o = c.options;
        int view = o.viewDistance;
        int sim = 0; // в 1.17.1 нет дальности симуляции
        int ent = (int) Math.round(o.entityDistanceScaling * 100.0);
        int part = o.particles.ordinal();

        boolean dirty = false;
        if (!applied) {
            origView = view; origSim = sim; origEnt = ent; origPart = part;
            applied = true;
            dirty = true;
        } else {
            // Игрок (или другой мод) поменял значение сам — это новый «оригинал».
            if (view != lastView) { origView = view; dirty = true; }
            if (sim != lastSim) { origSim = sim; dirty = true; }
            if (ent != lastEnt) { origEnt = ent; dirty = true; }
            if (part != lastPart) { origPart = part; dirty = true; }
        }

        BlockPos pos = c.player.getBlockPos();
        boolean dark = pos.getY() < 50 && c.world.getLightLevel(LightType.SKY, pos) == 0;
        if (dark) { ugCount++; surfCount = 0; } else { surfCount++; ugCount = 0; }
        if (f) underground = dark;
        else if (ugCount >= 3) underground = true;
        else if (surfCount >= 2) underground = false;

        int lvl = Math.max(0, Math.min(3, cfg.recommendedLevel));
        int tView = Math.max(2, Math.min(origView, Math.min(VIEW_CAP[lvl], underground ? VIEW_UG[lvl] : MAX)));
        int tSim = sim;
        int tEnt = Math.min(origEnt, ENT_CAP[lvl]);
        int tPart = Math.max(origPart, PART_MIN[lvl]);

        if (!f && cooldown > 0 && !dirty) {
            cooldown--;
            return;
        }
        boolean changed = false;
        if (tView != view) { o.viewDistance = tView; c.worldRenderer.scheduleTerrainUpdate(); changed = true; }
        if (tEnt != ent) { o.entityDistanceScaling = tEnt / 100.0f; changed = true; }
        if (tPart != part) { o.particles = ParticlesMode.values()[tPart]; changed = true; }

        lastView = o.viewDistance;
        lastSim = 0;
        lastEnt = (int) Math.round(o.entityDistanceScaling * 100.0);
        lastPart = o.particles.ordinal();
        if (changed) cooldown = 12;
        if (dirty) persist();
    }

    /** Вернуть значения игрока (выключение режима, выход из мира, закрытие игры). */
    public static void restore(MinecraftClient c) {
        if (c.options == null) return;
        GameOptions o = c.options;
        if (origView >= 0) { o.viewDistance = origView; c.worldRenderer.scheduleTerrainUpdate(); }
        if (origEnt >= 0) o.entityDistanceScaling = origEnt / 100.0f;
        if (origPart >= 0 && origPart < ParticlesMode.values().length) {
            o.particles = ParticlesMode.values()[origPart];
        }
        origView = origSim = origEnt = origPart = -1;
        applied = false;
        persist();
    }

    private static void persist() {
        PerfConfig real = PerfConfig.real();
        real.origViewDistance = origView;
        real.origSimDistance = origSim;
        real.origEntityPct = origEnt;
        real.origParticles = origPart;
        PerfConfig.save();
    }
}
