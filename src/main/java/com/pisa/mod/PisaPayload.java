package com.pisa.mod;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record PisaPayload() implements CustomPayload {
    public static final CustomPayload.Id<PisaPayload> PACKET_ID = new CustomPayload.Id<>(Identifier.of(PisaMod.MOD_ID, "toggle_mode"));
    public static final PacketCodec<RegistryByteBuf, PisaPayload> CODEC = PacketCodec.unit(new PisaPayload());

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }
}
