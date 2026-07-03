package net.pixeldreamstudios.kevstieredzmodifiers.imprint.behavior;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import draylar.tiered.api.imprint.DataImprint;
import draylar.tiered.api.imprint.ImprintRegistry;
import draylar.tiered.api.imprint.ImprintResolver;
import draylar.tiered.api.imprint.ability.AbilityBinding;
import draylar.tiered.api.imprint.ability.ImprintAbility;
import draylar.tiered.api.imprint.ability.ImprintAbilityRegistry;
import draylar.tiered.api.imprint.behavior.ImprintBehavior;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.pixeldreamstudios.kevstieredzmodifiers.imprint.resonance.ResonanceStatus;
import net.pixeldreamstudios.kevstieredzmodifiers.net.ResonancePlatePayload;
import org.jetbrains.annotations.Nullable;

public class ResonanceBehavior extends ImprintBehavior {

    public static final String ID = "kevs-tieredz-modifiers:resonance";

    public static int tier(PlayerEntity player) {
        return ImprintResolver.stackCount(player, ID);
    }

    @Nullable
    private static DataImprint resonance() {
        return ImprintRegistry.get(ID) instanceof DataImprint di ? di : null;
    }

    // Data-driven: whichever ability's mastered_when imprint is both present on the held item and
    // mastered wins (resonance.json order breaks ties). No hardcoded weapon-family tag list involved.
    @Nullable
    private static AbilityBinding activeBinding(PlayerEntity player) {
        if (tier(player) <= 0) return null;
        DataImprint res = resonance();
        if (res == null) return null;
        return res.activeBinding(player, player.getMainHandStack());
    }

    /** True if the currently active Resonance ability (on the held item) is the given ability id. */
    public static boolean activeAbilityIs(PlayerEntity player, String abilityId) {
        AbilityBinding binding = activeBinding(player);
        return binding != null && abilityId.equals(binding.getAbility());
    }

    /**
     * Merges the shared imprint-level params (e.g. resonance.json's top-level "target_*" shockwave
     * targeting, common to every ability that spawns an AftershockEntity) under an ability's own
     * per-tier params, so any ability can still override a shared key if it ever needs to.
     */
    private static Map<String, Float> mergedParams(AbilityBinding binding, int tier) {
        DataImprint res = resonance();
        Map<String, Float> shared = res == null ? null : res.definition().getParams();
        Map<String, Float> own = binding.getParams(tier);
        if (shared == null || shared.isEmpty()) return own;
        if (own.isEmpty()) return shared;
        Map<String, Float> merged = new HashMap<>(shared);
        merged.putAll(own);
        return merged;
    }

    @Override
    public void onDamageDealt(PlayerEntity player, LivingEntity target, float amount,
            float resolvedValue, Map<String, Float> params) {
        if (player.getWorld().isClient() || target == null) return;
        AbilityBinding binding = activeBinding(player);
        if (binding == null) return;
        ImprintAbility ability = ImprintAbilityRegistry.get(binding.getAbility());
        if (ability == null) return;
        int tier = Math.max(1, (int) resolvedValue);
        ability.onHit(player, target, amount, tier, mergedParams(binding, tier));
    }

    @Override
    public void onSpellHeal(PlayerEntity player, LivingEntity target, float amount,
            float resolvedValue, Map<String, Float> params) {
        if (player.getWorld().isClient() || target == null) return;
        AbilityBinding binding = activeBinding(player);
        if (binding == null) return;
        ImprintAbility ability = ImprintAbilityRegistry.get(binding.getAbility());
        if (ability == null) return;
        int tier = Math.max(1, (int) resolvedValue);
        ability.onHeal(player, target, amount, tier, mergedParams(binding, tier));
    }

    @Override
    public void onEquip(PlayerEntity player, float resolvedValue, Map<String, Float> params) {
        if (player.getWorld().isClient()) return;
        AbilityBinding binding = activeBinding(player);
        if (binding == null) return;
        ImprintAbility ability = ImprintAbilityRegistry.get(binding.getAbility());
        if (ability == null) return;
        int tier = Math.max(1, (int) resolvedValue);
        ability.onEquip(player, tier, mergedParams(binding, tier));
    }

    @Override
    public void tick(PlayerEntity player, float resolvedValue, Map<String, Float> params) {
        if (player.getWorld().isClient()) return;
        AbilityBinding binding = activeBinding(player);
        if (binding == null) return;
        ImprintAbility ability = ImprintAbilityRegistry.get(binding.getAbility());
        if (ability == null) return;
        int tier = Math.max(1, (int) resolvedValue);
        Map<String, Float> p = mergedParams(binding, tier);
        ability.tick(player, tier, p);

        if (ability instanceof ResonanceStatus status && player instanceof ServerPlayerEntity sp) {
            float fill = status.cooldownFill(player, tier, p);
            boolean active = status.active(player, tier, p);
            int snapshot = platePacketSnapshot(binding.getAbility(), fill, active);
            Integer last = LAST_PLATE_SENT.get(player);
            if (last == null || last != snapshot) {
                LAST_PLATE_SENT.put(player, snapshot);
                ServerPlayNetworking.send(sp, new ResonancePlatePayload(binding.getAbility(), fill, active));
            }
        }
    }

    private static final WeakHashMap<PlayerEntity, Integer> LAST_PLATE_SENT = new WeakHashMap<>();

    private static int platePacketSnapshot(String abilityId, float fill, boolean active) {
        int pct = Math.round(Math.max(0f, Math.min(1f, fill)) * 100f);
        int idHash = abilityId == null ? 0 : abilityId.hashCode();
        return (idHash * 31 + pct) * 2 + (active ? 1 : 0);
    }
}
