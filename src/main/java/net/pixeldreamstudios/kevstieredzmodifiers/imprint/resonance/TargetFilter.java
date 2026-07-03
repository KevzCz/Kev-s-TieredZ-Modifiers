package net.pixeldreamstudios.kevstieredzmodifiers.imprint.resonance;

import java.util.Map;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;

/** Data-gated "what can this ability hit". Defaults preserve legacy behavior (hostiles + neutrals). */
public record TargetFilter(boolean hostiles, boolean neutrals, boolean players,
                           boolean ownerPets, boolean self) {

    public static final TargetFilter LEGACY = new TargetFilter(true, true, true, false, false);
    public static final TargetFilter HOSTILES_ONLY = new TargetFilter(true, false, false, false, false);

    public static TargetFilter fromParams(Map<String, Float> params, TargetFilter def) {
        if (params == null) return def;
        return new TargetFilter(
                bool(params, "target_hostiles", def.hostiles),
                bool(params, "target_neutrals", def.neutrals),
                bool(params, "target_players", def.players),
                bool(params, "target_owner_pets", def.ownerPets),
                bool(params, "target_self", def.self));
    }

    private static boolean bool(Map<String, Float> p, String k, boolean d) {
        Float v = p.get(k);
        return v == null ? d : v != 0f;
    }

    public boolean canHit(LivingEntity candidate, PlayerEntity owner) {
        if (owner != null && candidate == owner) return self;
        if (candidate instanceof PlayerEntity) {
            if (owner != null && owner.isTeammate(candidate)) return false;
            return players;
        }
        if (candidate instanceof TameableEntity tame) {
            Entity petOwner = tame.getOwner();
            if (owner != null && petOwner != null && petOwner.getUuid().equals(owner.getUuid())) return ownerPets;
        }
        if (candidate instanceof Monster) return hostiles;
        return neutrals;
    }
}
