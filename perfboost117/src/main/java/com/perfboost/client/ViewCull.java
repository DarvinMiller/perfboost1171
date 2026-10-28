package com.perfboost.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.util.math.Vec3d;

/** Проверка «вне конуса обзора камеры». Не создаёт объектов: всё на примитивах и статических полях. */
public final class ViewCull {
    private static final double[] COS = new double[181];
    static {
        for (int i = 0; i <= 180; i++) COS[i] = Math.cos(Math.toRadians(i));
    }

    private static float cachedYaw = Float.NaN, cachedPitch = Float.NaN;
    private static double lx, ly, lz;

    private ViewCull() {}

    /** true, если точка дальше minDist от камеры и угол до направления взгляда больше halfAngleDeg. */
    public static boolean outsideCone(double x, double y, double z, double minDist, int halfAngleDeg) {
        if (halfAngleDeg >= 180) return false;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.gameRenderer == null) return false;
        Camera cam = mc.gameRenderer.getCamera();
        if (cam == null || !cam.isReady()) return false;

        float yaw = cam.getYaw(), pitch = cam.getPitch();
        if (yaw != cachedYaw || pitch != cachedPitch) {
            double yr = Math.toRadians(yaw), pr = Math.toRadians(pitch);
            double cp = Math.cos(pr);
            lx = -Math.sin(yr) * cp;
            ly = -Math.sin(pr);
            lz = Math.cos(yr) * cp;
            cachedYaw = yaw;
            cachedPitch = pitch;
        }
        Vec3d p = cam.getPos();
        double dx = x - p.x, dy = y - p.y, dz = z - p.z;
        double d2 = dx * dx + dy * dy + dz * dz;
        if (d2 < minDist * minDist) return false;
        double dot = dx * lx + dy * ly + dz * lz;
        return dot < COS[Math.max(0, halfAngleDeg)] * Math.sqrt(d2);
    }

    public static double distSq(double x, double y, double z) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.gameRenderer == null) return 0;
        Camera cam = mc.gameRenderer.getCamera();
        if (cam == null || !cam.isReady()) return 0;
        Vec3d p = cam.getPos();
        double dx = x - p.x, dy = y - p.y, dz = z - p.z;
        return dx * dx + dy * dy + dz * dz;
    }
}
