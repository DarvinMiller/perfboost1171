package com.perfboost.mixin.client;

import com.perfboost.client.PerfParticle;
import com.perfboost.client.ViewCull;
import com.perfboost.config.PerfConfig;
import com.perfboost.util.Stats;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.util.math.Box;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ParticleManager.class)
public abstract class ParticleManagerMixin {
    /**
     * Далёкие и находящиеся вне конуса обзора частицы в менеджер не добавляются (не тикают, не занимают память).
     * Метод void: фейерверки и др. используют возвращаемое значение addParticle(effect, ...), поэтому не null-им его.
     */
    @Inject(method = "addParticle(Lnet/minecraft/client/particle/Particle;)V", at = @At("HEAD"), cancellable = true)
    private void perfboost$skipSpawn(Particle particle, CallbackInfo ci) {
        PerfConfig c = PerfConfig.get();
        if (!c.offscreenCulling) return;
        Box b = particle.getBoundingBox();
        double x = (b.minX + b.maxX) * 0.5, y = b.minY, z = (b.minZ + b.maxZ) * 0.5;
        boolean far = c.particleMaxDistance < PerfConfig.UNLIMITED
                && ViewCull.distSq(x, y, z) > PerfConfig.limitSq(c.particleMaxDistance);
        if (far || ViewCull.outsideCone(x, y, z, 4.0, c.particleCullAngle)) {
            Stats.particlesNotSpawned++;
            ci.cancel();
        }
    }

    /** Частицы вне конуса обзора заморожены; далёкие не считают столкновения со стенами. */
    @Inject(method = "tickParticle", at = @At("HEAD"), cancellable = true)
    private void perfboost$tick(Particle particle, CallbackInfo ci) {
        PerfConfig c = PerfConfig.get();
        Box b = particle.getBoundingBox();
        double x = b.minX, y = b.minY, z = b.minZ;

        if (c.offscreenCulling && ViewCull.outsideCone(x, y, z, 3.0, c.particleCullAngle)) {
            Stats.particlesFrozen++;
            ci.cancel();
            return;
        }
        boolean far = c.particleCollisionDistance < PerfConfig.UNLIMITED
                && ViewCull.distSq(x, y, z) > PerfConfig.limitSq(c.particleCollisionDistance);
        ((PerfParticle) particle).perfboost$setFar(far);
    }
}
