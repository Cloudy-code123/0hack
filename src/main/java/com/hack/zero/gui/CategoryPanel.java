package com.hack.zero.gui;

import com.hack.zero.module.Category;
import com.hack.zero.module.Module;
import com.hack.zero.module.setting.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.opengl.GL11;

import java.util.*;

public class CategoryPanel {
    private final Category category;
    private int x, y;
    private static final int WIDTH = 176;
    private static final int HEADER_HEIGHT = 22;
    private static final int MODULE_HEIGHT = 16;
    private static final int SETTING_HEIGHT = 28;
    private static final int PANEL_MAX_HEIGHT = 340;
    private int scrollOffset = 0;
    private int contentHeight = 0;
    private final Set<Module> expanded = new HashSet<>();
    private final Set<String> loggedModules = new HashSet<>();

    private static final int BG_DARK = 0xE60D1117;
    private static final int HEADER_COLOR = 0xFF1A1E2E;
    private static final int MODULE_COLOR = 0xCC111520;
    private static final int MODULE_HOVER = 0xCC1C2033;
    private static final int ENABLED_COLOR = 0xFF00E676;
    private static final int DISABLED_COLOR = 0xFF6B7280;
    private static final int TEXT_COLOR = 0xFFD1D5DB;
    private static final int SLIDER_BG = 0xFF0A0E1A;
    private static final int SLIDER_FG = 0xFF3B82F6;
    private static final int SLIDER_KNOB = 0xFFFFFFFF;
    private static final int ACCENT = 0xFF3B82F6;
    private static final int TOGGLE_ON = 0xFF00E676;
    private static final int TOGGLE_OFF = 0xFF4B5563;
    private static final int MODE_COLOR = 0xFFFBBF24;
    private static final int BORDER_COLOR = 0xFF2D3348;
    private static final int SEPARATOR = 0x44FFFFFF;
    private static final int ARROW_COLOR = 0xFF888888;
    private static final int HINT_COLOR = 0xFF555555;
    private static final int SCROLLBAR_BG = 0x30FFFFFF;
    private static final int SCROLLBAR_FG = 0xFF555555;

    public static final int SLIDER_HEIGHT = 8;
    public static final int SLIDER_KNOB_SIZE = 12;
    public static final int SLIDER_Y_OFFSET = 18;

    public CategoryPanel(Category category, int x, int y) {
        this.category = category;
        this.x = x;
        this.y = y;
    }

    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        MinecraftClient mc = MinecraftClient.getInstance();
        List<Module> modules = com.hack.zero.ZeroHack.getInstance().getModuleManager().getModulesByCategory(category);

        contentHeight = HEADER_HEIGHT;
        for (Module mod : modules) {
            contentHeight += MODULE_HEIGHT;
            if (expanded.contains(mod)) {
                contentHeight += 2 + mod.getSettings().size() * SETTING_HEIGHT;
            }
        }

        int visibleHeight = Math.min(contentHeight, PANEL_MAX_HEIGHT);

        context.fill(x, y, x + WIDTH, y + visibleHeight, BG_DARK);
        context.fill(x, y, x + WIDTH, y + HEADER_HEIGHT, HEADER_COLOR);
        context.fill(x, y, x + WIDTH, y + 1, ACCENT);

        String categoryName = category.getDisplayName();
        int catWidth = mc.textRenderer.getWidth(categoryName);
        context.drawTextWithShadow(mc.textRenderer, categoryName, x + 6, y + 7, 0xFFFFFFFF);

        String countStr = modules.size() + "";
        context.drawTextWithShadow(mc.textRenderer, countStr, x + WIDTH - mc.textRenderer.getWidth(countStr) - 6, y + 7, 0xFF6B7280);

        double scaleFactor = mc.getWindow().getScaleFactor();
        int clipX1 = (int)(x * scaleFactor);
        int clipY1 = (int)((mc.getWindow().getHeight() - (y + visibleHeight)) * scaleFactor);
        int clipW = (int)(WIDTH * scaleFactor);
        int clipH = (int)(visibleHeight * scaleFactor);
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(clipX1, clipY1, clipW, clipH);

        int clipBottom = y + visibleHeight;
        int clipTop = y + HEADER_HEIGHT;
        int yOffset = y + HEADER_HEIGHT - scrollOffset;

