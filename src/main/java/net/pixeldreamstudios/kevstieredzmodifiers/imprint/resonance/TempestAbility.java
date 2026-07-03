package net.pixeldreamstudios.kevstieredzmodifiers.imprint.resonance;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public final class TempestAbility {

    private static final String CHARGE_HITS    = "tempest_hits";
    private static final String K_CHARGE_REQ   = "tempest_charge_req";
    private static final String K_HOMING_RADIUS= "tempest_homing_radius";
    private static final String K_MAX_CHAIN    = "tempest_max_chain";
    private static final String K_CHAIN_DAMAGE = "tempest_chain_damage";

    private static final int   DEF_CHARGE_REQUIRED = 3;
    private static final float DEF_HOMING_RADIUS   = 6f;
    private static final int   DEF_MAX_CHAIN        = 3;
    private static final float DEF_CHAIN_DAMAGE     = 6f;

    private TempestAbility() {
    }

    public static void onMeleeHit(PlayerEntity player, int tier, Map<String, Float> params) {
        int req = AbilityParams.i(params, "charge_required", DEF_CHARGE_REQUIRED);
        float cur = ResonanceState.get(player, CHARGE_HITS);
        if (cur < req) ResonanceState.set(player, CHARGE_HITS, Math.min(req, cur + 1f));
        ResonanceState.set(player, K_CHARGE_REQ,    req);
        ResonanceState.set(player, K_HOMING_RADIUS, AbilityParams.f(params, "homing_radius",  DEF_HOMING_RADIUS));
        ResonanceState.set(player, K_MAX_CHAIN,     AbilityParams.i(params, "max_chain",      DEF_MAX_CHAIN));
        ResonanceState.set(player, K_CHAIN_DAMAGE,  AbilityParams.f(params, "chain_damage",   DEF_CHAIN_DAMAGE));
    }

    public static boolean consumeCharge(PlayerEntity player) {
        float required = ResonanceState.get(player, K_CHARGE_REQ);
        if (required <= 0f) required = DEF_CHARGE_REQUIRED;
        if (ResonanceState.get(player, CHARGE_HITS) < required) return false;
        ResonanceState.set(player, CHARGE_HITS, 0f);
        return true;
    }

    public static float chargeFill(PlayerEntity player) {
        float required = ResonanceState.get(player, K_CHARGE_REQ);
        if (required <= 0f) required = DEF_CHARGE_REQUIRED;
        return Math.max(0f, Math.min(1f, ResonanceState.get(player, CHARGE_HITS) / required));
    }

    public static boolean isCharged(PlayerEntity player) {
        float required = ResonanceState.get(player, K_CHARGE_REQ);
        if (required <= 0f) required = DEF_CHARGE_REQUIRED;
        return ResonanceState.get(player, CHARGE_HITS) >= required;
    }

    public static void strikeHit(PlayerEntity owner, PersistentProjectileEntity trident, LivingEntity target) {
        if (!(trident.getWorld() instanceof ServerWorld world)) return;
        if (!(trident instanceof TempestTrident tt)) return;
        Set<UUID> struck = tt.kevs$struck();
        if (!struck.add(target.getUuid())) return;

        float damage = stashedF(owner, K_CHAIN_DAMAGE, DEF_CHAIN_DAMAGE);
        strike(world, owner, target, damage);
    }

    public static void homeToward(PersistentProjectileEntity trident, PlayerEntity owner) {
        if (!(trident.getWorld() instanceof ServerWorld world)) return;
        if (!(trident instanceof TempestTrident tt)) return;
        float radius = stashedF(owner, K_HOMING_RADIUS, DEF_HOMING_RADIUS);
        Set<UUID> struck = tt.kevs$struck();

        Box box = trident.getBoundingBox().expand(radius);
        LivingEntity best = null;
        double bestDist = Double.MAX_VALUE;
        for (Entity e : world.getOtherEntities(trident, box,
                x -> x instanceof LivingEntity && x != owner && !struck.contains(x.getUuid()))) {
            double d = e.squaredDistanceTo(trident);
            if (d < bestDist) { bestDist = d; best = (LivingEntity) e; }
        }
        if (best == null) return;

        Vec3d vel = trident.getVelocity();
        double speed = vel.length();
        Vec3d toTarget = best.getEyePos().subtract(trident.getPos()).normalize();

        Vec3d steered = vel.normalize().multiply(0.75).add(toTarget.multiply(0.25)).normalize().multiply(speed);
        trident.setVelocity(steered);
        trident.velocityModified = true;
    }

    public static void dischargeRing(PlayerEntity owner) {
        if (!(owner.getWorld() instanceof ServerWorld world)) return;
        int maxTargets = stashedI(owner, K_MAX_CHAIN, DEF_MAX_CHAIN);
        float damage = stashedF(owner, K_CHAIN_DAMAGE, DEF_CHAIN_DAMAGE);
        float radius = stashedF(owner, K_HOMING_RADIUS, DEF_HOMING_RADIUS);

        Box box = owner.getBoundingBox().expand(radius);
        List<LivingEntity> targets = new ArrayList<>();
        for (Entity e : world.getOtherEntities(owner, box, x -> x instanceof LivingEntity && x != owner)) {
            targets.add((LivingEntity) e);
            if (targets.size() >= maxTargets) break;
        }
        for (LivingEntity t : targets) strike(world, owner, t, damage);

        for (int i = 0; i < 24; i++) {
            double ang = Math.PI * 2 * i / 24;
            double px = owner.getX() + Math.cos(ang) * radius * 0.6;
            double pz = owner.getZ() + Math.sin(ang) * radius * 0.6;
            world.spawnParticles(ParticleTypes.ELECTRIC_SPARK, px, owner.getBodyY(0.2), pz, 2, 0.05, 0.1, 0.05, 0.02);
        }
        world.playSound(null, owner.getBlockPos(), SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER,
                SoundCategory.PLAYERS, 0.5f, 1.6f);
    }

    private static void strike(ServerWorld world, PlayerEntity owner, LivingEntity target, float damage) {
        LightningEntity bolt = EntityType.LIGHTNING_BOLT.create(world);
        if (bolt != null) {
            bolt.refreshPositionAfterTeleport(target.getX(), target.getY(), target.getZ());
            bolt.setCosmetic(true);
            if (owner instanceof ServerPlayerEntity sp) bolt.setChanneler(sp);
            world.spawnEntity(bolt);
        }
        target.damage(owner.getDamageSources().playerAttack(owner), damage);
        world.spawnParticles(ParticleTypes.ELECTRIC_SPARK, target.getX(), target.getBodyY(0.6), target.getZ(),
                12, 0.2, 0.4, 0.2, 0.1);
    }

    private static float stashedF(PlayerEntity p, String key, float def) {
        float v = ResonanceState.get(p, key);
        return v > 0f ? v : def;
    }

    private static int stashedI(PlayerEntity p, String key, int def) {
        float v = ResonanceState.get(p, key);
        return v > 0f ? (int) v : def;
    }
}
