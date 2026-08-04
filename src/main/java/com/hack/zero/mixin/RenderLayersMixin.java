package com.hack.zero.mixin;

import com.hack.zero.module.render.XRay;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.BlockRenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.fluid.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RenderLayers.class)
public abstract class RenderLayersMixin {
    @Inject(method = "getBlockLayer", at = @At("HEAD"), cancellable = true)
    private static void zeroHack$xrayGetBlockLayer(BlockState state, CallbackInfoReturnable<BlockRenderLayer> cir) {
        int alpha = XRay.getAlpha(state, null);
        if (alpha > 0 && alpha < 255) cir.setReturnValue(BlockRenderLayer.TRANSLUCENT);
    }

    @Inject(method = "getFluidLayer", at = @At("HEAD"), cancellable = true)
    private static void zeroHack$xrayGetFluidLayer(FluidState state, CallbackInfoReturnable<BlockRenderLayer> cir) {
        int alpha = XRay.getFluidAlpha(state, null);
        if (alpha > 0 && alpha < 255) cir.setReturnValue(BlockRenderLayer.TRANSLUCENT);
    }
}
