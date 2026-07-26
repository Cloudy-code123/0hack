package com.hack.zero;

import com.hack.zero.event.EventManager;
import com.hack.zero.gui.ClickGui;
import com.hack.zero.gui.HudOverlay;
import com.hack.zero.module.ModuleManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZeroHack implements ClientModInitializer {
    public static final String MOD_ID = "zerohack";
    public static final String NAME = "0hack";
    public static final Logger LOGGER = LoggerFactory.getLogger(NAME);
    private static ZeroHack INSTANCE;
    private ModuleManager moduleManager;
    private EventManager eventManager;
    private ClickGui clickGui;
    private HudOverlay hudOverlay;
    private KeyBinding openGuiKey;

    @Override
    public void onInitializeClient() {
        INSTANCE = this;
        LOGGER.info("Initializing {}", NAME);

        eventManager = new EventManager();
        moduleManager = new ModuleManager();
        moduleManager.init();
        clickGui = new ClickGui();
        hudOverlay = new HudOverlay();

        openGuiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.zerohack.opengui",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                "category.zerohack"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(this::onTick);
        HudRenderCallback.EVENT.register((drawContext, renderTickCounter) -> {
            hudOverlay.render(drawContext);
        });

        WorldRenderEvents.LAST.register(this::onWorldRenderLast);

        LOGGER.info("{} initialized with {} modules", NAME, moduleManager.getModules().size());
    }

    private void onTick(MinecraftClient client) {
        if (client.player == null || client.world == null) return;

        if (openGuiKey.wasPressed()) {
            if (client.currentScreen instanceof ClickGui) {
                client.setScreen(null);
            } else {
                client.setScreen(clickGui);
            }
        }

        moduleManager.getModules().forEach(module -> {
            if (module.isEnabled()) {
                module.onUpdate();
            }
        });
    }

    private void onWorldRenderLast(WorldRenderContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;
        float tickDelta = context.tickCounter().getDynamicDeltaTicks();
        net.minecraft.client.util.math.MatrixStack matrices = context.matrixStack();
        moduleManager.getModules().forEach(module -> {
            if (module.isEnabled()) {
                try {
                    module.onRender3D(matrices, tickDelta, context);
                } catch (Exception e) {
                    LOGGER.error("[0hack] Error rendering {}: {}", module.getName(), e.getMessage());
                    com.hack.zero.util.RenderUtil.restoreRenderState();
                }
            }
        });
    }

    public static ZeroHack getInstance() {
        return INSTANCE;
    }

    public ModuleManager getModuleManager() {
        return moduleManager;
    }

    public EventManager getEventManager() {
        return eventManager;
    }

    public ClickGui getClickGui() {
        return clickGui;
    }

    public HudOverlay getHudOverlay() {
        return hudOverlay;
    }
}
