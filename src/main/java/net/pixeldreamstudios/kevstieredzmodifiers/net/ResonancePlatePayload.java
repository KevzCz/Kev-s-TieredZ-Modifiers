package net.pixeldreamstudios.kevstieredzmodifiers.net;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.pixeldreamstudios.kevstieredzmodifiers.KevsTieredZModifiers;

public record ResonancePlatePayload(String imprintId, float cooldownFill, boolean active) implements CustomPayload {

    public static final CustomPayload.Id<ResonancePlatePayload> ID =
            new CustomPayload.Id<>(Identifier.of(KevsTieredZModifiers.MOD_ID, "resonance_plate"));

    public static final PacketCodec<RegistryByteBuf, ResonancePlatePayload> CODEC = PacketCodec.tuple(
            PacketCodecs.STRING, ResonancePlatePayload::imprintId,
            PacketCodecs.FLOAT,  ResonancePlatePayload::cooldownFill,
            PacketCodecs.BOOL,   ResonancePlatePayload::active,
            ResonancePlatePayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
