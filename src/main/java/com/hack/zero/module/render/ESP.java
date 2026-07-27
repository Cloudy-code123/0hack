package com.hack.zero.module.render;

import com.hack.zero.module.Category;
import com.hack.zero.module.Module;
import com.hack.zero.module.setting.BooleanSetting;
import com.hack.zero.module.setting.ModeSetting;
import com.hack.zero.util.RenderUtil;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexRendering;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.WaterAnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public final class ESP extends Module {
    private final ModeSetting mode = new ModeSetting("Mode", "Box", "Box", "Outline", "Glow");
    private final BooleanSetting players = new BooleanSetting("Players", true);
    private final BooleanSetting mobs = new BooleanSetting("Mobs", true);
    private final BooleanSetting animals = new BooleanSetting("Animals", true);
    private final BooleanSetting items = new BooleanSetting("Items", false);
    private final BooleanSetting chests = new BooleanSetting("Chests", true);
    private final BooleanSetting enderChests = new BooleanSetting("EnderChests", true);
    private final BooleanSetting furnaces = new BooleanSetting("Furnaces", true);
    private final BooleanSetting craftingTables = new BooleanSetting("CraftingTables", true);
    private final BooleanSetting shulkerBoxes = new BooleanSetting("ShulkerBoxes", true);

    public ESP() {
        super("ESP", "Highlights entities and blocks through walls.", Category.RENDER, 0);
        addSetting(mode);
        addSetting(players);
        addSetting(mobs);
        addSetting(animals);
        addSetting(items);
        addSetting(chests);
        addSetting(enderChests);
        addSetting(furnaces);
        addSetting(craftingTables);
        addSetting(shulkerBoxes);
    }

    @Override
    public void onRender3D(MatrixStack matrices, float tickDelta, WorldRenderContext context) {
        if (mc.world == null || mc.player == null || context.consumers() == null) return;

        Vec3d camera = context.camera().getPos();

        RenderUtil.setupRenderState();
        try {
            // ★ uses the no-depth layer → sees through blocks
            VertexConsumer lines = context.consumers().getBuffer(RenderUtil.getEspLines());

            for (Entity entity : mc.world.getEntities()) {
                if (entity instanceof ItemEntity && items.isOn()) {
                    Box itemBox = entity.getBoundingBox().expand(0.04).offset(-camera.x, -camera.y, -camera.z);
                    VertexRendering.drawBox(context.matrixStack(), lines, itemBox, 0.2f, 0.8f, 1.0f, 1.0f);
                    continue;
                }

                if (!(entity instanceof LivingEntity living) || living == mc.player || !living.isAlive() || !isAllowed(living)) {
                    continue;
                }

                Box box = living.getBoundingBox().expand(0.04).offset(-camera.x, -camera.y, -camera.z);

                float red, green, blue;
                if (living instanceof PlayerEntity) {
                    red = 0.0f; green = 0.4f; blue = 1.0f;
                } else if (living instanceof AnimalEntity || living instanceof WaterAnimalEntity) {
                    red = 1.0f; green = 0.6f; blue = 0.0f;
                } else {
                    red = 1.0f; green = 0.2f; blue = 0.2f;
                }

                VertexRendering.drawBox(context.matrixStack(), lines, box, red, green, blue, 1.0f);
            }

            boolean anyBlocks = chests.isOn() || enderChests.isOn() || furnaces.isOn()
                    || craftingTables.isOn() || shulkerBoxes.isOn();
            if (anyBlocks) {
                renderBlockESP(context, camera);
            }
        } finally {
            RenderUtil.restoreRenderState();
        }
    }

    private void renderBlockESP(WorldRenderContext context, Vec3d camera) {
        MatrixStack stack = context.matrixStack();
        // ★ same no-depth layer
        VertexConsumer lines = context.consumers().getBuffer(RenderUtil.getEspLines());

        BlockPos origin = mc.player.getBlockPos();
        for (BlockPos pos : BlockPos.iterateOutwards(origin, 64, 32, 64)) {
            Block block = mc.world.getBlockState(pos).getBlock();
            float r = 0, g = 0, b = 0;
            boolean matched = false;

            if (chests.isOn() && (block == Blocks.CHEST || block == Blocks.TRAPPED_CHEST)) {
                r = 0.0f; g = 1.0f; b = 0.0f;
                matched = true;
            } else if (enderChests.isOn() && block == Blocks.ENDER_CHEST) {
                r = 0.8f; g = 0.2f; b = 1.0f;
                matched = true;
            } else if (furnaces.isOn() && (block == Blocks.FURNACE || block == Blocks.SMOKER || block == Blocks.BLAST_FURNACE)) {
                r = 0.6f; g = 0.4f; b = 0.2f;
                matched = true;
            } else if (craftingTables.isOn() && block == Blocks.CRAFTING_TABLE) {
                r = 1.0f; g = 1.0f; b = 0.0f;
                matched = true;
            } else if (shulkerBoxes.isOn() && block instanceof ShulkerBoxBlock) {
                r = 1.0f; g = 1.0f; b = 1.0f;
                matched = true;
            }

            if (matched) {
                stack.push();
                stack.translate(pos.getX() - camera.x, pos.getY() - camera.y, pos.getZ() - camera.z);
                VertexRendering.drawBox(stack, lines, 0.0, 0.0, 0.0, 1.0, 1.0, 1.0, r, g, b, 1.0f);
                stack.pop();
            }
        }
    }

    private boolean isAllowed(LivingEntity entity) {
        if (entity instanceof PlayerEntity) return players.isOn();
        if (entity instanceof Monster || entity instanceof HostileEntity) return mobs.isOn();
        if (entity instanceof AnimalEntity || entity instanceof WaterAnimalEntity) return animals.isOn();
        if (entity instanceof MobEntity) return mobs.isOn();
        return false;
    }
}