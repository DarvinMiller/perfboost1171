package com.perfboost.mixin.client;

import com.perfboost.client.PerfParticle;
import net.minecraft.client.particle.Particle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Particle.class)
public abstract class ParticleMixin implements PerfParticle {
    @Shadow protected boolean collidesWithWorld;

    @Unique private boolean perfboost$far;
    @Unique private boolean perfboost$origCollide;

    @Override
    public void perfboost$setFar(boolean far) {
        if (far == perfboost$far) return;
        if (far) {
            perfboost$origCollide = collidesWithWorld;
            collidesWithWorld = false;
        } else {
            collidesWithWorld = perfboost$origCollide;
        }
        perfboost$far = far;
    }
}
