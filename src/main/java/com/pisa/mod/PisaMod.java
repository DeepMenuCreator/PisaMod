package com.pisa.mod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class PisaMod implements ModInitializer {
    public static final String MOD_ID = "pisamod";
    
    // Общий кулдаун 10 секунд для всех игроков
    private static long lastSpawnTime = 0;

    public static final EntityType<PisaEntity> PISA_ENTITY = Registry.register(
        Registries.ENTITY_TYPE,
        Identifier.of(MOD_ID, "pisa"),
        FabricEntityTypeBuilder.create(SpawnGroup.MISC, PisaEntity::new)
            .dimensions(EntityDimensions.fixed(1.0f, 1.5f))
            .build()
    );

    @Override
    public void onInitialize() {
        // Регистрация пакета переключения режимов
        PayloadTypeRegistry.playC2S().register(PisaPayload.PACKET_ID, PisaPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(PisaPayload.PACKET_ID, (payload, context) -> {
            context.server().execute(() -> {
                if (context.player().getVehicle() instanceof PisaEntity pisa) {
                    pisa.toggleMode();
                }
            });
        });

        // Регистрация команды /pisa spawn
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("pisa")
                .then(CommandManager.literal("spawn")
                    .executes(ctx -> {
                        long currentTime = System.currentTimeMillis();
                        long cooldownLeft = (10000 - (currentTime - lastSpawnTime)) / 1000;

                        if (cooldownLeft > 0) {
                            ctx.getSource().sendError(Text.literal("§cКулдаун! Подождите " + cooldownLeft + " сек."));
                            return 0;
                        }

                        lastSpawnTime = currentTime;
                        var player = ctx.getSource().getPlayerOrThrow();
                        PisaEntity entity = new PisaEntity(PISA_ENTITY, player.getWorld());
                        entity.refreshPositionAndAngles(player.getX(), player.getY(), player.getZ(), player.getYaw(), player.getPitch());
                        player.getWorld().spawnEntity(entity);

                        ctx.getSource().sendFeedback(() -> Text.literal("§aОбъект Pisa успешно заспавнен!"), false);
                        return 1;
                    })
                )
            );
        });
    }
                    }
