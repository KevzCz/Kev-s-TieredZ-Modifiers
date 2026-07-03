package net.pixeldreamstudios.kevstieredzmodifiers.net;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

public final class ModNetworking {

    private ModNetworking() {
    }

    public static void init() {
        PayloadTypeRegistry.playS2C().register(StunPayload.ID, StunPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ResonancePlatePayload.ID, ResonancePlatePayload.CODEC);
        PayloadTypeRegistry.playS2C().register(RequiemStacksPayload.ID, RequiemStacksPayload.CODEC);
    }
}
