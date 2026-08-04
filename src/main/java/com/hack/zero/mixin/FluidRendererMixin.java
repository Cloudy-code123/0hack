package com.hack.zero.mixin;

import com.hack.zero.module.render.XRay;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.FluidRenderer;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FluidRenderer.class)
public abstract class FluidRendererMixin {
    @Unique
    private static final ThreadLocal<Integer> FLUID_ALPHA = ThreadLocal.withInitial(() -> -1);

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void zeroHack$xrayRender(BlockRenderView world, BlockPos pos, VertexConsumer vertexConsumer, BlockState blockState, FluidState fluidState, CallbackInfo ci) {
        int alpha = XRay.getFluidAlpha(fluidState, pos);

        if (alpha == 0) ci.cancel();
        else FLUID_ALPHA.set(alpha);
    }

    @Inject(method = "vertex", at = @At("HEAD"), cancellable = true)
    private void zeroHack$xrayVertex(VertexConsumer vertexConsumer, float x, float y, float z, float red, float green, float blue, float u, float v, int light, CallbackInfo ci) {
        int alpha = FLUID_ALPHA.get();

        if (alpha != -1) {
            vertexConsumer.vertex(x, y, z).color((int) (red * 255), (int) (green * 255), (int) (blue * 255), alpha).texture(u, v).light(light).normal(0.0F, 1.0F, 0.0F);
            ci.cancel();
        }
    }
}
