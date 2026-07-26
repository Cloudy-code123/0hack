package com.hack.zero.module.player;

import com.hack.zero.module.Category;
import com.hack.zero.module.Module;
import com.hack.zero.module.setting.BooleanSetting;
import com.hack.zero.module.setting.NumberSetting;
import com.hack.zero.util.RenderUtil;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.item.BoatItem;
import net.minecraft.item.BlockItem;
import net.minecraft.item.FireworkRocketItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;

public class AirPlace extends Module {

    private final BooleanSetting render;
    private final NumberSetting range;
    private BlockHitResult renderHitResult;
    private BlockHitResult placeHitResult;

    public AirPlace() {
        super(
            "AirPlace",
            "Places blocks in mid-air where crosshair points without needing an adjacent supporting block.",
            Category.PLAYER,
            0
        );
        this.render = new BooleanSetting("Render", true);
        this.range  = new NumberSetting("Range", 5.0, 0.0, 6.0, 0.5);
        addSetting(this.render);
        addSetting(this.range);
    }

    @Override
    protected void onDisable() {
        renderHitResult = null;
        placeHitResult = null;
    }

    @Override
    public void onUpdate() {
        if (mc.player == null || mc.world == null) return;

        ItemStack mainHand = mc.player.getMainHandStack();
        ItemStack offHand = mc.player.getOffHandStack();
        if (!isPlaceable(mainHand) && !isPlaceable(offHand)) {
            renderHitResult = null;
            placeHitResult = null;
            return;
        }

        if (mc.options.getPerspective().isFirstPerson()) {
            HitResult crosshair = mc.crosshairTarget;
            if (crosshair != null && crosshair.getType() != HitResult.Type.MISS) {
                renderHitResult = null;
                placeHitResult = null;
                return;
            }
        }

        double r = ((Number) range.getValue()).doubleValue();

        placeHitResult = raycastFromEntity(mc.player, r);

        if (mc.options.getPerspective().isFirstPerson()) {
            renderHitResult = placeHitResult;
        } else {
            Entity camera = mc.cameraEntity;
            if (camera != null) {
                renderHitResult = raycastFromEntity(camera, r);
            } else {
                renderHitResult = placeHitResult;
            }
        }

        if (mc.options.useKey.isPressed() && placeHitResult != null) {
            place();
        }
    }

    private BlockHitResult raycastFromEntity(Entity entity, double range) {
        Vec3d eyePos = entity.getEyePos();
        Vec3d look = entity.getRotationVec(1.0f);
        Vec3d end = eyePos.add(look.multiply(range));

        RaycastContext ctx = new RaycastContext(
            eyePos,
            end,
            RaycastContext.ShapeType.OUTLINE,
            RaycastContext.FluidHandling.NONE,
            entity
        );

        BlockHitResult result = mc.world.raycast(ctx);

        if (result.getType() != HitResult.Type.MISS) {
            return result;
        }

        Vec3d missPos = result.getPos();
        int x = (int) Math.floor(missPos.x);
        int y = (int) Math.floor(missPos.y);
        int z = (int) Math.floor(missPos.z);
        BlockPos pos = new BlockPos(x, y, z);
        return BlockHitResult.createMissed(missPos, Direction.UP, pos);
    }

    private void place() {
        if (mc.interactionManager == null || mc.player == null || placeHitResult == null) return;

        BlockPos pos = placeHitResult.getBlockPos();
        if (pos == null) return;
        BlockState state = mc.world.getBlockState(pos);

        if (!state.isReplaceable()) return;

        ItemStack mainHand = mc.player.getMainHandStack();
        ItemStack offHand = mc.player.getOffHandStack();
        Hand hand = isPlaceable(mainHand) ? Hand.MAIN_HAND : Hand.OFF_HAND;

        mc.interactionManager.interactBlock(mc.player, hand, placeHitResult);
    }

    @Override
    public void onRender3D(MatrixStack matrices, float tickDelta, WorldRenderContext context) {
        if (!render.isOn() || renderHitResult == null || context.consumers() == null) return;

        BlockPos pos = renderHitResult.getBlockPos();
        if (pos == null) return;
        Vec3d cam = context.camera().getPos();

        Box box = new Box(
            pos.getX() - cam.x,
            pos.getY() - cam.y,
            pos.getZ() - cam.z,
            pos.getX() + 1.0 - cam.x,
            pos.getY() + 1.0 - cam.y,
            pos.getZ() + 1.0 - cam.z
        );

        RenderUtil.setupRenderState();
        try {
            VertexConsumer buffer = context.consumers().getBuffer(RenderLayer.getLines());
            Matrix4f matrix = context.matrixStack().peek().getPositionMatrix();
            RenderUtil.drawBoxLines(buffer, matrix, box, 1.0f, 0.0f, 0.0f, 1.0f);
        } finally {
            RenderUtil.restoreRenderState();
        }
    }

    private boolean isPlaceable(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        Item item = stack.getItem();
        return item instanceof BlockItem
                || item instanceof SpawnEggItem
                || (item instanceof BoatItem && mc.player != null && mc.player.isTouchingWater())
                || item instanceof FireworkRocketItem;
    }
}
