package com.hack.zero.module.combat;

import com.hack.zero.module.Category;
import com.hack.zero.module.Module;
import com.hack.zero.module.setting.BooleanSetting;
import com.hack.zero.module.setting.NumberSetting;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;

public class AutoTotem extends Module {
    private final BooleanSetting softSwitch = new BooleanSetting("SoftSwitch", false);
    private final NumberSetting healthThreshold = new NumberSetting("HealthThreshold", 8.0, 1.0, 20.0, 0.5);
    private long lastSwapTime = 0;

    public AutoTotem() {
        super("AutoTotem", "Equip totems automatically", Category.COMBAT, 0);
        addSetting(softSwitch);
        addSetting(healthThreshold);
        setExpandable(false);
    }

    @Override
    protected void onEnable() {
        lastSwapTime = 0;
    }

    @Override
    public void onUpdate() {
        if (mc.player == null || mc.world == null) return;

        if (mc.player.getOffHandStack().getItem() == Items.TOTEM_OF_UNDYING) return;

        if (mc.player.getHealth() > healthThreshold.getValue()) return;

        long now = System.currentTimeMillis();
        if (now - lastSwapTime < 500) return;

        int totemSlot = findTotem();
        if (totemSlot == -1) return;

        lastSwapTime = now;
        int currentOffhand = 45;
        mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId, totemSlot, 0, SlotActionType.PICKUP, mc.player);
        mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId, currentOffhand, 0, SlotActionType.PICKUP, mc.player);

        if (!mc.player.currentScreenHandler.getCursorStack().isEmpty()) {
            if (softSwitch.isOn()) {
                int emptySlot = findEmptySlot();
                if (emptySlot != -1) {
                    mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId, emptySlot, 0, SlotActionType.PICKUP, mc.player);
                }
            } else {
                mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId, totemSlot, 0, SlotActionType.PICKUP, mc.player);
            }
        }
    }

    private int findTotem() {
        for (int i = 9; i < 45; i++) {
            if (mc.player.currentScreenHandler.getSlot(i).getStack().getItem() == Items.TOTEM_OF_UNDYING) {
                return i;
            }
        }
        return -1;
    }

    private int findEmptySlot() {
        for (int i = 9; i < 45; i++) {
            if (mc.player.currentScreenHandler.getSlot(i).getStack().isEmpty()) {
                return i;
            }
        }
        return -1;
    }
}
