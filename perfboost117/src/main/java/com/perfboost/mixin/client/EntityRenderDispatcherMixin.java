package com.perfboost.mixin.client;

import com.perfboost.client.ViewCull;
import com.perfboost.config.PerfConfig;
import com.perfboost.util.Stats;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.decoration.painting.PaintingEntity;
import net.minecraft.world.WorldView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin {
    /** Пауза: скрываем мобов/игроков/предметы. Дальность: предметы, опыт, рамки, стойки, картины. */
    @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
    private void perfboost$hide(Entity entity, Frustum frustum, double x, double y, double z,
                                CallbackInfoReturnable<Boolean> cir) {
        PerfConfig c = PerfConfig.get();
        if (c.hideEntitiesOnPause
                && MinecraftClient.getInstance().currentScreen instanceof GameMenuScreen
                && (entity instanceof LivingEntity || entity instanceof ItemEntity)) {
            Stats.entitiesHidden++;
            cir.setReturnValue(false);
            return;
        }
        int limit = PerfConfig.UNLIMITED;
        if (entity instanceof ItemEntity || entity instanceof ExperienceOrbEntity) limit = c.itemRenderDistance;
        else if (entity instanceof ItemFrameEntity || entity instanceof ArmorStandEntity
                || entity instanceof PaintingEntity) limit = c.frameDistance;
        if (limit < PerfConfig.UNLIMITED
                && ViewCull.distSq(entity.getX(), entity.getY(), entity.getZ()) > PerfConfig.limitSq(limit)) {
            Stats.entitiesHidden++;
            cir.setReturnValue(false);
        }
    }

    /** Тени сущностей: не рисуем дальше заданной дистанции. */
    @Inject(method = "renderShadow", at = @At("HEAD"), cancellable = true, require = 0)
    private static void perfboost$shadow(MatrixStack matrices, VertexConsumerProvider vcp, Entity entity,
                                         float opacity, float tickDelta, WorldView world, float radius, CallbackInfo ci) {
        int limit = PerfConfig.get().shadowDistance;
        if (limit < PerfConfig.UNLIMITED
                && ViewCull.distSq(entity.getX(), entity.getY(), entity.getZ()) > PerfConfig.limitSq(limit)) {
            Stats.shadowsSkipped++;
            ci.cancel();
        }
    }
}
