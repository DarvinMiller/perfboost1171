package com.perfboost.util;

/** Счётчики работы мода (для окна статистики). Только инкременты примитивов, без аллокаций. */
public final class Stats {
    public static int particlesNotSpawned, particlesNotDrawn, particlesFrozen;
    public static int entitiesFrozen, entitiesLod, entitiesHidden;
    public static int shadowsSkipped, blockEntitiesSkipped, itemTicksSkipped;
    private Stats() {}
}
