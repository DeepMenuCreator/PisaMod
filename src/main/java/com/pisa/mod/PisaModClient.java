package com.pisa.mod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.entity.EmptyEntityRenderer;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class PisaModClient implements ClientModInitializer {
    private static KeyBinding toggleKey;

    @Override
    public void onInitializeClient() {
        // Отображение сущности (при необходимости можно заменить на кастомную модель)
        EntityRendererRegistry.register(PisaMod.PISA_ENTITY, EmptyEntityRenderer::new);

        // Регистрация клавиши K
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.pisamod.toggle_mode",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_K,
            "category.pisamod"
        ));

        // Отслеживание нажатия клавиши в игре
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleKey.wasPressed()) {
                if (client.player != null && client.player.getVehicle() instanceof PisaEntity) {
                    ClientPlayNetworking.send(new PisaPayload());
                }
            }
        });
    }
}
