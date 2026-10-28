package com.perfboost.mixin.client;

import com.perfboost.client.RenderState;
import com.perfboost.config.PerfConfig;
import net.minecraft.client.render.item.ItemRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/** Отключает glint зачарований у сущностей дальше заданной дистанции. */
@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {
    @ModifyVariable(method = {"getArmorGlintConsumer", "getItemGlintConsumer", "getDirectItemGlintConsumer"},
            at = @At("HEAD"), argsOnly = true, ordinal = 1)
    private static boolean perfboost$glint(boolean glint) {
        if (!glint) return false;
        double d = RenderState.currentDistSq;
        return d < 0 || d <= PerfConfig.limitSq(PerfConfig.get().glintDistance);
    }
}
