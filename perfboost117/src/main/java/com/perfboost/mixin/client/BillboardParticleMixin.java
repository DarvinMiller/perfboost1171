package com.perfboost.mixin.client;

import com.perfboost.client.ViewCull;
import com.perfboost.config.PerfConfig;
import com.perfboost.util.Stats;
import net.minecraft.client.particle.BillboardParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.util.math.Box;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Не отправляем в GPU геометрию частиц вне конуса обзора. */
@Mixin(BillboardParticle.class)
public abstract class BillboardParticleMixin {
    @Inject(method = "buildGeometry", at = @At("HEAD"), cancellable = true)
    private void perfboost$cull(VertexConsumer vc, Camera camera, float tickDelta, CallbackInfo ci) {
        PerfConfig c = PerfConfig.get();
        Box b0 = ((Particle) (Object) this).getBoundingBox();
        if (c.particleMaxDistance < PerfConfig.UNLIMITED
                && ViewCull.distSq(b0.minX, b0.minY, b0.minZ) > PerfConfig.limitSq(c.particleMaxDistance)) {
            Stats.particlesNotDrawn++;
            ci.cancel();
            return;
        }
        if (!c.offscreenCulling) return;
        // x/y/z объявлены в Particle (суперкласс), @Shadow их здесь не найдёт — берём из bounding box.
        Box b = ((Particle) (Object) this).getBoundingBox();
        if (ViewCull.outsideCone(b.minX, b.minY, b.minZ, 3.0, c.particleCullAngle)) {
            Stats.particlesNotDrawn++;
            ci.cancel();
        }
    }
}
