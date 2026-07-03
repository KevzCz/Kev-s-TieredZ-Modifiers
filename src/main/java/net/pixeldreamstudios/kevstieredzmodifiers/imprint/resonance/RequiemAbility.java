package net.pixeldreamstudios.kevstieredzmodifiers.imprint.resonance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.pixeldreamstudios.kevstieredzmodifiers.entity.AftershockEntity;

/**
 * Requiem: healing drives two layers.
 * (A) player-wide ramping heal-output buff (read by the LivingEntity.heal mixin).
 * (B) per-target stacks; each healed entity erupts a healer-attributed aftershock at 10 stacks or 5s.
 */
public final class RequiemAbility {

    private RequiemAbility() {
    }

    private static final int DEF_STACKS = 10;
    private static final int DEF_WINDOW = 100;
    private static final float DEF_COEFF = 0.6f;
    private static final float DEF_CAP = 20f;
    private static final float DEF_BUFF_MIN = 0.05f;
    private static final float DEF_BUFF_MAX = 0.30f;
    private static final int DEF_BUFF_HOLD = 100;
    private static final int MAX_TARGETS = 8;

    private static final class Window {
        int stacks;
        float healing;
        long startTick;
    }

    private static final class PlayerState {
        final Map<UUID, Window> targets = new HashMap<>();
        float buffMultiplier = 1f;
        long buffHoldUntil = 0L;
    }

    private static final Map<UUID, PlayerState> STATES = new WeakHashMap<>();

    private static PlayerState state(PlayerEntity p) {
        return STATES.computeIfAbsent(p.getUuid(), k -> new PlayerState());
    }

    /** Current heal-output multiplier for a player (read by the heal mixin). 1f when inactive. */
    public static float healMultiplier(UUID playerUuid) {
        PlayerState s = STATES.get(playerUuid);
        return s == null ? 1f : s.buffMultiplier;
    }

    public static void onHeal(PlayerEntity healer, LivingEntity healed, float amount, int tier, Map<String, Float> params) {
        if (healer == null || healed == null || amount <= 0f) return;
        if (healer.getWorld().isClient()) return;
        long now = healer.getWorld().getTime();
        PlayerState s = state(healer);

        // Layer A: ramp the buff and refresh its hold window.
        rampBuff(s, params, now, healer.getWorld().getTime());

        // Layer B: per-target stacking.
        int required = intp(params, "stacks_required", DEF_STACKS);
        UUID key = healed.getUuid();
        Window w = s.targets.get(key);
        if (w == null) {
            if (s.targets.size() >= MAX_TARGETS) evictOldest(s);
            w = new Window();
            w.startTick = now;
            s.targets.put(key, w);
        }
        w.stacks++;
        w.healing += amount;
        if (w.stacks >= required) {
            erupt(healer, healed, w, params);
            s.targets.remove(key);
        }
    }

    public static void tick(PlayerEntity player, int tier, Map<String, Float> params) {
        if (player.getWorld().isClient()) return;
        long now = player.getWorld().getTime();
        PlayerState s = STATES.get(player.getUuid());
        if (s == null) return;

        // Buff decay.
        if (now >= s.buffHoldUntil && s.buffMultiplier != 1f) {
            s.buffMultiplier = 1f;
        }

        // Per-target 5s timeout releases.
        int window = intp(params, "window_ticks", DEF_WINDOW);
        Iterator<Map.Entry<UUID, Window>> it = s.targets.entrySet().iterator();
        List<Map.Entry<UUID, Window>> toErupt = new ArrayList<>();
        while (it.hasNext()) {
            Map.Entry<UUID, Window> e = it.next();
            if (now - e.getValue().startTick >= window) {
                toErupt.add(e);
                it.remove();
            }
        }
        for (Map.Entry<UUID, Window> e : toErupt) {
            LivingEntity target = resolve(player, e.getKey());
            if (target != null) erupt(player, target, e.getValue(), params);
        }

        syncStacks(player, s);
    }

    private static void syncStacks(PlayerEntity player, PlayerState s) {
        if (!(player instanceof net.minecraft.server.network.ServerPlayerEntity sp)) return;
        java.util.List<Integer> ids = new ArrayList<>();
        java.util.List<Integer> stacks = new ArrayList<>();
        for (Map.Entry<UUID, Window> e : s.targets.entrySet()) {
            LivingEntity t = resolve(player, e.getKey());
            if (t == null) continue;
            ids.add(t.getId());
            stacks.add(e.getValue().stacks);
        }
        net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(sp,
                new net.pixeldreamstudios.kevstieredzmodifiers.net.RequiemStacksPayload(ids, stacks));
    }

