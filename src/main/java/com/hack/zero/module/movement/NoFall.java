package com.hack.zero.module.movement;

import com.hack.zero.module.Category;
import com.hack.zero.module.Module;
import com.hack.zero.module.setting.ModeSetting;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

public class NoFall extends Module {

    private final ModeSetting mode = new ModeSetting("Mode", "Packet", "Packet", "MLG", "Cancel");

    public NoFall() {
        super("NoFall", "Prevents fall damage on the client side", Category.MOVEMENT, 0);
        addSetting(mode);
        setExpandable(false);
    }

    @Override
    public void onUpdate() {
        if (mc.player == null || mc.player.networkHandler == null) return;

        if (mc.player.fallDistance > 0.0f) {
            mc.player.fallDistance = 0.0f;
        }

        mc.player.networkHandler.sendPacket(
                new PlayerMoveC2SPacket.OnGroundOnly(true, false));
    }
}
