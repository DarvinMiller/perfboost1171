package com.perfboost.mixin.client;

import com.perfboost.client.RenderState;
import com.perfboost.client.ViewCull;
import com.perfboost.config.PerfConfig;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.render.entity.feature.CapeFeatureRenderer;
import net.minecraft.client.render.entity.feature.ElytraFeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.HeadFeatureRenderer;
import net.minecraft.client.render.entity.feature.HeldItemFeatureRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Модуль 4: единая точка отсечения слоёв сущности (броня, предметы в руках, плащ/элитры)
 * по дистанции до камеры + запоминание дистанции для отсечения glint.
 */
@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin {
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Redirect(
            method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/render/entity/feature/FeatureRenderer;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/entity/Entity;FFFFFF)V"))
    private void perfboost$feature(FeatureRenderer self, MatrixStack matrices, VertexConsumerProvider vcp,
                                   int light, Entity entity, float a, float b, float c, float d, float e, float f) {
        PerfConfig cfg = PerfConfig.get();
        double distSq = RenderState.distSq(entity);

        int limit = PerfConfig.UNLIMITED;
        boolean detail = true;
        if (self instanceof ArmorFeatureRenderer || self instanceof HeadFeatureRenderer) limit = cfg.armorDistance;
        else if (self instanceof HeldItemFeatureRenderer) limit = cfg.weaponDistance;
        else if (self instanceof CapeFeatureRenderer || self instanceof ElytraFeatureRenderer) limit = cfg.capeDistance;
        else detail = false;

        if (distSq > PerfConfig.limitSq(limit)) return;
        // Сбоку/сзади — упрощаем до силуэта: без брони, предметов в руках и плащей.
        if (detail && cfg.offscreenCulling
                && ViewCull.outsideCone(entity.getX(), entity.getY(), entity.getZ(), 6.0, cfg.layerCullAngle)) return;

        RenderState.currentDistSq = distSq;
        try {
            self.render(matrices, vcp, light, entity, a, b, c, d, e, f);
        } finally {
            RenderState.currentDistSq = -1;
        }
    }
}
