package com.perfboost.mixin;

import com.perfboost.config.PerfConfig;
import com.perfboost.util.Stats;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Лимит тиков лежащих предметов: если предмет покоится на земле и рядом нет игроков,
 * полный тик (физика/слияние) выполняется раз в N тиков. Возраст и задержка подбора
 * продолжают идти, поэтому время despawn не меняется.
 */
@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin {
    @Shadow private int itemAge;
    @Shadow private int pickupDelay;

    @Unique private int perfboost$interval = 1;

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void perfboost$cap(CallbackInfo ci) {
        PerfConfig cfg = PerfConfig.get();
        if (!cfg.itemTickCap) return;
        ItemEntity self = (ItemEntity) (Object) this;

        if ((self.age + self.getId()) % 20 == 0) {
            PlayerEntity near = self.world.getClosestPlayer(self, cfg.itemNearDistance);
            perfboost$interval = near == null ? Math.max(1, cfg.itemFarInterval) : 1;
        }
        if (perfboost$interval <= 1) return;

        if (!self.isOnGround() || self.isTouchingWater() || self.isInLava() || self.isOnFire()
                || self.getVelocity().lengthSquared() > 1.0E-6 || itemAge >= 5990) return;
        if ((self.age + self.getId()) % perfboost$interval == 0) return;

        if (pickupDelay > 0 && pickupDelay != 32767) --pickupDelay;
        if (itemAge != -32768) ++itemAge;
        Stats.itemTicksSkipped++;
        ci.cancel();
    }
}
