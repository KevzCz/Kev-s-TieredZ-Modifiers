package net.pixeldreamstudios.kevstieredzmodifiers.imprint.resonance;

import java.util.Map;
import java.util.Optional;

import draylar.tiered.api.Cooldowns;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.hit.BlockHitResult;
import net.pixeldreamstudios.kevstieredzmodifiers.entity.AftershockEntity;

public final class SiegeShot {

    private static final String CHARGE_CD = "kevs-tieredz-modifiers:resonance/siege_charge";

    private static final int DEF_RECHARGE_TICKS = 600;
    private static final int DEF_SHOT_REDUCTION_TICKS = 50;
    private static final float DEF_SHOCKWAVE_RADIUS = 4f;
    private static final float DEF_DAMAGE_BONUS = 4f;
    private static final float DEF_AOE_DAMAGE_FRACTION = 1f;

    private static final String K_RECHARGE = "siege_recharge";
    private static final String K_REDUCTION = "siege_reduction";
    private static final String K_RADIUS = "siege_radius";
    private static final String K_DAMAGE = "siege_damage";
    private static final String K_AOE = "siege_aoe";
    private static final String K_TGT_HOSTILES = "siege_tgt_hostiles";
    private static final String K_TGT_NEUTRALS = "siege_tgt_neutrals";
    private static final String K_TGT_PLAYERS = "siege_tgt_players";
    private static final String K_TGT_OWNER_PETS = "siege_tgt_owner_pets";
    private static final String K_TGT_SELF = "siege_tgt_self";

    private SiegeShot() {
    }

    private static int recharge(PlayerEntity p) {
        float v = ResonanceState.get(p, K_RECHARGE);
        return v > 0 ? (int) v : DEF_RECHARGE_TICKS;
    }

    public static void seedCharge(PlayerEntity player, int tier, Map<String, Float> params) {

        ResonanceState.set(player, K_RECHARGE, AbilityParams.i(params, "recharge_ticks", DEF_RECHARGE_TICKS));
        ResonanceState.set(player, K_REDUCTION, AbilityParams.i(params, "shot_reduction_ticks", DEF_SHOT_REDUCTION_TICKS));
        ResonanceState.set(player, K_RADIUS, AbilityParams.f(params, "shockwave_radius", DEF_SHOCKWAVE_RADIUS));
        ResonanceState.set(player, K_DAMAGE, AbilityParams.f(params, "damage_bonus", DEF_DAMAGE_BONUS));
        ResonanceState.set(player, K_AOE, AbilityParams.f(params, "aoe_damage_fraction", DEF_AOE_DAMAGE_FRACTION));

        TargetFilter tf = TargetFilter.fromParams(params, TargetFilter.HOSTILES_ONLY);
        ResonanceState.set(player, K_TGT_HOSTILES, tf.hostiles() ? 1f : 0f);
        ResonanceState.set(player, K_TGT_NEUTRALS, tf.neutrals() ? 1f : 0f);
        ResonanceState.set(player, K_TGT_PLAYERS, tf.players() ? 1f : 0f);
        ResonanceState.set(player, K_TGT_OWNER_PETS, tf.ownerPets() ? 1f : 0f);
        ResonanceState.set(player, K_TGT_SELF, tf.self() ? 1f : 0f);

        if (Cooldowns.remaining(player, CHARGE_CD) <= 0) {
            Cooldowns.start(player, CHARGE_CD, recharge(player));
        }
    }

    public static void onArrowFired(PlayerEntity player) {
        int remaining = Cooldowns.remaining(player, CHARGE_CD);
        if (remaining <= 0) return;
        float reduction = ResonanceState.get(player, K_REDUCTION);
        int red = reduction > 0 ? (int) reduction : DEF_SHOT_REDUCTION_TICKS;
        Cooldowns.start(player, CHARGE_CD, Math.max(0, remaining - red));
    }

    public static boolean consumeCharge(PlayerEntity player) {
        if (!Cooldowns.ready(player, CHARGE_CD)) return false;
        Cooldowns.start(player, CHARGE_CD, recharge(player));
        return true;
    }

