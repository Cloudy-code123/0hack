package com.hack.zero.mixin;

import com.hack.zero.ZeroHack;
import com.hack.zero.module.render.Freecam;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Inject(method = "renderWorld", at = @At("HEAD"))
    private void onRenderWorld(RenderTickCounter tickCounter, CallbackInfo ci) {
    }

    @Inject(method = "renderHand", at = @At("HEAD"), cancellable = true)
    private void zeroHack$renderHand(float tickDelta, boolean fancy, Matrix4f projectionMatrix, CallbackInfo ci) {
        ZeroHack instance = ZeroHack.getInstance();
        if (instance == null) return;

        Freecam freecam = (Freecam) instance.getModuleManager().getModule("Freecam").orElse(null);
        if (freecam != null && !freecam.renderHands()) {
            ci.cancel();
        }
    }
}
