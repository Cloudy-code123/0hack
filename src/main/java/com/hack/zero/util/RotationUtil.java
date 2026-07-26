package com.hack.zero.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class RotationUtil {
    private static final MinecraftClient mc = MinecraftClient.getInstance();

    public static float[] getRotations(Entity entity) {
        if (mc.player == null) return new float[]{0, 0};
        Vec3d eyePos = mc.player.getEyePos();
        Vec3d targetPos = entity instanceof LivingEntity living
                ? living.getPos().add(0, living.getStandingEyeHeight() * 0.85, 0)
                : entity.getPos().add(0, entity.getHeight() * 0.5, 0);

        double dx = targetPos.x - eyePos.x;
        double dy = targetPos.y - eyePos.y;
        double dz = targetPos.z - eyePos.z;
        double dist = Math.sqrt(dx * dx + dz * dz);

        float yaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90.0f;
        float pitch = (float) -Math.toDegrees(Math.atan2(dy, dist));

        return new float[]{
                mc.player.getYaw() + MathHelper.wrapDegrees(yaw - mc.player.getYaw()),
                MathHelper.clamp(pitch, -90.0f, 90.0f)
        };
    }

    public static float[] getRotationTo(double x, double y, double z) {
        if (mc.player == null) return new float[]{0, 0};
        Vec3d eyePos = mc.player.getEyePos();
        double dx = x - eyePos.x;
        double dy = y - eyePos.y;
        double dz = z - eyePos.z;
        double dist = Math.sqrt(dx * dx + dz * dz);

        float yaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90.0f;
        float pitch = (float) -Math.toDegrees(Math.atan2(dy, dist));

        return new float[]{
                mc.player.getYaw() + MathHelper.wrapDegrees(yaw - mc.player.getYaw()),
                MathHelper.clamp(pitch, -90.0f, 90.0f)
        };
    }

    public static float[] lerpRotation(float[] current, float[] target, float speed) {
        float yawDiff = MathHelper.wrapDegrees(target[0] - current[0]);
        float pitchDiff = target[1] - current[1];
        return new float[]{
                current[0] + yawDiff * speed,
                MathHelper.clamp(current[1] + pitchDiff * speed, -90.0f, 90.0f)
        };
    }

    public static float getAngleDiff(float a, float b) {
        return MathHelper.wrapDegrees(a - b);
    }

    public static boolean isLookingAt(Entity entity, float fov) {
        if (mc.player == null) return false;
        float[] rot = getRotations(entity);
        float yawDiff = Math.abs(getAngleDiff(rot[0], mc.player.getYaw()));
        float pitchDiff = Math.abs(rot[1] - mc.player.getPitch());
        return yawDiff < fov / 2 && pitchDiff < fov / 2;
    }
}
