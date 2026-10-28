package com.perfboost.mixin.client;

import com.perfboost.client.ViewCull;
import com.perfboost.config.PerfConfig;
import com.perfboost.util.Stats;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Клиентский тик сущностей: за спиной и далеко — заморожен; далёкие мобы (LOD) тикают реже
 * (дальше 40 блоков — каждый 2-й тик, дальше 80 — каждый 3-й). Игроки, транспорт и пассажиры не затрагиваются.
 */
@Mixin(ClientWorld.class)
public abstract class ClientWorldMixin {
    @Inject(method = "tickEntity(Lnet/minecraft/entity/Entity;)V", at = @At("HEAD"), cancellable = true)
    private void perfboost$freeze(Entity entity, CallbackInfo ci) {
        PerfConfig c = PerfConfig.get();
        if (entity instanceof PlayerEntity || entity.hasVehicle() || entity.hasPassengers()) return;

        if (c.offscreenCulling && c.entityFreezeDistance < PerfConfig.UNLIMITED
                && ViewCull.outsideCone(entity.getX(), entity.getY(), entity.getZ(), c.entityFreezeDistance, 110)) {
            Stats.entitiesFrozen++;
            ci.cancel();
            return;
        }
        if (c.lodAnimation && entity instanceof LivingEntity) {
            double d = ViewCull.distSq(entity.getX(), entity.getY(), entity.getZ());
            int step = d > 6400 ? 3 : d > 1600 ? 2 : 1;
            if (step > 1 && (((ClientWorld) (Object) this).getTime() + entity.getId()) % step != 0) {
                Stats.entitiesLod++;
                ci.cancel();
            }
        }
    }
}
