package com.hack.zero.mixin;

import com.hack.zero.module.render.XRay;
import net.minecraft.block.AbstractBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractBlock.AbstractBlockState.class)
public abstract class BlockStateMixin {
    @Inject(method = "getOpacity", at = @At("HEAD"), cancellable = true)
    private void zeroHack$xrayOpacity(CallbackInfoReturnable<Integer> cir) {
        if (XRay.isActive()) cir.setReturnValue(0);
    }

    @Inject(method = "getAmbientOcclusionLightLevel", at = @At("HEAD"), cancellable = true)
    private void zeroHack$xrayAmbientOcclusion(BlockView world, BlockPos pos, CallbackInfoReturnable<Float> cir) {
        if (XRay.isActive()) cir.setReturnValue(1.0F);
    }
}
