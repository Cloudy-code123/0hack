package com.hack.zero.module;

import com.hack.zero.module.setting.Setting;
import net.minecraft.client.MinecraftClient;

import java.util.ArrayList;
import java.util.List;

public abstract class Module {
    protected final MinecraftClient mc = MinecraftClient.getInstance();
    private final String name;
    private final String description;
    private final Category category;
    private int keyBind;
    private boolean enabled;
    private final List<Setting<?>> settings = new ArrayList<>();
    private boolean expandable = true;

    public Module(String name, String description, Category category, int keyBind) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.keyBind = keyBind;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Category getCategory() {
        return category;
    }

    public int getKeyBind() {
        return keyBind;
    }

    public void setKeyBind(int keyBind) {
        this.keyBind = keyBind;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        if (this.enabled != enabled) {
            this.enabled = enabled;
            if (enabled) {
                onEnable();
            } else {
                onDisable();
            }
        }
    }

    public void toggle() {
        setEnabled(!enabled);
    }

    public List<Setting<?>> getSettings() {
        return settings;
    }

    protected void addSetting(Setting<?> setting) {
        settings.add(setting);
    }

    public boolean isExpandable() {
        return expandable;
    }

    public void setExpandable(boolean expandable) {
        this.expandable = expandable;
    }

    protected void onEnable() {}

    protected void onDisable() {}

    public void onUpdate() {}

    public void onRender() {}

    public void onRender3D(net.minecraft.client.util.math.MatrixStack matrices, float tickDelta, net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext context) {}

    public String getInfoString() {
        return null;
    }
}
