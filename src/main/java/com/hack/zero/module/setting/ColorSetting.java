package com.hack.zero.module.setting;

public class ColorSetting extends Setting<Integer> {
    private static final int[] PRESETS = {
            0xFFFF4444, // Red
            0xFF44FF44, // Green
            0xFF4444FF, // Blue
            0xFFFFFF44, // Yellow
            0xFF44FFFF, // Cyan
            0xFFFF44FF, // Magenta
            0xFFFF8844, // Orange
            0xFFFFFFFF, // White
            0xFF44FF88, // Mint
            0xFFFF4488, // Pink
            0xFF8844FF, // Purple
            0xFF888888, // Gray
    };
    private int presetIndex;

    public ColorSetting(String name, int defaultARGB) {
        super(name, defaultARGB);
        this.presetIndex = findClosestPreset(defaultARGB);
    }

    public int getRed() { return (getValue() >> 16) & 0xFF; }
    public int getGreen() { return (getValue() >> 8) & 0xFF; }
    public int getBlue() { return getValue() & 0xFF; }
    public int getAlpha() { return (getValue() >> 24) & 0xFF; }

    public float getRedF() { return getRed() / 255.0f; }
    public float getGreenF() { return getGreen() / 255.0f; }
    public float getBlueF() { return getBlue() / 255.0f; }
    public float getAlphaF() { return getAlpha() / 255.0f; }

    public void cycle() {
        presetIndex = (presetIndex + 1) % PRESETS.length;
        setValue(PRESETS[presetIndex]);
    }

    public void setRGB(int r, int g, int b) {
        int a = getAlpha();
        setValue((a << 24) | (r << 16) | (g << 8) | b);
    }

    private int findClosestPreset(int color) {
        int bestIdx = 0;
        int bestDist = Integer.MAX_VALUE;
        int cr = (color >> 16) & 0xFF, cg = (color >> 8) & 0xFF, cb = color & 0xFF;
        for (int i = 0; i < PRESETS.length; i++) {
            int pr = (PRESETS[i] >> 16) & 0xFF, pg = (PRESETS[i] >> 8) & 0xFF, pb = PRESETS[i] & 0xFF;
            int dist = (cr - pr) * (cr - pr) + (cg - pg) * (cg - pg) + (cb - pb) * (cb - pb);
            if (dist < bestDist) { bestDist = dist; bestIdx = i; }
        }
        return bestIdx;
    }
}
