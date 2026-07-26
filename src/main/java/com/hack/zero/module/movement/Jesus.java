package com.hack.zero.module.movement;

import com.hack.zero.module.Category;
import com.hack.zero.module.Module;
import com.hack.zero.module.setting.BooleanSetting;
import com.hack.zero.module.setting.ModeSetting;
import com.hack.zero.module.setting.NumberSetting;
import com.hack.zero.util.PacketUtil;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class Jesus extends Module {
    private final ModeSetting mode = new ModeSetting("Mode", "Solid", "Solid", "Bounce", "NCP");
    private final NumberSetting motionY = new NumberSetting("MotionY", 0.1, 0.01, 0.5, 0.01);
    private final BooleanSetting sink = new BooleanSetting("AntiSink", true);

    public Jesus() {
        super("Jesus", "Walk on water and lava", Category.MOVEMENT, 0);
        addSetting(mode);
        addSetting(motionY);
        addSetting(sink);
        setExpandable(false);
    }

    @Override
    protected void onEnable() {}

    @Override
    protected void onDisable() {
        if (mc.player != null) {
            mc.player.setVelocity(mc.player.getVelocity().x, 0, mc.player.getVelocity().z);
        }
    }

    @Override
    public void onUpdate() {
        if (mc.player == null || mc.world == null) return;
        if (mc.player.isSubmergedInWater() || mc.player.isInLava()) return;

        Vec3d pos = mc.player.getPos();
        BlockPos blockBelow = new BlockPos((int) pos.x, (int) (pos.y - 0.1), (int) pos.z);
        BlockPos blockAt = new BlockPos((int) pos.x, (int) pos.y, (int) pos.z);

        boolean onWater = isWater(blockBelow);
        boolean onLava = isLava(blockBelow);
        boolean onWaterAt = isWater(blockAt);
        boolean onLavaAt = isLava(blockAt);

        if (!onWater && !onLava && !onWaterAt && !onLavaAt) return;

        if (mc.player.getVelocity().y < 0 && sink.isOn()) {
            mc.player.setVelocity(mc.player.getVelocity().x, 0, mc.player.getVelocity().z);
        }

        switch (mode.getValue()) {
            case "Solid" -> {
                if (mc.player.isTouchingWater() || mc.player.isInLava()) return;
                if (mc.player.getVelocity().y < 0) {
                    mc.player.setVelocity(mc.player.getVelocity().x, 0, mc.player.getVelocity().z);
                }
                PacketUtil.spoofOnGround(true);
            }
            case "Bounce" -> {
                if (mc.player.hurtTime > 0 && mc.player.getVelocity().y < 0) {
                    mc.player.setVelocity(mc.player.getVelocity().x, 0.42, mc.player.getVelocity().z);
                }
            }
            case "NCP" -> {
                if (mc.player.isTouchingWater() || mc.player.isInLava()) return;
                mc.player.setVelocity(mc.player.getVelocity().x, 0.1, mc.player.getVelocity().z);
                PacketUtil.spoofOnGround(true);
                mc.player.setOnGround(true);
            }
        }
    }

    private boolean isWater(BlockPos pos) {
        return mc.world.getBlockState(pos).getBlock() == Blocks.WATER;
    }

    private boolean isLava(BlockPos pos) {
        return mc.world.getBlockState(pos).getBlock() == Blocks.LAVA;
    }
}
