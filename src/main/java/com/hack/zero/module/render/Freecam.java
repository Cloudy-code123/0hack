package com.hack.zero.module.render;

import com.hack.zero.module.Category;
import com.hack.zero.module.Module;
import com.hack.zero.module.setting.BooleanSetting;
import com.hack.zero.module.setting.ModeSetting;
import com.hack.zero.module.setting.NumberSetting;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.option.Perspective;
import net.minecraft.entity.Entity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.Text;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.joml.Vector3d;

public class Freecam extends Module {
    private final ModeSetting viewMode = new ModeSetting("Mode", "First Person", "First Person", "Third Person");
    private final NumberSetting speed = new NumberSetting("Speed", 1.0, 0.1, 20.0, 0.1);
    private final NumberSetting speedScrollSensitivity = new NumberSetting("Scroll Sensitivity", 0.0, 0.0, 2.0, 0.1);
    private final BooleanSetting staySneaking = new BooleanSetting("Stay Sneaking", true);
    private final BooleanSetting toggleOnDamage = new BooleanSetting("Toggle On Damage", false);
    private final BooleanSetting toggleOnDeath = new BooleanSetting("Toggle On Death", false);
    private final BooleanSetting toggleOnLog = new BooleanSetting("Toggle On Log", true);
    private final BooleanSetting reloadChunks = new BooleanSetting("Reload Chunks", true);
    private final BooleanSetting showHands = new BooleanSetting("Show Hands", true);
    private final BooleanSetting rotate = new BooleanSetting("Rotate", false);
    private final BooleanSetting staticView = new BooleanSetting("Static", true);
    private final BooleanSetting showPlayer = new BooleanSetting("Show Player", true);
    private final BooleanSetting movePlayer = new BooleanSetting("Move Player", false);

    public final Vector3d pos = new Vector3d();

    private Perspective perspective;

    public float yaw, pitch;

    private double fovScale;
    private boolean bobView;

    private boolean isSneaking;
    private boolean lastPerspectiveKeyDown;

    private boolean wasNoClip;
    private boolean wasInvulnerable;

    private Vec3d bodyPos;

    private float lastHealth;
    private RegistryKey<World> lastDim;

    public Freecam() {
        super("Freecam", "Allows the camera to move away from the player.", Category.RENDER, 0);
        addSetting(viewMode);
        addSetting(speed);
        addSetting(speedScrollSensitivity);
        addSetting(staySneaking);
        addSetting(toggleOnDamage);
        addSetting(toggleOnDeath);
        addSetting(toggleOnLog);
        addSetting(reloadChunks);
        addSetting(showHands);
        addSetting(rotate);
        addSetting(staticView);
        addSetting(showPlayer);
        addSetting(movePlayer);

        ClientPlayConnectionEvents.DISCONNECT.register(this::onDisconnect);
        WorldRenderEvents.LAST.register(this::onWorldRender);
    }

    @Override
    public void onEnable() {
        if (mc.player == null || mc.world == null) {
            setEnabled(false);
            return;
        }

        fovScale = mc.options.getFovEffectScale().getValue();
        bobView = mc.options.getBobView().getValue();
        if (staticView.isOn()) {
            mc.options.getFovEffectScale().setValue(0.0);
            mc.options.getBobView().setValue(false);
        }

        yaw = mc.player.getYaw();
        pitch = mc.player.getPitch();

        perspective = mc.options.getPerspective();

        Vec3d camPos = mc.gameRenderer.getCamera().getCameraPos();
        pos.set(camPos.x, camPos.y, camPos.z);

        if (perspective.isFrontView()) {
            yaw += 180;
            pitch *= -1;
        }

        isSneaking = mc.options.sneakKey.isPressed();

        wasNoClip = mc.player.noClip;
        wasInvulnerable = mc.player.isInvulnerable();
        bodyPos = mc.player.getPos();

        applyPerspectiveMode();
        mc.player.setInvulnerable(true);

        lastHealth = mc.player.getHealth();
        lastDim = mc.player.getWorld().getRegistryKey();

        if (reloadChunks.isOn()) mc.worldRenderer.reload();
    }

    @Override
    public void onDisable() {
        if (reloadChunks.isOn() && mc.worldRenderer != null) {
            mc.execute(mc.worldRenderer::reload);
        }

        if (perspective != null) {
            mc.options.setPerspective(perspective);
        }
        if (staticView.isOn()) {
            mc.options.getFovEffectScale().setValue(fovScale);
            mc.options.getBobView().setValue(bobView);
        }
        if (mc.player != null) {
            mc.player.noClip = wasNoClip;
            mc.player.setInvulnerable(wasInvulnerable);
        }

        isSneaking = false;
        lastPerspectiveKeyDown = false;
    }

    @Override
    public void onUpdate() {
        if (mc.player == null || mc.world == null) return;

        Entity cameraEntity = mc.getCameraEntity();
        if (cameraEntity != null && cameraEntity.isInsideWall()) {
            cameraEntity.noClip = true;
        }

        if (mc.player != null) {
            mc.player.noClip = true;
            if (!movePlayer.isOn() && bodyPos != null) {
                mc.player.setPosition(bodyPos.x, bodyPos.y, bodyPos.z);
                mc.player.setVelocity(Vec3d.ZERO);
                mc.player.fallDistance = 0.0f;
            }
        }

        boolean perspectiveKeyDown = mc.options.togglePerspectiveKey.isPressed();
        if (perspectiveKeyDown && !lastPerspectiveKeyDown) {
            viewMode.cycle();
        }
        lastPerspectiveKeyDown = perspectiveKeyDown;

        if (perspective != null) {
            applyPerspectiveMode();
        }

        if (rotate.isOn()) rotatePlayer();

        if (toggleOnDeath.isOn() && mc.player.isDead()) {
            toggleOff("you died");
            return;
        }
        if (toggleOnDamage.isOn() && lastHealth > mc.player.getHealth()) {
            toggleOff("you took damage");
            return;
        }
        lastHealth = mc.player.getHealth();

        RegistryKey<World> dim = mc.player.getWorld().getRegistryKey();
        if (lastDim != null && !lastDim.equals(dim)) {
            toggleOff("you changed dimensions");
            return;
        }
        lastDim = dim;
    }

