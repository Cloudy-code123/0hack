package com.hack.zero.module.movement;

import com.hack.zero.module.Category;
import com.hack.zero.module.Module;
import com.hack.zero.module.setting.BooleanSetting;
import com.hack.zero.module.setting.ModeSetting;
import com.hack.zero.module.setting.NumberSetting;
import com.hack.zero.util.PacketUtil;
import com.hack.zero.util.PlayerUtil;
import net.minecraft.util.math.Vec3d;

public class Fly extends Module {
    private final ModeSetting mode = new ModeSetting("Mode", "Vanilla", "Vanilla", "Packet", "Glide");
    private final NumberSetting speed = new NumberSetting("Speed", 1.0, 0.1, 6.0, 0.1);
    private final BooleanSetting antiKick = new BooleanSetting("AntiKick", true);
    private final NumberSetting glideSpeed = new NumberSetting("GlideSpeed", -0.04, -0.1, 0.0, 0.005);
    private int tickCounter = 0;

    public Fly() {
        super("Fly", "Fly around the map. Modes: Vanilla, Packet, Glide", Category.MOVEMENT, 70);
        addSetting(mode);
        addSetting(speed);
        addSetting(antiKick);
        addSetting(glideSpeed);
    }

    @Override
    protected void onEnable() {
        tickCounter = 0;
    }

    @Override
    protected void onDisable() {
        if (mc.player != null) {
            mc.player.getAbilities().flying = false;
            mc.player.setVelocity(Vec3d.ZERO);
        }
    }

    @Override
    public void onUpdate() {
        if (mc.player == null || mc.world == null) return;
        if (mc.player.isGliding()) return;

        mc.player.getAbilities().flying = false;
        mc.player.setVelocity(mc.player.getVelocity().x * 0.95, mc.player.getVelocity().y * 0.95, mc.player.getVelocity().z * 0.95);

        Vec3d forward = Vec3d.fromPolar(0, mc.player.getYaw()).normalize();
        Vec3d strafe = Vec3d.fromPolar(0, mc.player.getYaw() - 90).normalize();

        float inputForward = PlayerUtil.getInputForward();
        float inputSideways = PlayerUtil.getInputSideways();

        double moveX = 0, moveZ = 0, moveY = 0;
        if (inputForward != 0) {
            moveX += forward.x * inputForward;
            moveZ += forward.z * inputForward;
        }
        if (inputSideways != 0) {
            moveX += strafe.x * inputSideways;
            moveZ += strafe.z * inputSideways;
        }

        double len = Math.sqrt(moveX * moveX + moveZ * moveZ);
        if (len > 0) {
            moveX = moveX / len * speed.getValue();
            moveZ = moveZ / len * speed.getValue();
        }

        if (mc.options.jumpKey.isPressed()) moveY = speed.getValue() * 0.5;
        if (mc.options.sneakKey.isPressed()) moveY = -speed.getValue() * 0.5;

        switch (mode.getValue()) {
            case "Vanilla" -> {
                mc.player.setVelocity(moveX, moveY, moveZ);
            }
            case "Packet" -> {
                mc.player.setVelocity(moveX, moveY, moveZ);
                if (tickCounter % 20 == 0 && antiKick.isOn()) {
                    PacketUtil.sendPosition(mc.player.getX(), mc.player.getY() - 0.04, mc.player.getZ(), true);
                }
                tickCounter++;
            }
            case "Glide" -> {
                double yVelocity = mc.player.getVelocity().y;
                if (inputForward != 0 || inputSideways != 0) {
                    mc.player.setVelocity(moveX, Math.max(yVelocity, glideSpeed.getValue()), moveZ);
                } else {
                    mc.player.setVelocity(0, glideSpeed.getValue(), 0);
                }
            }
        }
    }
}
