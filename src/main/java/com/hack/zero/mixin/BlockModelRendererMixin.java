package com.hack.zero.mixin;

import com.hack.zero.ZeroHack;
import com.hack.zero.module.render.XRay;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.block.BlockModelRenderer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import net.minecraft.client.render.model.BlockModelPart;

@Mixin(BlockModelRenderer.class)
public class BlockModelRendererMixin {

    @Inject(method = "render(Lnet/minecraft/world/BlockRenderView;Ljava/util/List;Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;ZI)V", at = @At("HEAD"), cancellable = true)
    private void onRender(BlockRenderView world, List<BlockModelPart> parts, BlockState state, BlockPos pos, net.minecraft.client.util.math.MatrixStack matrices, net.minecraft.client.render.VertexConsumer vertexConsumer, boolean checkSides, int seed, CallbackInfo ci) {
        XRay xray = (XRay) ZeroHack.getInstance().getModuleManager().getModule("XRay").orElse(null);
        if (xray != null && xray.isEnabled() && !xray.shouldRender(state)) {
            ci.cancel();
        }
    }
}