    public static float rechargeFill(PlayerEntity player, Map<String, Float> params) {
        int total = AbilityParams.i(params, "recharge_ticks", DEF_RECHARGE_TICKS);
        if (total <= 0) return 1f;
        int remaining = Cooldowns.remaining(player, CHARGE_CD);
        if (remaining <= 0) return 1f;
        return Math.max(0f, Math.min(1f, 1f - (float) remaining / total));
    }

    public static void empower(PlayerEntity shooter, PersistentProjectileEntity arrow) {
        float bonus = ResonanceState.get(shooter, K_DAMAGE);
        if (bonus <= 0) bonus = DEF_DAMAGE_BONUS;
        arrow.setDamage(arrow.getDamage() + bonus);

        Vec3d look = shooter.getRotationVector();
        double kick = 0.8;
        shooter.addVelocity(-look.x * kick, 0.25, -look.z * kick);
        shooter.velocityModified = true;
        shooter.fallDistance = 0f;
        if (shooter.getWorld() instanceof ServerWorld world) {
            world.playSound(null, shooter.getBlockPos(), SoundEvents.ITEM_MACE_SMASH_GROUND_HEAVY,
                    SoundCategory.PLAYERS, 0.7f, 1.2f);
        }
    }

    public static void onBlockImpact(PersistentProjectileEntity arrow, BlockHitResult hit) {
        shockwave(arrow, hit.getPos());
    }

    public static void onEntityImpact(PersistentProjectileEntity arrow, Vec3d pos) {
        shockwave(arrow, pos);
    }

    private static void shockwave(PersistentProjectileEntity arrow, Vec3d p) {
        if (!(arrow.getWorld() instanceof ServerWorld world)) return;
        if (!(arrow.getOwner() instanceof PlayerEntity owner)) return;
        float radius = ResonanceState.get(owner, K_RADIUS);
        if (radius <= 0) radius = DEF_SHOCKWAVE_RADIUS;
        float aoeFrac = ResonanceState.get(owner, K_AOE);
        if (aoeFrac <= 0) aoeFrac = DEF_AOE_DAMAGE_FRACTION;
        float sourceDamage = rangedWeaponDamage(owner) * (critStrikeDamage(owner) / 100f);
        AftershockEntity pulse = new AftershockEntity(AftershockEntity.TYPE, world);
        pulse.refreshPositionAndAngles(p.x, p.y, p.z, 0f, 0f);
        pulse.configure(owner, sourceDamage, 1, 1,
                new float[]{radius}, new float[]{aoeFrac});
        TargetFilter tf = new TargetFilter(
                ResonanceState.get(owner, K_TGT_HOSTILES) != 0f,
                ResonanceState.get(owner, K_TGT_NEUTRALS) != 0f,
                ResonanceState.get(owner, K_TGT_PLAYERS) != 0f,
                ResonanceState.get(owner, K_TGT_OWNER_PETS) != 0f,
                ResonanceState.get(owner, K_TGT_SELF) != 0f);
        pulse.configureBehavior(tf, false, 2f, true);
        world.spawnEntity(pulse);
    }

    public static void trail(PersistentProjectileEntity arrow) {
        if (arrow.getWorld() instanceof ServerWorld world) {
            world.spawnParticles(ParticleTypes.CRIT, arrow.getX(), arrow.getY(), arrow.getZ(), 2, 0.05, 0.05, 0.05, 0.01);
        }
    }

    private static float rangedWeaponDamage(PlayerEntity owner) {
        EntityAttributeInstance inst = attribute(owner, "ranged_weapon", "damage");
        return inst != null ? (float) inst.getValue() : 0f;
    }

    private static float critStrikeDamage(PlayerEntity owner) {
        EntityAttributeInstance inst = attribute(owner, "critical_strike", "damage");
        return inst != null ? (float) inst.getValue() : 100f;
    }

    private static EntityAttributeInstance attribute(PlayerEntity player, String namespace, String path) {
        Optional<RegistryEntry.Reference<EntityAttribute>> attr =
                Registries.ATTRIBUTE.getEntry(Identifier.of(namespace, path));
        return attr.map(player::getAttributeInstance).orElse(null);
    }
}
