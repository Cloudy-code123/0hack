package com.hack.zero.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

public class PacketUtil {
    private static final MinecraftClient mc = MinecraftClient.getInstance();

    public static void sendPacket(net.minecraft.network.packet.Packet<?> packet) {
        if (mc.getNetworkHandler() != null) {
            mc.getNetworkHandler().sendPacket(packet);
        }
    }

    public static void sendPosition(double x, double y, double z, boolean onGround) {
        if (mc.getNetworkHandler() != null) {
            mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y, z, onGround, false));
        }
    }

    public static void sendRotation(float yaw, float pitch, boolean onGround) {
        if (mc.getNetworkHandler() != null) {
            mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(yaw, pitch, onGround, false));
        }
    }

    public static void sendPositionAndRotation(double x, double y, double z, float yaw, float pitch, boolean onGround) {
        if (mc.getNetworkHandler() != null) {
            mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.Full(x, y, z, yaw, pitch, onGround, false));
        }
    }

    public static void spoofOnGround(boolean onGround) {
        if (mc.player != null && mc.getNetworkHandler() != null) {
            mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(onGround, false));
        }
    }

    public static void jitterRotation(float yawOffset, float pitchOffset) {
        if (mc.player == null) return;
        java.util.Random random = new java.util.Random();
        float yaw = mc.player.getYaw() + (random.nextFloat() - 0.5f) * yawOffset;
        float pitch = mc.player.getPitch() + (random.nextFloat() - 0.5f) * pitchOffset;
        pitch = Math.max(-90, Math.min(90, pitch));
        sendRotation(yaw, pitch, mc.player.isOnGround());
    }
}
