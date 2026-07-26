package com.hack.zero.module.movement;

import com.hack.zero.module.Category;
import com.hack.zero.module.Module;
import com.hack.zero.module.setting.BooleanSetting;
import com.hack.zero.module.setting.NumberSetting;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.Vec3d;

public class Velocity extends Module {

    private final NumberSetting speedMultiplier = new NumberSetting("Speed", 1.5, 0.1, 10.0, 0.1);
    private final NumberSetting acceleration = new NumberSetting("Acceleration", 0.5, 0.1, 1.0, 0.1);
    private final BooleanSetting sprintBoost = new BooleanSetting("SprintBoost", false);
    private final BooleanSetting jumpBoost = new BooleanSetting("JumpBoost", false);

    private double currentEffectiveMultiplier = 1.0;
    private double currentBoostAccumulator = 0.0;

    private static final double INTERPOLATION_SPEED = 0.1;
    private static final double SNAP_THRESHOLD = 0.005;
    private static final double MAX_MULTIPLIER = 10.0;
    private static final double MIN_MULTIPLIER = 0.1;

    public Velocity() {
        super("Velocity", "Player movement speed enhancement with smooth acceleration", Category.MOVEMENT, 0);
        addSetting(speedMultiplier);
        addSetting(acceleration);
        addSetting(sprintBoost);
        addSetting(jumpBoost);
        setExpandable(false);
    }

    @Override
    protected void onEnable() {
        currentEffectiveMultiplier = 1.0;
        currentBoostAccumulator = 0.0;
    }

    @Override
    protected void onDisable() {
        currentEffectiveMultiplier = 1.0;
        currentBoostAccumulator = 0.0;
    }

    @Override
    public void onUpdate() {
    }

    public void tick(ClientPlayerEntity player) {
        if (!isEnabled()) {
            smoothReturnToNormal();
            return;
        }

        double targetSpeed = speedMultiplier.getValue();

        if (sprintBoost.isOn() && player.isSprinting()) {
            targetSpeed *= 1.3;
        }

        targetSpeed = clamp(targetSpeed, MIN_MULTIPLIER, MAX_MULTIPLIER);

        double accelRate = acceleration.getValue() * INTERPOLATION_SPEED;
        double diff = targetSpeed - currentEffectiveMultiplier;

        if (Math.abs(diff) < SNAP_THRESHOLD) {
            currentEffectiveMultiplier = targetSpeed;
        } else {
            currentEffectiveMultiplier += diff * Math.max(accelRate, 0.01);
        }

        currentEffectiveMultiplier = clamp(currentEffectiveMultiplier, MIN_MULTIPLIER, MAX_MULTIPLIER);
    }

    public void onJump(ClientPlayerEntity player) {
        if (!isEnabled() || !jumpBoost.isOn()) return;

        Vec3d vel = player.getVelocity();
        double boost = speedMultiplier.getValue() * 0.12;
        player.setVelocity(vel.x, vel.y + boost, vel.z);
    }

    public double getCurrentEffectiveMultiplier() {
        return currentEffectiveMultiplier;
    }

    public boolean isJumpBoostEnabled() {
        return jumpBoost.isOn();
    }

    public boolean isSprintBoostEnabled() {
        return sprintBoost.isOn();
    }

    private void smoothReturnToNormal() {
        double diff = 1.0 - currentEffectiveMultiplier;
        if (Math.abs(diff) < SNAP_THRESHOLD) {
            currentEffectiveMultiplier = 1.0;
        } else {
            currentEffectiveMultiplier += diff * 0.15;
        }
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