    private static void rampBuff(PlayerState s, Map<String, Float> params, long now, long worldTime) {
        float min = fp(params, "heal_buff_min", DEF_BUFF_MIN);
        float max = fp(params, "heal_buff_max", DEF_BUFF_MAX);
        int hold = intp(params, "heal_buff_hold_ticks", DEF_BUFF_HOLD);
        // Ramp by fraction of the active-target progress toward max (simple: step up each heal).
        float step = (max - min) / 6f;
        float current = s.buffMultiplier <= 1f ? min : (s.buffMultiplier - 1f) + step;
        s.buffMultiplier = 1f + Math.min(max, Math.max(min, current));
        s.buffHoldUntil = now + hold;
    }

    private static void erupt(PlayerEntity healer, LivingEntity target, Window w, Map<String, Float> params) {
        if (!(healer.getWorld() instanceof ServerWorld world)) return;
        float coeff = fp(params, "heal_coeff", DEF_COEFF);
        float cap = fp(params, "damage_cap", DEF_CAP);
        float damage = Math.min(cap, w.healing * coeff);
        if (damage <= 0f) return;

        float radius = fp(params, "radius", fp(params, "radius3", 4.5f));

        AftershockEntity quake = new AftershockEntity(AftershockEntity.TYPE, world);
        quake.refreshPositionAndAngles(target.getX(), target.getY(), target.getZ(), 0f, 0f);
        // Single eruption (1 wave), not 3 expanding rings.
        quake.configure(healer, damage, 1, 1, new float[]{radius}, new float[]{1.0f});
        TargetFilter filter = TargetFilter.fromParams(params, TargetFilter.HOSTILES_ONLY);
        quake.configureBehavior(filter, false, 2f, true, true);
        world.spawnEntity(quake);
        // Do NOT stun the healed target (it's the healer/ally/pet). Enemy stun, if any, is handled
        // inside the aftershock's own damage pass — never applied to the eruption's center entity here.
    }

    private static void evictOldest(PlayerState s) {
        UUID oldest = null;
        long best = Long.MAX_VALUE;
        for (Map.Entry<UUID, Window> e : s.targets.entrySet()) {
            if (e.getValue().startTick < best) { best = e.getValue().startTick; oldest = e.getKey(); }
        }
        if (oldest != null) s.targets.remove(oldest);
    }

    private static LivingEntity resolve(PlayerEntity player, UUID id) {
        if (player.getUuid().equals(id)) return player;
        if (player.getWorld() instanceof ServerWorld sw && sw.getEntity(id) instanceof LivingEntity le) return le;
        return null;
    }

    /** Current stacks on a given target (for the client visual sync). */
    public static int stacksOn(UUID playerUuid, UUID target) {
        PlayerState s = STATES.get(playerUuid);
        if (s == null) return 0;
        Window w = s.targets.get(target);
        return w == null ? 0 : w.stacks;
    }

    public static Map<UUID, Integer> allStacks(UUID playerUuid) {
        PlayerState s = STATES.get(playerUuid);
        if (s == null) return Map.of();
        Map<UUID, Integer> out = new HashMap<>();
        for (Map.Entry<UUID, Window> e : s.targets.entrySet()) out.put(e.getKey(), e.getValue().stacks);
        return out;
    }

    public static float buffFill(PlayerEntity player, Map<String, Float> params) {
        PlayerState s = STATES.get(player.getUuid());
        if (s == null || s.buffMultiplier <= 1f) return 0f;
        float min = fp(params, "heal_buff_min", DEF_BUFF_MIN);
        float max = fp(params, "heal_buff_max", DEF_BUFF_MAX);
        float bonus = s.buffMultiplier - 1f;
        return Math.max(0f, Math.min(1f, (bonus - min) / Math.max(0.0001f, max - min)));
    }

    public static boolean buffActive(PlayerEntity player) {
        PlayerState s = STATES.get(player.getUuid());
        return s != null && s.buffMultiplier > 1f;
    }

    private static float fp(Map<String, Float> p, String k, float d) {
        if (p == null) return d;
        Float v = p.get(k);
        return v == null ? d : v;
    }

    private static int intp(Map<String, Float> p, String k, int d) {
        if (p == null) return d;
        Float v = p.get(k);
        return v == null ? d : Math.round(v);
    }
}
