package com.hack.zero.mixin;

import com.hack.zero.module.render.XRay;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.BlockModelRenderer;
import net.minecraft.client.render.model.BlockModelPart;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(BlockModelRenderer.class)
public abstract class BlockModelRendererMixin {
    @Unique
    private static final ThreadLocal<Integer> ALPHA = ThreadLocal.withInitial(() -> -1);

    @Inject(method = {"renderSmooth", "renderFlat"}, at = @At("HEAD"), cancellable = true)
    private void zeroHack$xrayAlpha(BlockRenderView world, List<BlockModelPart> parts, BlockState state, BlockPos pos, MatrixStack matrices, VertexConsumer vertexConsumer, boolean cull, int overlay, CallbackInfo ci) {
        int alpha = XRay.getAlpha(state, pos);

        if (alpha == 0) ci.cancel();
        else ALPHA.set(alpha);
    }

    @ModifyConstant(method = "renderQuad", constant = @Constant(floatValue = 1.0F, ordinal = 3))
    private float zeroHack$modifyQuadAlpha(float original) {
        int alpha = ALPHA.get();
        return alpha == -1 ? original : alpha / 255.0F;
    }
}
