package com.hack.zero.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;

public class PlayerUtil {
    private static final MinecraftClient mc = MinecraftClient.getInstance();

    public static boolean isPlayerNull() {
        return mc.player == null || mc.world == null || mc.getNetworkHandler() == null;
    }

    public static boolean isMoving() {
        return mc.player != null && (getInputForward() != 0 || getInputSideways() != 0);
    }

    public static float getInputForward() {
        if (mc.player == null) return 0;
        float forward = 0;
        if (mc.options.forwardKey.isPressed()) forward += 1;
        if (mc.options.backKey.isPressed()) forward -= 1;
        return forward;
    }

    public static float getInputSideways() {
        if (mc.player == null) return 0;
        float sideways = 0;
        if (mc.options.leftKey.isPressed()) sideways += 1;
        if (mc.options.rightKey.isPressed()) sideways -= 1;
        return sideways;
    }

    public static double getSpeed() {
        if (mc.player == null) return 0;
        Vec3d vel = mc.player.getVelocity();
        return Math.sqrt(vel.x * vel.x + vel.z * vel.z);
    }

    public static Vec3d getPlayerPos() {
        if (mc.player == null) return Vec3d.ZERO;
        return mc.player.getPos();
    }

    public static boolean isOnLiquid() {
        if (mc.player == null || mc.world == null) return false;
        Vec3d pos = mc.player.getPos();
        return mc.world.getBlockState(new net.minecraft.util.math.BlockPos((int) pos.x, (int) (pos.y - 0.1), (int) pos.z)).getFluidState().isStill();
    }

    public static boolean isOnWater() {
        if (mc.player == null || mc.world == null) return false;
        Vec3d pos = mc.player.getPos();
        return mc.world.getBlockState(new net.minecraft.util.math.BlockPos((int) pos.x, (int) (pos.y - 0.1), (int) pos.z)).getFluidState().isOf(net.minecraft.fluid.Fluids.WATER);
    }

    public static boolean isOnLava() {
        if (mc.player == null || mc.world == null) return false;
        Vec3d pos = mc.player.getPos();
        return mc.world.getBlockState(new net.minecraft.util.math.BlockPos((int) pos.x, (int) (pos.y - 0.1), (int) pos.z)).getFluidState().isOf(net.minecraft.fluid.Fluids.LAVA);
    }

    public static boolean isGliding() {
        return mc.player != null && mc.player.isGliding();
    }
}
