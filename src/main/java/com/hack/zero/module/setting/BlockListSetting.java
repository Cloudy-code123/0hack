package com.hack.zero.module.setting;

import net.minecraft.block.Block;

import java.util.Set;

public class BlockListSetting extends Setting<Set<Block>> {
    public BlockListSetting(String name, Set<Block> defaultValue) {
        super(name, defaultValue);
    }

    public boolean contains(Block block) {
        return getValue().contains(block);
    }
}
