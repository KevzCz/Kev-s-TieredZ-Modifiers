package net.pixeldreamstudios.kevstieredzmodifiers.imprint.behavior;

import java.util.Map;
import java.util.WeakHashMap;

import draylar.tiered.api.imprint.DataImprint;
import draylar.tiered.api.imprint.ImprintRegistry;
import draylar.tiered.api.imprint.ImprintResolver;
import draylar.tiered.api.imprint.ability.AbilityBinding;
import draylar.tiered.api.imprint.ability.AbilityBridgeBehavior;
import draylar.tiered.api.imprint.ability.ImprintAbility;
import draylar.tiered.api.imprint.ability.ImprintAbilityRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.pixeldreamstudios.kevstieredzmodifiers.imprint.resonance.ResonanceStatus;
import net.pixeldreamstudios.kevstieredzmodifiers.net.ResonancePlatePayload;
import org.jetbrains.annotations.Nullable;


public class ResonanceBehavior extends AbilityBridgeBehavior {

    public static final String ID = "kevs-tieredz-modifiers:resonance";

    public static int tier(PlayerEntity player) {
        return ImprintResolver.stackCount(player, ID);
    }

    @Override
    protected String imprintId() {
        return ID;
    }

    @Override
    @Nullable
    protected AbilityBinding activeBinding(PlayerEntity player) {
        return tier(player) <= 0 ? null : super.activeBinding(player);
    }

    public static boolean activeAbilityIs(PlayerEntity player, String abilityId) {
        if (tier(player) <= 0) return false;
        DataImprint res = ImprintRegistry.get(ID) instanceof DataImprint di ? di : null;
        if (res == null) return false;
        AbilityBinding binding = res.activeBinding(player, player.getMainHandStack());
        return binding != null && abilityId.equals(binding.getAbility());
    }

    @Override
    public void tick(PlayerEntity player, float resolvedValue, Map<String, Float> params) {
        super.tick(player, resolvedValue, params);

        if (!(player instanceof ServerPlayerEntity sp)) return;
        AbilityBinding binding = activeBinding(player);
        if (binding == null) return;
        ImprintAbility ability = ImprintAbilityRegistry.get(binding.getAbility());
        if (!(ability instanceof ResonanceStatus status)) return;

        int tier = tierOf(resolvedValue);
        Map<String, Float> merged = mergedParams(binding, tier);
        float fill = status.cooldownFill(player, tier, merged);
        boolean active = status.active(player, tier, merged);
        int snapshot = platePacketSnapshot(binding.getAbility(), fill, active);
        Integer last = LAST_PLATE_SENT.get(player);
        if (last == null || last != snapshot) {
            LAST_PLATE_SENT.put(player, snapshot);
            ServerPlayNetworking.send(sp, new ResonancePlatePayload(binding.getAbility(), fill, active));
        }
    }

    private static final WeakHashMap<PlayerEntity, Integer> LAST_PLATE_SENT = new WeakHashMap<>();

    private static int platePacketSnapshot(String abilityId, float fill, boolean active) {
        int pct = Math.round(Math.max(0f, Math.min(1f, fill)) * 100f);
        int idHash = abilityId == null ? 0 : abilityId.hashCode();
        return (idHash * 31 + pct) * 2 + (active ? 1 : 0);
    }
}
