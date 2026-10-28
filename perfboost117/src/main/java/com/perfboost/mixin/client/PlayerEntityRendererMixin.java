package com.perfboost.mixin.client;

import com.perfboost.client.RenderState;
import com.perfboost.config.PerfConfig;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Скрывает внешние слои скина (шляпа, куртка, рукава, штаны) у далёких игроков. */
@Mixin(PlayerEntityRenderer.class)
public abstract class PlayerEntityRendererMixin {
    @Inject(method = "setModelPose", at = @At("RETURN"))
    private void perfboost$skinLayers(AbstractClientPlayerEntity player, CallbackInfo ci) {
        int limit = PerfConfig.get().skinLayerDistance;
        if (limit >= PerfConfig.UNLIMITED) return;
        if (RenderState.distSq(player) > PerfConfig.limitSq(limit)) {
            PlayerEntityModel<?> m = ((PlayerEntityRenderer) (Object) this).getModel();
            m.hat.visible = false;
            m.jacket.visible = false;
            m.leftSleeve.visible = false;
            m.rightSleeve.visible = false;
            m.leftPants.visible = false;
            m.rightPants.visible = false;
        }
    }
}
