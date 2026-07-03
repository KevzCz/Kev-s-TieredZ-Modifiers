package net.pixeldreamstudios.kevstieredzmodifiers.imprint.resonance;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.pixeldreamstudios.kevstieredzmodifiers.net.StunPayload;

public final class StunManager {

    private StunManager() {
    }

    // Per-target pose captured once while stunned; independent of source.
    private record Pose(float yaw, float bodyYaw, float headYaw, float pitch) {
    }

    private record Entry(Pose pose, Map<String, Long> untilBySource) {
    }

    // key per source: sourceUuid + ":" + abilityId
    private static final Map<LivingEntity, Entry> STUNNED = new WeakHashMap<>();

    /**
     * Stun {@code target}, keyed by {@code (source, abilityId)}. Re-applying from the same key
     * REFRESHES that key's duration (does not stack). Different sources coexist; the target stays
     * frozen while any key is active.
     */
    public static void stun(LivingEntity target, int durationTicks, UUID source, String abilityId) {
        if (target == null || durationTicks <= 0) return;
        long until = target.getWorld().getTime() + durationTicks;
        String key = (source == null ? "-" : source.toString()) + ":" + abilityId;

        Entry entry = STUNNED.get(target);
        if (entry == null) {
            Pose pose = new Pose(target.getYaw(), target.bodyYaw, target.headYaw, target.getPitch());
            entry = new Entry(pose, new HashMap<>());
            STUNNED.put(target, entry);
        }
        entry.untilBySource().put(key, until); // overwrite = refresh

        target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, durationTicks, 250, false, false, true));

        if (target instanceof ServerPlayerEntity sp) {
            ServerPlayNetworking.send(sp, new StunPayload(stunnedUntil(target)));
        }
    }

    public static void tickFreeze(LivingEntity entity) {
        Entry entry = STUNNED.get(entity);
        if (entry == null) return;
        if (pruneAndMaxUntil(entity, entry) == 0L) {
            STUNNED.remove(entity);
            return;
        }
        if (entity instanceof MobEntity) {
            Pose p = entry.pose();
            entity.setYaw(p.yaw());
            entity.setPitch(p.pitch());
            entity.bodyYaw = p.bodyYaw();
            entity.headYaw = p.headYaw();
            entity.setVelocity(0, entity.getVelocity().y, 0);
        }
    }

    public static long stunnedUntil(LivingEntity entity) {
        Entry entry = STUNNED.get(entity);
        if (entry == null) return 0L;
        long max = pruneAndMaxUntil(entity, entry);
        if (max == 0L) STUNNED.remove(entity);
        return max;
    }

    private static long pruneAndMaxUntil(LivingEntity entity, Entry entry) {
        long now = entity.getWorld().getTime();
        long max = 0L;
        Iterator<Map.Entry<String, Long>> it = entry.untilBySource().entrySet().iterator();
        while (it.hasNext()) {
            long until = it.next().getValue();
            if (until <= now) it.remove();
            else if (until > max) max = until;
        }
        return max;
    }
}
