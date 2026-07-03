package net.pixeldreamstudios.kevstieredzmodifiers.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import draylar.tiered.api.imprint.ImprintCooldownDisplay;
import net.pixeldreamstudios.kevstieredzmodifiers.entity.AftershockEntity;
import net.pixeldreamstudios.kevstieredzmodifiers.net.RequiemStacksPayload;
import net.pixeldreamstudios.kevstieredzmodifiers.net.ResonancePlatePayload;
import net.pixeldreamstudios.kevstieredzmodifiers.net.StunPayload;

@Environment(EnvType.CLIENT)
public final class KevsClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(AftershockEntity.TYPE, AftershockEntityRenderer::new);

        ClientPlayNetworking.registerGlobalReceiver(StunPayload.ID, (payload, context) ->
                context.client().execute(() -> ClientStunState.set(payload.until())));

        ClientPlayNetworking.registerGlobalReceiver(ResonancePlatePayload.ID, (payload, context) ->
                context.client().execute(() -> {
                    ImprintCooldownDisplay.set(payload.imprintId(), payload.cooldownFill());
                    ImprintCooldownDisplay.setActive(payload.imprintId(), payload.active());
                }));

        ClientPlayNetworking.registerGlobalReceiver(RequiemStacksPayload.ID, (payload, context) ->
                context.client().execute(() -> ClientRequiemStacks.set(payload.entityIds(), payload.stacks())));

        WorldRenderEvents.AFTER_ENTITIES.register(RequiemStackRenderer::render);
    }
}
