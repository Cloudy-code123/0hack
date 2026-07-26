package com.hack.zero.module.render;

import com.hack.zero.module.Category;
import com.hack.zero.module.Module;
import com.hack.zero.module.setting.BooleanSetting;
import com.hack.zero.module.setting.NumberSetting;
import com.hack.zero.util.RenderUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public final class Tracers extends Module {
    private final BooleanSetting players = new BooleanSetting("Players", true);
    private final BooleanSetting monsters = new BooleanSetting("Monsters", true);
    private final BooleanSetting animals = new BooleanSetting("Animals", true);
    private final BooleanSetting items = new BooleanSetting("Items", false);
    private final BooleanSetting chests = new BooleanSetting("Chests", true);
    private final BooleanSetting enderChests = new BooleanSetting("EnderChests", true);
    private final BooleanSetting shulkerBoxes = new BooleanSetting("ShulkerBoxes", true);
    private final BooleanSetting furnaces = new BooleanSetting("Furnaces", true);
    private final BooleanSetting craftingTables = new BooleanSetting("CraftingTables", true);
    private final NumberSetting thickness = new NumberSetting("Thickness", 1.0, 0.5, 5.0, 0.5);

    public Tracers() {
        super("Tracers", "Draws lines to nearby targets.", Category.RENDER, 0);
        addSetting(players);
        addSetting(monsters);
        addSetting(animals);
        addSetting(items);
        addSetting(chests);
        addSetting(enderChests);
        addSetting(shulkerBoxes);
        addSetting(furnaces);
        addSetting(craftingTables);
        addSetting(thickness);
    }

    @Override
    public void onRender3D(net.minecraft.client.util.math.MatrixStack matrices, float tickDelta, WorldRenderContext context) {
        if (mc.world == null || mc.player == null || context.consumers() == null) {
            return;
        }
        Vec3d camera = context.camera().getPos();
        float camPitch = context.camera().getPitch();
        float camYaw = context.camera().getYaw();
        Vec3d crosshair = new Vec3d(0.0, 0.0, 1.0)
                .rotateX((float) -Math.toRadians(camPitch))
                .rotateY((float) -Math.toRadians(camYaw));

        RenderUtil.setupRenderState();
        try {
            for (Entity entity : mc.world.getEntities()) {
                if (entity instanceof ItemEntity && items.isOn()) {
                    Vec3d target = entity.getBlockPos().toCenterPos().subtract(camera);
                    drawTracer(context, crosshair, target, 0.2f, 0.8f, 1.0f);
                    continue;
                }
                if (!(entity instanceof LivingEntity living) || living == mc.player || !living.isAlive() || !isAllowed(living)) {
                    continue;
                }
                Vec3d target = living.getBlockPos().toCenterPos().subtract(camera);
                float r, g, b;
                if (entity instanceof PlayerEntity) {
                    r = 0.2f; g = 1.0f; b = 0.2f;
                } else if (entity instanceof HostileEntity) {
                    r = 1.0f; g = 0.3f; b = 0.2f;
                } else {
                    r = 0.2f; g = 0.8f; b = 1.0f;
                }
                drawTracer(context, crosshair, target, r, g, b);
            }
            BlockPos origin = mc.player.getBlockPos();
            for (BlockPos pos : BlockPos.iterateOutwards(origin, 64, 32, 64)) {
                Block block = mc.world.getBlockState(pos).getBlock();
                Vec3d target;
                if (chests.isOn() && (block == Blocks.CHEST || block == Blocks.TRAPPED_CHEST)) {
                    target = Vec3d.ofCenter(pos).subtract(camera);
                    drawTracer(context, crosshair, target, 1.0f, 0.8f, 0.2f);
                    continue;
                }
                if (enderChests.isOn() && block == Blocks.ENDER_CHEST) {
                    target = Vec3d.ofCenter(pos).subtract(camera);
                    drawTracer(context, crosshair, target, 0.0f, 1.0f, 0.9f);
                    continue;
                }
                if (shulkerBoxes.isOn() && block instanceof ShulkerBoxBlock) {
                    target = Vec3d.ofCenter(pos).subtract(camera);
                    drawTracer(context, crosshair, target, 0.8f, 0.2f, 1.0f);
                    continue;
                }
                if (furnaces.isOn() && (block == Blocks.FURNACE || block == Blocks.SMOKER || block == Blocks.BLAST_FURNACE)) {
                    target = Vec3d.ofCenter(pos).subtract(camera);
                    drawTracer(context, crosshair, target, 1.0f, 0.4f, 0.0f);
                    continue;
                }
                if (craftingTables.isOn() && block == Blocks.CRAFTING_TABLE) {
                    target = Vec3d.ofCenter(pos).subtract(camera);
                    drawTracer(context, crosshair, target, 1.0f, 1.0f, 0.2f);
                }
            }
        } finally {
            RenderUtil.restoreRenderState();
        }
    }

    private void drawTracer(WorldRenderContext context, Vec3d crosshair, Vec3d target, float red, float green, float blue) {
        int color = 0xFF000000 | (int) (red * 255.0f) << 16 | (int) (green * 255.0f) << 8 | (int) (blue * 255.0f);
        RenderUtil.drawLine(context.matrixStack(), context.consumers(), crosshair, target, color);
    }

    private boolean isAllowed(LivingEntity entity) {
        if (entity instanceof PlayerEntity) return players.isOn();
        if (entity instanceof HostileEntity) return monsters.isOn();
        if (entity instanceof AnimalEntity) return animals.isOn();
        return false;
    }
}
