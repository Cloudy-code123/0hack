package com.hack.zero.mixin;

import com.hack.zero.module.movement.Velocity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityMixin {

    @Inject(method = "setVelocityClient", at = @At("HEAD"), cancellable = true)
    private void onSetVelocityClient(double x, double y, double z, CallbackInfo ci) {
        try {
            if (!((Object) this instanceof ClientPlayerEntity)) return;
            com.hack.zero.ZeroHack instance = com.hack.zero.ZeroHack.getInstance();
            if (instance == null) return;
            Velocity velocity = (Velocity) instance.getModuleManager().getModule("Velocity").orElse(null);
            if (velocity == null || !velocity.isEnabled()) return;
            ci.cancel();
        } catch (Exception ignored) {}
    }
}
