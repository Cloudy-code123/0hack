package com.hack.zero.gui;

import com.hack.zero.module.Module;
import com.hack.zero.module.setting.*;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.EnumMap;
import java.util.Map;

public class ClickGui extends Screen {
    private final Map<com.hack.zero.module.Category, CategoryPanel> panels = new EnumMap<>(com.hack.zero.module.Category.class);
    private CategoryPanel dragging = null;
    private double dragOffX, dragOffY;
    private NumberSetting draggingSlider = null;

    public ClickGui() {
        super(Text.of("0hack ClickGUI"));
        int x = 10;
        for (com.hack.zero.module.Category cat : com.hack.zero.module.Category.values()) {
            panels.put(cat, new CategoryPanel(cat, x, 30));
            x += 186;
        }
    }

    @Override
    public void render(net.minecraft.client.gui.DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        for (CategoryPanel panel : panels.values()) {
            panel.render(context, mouseX, mouseY, delta);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            for (CategoryPanel panel : panels.values()) {
                if (panel.isHoveringHeader(mouseX, mouseY)) {
                    dragging = panel;
                    dragOffX = mouseX - panel.getX();
                    dragOffY = mouseY - panel.getY();
                    return true;
                }

                Module clickedModule = panel.getHoveredModule(mouseX, mouseY);
                if (clickedModule != null) {
                    clickedModule.toggle();
                    return true;
                }

                Setting<?> clickedSetting = panel.getHoveredSetting(mouseX, mouseY);
                if (clickedSetting != null) {
                    System.out.println("[0hack] ClickGui: clicked setting=" + clickedSetting.getName() + " type=" + clickedSetting.getClass().getSimpleName() + " value=" + clickedSetting.getValue());
                    if (clickedSetting instanceof NumberSetting numSetting && !(clickedSetting instanceof ColorSetting)) {
                        double sliderX = panel.getSliderX();
                        double sliderW = panel.getSliderWidth();
                        double ratio = Math.max(0, Math.min(1, (mouseX - sliderX) / sliderW));
                        double value = numSetting.getMin() + ratio * (numSetting.getMax() - numSetting.getMin());
                        double stepped = Math.round(value / numSetting.getStep()) * numSetting.getStep();
                        numSetting.setValue(Math.max(numSetting.getMin(), Math.min(numSetting.getMax(), stepped)));
                        draggingSlider = numSetting;
                        return true;
                    }
                    handleSettingClick(clickedSetting);
                    return true;
                }
            }
        } else if (button == 1) {
            for (CategoryPanel panel : panels.values()) {
                Module clickedModule = panel.getHoveredModule(mouseX, mouseY);
                if (clickedModule != null && clickedModule.isExpandable()) {
                    panel.toggleExpand(clickedModule);
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        dragging = null;
        draggingSlider = null;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (dragging != null) {
            dragging.setPosition((int) (mouseX - dragOffX), (int) (mouseY - dragOffY));
            return true;
        }

        if (draggingSlider != null) {
            for (CategoryPanel panel : panels.values()) {
                Setting<?> draggedSetting = panel.getDraggedSetting(mouseX, mouseY);
                if (draggedSetting == draggingSlider) {
                    double sliderX = panel.getSliderX();
                    double sliderW = panel.getSliderWidth();
                    double ratio = Math.max(0, Math.min(1, (mouseX - sliderX) / sliderW));
                    double value = draggingSlider.getMin() + ratio * (draggingSlider.getMax() - draggingSlider.getMin());
                    double stepped = Math.round(value / draggingSlider.getStep()) * draggingSlider.getStep();
                    draggingSlider.setValue(Math.max(draggingSlider.getMin(), Math.min(draggingSlider.getMax(), stepped)));
                    return true;
                }
            }
            return true;
        }

        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        for (CategoryPanel panel : panels.values()) {
            panel.scroll((int) verticalAmount);
        }
        return true;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_RIGHT_SHIFT) {
            close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void handleSettingClick(Setting<?> setting) {
        if (setting instanceof BooleanSetting boolSetting) {
            boolSetting.toggle();
        } else if (setting instanceof ModeSetting modeSetting) {
            modeSetting.cycle();
        } else if (setting instanceof ColorSetting colorSetting) {
            colorSetting.cycle();
        }
    }
}
