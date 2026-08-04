package com.hack.zero.module.render;

import com.hack.zero.module.Category;
import com.hack.zero.module.Module;
import com.hack.zero.module.setting.BlockListSetting;
import com.hack.zero.module.setting.BooleanSetting;
import com.hack.zero.module.setting.ModeSetting;
import com.hack.zero.module.setting.NumberSetting;
import com.hack.zero.util.BlockUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShapes;

import java.util.HashSet;
import java.util.List;

public final class XRay extends Module {
    public static final List<Block> ORES = List.of(
            Blocks.COAL_ORE, Blocks.DEEPSLATE_COAL_ORE,
            Blocks.IRON_ORE, Blocks.DEEPSLATE_IRON_ORE,
            Blocks.GOLD_ORE, Blocks.DEEPSLATE_GOLD_ORE,
            Blocks.LAPIS_ORE, Blocks.DEEPSLATE_LAPIS_ORE,
            Blocks.REDSTONE_ORE, Blocks.DEEPSLATE_REDSTONE_ORE,
            Blocks.DIAMOND_ORE, Blocks.DEEPSLATE_DIAMOND_ORE,
            Blocks.EMERALD_ORE, Blocks.DEEPSLATE_EMERALD_ORE,
            Blocks.COPPER_ORE, Blocks.DEEPSLATE_COPPER_ORE,
            Blocks.NETHER_GOLD_ORE, Blocks.NETHER_QUARTZ_ORE,
            Blocks.ANCIENT_DEBRIS
    );

    private static XRay INSTANCE;

    private final BlockListSetting blocks = new BlockListSetting("Whitelist", new HashSet<>(ORES));
    private final NumberSetting opacity = new NumberSetting("Opacity", 25, 0, 255, 1);
    private final ModeSetting fluidOpacity = new ModeSetting("Fluid Opacity", "Both", "None", "Water", "Lava", "Both");
    private final BooleanSetting exposedOnly = new BooleanSetting("Exposed Only", false);

    public XRay() {
        super("XRay", "Only renders specified blocks. Good for mining.", Category.RENDER, 0);
        INSTANCE = this;
        addSetting(blocks);
        addSetting(opacity);
        addSetting(fluidOpacity);
        addSetting(exposedOnly);
    }

    public static XRay getInstance() {
        return INSTANCE;
    }

    public static boolean isActive() {
        return INSTANCE != null && INSTANCE.isEnabled();
    }

    public BlockListSetting getWhitelistSetting() {
        return blocks;
    }

    public void addBlock(Block block) {
        blocks.getValue().add(block);
    }

    public void removeBlock(Block block) {
        blocks.getValue().remove(block);
    }

    public void resetBlocks() {
        blocks.getValue().clear();
        blocks.getValue().addAll(ORES);
    }

    @Override
    protected void onEnable() {
        reloadWorldRenderer();
    }

    @Override
    protected void onDisable() {
        reloadWorldRenderer();
    }

    private void reloadWorldRenderer() {
        if (mc.worldRenderer != null) {
            mc.worldRenderer.reload();
        }
    }

    public boolean modifyDrawSide(BlockState state, BlockState otherState, Direction facing, boolean returns) {
        if (!returns && !isBlocked(state, otherState)) {
            return otherState.getCullingFace(facing.getOpposite()) != VoxelShapes.fullCube()
                    || otherState.getBlock() != state.getBlock()
                    || !otherState.isOpaque();
        }

        return returns;
    }

    public boolean isBlocked(Block block, BlockPos blockPos) {
        return !(blocks.contains(block) && (!exposedOnly.isOn() || (blockPos == null || BlockUtils.isExposed(blockPos))));
    }

    public boolean isBlocked(BlockState state, BlockState otherState) {
        return !(blocks.contains(state.getBlock()) && (!exposedOnly.isOn() || !otherState.isOpaque()));
    }

    public static int getAlpha(BlockState state, BlockPos pos) {
        XRay xray = INSTANCE;
        if (xray == null || !xray.isEnabled()) return -1;

        if (xray.isBlocked(state.getBlock(), pos)) {
            return xray.opacity.getInt();
        }

        return -1;
    }

    public static int getFluidAlpha(FluidState state, BlockPos pos) {
        XRay xray = INSTANCE;
        if (xray == null || !xray.isEnabled()) return -1;

        Block fluidBlock = state.getBlockState().getBlock();
        if (xray.shouldApplyFluidOpacity(state) && xray.isBlocked(fluidBlock, pos)) {
            return xray.opacity.getInt();
        }

        return -1;
    }

    private boolean shouldApplyFluidOpacity(FluidState state) {
        return switch (fluidOpacity.getValue()) {
            case "None" -> false;
            case "Water" -> state.isIn(FluidTags.WATER);
            case "Lava" -> state.isIn(FluidTags.LAVA);
            default -> state.isIn(FluidTags.WATER) || state.isIn(FluidTags.LAVA);
        };
    }
}
