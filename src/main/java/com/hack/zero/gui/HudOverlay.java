package com.hack.zero.gui;

import com.hack.zero.ZeroHack;
import com.hack.zero.module.Module;
import com.hack.zero.util.ColorUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class HudOverlay {
    private final MinecraftClient mc = MinecraftClient.getInstance();

    public void render(DrawContext context) {
        if (mc.player == null || mc.world == null) return;
        if (mc.currentScreen instanceof ClickGui) return;

        renderArrayList(context);
        renderWatermark(context);
        renderCoordinates(context);
        renderStats(context);
    }

    private void renderArrayList(DrawContext context) {
        List<Module> enabled = ZeroHack.getInstance().getModuleManager().getModules().stream()
                .filter(Module::isEnabled)
                .sorted(Comparator.comparingInt(m -> -mc.textRenderer.getWidth(m.getName())))
                .collect(Collectors.toList());

        int y = 2;
        int screenWidth = mc.getWindow().getScaledWidth();

        for (int i = 0; i < enabled.size(); i++) {
            Module mod = enabled.get(i);
            String name = mod.getName();
            String info = mod.getInfoString();
            String displayName = info != null ? name + " \u00a77" + info : name;
            int width = mc.textRenderer.getWidth(displayName);
            int x = screenWidth - width - 4;

            int color = ColorUtil.rainbow(i * 30, 0.7f);

            context.fill(x - 2, y, screenWidth, y + 12, 0x80000000);
            context.drawTextWithShadow(mc.textRenderer, displayName, x, y + 2, color);

            y += 12;
        }
    }

    private void renderWatermark(DrawContext context) {
        String text = "0hack v1.0";
        context.fill(2, 2, mc.textRenderer.getWidth(text) + 6, 14, 0x80000000);
        context.drawTextWithShadow(mc.textRenderer, text, 4, 4, 0xFF00CCFF);
    }

    private void renderCoordinates(DrawContext context) {
        if (mc.player == null) return;
        int y = mc.getWindow().getScaledHeight() - 26;
        String coords = String.format("X: %.1f Y: %.1f Z: %.1f",
                mc.player.getX(), mc.player.getY(), mc.player.getZ());
        context.fill(2, y - 2, mc.textRenderer.getWidth(coords) + 6, y + 10, 0x80000000);
        context.drawTextWithShadow(mc.textRenderer, coords, 4, y, 0xFFCCCCCC);
    }

    private void renderStats(DrawContext context) {
        if (mc.player == null) return;
        int fps = mc.getCurrentFps();
        int ping = 0;
        var entry = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
        if (entry != null) {
            ping = entry.getLatency();
        }
        int y = mc.getWindow().getScaledHeight() - 38;
        String stats = String.format("FPS: %d  Ping: %dms", fps, ping);
        context.fill(2, y - 2, mc.textRenderer.getWidth(stats) + 6, y + 10, 0x80000000);
        context.drawTextWithShadow(mc.textRenderer, stats, 4, y, 0xFF00FF00);
    }
}
