package com.hack.zero.gui;

import com.hack.zero.module.setting.BlockListSetting;
import net.minecraft.block.Block;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class BlockListScreen extends Screen {
    private static final int ROW_HEIGHT = 12;
    private static final int LIST_X = 120;
    private static final int LIST_WIDTH = 240;

    private final BlockListSetting setting;
    private final Runnable resetAction;
    private final List<Block> blocks;
    private TextFieldWidget search;
    private int scrollOffset;
    private String query = "";

    public BlockListScreen(BlockListSetting setting, Runnable resetAction) {
        super(Text.literal("Block List"));
        this.setting = setting;
        this.resetAction = resetAction;
        this.blocks = new ArrayList<>();
        Registries.BLOCK.forEach(blocks::add);
        blocks.sort(Comparator.comparing(block -> block.getName().getString()));
    }

    @Override
    protected void init() {
        search = new TextFieldWidget(this.textRenderer, this.width / 2 - LIST_X, 20, LIST_WIDTH, 20, Text.literal("Search blocks"));
        search.setChangedListener(text -> {
            query = text.toLowerCase();
            scrollOffset = 0;
        });
        this.addDrawableChild(search);

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Reset"), button -> resetAction.run())
                .dimensions(this.width / 2 - LIST_X, this.height - 30, LIST_WIDTH / 2 - 2, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Done"), button -> this.close())
                .dimensions(this.width / 2 + 2, this.height - 30, LIST_WIDTH / 2 - 2, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        int x = this.width / 2 - LIST_X;
        int y = 50;
        int row = 0;
        for (Block block : blocks) {
            String name = block.getName().getString();
            if (!name.toLowerCase().contains(query)) continue;
            if (row++ < scrollOffset) continue;
            if (y > this.height - 40) break;

            boolean selected = setting.getValue().contains(block);
            context.drawTextWithShadow(this.textRenderer, name, x + 6, y, selected ? 0xFFFFFFFF : 0xFF909090);
            String marker = selected ? "[x]" : "[ ]";
            context.drawTextWithShadow(this.textRenderer, marker, x + LIST_WIDTH - 30, y, selected ? 0xFF55FF55 : 0xFFAAAAAA);
            y += ROW_HEIGHT;
        }

        String hint = "Scroll to browse, click to toggle";
        context.drawCenteredTextWithShadow(this.textRenderer, hint, this.width / 2, this.height - 10, 0xFF606060);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int x = this.width / 2 - LIST_X;
            int y = 50;
            int row = 0;
            for (Block block : blocks) {
                String name = block.getName().getString();
                if (!name.toLowerCase().contains(query)) continue;
                if (row++ < scrollOffset) continue;
                if (y > this.height - 40) break;

                if (mouseX >= x && mouseX <= x + LIST_WIDTH && mouseY >= y && mouseY <= y + ROW_HEIGHT) {
                    if (setting.getValue().contains(block)) {
                        setting.getValue().remove(block);
                    } else {
                        setting.getValue().add(block);
                    }
                    return true;
                }
                y += ROW_HEIGHT;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        scrollOffset += (int) -verticalAmount;
        if (scrollOffset < 0) scrollOffset = 0;
        return true;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
