package com.perfboost.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;

/** Состояние текущей отрисовки (только render-поток). */
public final class RenderState {
    /** Квадрат дистанции до камеры сущности, чьи слои рендерятся сейчас; -1 — вне контекста сущности. */
    public static double currentDistSq = -1;

    private RenderState() {}

    public static double distSq(Entity e) {
        return e.squaredDistanceTo(MinecraftClient.getInstance().gameRenderer.getCamera().getPos());
    }
}
