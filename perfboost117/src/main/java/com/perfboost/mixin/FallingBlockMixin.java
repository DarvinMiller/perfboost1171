package com.perfboost.mixin;

import com.perfboost.config.PerfConfig;
import net.minecraft.block.BlockState;
import net.minecraft.block.FallingBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * «Сон» гравитационных блоков: тик падения планируется только если изменился блок
 * ПОД ним и он стал проходимым. Обновления от боковых/верхних соседей и от твёрдого
 * блока снизу больше не создают scheduled tick.
 */
@Mixin(FallingBlock.class)
public abstract class FallingBlockMixin {
    @Inject(method = "getStateForNeighborUpdate", at = @At("HEAD"), cancellable = true)
    private void perfboost$sleep(BlockState state, Direction direction, BlockState neighborState,
                                 WorldAccess world, BlockPos pos, BlockPos neighborPos,
                                 CallbackInfoReturnable<BlockState> cir) {
        if (!PerfConfig.get().gravitySleep) return;
        if (direction != Direction.DOWN || !FallingBlock.canFallThrough(neighborState)) {
            cir.setReturnValue(state);
        }
    }
}
