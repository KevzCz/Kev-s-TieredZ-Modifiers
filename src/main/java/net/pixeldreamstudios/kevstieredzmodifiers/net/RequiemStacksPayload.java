package net.pixeldreamstudios.kevstieredzmodifiers.net;

import java.util.List;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.pixeldreamstudios.kevstieredzmodifiers.KevsTieredZModifiers;

/** Healer-only sync of current Requiem stack counts, keyed by target entity network id. */
public record RequiemStacksPayload(List<Integer> entityIds, List<Integer> stacks) implements CustomPayload {

    public static final CustomPayload.Id<RequiemStacksPayload> ID =
            new CustomPayload.Id<>(Identifier.of(KevsTieredZModifiers.MOD_ID, "requiem_stacks"));

    public static final PacketCodec<RegistryByteBuf, RequiemStacksPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.INTEGER.collect(PacketCodecs.toList()), RequiemStacksPayload::entityIds,
            PacketCodecs.INTEGER.collect(PacketCodecs.toList()), RequiemStacksPayload::stacks,
            RequiemStacksPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
