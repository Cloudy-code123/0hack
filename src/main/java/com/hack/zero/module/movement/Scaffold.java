package com.hack.zero.module.movement;

import com.hack.zero.module.Category;
import com.hack.zero.module.Module;
import com.hack.zero.module.setting.BooleanSetting;
import com.hack.zero.module.setting.ModeSetting;
import com.hack.zero.module.setting.NumberSetting;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class Scaffold extends Module {

    private final ModeSetting mode = new ModeSetting("Mode", "Normal", "Normal", "Paving", "Tower");
    private final NumberSetting delay = new NumberSetting("Delay", 0.0, 0.0, 2.0, 1.0);
    private final BooleanSetting rotate = new BooleanSetting("Rotate", true);
    private final BooleanSetting sprint = new BooleanSetting("Sprint", true);
    private int ticks;

    public Scaffold() {
        super("Scaffold", "Strict single-block depth placement engine", Category.MOVEMENT, 0);
        addSetting(mode);
        addSetting(delay);
        addSetting(rotate);
        addSetting(sprint);
    }

    @Override
    protected void onEnable() {
        ticks = 0;
    }

    @Override
    protected void onDisable() {
    }

    @Override
    public void onUpdate() {
        if (mc.player == null) return;

        if ("Tower".equals(mode.getValue()) && mc.options.jumpKey.isPressed()
                && mc.player.getVelocity().y < 0.3) {
            mc.player.setVelocity(mc.player.getVelocity().x, 0.42, mc.player.getVelocity().z);
        }

        tryPlaceBlock();
    }

    private void tryPlaceBlock() {
        if (mc.player == null || mc.interactionManager == null || mc.world == null || mc.player.networkHandler == null) {
            return;
        }
        if (ticks++ % (delay.getInt() + 1) != 0) {
            return;
        }
        mc.player.setSprinting(sprint.isOn());

        int blockSlot = findBlockSlot();
        if (blockSlot == -1) {
            return;
        }

        int targetY = (int) Math.floor(mc.player.getY() - 1.0);
        if (mc.player.getVelocity().y < -0.1) {
            targetY = (int) Math.floor(mc.player.getY() - 0.5);
        }

        if ("Paving".equals(mode.getValue())) {
            BlockPos playerFloor = new BlockPos(
                    (int) Math.floor(mc.player.getX()),
                    targetY,
                    (int) Math.floor(mc.player.getZ()));
            for (int xOffset = -2; xOffset <= 3; xOffset++) {
                for (int zOffset = -2; zOffset <= 3; zOffset++) {
                    BlockPos checkPos = playerFloor.add(xOffset, 0, zOffset);
                    if (!mc.world.getBlockState(checkPos).isAir()) continue;
                    BlockPos support = findSupportBlock(checkPos);
                    if (support == null) continue;
                    placeSingleBlock(checkPos, support, blockSlot);
                }
            }
            return;
        }

        double factor = 1.1;
        double motionX = mc.player.getVelocity().x * factor;
        double motionZ = mc.player.getVelocity().z * factor;
        Box playerBox = mc.player.getBoundingBox().expand(motionX, 0.0, motionZ);

        BlockPos targetPos = null;
        for (double x = playerBox.minX; x <= playerBox.maxX; x += 0.3) {
            for (double z = playerBox.minZ; z <= playerBox.maxZ; z += 0.3) {
                BlockPos checkPos = new BlockPos((int) Math.floor(x), targetY, (int) Math.floor(z));
                if (!mc.world.getBlockState(checkPos).isAir()) continue;
                if (findSupportBlock(checkPos) == null) continue;
                targetPos = checkPos;
                break;
            }
            if (targetPos != null) break;
        }

        if (targetPos == null) {
            return;
        }

        BlockPos support = findSupportBlock(targetPos);
        if (support == null) {
            return;
        }

        placeSingleBlock(targetPos, support, blockSlot);
    }

    private void placeSingleBlock(BlockPos targetPos, BlockPos support, int blockSlot) {
        Direction side = getPlaceDirection(targetPos, support);
        Vec3d hitVec = Vec3d.ofCenter(support).add(
                new Vec3d(side.getVector()).multiply(0.5));

        if (rotate.isOn()) {
            mc.player.networkHandler.sendPacket(
                    new PlayerMoveC2SPacket.LookAndOnGround(
                            mc.player.getYaw(), 82.5f,
                            mc.player.isOnGround(), mc.player.horizontalCollision));
        }

        int previousSlot = mc.player.getInventory().getSelectedSlot();
        mc.player.getInventory().setSelectedSlot(blockSlot);

        BlockHitResult hitResult = new BlockHitResult(hitVec, side, support, false);
        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hitResult);
        mc.player.swingHand(Hand.MAIN_HAND);

        mc.player.getInventory().setSelectedSlot(previousSlot);
    }

    private BlockPos findSupportBlock(BlockPos target) {
        if (!mc.world.getBlockState(target.down()).isAir()) return target.down();
        if (!mc.world.getBlockState(target.north()).isAir()) return target.north();
        if (!mc.world.getBlockState(target.east()).isAir()) return target.east();
        if (!mc.world.getBlockState(target.south()).isAir()) return target.south();
        if (!mc.world.getBlockState(target.west()).isAir()) return target.west();

        BlockPos downPos = target.down();
        if (!mc.world.getBlockState(downPos.north()).isAir()) return downPos.north();
        if (!mc.world.getBlockState(downPos.east()).isAir()) return downPos.east();
        if (!mc.world.getBlockState(downPos.south()).isAir()) return downPos.south();
        if (!mc.world.getBlockState(downPos.west()).isAir()) return downPos.west();

        return null;
    }

    private Direction getPlaceDirection(BlockPos target, BlockPos support) {
        if (target.getY() > support.getY()) return Direction.UP;
        if (target.getZ() > support.getZ()) return Direction.SOUTH;
        if (target.getZ() < support.getZ()) return Direction.NORTH;
        if (target.getX() > support.getX()) return Direction.EAST;
        return Direction.WEST;
    }

    private int findBlockSlot() {
        for (int slot = 0; slot < 9; slot++) {
            ItemStack stack = mc.player.getInventory().getStack(slot);
            if (stack.isEmpty()) continue;
            if (!(stack.getItem() instanceof BlockItem blockItem)) continue;
            if (blockItem.getBlock() == net.minecraft.block.Blocks.AIR) continue;
            if (!blockItem.getBlock().getDefaultState().isSolidBlock(mc.world, BlockPos.ORIGIN)) continue;
            return slot;
        }
        return -1;
    }
}
