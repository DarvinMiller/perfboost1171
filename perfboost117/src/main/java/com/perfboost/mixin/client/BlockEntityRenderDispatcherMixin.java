package com.perfboost.mixin.client;

import com.perfboost.client.ViewCull;
import com.perfboost.config.PerfConfig;
import com.perfboost.util.Stats;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Сундуки, таблички, баннеры, головы и прочие блочные сущности дальше лимита не рисуются. */
@Mixin(BlockEntityRenderDispatcher.class)
public abstract class BlockEntityRenderDispatcherMixin {
    @Inject(method = "render(Lnet/minecraft/block/entity/BlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;)V",
            at = @At("HEAD"), cancellable = true)
    private void perfboost$cull(BlockEntity be, float tickDelta, MatrixStack matrices, VertexConsumerProvider vcp,
                                CallbackInfo ci) {
        int limit = PerfConfig.get().blockEntityDistance;
        if (limit >= PerfConfig.UNLIMITED) return;
        BlockPos p = be.getPos();
        if (ViewCull.distSq(p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5) > PerfConfig.limitSq(limit)) {
            Stats.blockEntitiesSkipped++;
            ci.cancel();
        }
    }
}
