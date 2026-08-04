package com.hack.zero.mixin;

import com.hack.zero.ZeroHack;
import com.hack.zero.module.render.Freecam;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public class MouseMixin {

    @Inject(method = "onMouseScroll", at = @At("HEAD"), cancellable = true)
    private void zeroHack$onMouseScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        if (MinecraftClient.getInstance().currentScreen != null) return;

        ZeroHack instance = ZeroHack.getInstance();
        if (instance == null) return;

        Freecam freecam = (Freecam) instance.getModuleManager().getModule("Freecam").orElse(null);
        if (freecam != null && freecam.isEnabled() && freecam.onScroll(vertical)) {
            ci.cancel();
        }
    }
}
