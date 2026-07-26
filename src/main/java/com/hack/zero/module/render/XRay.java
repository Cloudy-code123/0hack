package com.hack.zero.module.render;

import com.hack.zero.module.Category;
import com.hack.zero.module.Module;
import com.hack.zero.module.setting.BooleanSetting;
import com.hack.zero.module.setting.ModeSetting;
import com.hack.zero.module.setting.NumberSetting;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.BlockPos;

import java.util.HashSet;
import java.util.Set;

public class XRay extends Module {
    private static XRay INSTANCE;

    private static final Set<Block> DEFAULT_ORES = new HashSet<>();
    static {
        DEFAULT_ORES.add(Blocks.COAL_ORE); DEFAULT_ORES.add(Blocks.DEEPSLATE_COAL_ORE);
        DEFAULT_ORES.add(Blocks.IRON_ORE); DEFAULT_ORES.add(Blocks.DEEPSLATE_IRON_ORE);
        DEFAULT_ORES.add(Blocks.GOLD_ORE); DEFAULT_ORES.add(Blocks.DEEPSLATE_GOLD_ORE);
        DEFAULT_ORES.add(Blocks.LAPIS_ORE); DEFAULT_ORES.add(Blocks.DEEPSLATE_LAPIS_ORE);
        DEFAULT_ORES.add(Blocks.REDSTONE_ORE); DEFAULT_ORES.add(Blocks.DEEPSLATE_REDSTONE_ORE);
        DEFAULT_ORES.add(Blocks.DIAMOND_ORE); DEFAULT_ORES.add(Blocks.DEEPSLATE_DIAMOND_ORE);
        DEFAULT_ORES.add(Blocks.EMERALD_ORE); DEFAULT_ORES.add(Blocks.DEEPSLATE_EMERALD_ORE);
        DEFAULT_ORES.add(Blocks.COPPER_ORE); DEFAULT_ORES.add(Blocks.DEEPSLATE_COPPER_ORE);
        DEFAULT_ORES.add(Blocks.NETHER_GOLD_ORE); DEFAULT_ORES.add(Blocks.NETHER_QUARTZ_ORE);
        DEFAULT_ORES.add(Blocks.ANCIENT_DEBRIS);
        DEFAULT_ORES.add(Blocks.RAW_IRON_BLOCK); DEFAULT_ORES.add(Blocks.RAW_GOLD_BLOCK);
        DEFAULT_ORES.add(Blocks.RAW_COPPER_BLOCK);
        DEFAULT_ORES.add(Blocks.CHEST); DEFAULT_ORES.add(Blocks.ENDER_CHEST);
        DEFAULT_ORES.add(Blocks.TRAPPED_CHEST); DEFAULT_ORES.add(Blocks.BARREL);
        DEFAULT_ORES.add(Blocks.DISPENSER); DEFAULT_ORES.add(Blocks.DROPPER);
        DEFAULT_ORES.add(Blocks.HOPPER); DEFAULT_ORES.add(Blocks.SHULKER_BOX);
        DEFAULT_ORES.add(Blocks.DEEPSLATE);
    }

    private final Set<Block> blocks = new HashSet<>(DEFAULT_ORES);
    private final NumberSetting opacity = new NumberSetting("Opacity", 25.0, 0.0, 255.0, 1.0);
    private final ModeSetting fluidOpacity = new ModeSetting("FluidOpacity", "Both", "None", "Water", "Lava", "Both");
    private final BooleanSetting containers = new BooleanSetting("Containers", true);

    public XRay() {
        super("XRay", "Only renders specified blocks. Good for mining.", Category.RENDER, 0);
        addSetting(opacity);
        addSetting(fluidOpacity);
        addSetting(containers);
        INSTANCE = this;
    }

    public static XRay getInstance() {
        return INSTANCE;
    }

    @Override
    protected void onEnable() {
        mc.worldRenderer.reload();
    }

    @Override
    protected void onDisable() {
        mc.worldRenderer.reload();
    }

    public void addBlock(Block block) {
        blocks.add(block);
        if (isEnabled()) mc.worldRenderer.reload();
    }

    public void removeBlock(Block block) {
        blocks.remove(block);
        if (isEnabled()) mc.worldRenderer.reload();
    }

    public Set<Block> getBlocks() {
        return blocks;
    }

    public boolean isBlocked(Block block, BlockPos pos) {
        return !blocks.contains(block);
    }

    public boolean isWhitelisted(BlockState state) {
        if (!isEnabled()) return true;
        Block block = state.getBlock();
        return blocks.contains(block) || block == Blocks.AIR || block == Blocks.CAVE_AIR || block == Blocks.VOID_AIR
                || block == Blocks.WATER || block == Blocks.LAVA;
    }

    public boolean shouldRender(BlockState state) {
        if (!isEnabled()) return true;
        Block block = state.getBlock();
        return blocks.contains(block) || block == Blocks.AIR || block == Blocks.CAVE_AIR || block == Blocks.VOID_AIR
                || block == Blocks.WATER || block == Blocks.LAVA;
    }

    public static int getAlpha(BlockState state, BlockPos pos) {
        XRay xray = INSTANCE;
        if (xray == null || !xray.isEnabled()) return -1;

        Block block = state.getBlock();
        if (block == Blocks.AIR || block == Blocks.CAVE_AIR || block == Blocks.VOID_AIR
                || block == Blocks.WATER || block == Blocks.LAVA) return -1;

        if (!xray.blocks.contains(block)) {
            return xray.opacity.getInt();
        }

        return -1;
    }

    public static int getFluidAlpha(FluidState state, BlockPos pos) {
        XRay xray = INSTANCE;
        if (xray == null || !xray.isEnabled()) return -1;
        if (xray.shouldApplyFluidOpacity(state)) {
            return xray.opacity.getInt();
        }
        return -1;
    }

    private boolean shouldApplyFluidOpacity(FluidState state) {
        return switch (fluidOpacity.getValue()) {
            case "None" -> false;
            case "Water" -> state.isIn(FluidTags.WATER);
            case "Lava" -> state.isIn(FluidTags.LAVA);
            case "Both" -> state.isIn(FluidTags.WATER) || state.isIn(FluidTags.LAVA);
            default -> false;
        };
    }

    public int getOpacityValue() {
        return opacity.getInt();
    }

    public float getOpacityFloat() {
        return opacity.getFloat() / 255.0f;
    }
}
