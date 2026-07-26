package com.hack.zero.module.movement;

import com.hack.zero.module.Category;
import com.hack.zero.module.Module;
import com.hack.zero.module.setting.BooleanSetting;
import com.hack.zero.module.setting.NumberSetting;
import com.hack.zero.util.PacketUtil;
import com.hack.zero.util.PlayerUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.util.math.Vec3d;

public class BoatFly extends Module {
    private final NumberSetting hSpeed = new NumberSetting("Horizontal Speed", 1.5, 0.1, 5.0, 0.1);
    private final NumberSetting vSpeed = new NumberSetting("Vertical Speed", 1.0, 0.1, 3.0, 0.1);
    private final BooleanSetting antiKick = new BooleanSetting("AntiKick", true);
    private int tickCounter = 0;

    public BoatFly() {
        super("BoatFly", "Fly while in a boat. Adjust horizontal and vertical speed", Category.MOVEMENT, 0);
        addSetting(hSpeed);
        addSetting(vSpeed);
        addSetting(antiKick);
    }

    @Override
    protected void onEnable() {
        tickCounter = 0;
    }

    @Override
    protected void onDisable() {
        tickCounter = 0;
        if (mc.player != null && mc.world != null && mc.player.hasVehicle()) {
            Entity vehicle = mc.player.getVehicle();
            if (vehicle instanceof BoatEntity boat) {
                boat.setNoGravity(false);
            }
        }
    }

    @Override
    public void onUpdate() {
        if (mc.player == null || mc.world == null) return;
        if (!mc.player.hasVehicle()) return;

        Entity vehicle = mc.player.getVehicle();
        if (!(vehicle instanceof BoatEntity boat)) return;

        Vec3d forward = Vec3d.fromPolar(0, mc.player.getYaw()).normalize();
        Vec3d strafe = Vec3d.fromPolar(0, mc.player.getYaw() - 90).normalize();

        float inputForward = PlayerUtil.getInputForward();
        float inputSideways = PlayerUtil.getInputSideways();

        double moveX = 0, moveZ = 0, moveY = 0;

        if (inputForward != 0) {
            moveX += forward.x * inputForward * hSpeed.getValue();
            moveZ += forward.z * inputForward * hSpeed.getValue();
        }
        if (inputSideways != 0) {
            moveX += strafe.x * inputSideways * hSpeed.getValue();
            moveZ += strafe.z * inputSideways * hSpeed.getValue();
        }

        if (mc.options.jumpKey.isPressed()) moveY = vSpeed.getValue();
        if (mc.options.sneakKey.isPressed()) moveY = -vSpeed.getValue();

        boat.setVelocity(moveX, moveY, moveZ);
        boat.setNoGravity(true);
        boat.fallDistance = 0;

        if (antiKick.isOn() && tickCounter % 20 == 0) {
            PacketUtil.sendPosition(boat.getX(), boat.getY() - 0.04, boat.getZ(), true);
        }

        tickCounter++;
    }
}