    private void onWorldRender(WorldRenderContext context) {
        if (!isEnabled() || mc.isPaused()) return;
        if (mc.player == null || mc.world == null) return;
        if (mc.currentScreen != null) return;

        float ticks = context.tickCounter().getDynamicDeltaTicks();
        if (ticks <= 0) return;
        if (ticks > 1.0f) ticks = 1.0f;

        boolean forward = mc.options.forwardKey.isPressed();
        boolean backward = mc.options.backKey.isPressed();
        boolean left = mc.options.leftKey.isPressed();
        boolean right = mc.options.rightKey.isPressed();
        boolean up = mc.options.jumpKey.isPressed();
        boolean down = mc.options.sneakKey.isPressed();

        double s = mc.options.sprintKey.isPressed() ? 1.0 : 0.5;
        double speedValue = speed.getValue();

        Vec3d forwardDir = Vec3d.fromPolar(0, yaw);
        Vec3d rightDir = Vec3d.fromPolar(0, yaw + 90);

        double moveX = 0, moveY = 0, moveZ = 0;
        if (forward) {
            moveX += forwardDir.x * s * speedValue;
            moveZ += forwardDir.z * s * speedValue;
        }
        if (backward) {
            moveX -= forwardDir.x * s * speedValue;
            moveZ -= forwardDir.z * s * speedValue;
        }
        if (right) {
            moveX += rightDir.x * s * speedValue;
            moveZ += rightDir.z * s * speedValue;
        }
        if (left) {
            moveX -= rightDir.x * s * speedValue;
            moveZ -= rightDir.z * s * speedValue;
        }

        if ((forward || backward) && (left || right)) {
            double diagonal = 1 / Math.sqrt(2);
            moveX *= diagonal;
            moveZ *= diagonal;
        }

        if (up) moveY += s * speedValue;
        if (down) moveY -= s * speedValue;

        pos.set(pos.x + moveX * ticks, pos.y + moveY * ticks, pos.z + moveZ * ticks);

        if (movePlayer.isOn() && mc.player != null) {
            mc.player.noClip = true;
            mc.player.setYaw(yaw);
            mc.player.setPitch(pitch);
            mc.player.setPosition(pos.x, pos.y - mc.player.getEyeHeight(mc.player.getPose()), pos.z);
            mc.player.fallDistance = 0.0f;
        }
    }

    private void rotatePlayer() {
        HitResult hit = mc.crosshairTarget;
        if (hit == null || hit.getType() == HitResult.Type.MISS) return;

        Vec3d eye = mc.gameRenderer.getCamera().getCameraPos();
        Vec3d target = hit.getPos();

        double dx = target.x - eye.x;
        double dy = target.y - eye.y;
        double dz = target.z - eye.z;
        double dist = Math.sqrt(dx * dx + dz * dz);

        float targetYaw = (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
        float targetPitch = (float) -Math.toDegrees(Math.atan2(dy, dist));

        mc.player.setYaw(targetYaw);
        mc.player.setPitch(MathHelper.clamp(targetPitch, -90.0f, 90.0f));
    }

    private void toggleOff(String reason) {
        setEnabled(false);
        if (mc.player != null) {
            mc.player.sendMessage(Text.literal("[0hack] Freecam disabled (" + reason + ")."), false);
        }
    }

    private void onDisconnect(ClientPlayNetworkHandler handler, MinecraftClient client) {
        if (toggleOnLog.isOn() && isEnabled()) {
            setEnabled(false);
        }
    }

    public boolean onScroll(double amount) {
        if (speedScrollSensitivity.getValue() > 0) {
            double next = speed.getValue() + amount * 0.25 * (speedScrollSensitivity.getValue() * speed.getValue());
            speed.setValue(MathHelper.clamp(next, speed.getMin(), speed.getMax()));
            return true;
        }
        return false;
    }

    public void changeLookDirection(double deltaX, double deltaY) {
        yaw += (float) deltaX;
        pitch += (float) deltaY;

        pitch = MathHelper.clamp(pitch, -90, 90);
    }

    public boolean renderHands() {
        return !isEnabled() || showHands.isOn();
    }

    public boolean showPlayer() {
        return isEnabled() && isThirdPersonMode() && showPlayer.isOn();
    }

    private boolean isThirdPersonMode() {
        return "Third Person".equals(viewMode.getValue());
    }

    private void applyPerspectiveMode() {
        mc.options.setPerspective(isThirdPersonMode() ? Perspective.THIRD_PERSON_BACK : Perspective.FIRST_PERSON);
    }

    public boolean staySneaking() {
        return isEnabled() && mc.player != null && !mc.player.getAbilities().flying && staySneaking.isOn() && isSneaking;
    }

    public double getX(float tickDelta) {
        return pos.x;
    }

    public double getY(float tickDelta) {
        return pos.y;
    }

    public double getZ(float tickDelta) {
        return pos.z;
    }

    public double getYaw(float tickDelta) {
        return yaw;
    }

    public double getPitch(float tickDelta) {
        return pitch;
    }
}
