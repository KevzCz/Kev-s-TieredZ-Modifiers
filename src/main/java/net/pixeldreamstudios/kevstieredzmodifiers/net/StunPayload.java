package net.pixeldreamstudios.kevstieredzmodifiers.net;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.pixeldreamstudios.kevstieredzmodifiers.KevsTieredZModifiers;

public record StunPayload(long until) implements CustomPayload {

    public static final CustomPayload.Id<StunPayload> ID =
            new CustomPayload.Id<>(Identifier.of(KevsTieredZModifiers.MOD_ID, "stun"));

    public static final PacketCodec<RegistryByteBuf, StunPayload> CODEC =
            PacketCodec.tuple(PacketCodecs.VAR_LONG, StunPayload::until, StunPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
