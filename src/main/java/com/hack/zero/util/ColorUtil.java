package com.hack.zero.util;

import java.awt.Color;

public class ColorUtil {
    public static float[] getRGB(int color) {
        return new float[]{
                ((color >> 16) & 0xFF) / 255.0f,
                ((color >> 8) & 0xFF) / 255.0f,
                (color & 0xFF) / 255.0f
        };
    }

    public static float[] getRGBA(int color) {
        return new float[]{
                ((color >> 16) & 0xFF) / 255.0f,
                ((color >> 8) & 0xFF) / 255.0f,
                (color & 0xFF) / 255.0f,
                ((color >> 24) & 0xFF) / 255.0f
        };
    }

    public static int getHealthColor(float health, float maxHealth) {
        float ratio = health / maxHealth;
        int r = (int) (255 * (1.0f - ratio));
        int g = (int) (255 * ratio);
        return new Color(r, g, 0, 200).getRGB();
    }

    public static int fade(int color, int alpha) {
        float a = alpha / 255.0f;
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        return ((int) (alpha * a) << 24) | (r << 16) | (g << 8) | b;
    }

    public static int rainbow(int offset, float saturation) {
        float hue = (System.currentTimeMillis() % 3600L) / 3600.0f + offset / 100.0f;
        return Color.HSBtoRGB(hue, saturation, 1.0f);
    }
}