        for (Module mod : modules) {
            int moduleTop = yOffset;
            int moduleBottom = yOffset + MODULE_HEIGHT;

            boolean expandedModule = expanded.contains(mod);
            int settingsHeight = expandedModule ? 2 + mod.getSettings().size() * SETTING_HEIGHT : 0;
            int blockBottom = moduleBottom + settingsHeight;

            if (blockBottom < clipTop || moduleTop > clipBottom) {
                yOffset = blockBottom;
                continue;
            }

            if (moduleBottom >= clipTop && moduleTop <= clipBottom) {
                boolean hovering = mouseX >= x && mouseX <= x + WIDTH && mouseY >= moduleTop && mouseY <= moduleBottom;
                context.fill(x + 1, moduleTop, x + WIDTH - 1, moduleBottom, hovering ? MODULE_HOVER : MODULE_COLOR);

                if (mod.isEnabled()) {
                    context.fill(x, moduleTop, x + 2, moduleBottom, ENABLED_COLOR);
                }

                String arrow = expandedModule ? "\u25BC" : "\u25B6";
                int nameColor = mod.isEnabled() ? ENABLED_COLOR : DISABLED_COLOR;
                context.drawTextWithShadow(mc.textRenderer, mod.getName(), x + 14, moduleTop + 5, nameColor);
                context.drawTextWithShadow(mc.textRenderer, arrow, x + 4, moduleTop + 5, ARROW_COLOR);

                String toggleText = mod.isEnabled() ? "ON" : "OFF";
                int toggleColor = mod.isEnabled() ? TOGGLE_ON : TOGGLE_OFF;
                context.fill(x + WIDTH - 30, moduleTop + 4, x + WIDTH - 4, moduleTop + 12, toggleColor);
                int toggleTextX = x + WIDTH - 27 + (23 - mc.textRenderer.getWidth(toggleText)) / 2;
                context.drawTextWithShadow(mc.textRenderer, toggleText, toggleTextX, moduleTop + 5, 0xFF000000);

                if (!expandedModule && !mod.getSettings().isEmpty() && mod.isExpandable()) {
                    String hint = "RClick";
                    context.drawTextWithShadow(mc.textRenderer, hint, x + 14 + mc.textRenderer.getWidth(mod.getName()) + 4, moduleTop + 5, HINT_COLOR);
                }
            }

            yOffset = moduleBottom;

            if (expandedModule && !mod.getSettings().isEmpty()) {
                if (mod.getSettings().size() > 0 && !loggedModules.contains(mod.getName())) {
                    loggedModules.add(mod.getName());
                    System.out.println("[0hack] CategoryPanel.render: " + mod.getName() + " settings count=" + mod.getSettings().size());
                    for (Setting<?> s : mod.getSettings()) {
                        System.out.println("[0hack]   -> " + s.getName() + " (" + s.getClass().getSimpleName() + ")");
                    }
                }
                context.fill(x + 4, yOffset, x + WIDTH - 4, yOffset + 1, SEPARATOR);
                yOffset += 2;

                for (Setting<?> setting : mod.getSettings()) {
                    int settingTop = yOffset;
                    int settingBottom = yOffset + SETTING_HEIGHT;

                    if (settingBottom >= clipTop && settingTop <= clipBottom) {
                        renderSetting(context, setting, x, settingTop, mouseX, mouseY);
                    }
                    yOffset = settingBottom;
                }
            }
        }

        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        context.fill(x, y, x + 1, y + visibleHeight, BORDER_COLOR);
        context.fill(x + WIDTH - 1, y, x + WIDTH, y + visibleHeight, BORDER_COLOR);
        context.fill(x, y + visibleHeight - 1, x + WIDTH, y + visibleHeight, BORDER_COLOR);

        if (contentHeight > PANEL_MAX_HEIGHT) {
            int scrollableHeight = visibleHeight - HEADER_HEIGHT;
            int scrollbarMax = scrollableHeight - 8;
            int maxScroll = Math.max(1, contentHeight - PANEL_MAX_HEIGHT);
            double scrollRatio = Math.min(1.0, (double) scrollOffset / maxScroll);
            int scrollbarY = y + HEADER_HEIGHT + (int)(scrollRatio * scrollbarMax);
            int scrollbarH = Math.max(8, (int)(scrollableHeight * ((double) PANEL_MAX_HEIGHT / contentHeight)));

            context.fill(x + WIDTH - 4, y + HEADER_HEIGHT, x + WIDTH - 1, y + visibleHeight - 1, SCROLLBAR_BG);
            context.fill(x + WIDTH - 4, scrollbarY, x + WIDTH - 1, scrollbarY + scrollbarH, SCROLLBAR_FG);
        }

