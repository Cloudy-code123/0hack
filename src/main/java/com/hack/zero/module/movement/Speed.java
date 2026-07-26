package com.hack.zero.module.movement;

import com.hack.zero.module.Category;
import com.hack.zero.module.Module;
import com.hack.zero.module.setting.BooleanSetting;
import com.hack.zero.module.setting.ModeSetting;
import com.hack.zero.module.setting.NumberSetting;
import com.hack.zero.util.PlayerUtil;
import com.hack.zero.ZeroHack;
import net.minecraft.util.math.Vec3d;

public class Speed extends Module {
    private final ModeSetting mode = new ModeSetting("Mode", "Strafe", "Strafe", "Ground");
    private final NumberSetting speed = new NumberSetting("Speed", 1.5, 0.0, 2.0, 0.1);
    private final BooleanSetting sprint = new BooleanSetting("Sprint", true);
    private final BooleanSetting onlyOnGround = new BooleanSetting("OnlyOnGround", false);
    private final BooleanSetting inWater = new BooleanSetting("InWater", false);
    private final NumberSetting waterMultiplier = new NumberSetting("WaterMult", 0.7, 0.1, 1.0, 0.05);

    private int stage = 4;
    private double moveSpeed;
    private double lastDist;

    public Speed() {
        super("Speed", "Makes you go faster", Category.MOVEMENT, 0);
        addSetting(mode);
        addSetting(speed);
        addSetting(sprint);
        addSetting(onlyOnGround);
        addSetting(inWater);
        addSetting(waterMultiplier);
    }

    @Override
    protected void onEnable() {
        stage = 4;
        moveSpeed = 0;
        lastDist = 0;
    }

    @Override
    protected void onDisable() {
        stage = 4;
        moveSpeed = 0;
        lastDist = 0;
    }

    @Override
    public void onUpdate() {
        if (mc.player == null || mc.world == null) return;
        if (ZeroHack.getInstance().getModuleManager().getModule("Fly").map(Module::isEnabled).orElse(false)) return;

        if (onlyOnGround.isOn() && !mc.player.isOnGround() && !inWater.isOn()) return;
        if (!inWater.isOn() && mc.player.isTouchingWater()) return;

        if (sprint.isOn()) mc.player.setSprinting(true);

        double x = mc.player.getX() - mc.player.lastX;
        double z = mc.player.getZ() - mc.player.lastZ;
        lastDist = Math.sqrt(x * x + z * z);

        boolean onWater = mc.player.isTouchingWater();

        switch (mode.getValue()) {
            case "Strafe" -> strafe(onWater);
            case "Ground" -> ground(onWater);
        }
    }

    private void strafe(boolean onWater) {
        if (PlayerUtil.isMoving()) {
            if (mc.player.isOnGround()) stage = 2;

            if (stage == 1) {
                moveSpeed = speed.getValue() * 0.35;
            } else if (stage == 2) {
                moveSpeed = speed.getValue() * 0.6;
                stage = 3;
            } else if (stage == 3) {
                moveSpeed = lastDist - 0.66 * (lastDist - 0.2873);
                stage = 4;
            } else {
                moveSpeed = lastDist - lastDist / 159.0;
            }

            moveSpeed = Math.max(moveSpeed, 0.2873);

            if (onWater) {
                moveSpeed *= waterMultiplier.getValue();
            }

            double forward = mc.player.forwardSpeed;
            double strafe = mc.player.sidewaysSpeed;
            float yaw = mc.player.getYaw();

            if (forward == 0 && strafe == 0) {
                mc.player.setVelocity(0, mc.player.getVelocity().y, 0);
            } else {
                if (forward != 0) {
                    if (strafe > 0) yaw += (forward > 0 ? -45 : 45);
                    else if (strafe < 0) yaw += (forward > 0 ? 45 : -45);
                    strafe = 0;
                    if (forward > 0) forward = 1;
                    else if (forward < 0) forward = -1;
                }

                double rad = Math.toRadians(yaw + 90);
                double sin = Math.sin(rad);
                double cos = Math.cos(rad);

                mc.player.setVelocity(forward * moveSpeed * cos + strafe * moveSpeed * sin,
                    mc.player.getVelocity().y,
                    forward * moveSpeed * sin - strafe * moveSpeed * cos);
            }
        } else {
            mc.player.setVelocity(0, mc.player.getVelocity().y, 0);
        }

        stage++;
    }

    private void ground(boolean onWater) {
        if (PlayerUtil.isMoving()) {
            double mult = onWater ? waterMultiplier.getValue() : 1.0;
            Vec3d vel = Vec3d.fromPolar(0, mc.player.getYaw()).normalize().multiply(speed.getValue() / 10 * mult);
            mc.player.setVelocity(vel.x, mc.player.getVelocity().y, vel.z);
        }
    }
}
