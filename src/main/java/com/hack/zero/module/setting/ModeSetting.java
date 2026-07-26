package com.hack.zero.module.setting;

import java.util.Arrays;
import java.util.List;

public class ModeSetting extends Setting<String> {
    private final List<String> modes;

    public ModeSetting(String name, String defaultValue, String... modes) {
        super(name, defaultValue);
        this.modes = Arrays.asList(modes);
    }

    public List<String> getModes() {
        return modes;
    }

    public int getModeIndex() {
        return modes.indexOf(getValue());
    }

    public void cycle() {
        int next = (getModeIndex() + 1) % modes.size();
        setValue(modes.get(next));
    }

    public void setMode(String mode) {
        if (modes.contains(mode)) {
            setValue(mode);
        }
    }
}
