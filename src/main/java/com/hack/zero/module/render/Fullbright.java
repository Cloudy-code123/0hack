package com.hack.zero.module.render;

import com.hack.zero.module.Category;
import com.hack.zero.module.Module;
import com.hack.zero.module.setting.ModeSetting;
import com.hack.zero.module.setting.NumberSetting;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

public class Fullbright extends Module {
    private final ModeSetting mode = new ModeSetting("Mode", "Both", "Gamma", "Potion", "Both");
    private final NumberSetting gammaValue = new NumberSetting("Gamma", 1.0, 0.1, 1.0, 0.1);

    private double originalGamma;
    private boolean hadNightVision;

    private static final int NIGHT_VISION_DURATION = 600;
    private static final int NIGHT_VISION_REFRESH_THRESHOLD = 200;

    public Fullbright() {
        super("Fullbright", "See in the dark", Category.RENDER, 0);
        addSetting(mode);
        addSetting(gammaValue);
        setExpandable(false);
    }

    @Override
    protected void onEnable() {
        if (mc.options != null) {
            originalGamma = mc.options.getGamma().getValue();
        }
        if (mc.player != null) {
            hadNightVision = mc.player.hasStatusEffect(StatusEffects.NIGHT_VISION);
        }
    }

    @Override
    protected void onDisable() {
        if (mc.options != null) {
            mc.options.getGamma().setValue(originalGamma);
        }
        if (mc.player != null) {
            mc.player.removeStatusEffect(StatusEffects.NIGHT_VISION);
            if (hadNightVision) {
                mc.player.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.NIGHT_VISION, NIGHT_VISION_REFRESH_THRESHOLD, 0, true, false, false));
            }
            hadNightVision = false;
        }
    }

    @Override
    public void onUpdate() {
        if (mc.player == null || mc.options == null) return;

        String modeVal = mode.getValue();

        if ("Gamma".equals(modeVal) || "Both".equals(modeVal)) {
            mc.options.getGamma().setValue(Math.min(gammaValue.getValue(), 1.0));
        }

        if ("Potion".equals(modeVal) || "Both".equals(modeVal)) {
            StatusEffectInstance current = mc.player.getStatusEffect(StatusEffects.NIGHT_VISION);
            if (current == null || current.getDuration() <= NIGHT_VISION_REFRESH_THRESHOLD) {
                mc.player.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.NIGHT_VISION, NIGHT_VISION_DURATION, 0, true, false, false));
            }
        }
    }
}
