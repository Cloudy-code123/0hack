package com.hack.zero.mixin;

import com.hack.zero.module.movement.Velocity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Unique
    private Vec3d velocityPlus$scaleMovementInput(Vec3d movementInput) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!(self instanceof PlayerEntity)) return movementInput;
        if (!self.getWorld().isClient()) return movementInput;

        ClientPlayerEntity player = (ClientPlayerEntity) self;
        Velocity velocity = getVelocityModule();
        if (velocity == null || !velocity.isEnabled()) return movementInput;

        velocity.tick(player);

        double multiplier = velocity.getCurrentEffectiveMultiplier();
        return new Vec3d(movementInput.x * multiplier, movementInput.y, movementInput.z * multiplier);
    }

    @Unique
    private void velocityPlus$applyJumpBoost(CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!(self instanceof PlayerEntity)) return;
        if (!self.getWorld().isClient()) return;

        ClientPlayerEntity player = (ClientPlayerEntity) self;
        Velocity velocity = getVelocityModule();
        if (velocity == null || !velocity.isEnabled()) return;
        if (!velocity.isJumpBoostEnabled()) return;

        velocity.onJump(player);
    }

    @Unique
    private void velocityPlus$cancelKnockback(double strength, double x, double z, CallbackInfo ci) {
        try {
            if (!((Object) this instanceof ClientPlayerEntity)) return;
            Velocity velocity = getVelocityModule();
            if (velocity == null || !velocity.isEnabled()) return;
            ci.cancel();
        } catch (Exception ignored) {}
    }

    @Unique
    private Velocity getVelocityModule() {
        com.hack.zero.ZeroHack instance = com.hack.zero.ZeroHack.getInstance();
        if (instance == null) return null;
        return (Velocity) instance.getModuleManager().getModule("Velocity").orElse(null);
    }

    @ModifyVariable(method = "travel", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private Vec3d modifyMovementInput(Vec3d movementInput) {
        return velocityPlus$scaleMovementInput(movementInput);
    }

    @Inject(method = "jump", at = @At("HEAD"))
    private void onJump(CallbackInfo ci) {
        velocityPlus$applyJumpBoost(ci);
    }

    @Inject(method = "takeKnockback", at = @At("HEAD"), cancellable = true)
    private void onTakeKnockback(double strength, double x, double z, CallbackInfo ci) {
        velocityPlus$cancelKnockback(strength, x, z, ci);
    }
}
