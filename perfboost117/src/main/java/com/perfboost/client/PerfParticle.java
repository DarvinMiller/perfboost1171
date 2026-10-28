package com.perfboost.client;

/** Реализуется миксином в Particle: временное отключение столкновений для далёких частиц. */
public interface PerfParticle {
    void perfboost$setFar(boolean far);
}