        context.fill(x, y, x + WIDTH, y + 1, ACCENT);
    }

    private void renderSetting(DrawContext context, Setting<?> setting, int sx, int sy, int mouseX, int mouseY) {
        MinecraftClient mc = MinecraftClient.getInstance();

        if (setting instanceof BooleanSetting boolSetting) {
            context.fill(sx + 1, sy, sx + WIDTH - 1, sy + SETTING_HEIGHT, MODULE_COLOR);
            context.drawTextWithShadow(mc.textRenderer, setting.getName(), sx + 10, sy + 6, TEXT_COLOR);

            boolean toggled = boolSetting.isOn();
            int boxX = sx + WIDTH - 34;
            int boxY = sy + 6;
            context.fill(boxX, boxY, boxX + 26, boxY + 14, toggled ? TOGGLE_ON : TOGGLE_OFF);
            int circleX = toggled ? boxX + 15 : boxX + 3;
            context.fill(circleX, boxY + 2, circleX + 8, boxY + 12, 0xFFFFFFFF);

        } else if (setting instanceof ModeSetting modeSetting) {
            context.fill(sx + 1, sy, sx + WIDTH - 1, sy + SETTING_HEIGHT, MODULE_COLOR);
            context.drawTextWithShadow(mc.textRenderer, setting.getName(), sx + 10, sy + 6, TEXT_COLOR);

            String modeStr = "< " + modeSetting.getValue() + " >";
            context.drawTextWithShadow(mc.textRenderer, modeStr, sx + WIDTH - mc.textRenderer.getWidth(modeStr) - 8, sy + 6, MODE_COLOR);

        } else if (setting instanceof ColorSetting colorSetting) {
            context.fill(sx + 1, sy, sx + WIDTH - 1, sy + SETTING_HEIGHT, MODULE_COLOR);
            context.drawTextWithShadow(mc.textRenderer, setting.getName(), sx + 10, sy + 6, TEXT_COLOR);

            int boxX = sx + WIDTH - 34;
            int boxY = sy + 5;
            int color = colorSetting.getValue();
            context.fill(boxX, boxY, boxX + 26, boxY + 16, color);
            context.fill(boxX - 1, boxY - 1, boxX, boxY + 17, 0xFF555555);
            context.fill(boxX + 26, boxY - 1, boxX + 27, boxY + 17, 0xFF555555);
            context.fill(boxX - 1, boxY - 1, boxX + 27, boxY, 0xFF555555);
            context.fill(boxX - 1, boxY + 16, boxX + 27, boxY + 17, 0xFF555555);

        } else if (setting instanceof NumberSetting numSetting) {
            context.fill(sx + 1, sy, sx + WIDTH - 1, sy + SETTING_HEIGHT, MODULE_COLOR);

            context.drawTextWithShadow(mc.textRenderer, setting.getName(), sx + 10, sy + 4, TEXT_COLOR);

            String valStr = String.format("%.2f", numSetting.getValue());
            context.drawTextWithShadow(mc.textRenderer, valStr, sx + WIDTH - mc.textRenderer.getWidth(valStr) - 8, sy + 4, SLIDER_FG);

            int sliderX = sx + 10;
            int sliderW = WIDTH - 20;
            double ratio = (numSetting.getValue() - numSetting.getMin()) / (numSetting.getMax() - numSetting.getMin());
            ratio = Math.max(0, Math.min(1, ratio));

            int barY = sy + SLIDER_Y_OFFSET;
            context.fill(sliderX, barY, sliderX + sliderW, barY + SLIDER_HEIGHT, SLIDER_BG);

            int filledW = (int)(sliderW * ratio);
            if (filledW > 0) {
                context.fill(sliderX, barY, sliderX + filledW, barY + SLIDER_HEIGHT, SLIDER_FG);
            }

            int knobX = sliderX + filledW;
            int knobHalf = SLIDER_KNOB_SIZE / 2;
            context.fill(knobX - knobHalf, barY + SLIDER_HEIGHT / 2 - knobHalf,
                    knobX + knobHalf, barY + SLIDER_HEIGHT / 2 + knobHalf, SLIDER_KNOB);

            int innerR = knobHalf - 2;
            if (innerR > 0) {
                context.fill(knobX - innerR, barY + SLIDER_HEIGHT / 2 - innerR,
                        knobX + innerR, barY + SLIDER_HEIGHT / 2 + innerR, SLIDER_FG);
            }
        }
    }

    public boolean isHoveringHeader(double mx, double my) {
        return mx >= x && mx <= x + WIDTH && my >= y && my <= y + HEADER_HEIGHT;
    }

    public Module getHoveredModule(double mx, double my) {
        if (mx < x || mx > x + WIDTH) return null;
        List<Module> modules = com.hack.zero.ZeroHack.getInstance().getModuleManager().getModulesByCategory(category);

        int yOffset = y + HEADER_HEIGHT - scrollOffset;
        for (Module mod : modules) {
            if (my >= yOffset && my <= yOffset + MODULE_HEIGHT && mx >= x && mx <= x + WIDTH) {
                return mod;
            }
            yOffset += MODULE_HEIGHT;
            if (expanded.contains(mod)) {
                yOffset += 2 + mod.getSettings().size() * SETTING_HEIGHT;
            }
        }
        return null;
    }

    public Setting<?> getHoveredSetting(double mx, double my) {
        if (mx < x || mx > x + WIDTH) return null;
        List<Module> modules = com.hack.zero.ZeroHack.getInstance().getModuleManager().getModulesByCategory(category);

        int yOffset = y + HEADER_HEIGHT - scrollOffset;
        for (Module mod : modules) {
            yOffset += MODULE_HEIGHT;
            if (expanded.contains(mod)) {
                yOffset += 2;
                for (Setting<?> setting : mod.getSettings()) {
                    if (my >= yOffset && my <= yOffset + SETTING_HEIGHT) {
                        return setting;
                    }
                    yOffset += SETTING_HEIGHT;
                }
            }
        }
        return null;
    }

    public boolean isHoveringSliderArea(double mx, double my) {
        Setting<?> setting = getHoveredSetting(mx, my);
        if (setting instanceof NumberSetting && !(setting instanceof ColorSetting)) {
            int sliderX = x + 10;
            int sliderW = WIDTH - 20;
            int sliderTop = 0;
            int sliderBottom = 0;

            List<Module> modules = com.hack.zero.ZeroHack.getInstance().getModuleManager().getModulesByCategory(category);
            int yOffset = y + HEADER_HEIGHT - scrollOffset;
            for (Module mod : modules) {
                yOffset += MODULE_HEIGHT;
                if (expanded.contains(mod)) {
                    yOffset += 2;
                    for (Setting<?> s : mod.getSettings()) {
                        if (s == setting) {
                            sliderTop = yOffset + SLIDER_Y_OFFSET - 2;
                            sliderBottom = yOffset + SLIDER_Y_OFFSET + SLIDER_HEIGHT + 4;
                            break;
                        }
                        yOffset += SETTING_HEIGHT;
                    }
                    if (sliderTop != 0) break;
                }
            }

            return mx >= sliderX && mx <= sliderX + sliderW && my >= sliderTop && my <= sliderBottom;
        }
        return false;
    }

    public Setting<?> getDraggedSetting(double mx, double my) {
        Setting<?> setting = getHoveredSetting(mx, my);
        if (setting instanceof NumberSetting && !(setting instanceof ColorSetting)) {
            return setting;
        }
        return null;
    }

    public void toggleExpand(Module mod) {
        if (!mod.isExpandable()) return;
        if (expanded.contains(mod)) expanded.remove(mod);
        else expanded.add(mod);
        scrollOffset = 0;
    }

    public void setPosition(int x, int y) { this.x = x; this.y = y; }
    public void scroll(int amount) {
        int maxScroll = Math.max(0, contentHeight - PANEL_MAX_HEIGHT);
        scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset - amount * 14));
    }
    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return WIDTH; }
    public int getSliderX() { return x + 10; }
    public int getSliderWidth() { return WIDTH - 20; }
}
