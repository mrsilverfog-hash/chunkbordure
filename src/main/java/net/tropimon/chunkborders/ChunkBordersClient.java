package net.tropimon.chunkborders;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

public class ChunkBordersClient implements ClientModInitializer {

    public static final String MOD_ID = "chunkborders";

    /** Affichage actif ou non. */
    public static boolean enabled = false;

    private static KeyBinding toggleKey;

    @Override
    public void onInitializeClient() {
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.chunkborders.toggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_F9,
                "category.chunkborders"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleKey.wasPressed()) {
                enabled = !enabled;
                actionBar(client, enabled
                        ? Text.literal("Bordures de chunks : ON").formatted(Formatting.GREEN)
                        : Text.literal("Bordures de chunks : OFF").formatted(Formatting.RED));
            }
        });

        WorldRenderEvents.LAST.register(ChunkBordersRenderer::render);
    }

    private static void actionBar(MinecraftClient client, Text text) {
        if (client.player != null) {
            client.player.sendMessage(text, true);
        }
    }
}
