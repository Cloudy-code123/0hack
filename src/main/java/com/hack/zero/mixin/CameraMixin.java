package com.hack.zero.mixin;

import com.hack.zero.ZeroHack;
import com.hack.zero.module.render.Freecam;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin {
    @Shadow
    protected abstract void setPos(Vec3d pos);

    @Shadow
    protected abstract void setRotation(float yaw, float pitch);

    @Inject(method = "update", at = @At("TAIL"))
    private void zeroHack$applyFreecam(BlockView area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo ci) {
        Freecam freecam = getFreecam();
        if (freecam == null || !freecam.isEnabled()) return;

        setPos(new Vec3d(freecam.getX(tickDelta), freecam.getY(tickDelta), freecam.getZ(tickDelta)));
        setRotation((float) freecam.getYaw(tickDelta), (float) freecam.getPitch(tickDelta));
    }

    private static Freecam getFreecam() {
        ZeroHack instance = ZeroHack.getInstance();
        if (instance == null) return null;
        return (Freecam) instance.getModuleManager().getModule("Freecam").orElse(null);
    }
}
