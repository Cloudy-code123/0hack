package com.hack.zero.mixin;

import com.hack.zero.module.render.XRay;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractBlock.AbstractBlockState.class)
public class BlockMixin {

    @Inject(method = "isSideInvisible", at = @At("HEAD"), cancellable = true)
    private void onIsSideInvisible(BlockState state, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        XRay xray = XRay.getInstance();
        if (xray != null && xray.isEnabled()) {
            cir.setReturnValue(false);
        }
    }
}
