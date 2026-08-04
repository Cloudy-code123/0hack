package com.hack.zero.mixin;

import com.hack.zero.ZeroHack;
import com.hack.zero.module.render.Freecam;
import net.minecraft.client.input.KeyboardInput;
import net.minecraft.util.PlayerInput;
import net.minecraft.util.math.Vec2f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardInput.class)
public abstract class KeyboardInputMixin {
    @Inject(method = "tick", at = @At("TAIL"))
    private void zeroHack$freezeInput(CallbackInfo ci) {
        Freecam freecam = getFreecam();
        if (freecam != null && freecam.isEnabled()) {
            InputAccessor input = (InputAccessor) (Object) this;
            input.setPlayerInput(PlayerInput.DEFAULT);
            input.setMovementVector(Vec2f.ZERO);
        }
    }

    private static Freecam getFreecam() {
        ZeroHack instance = ZeroHack.getInstance();
        if (instance == null) return null;
        return (Freecam) instance.getModuleManager().getModule("Freecam").orElse(null);
    }
}
