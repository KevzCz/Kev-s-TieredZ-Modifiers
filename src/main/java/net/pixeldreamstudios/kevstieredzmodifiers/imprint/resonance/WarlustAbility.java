package net.pixeldreamstudios.kevstieredzmodifiers.imprint.resonance;

import java.util.Map;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.pixeldreamstudios.kevstieredzmodifiers.entity.AftershockEntity;

public final class WarlustAbility {

    private static final String RAGE         = "warlust_rage";
    private static final String CONE_CD_KEY  = "warlust_cone_cd";
    private static final String HEALED_FLAG  = "warlust_healed";
    private static final String KB_UNTIL     = "warlust_kb_until";
    private static final String APPLIED_FLAG = "warlust_applied";

    private static final String ACTIVE_UNTIL_HI = "warlust_active_until/hi";
    private static final String ACTIVE_UNTIL_LO = "warlust_active_until/lo";
    private static final String LAST_HIT_HI     = "warlust_last_hit/hi";
    private static final String LAST_HIT_LO     = "warlust_last_hit/lo";

    private static final int   DEF_RAGE_THRESHOLD     = 20;
    private static final int   DEF_WARLUST_TICKS      = 1200;
    private static final float DEF_HEAL_FRACTION      = 0.10f;
    private static final float DEF_DAMAGE_BONUS       = 0.30f;
    private static final float DEF_KB_IGNORE_CHANCE   = 0.20f;
    private static final int   DEF_CONE_CD_TICKS      = 1200;
    private static final int   DEF_CONE_CD_REDUCTION  = 10;
    private static final float DEF_RAGE_DECAY_TICKS   = 160f;

    private static final Identifier DAMAGE_MODIFIER_ID =
            Identifier.of("kevs-tieredz-modifiers", "warlust_damage");
    private static final Identifier KB_MODIFIER_ID =
            Identifier.of("kevs-tieredz-modifiers", "warlust_kb");

    private static final ThreadLocal<Boolean> APPLYING = ThreadLocal.withInitial(() -> false);

    private WarlustAbility() {}

    public static void onMeleeHit(PlayerEntity player, LivingEntity target, float amount, int tier,
            Map<String, Float> params) {
        if (APPLYING.get()) return;

        int rageThreshold   = AbilityParams.i(params, "rage_threshold",    DEF_RAGE_THRESHOLD);
        int warlustTicks    = AbilityParams.i(params, "warlust_ticks",     DEF_WARLUST_TICKS);
        float healFraction  = AbilityParams.f(params, "heal_fraction",     DEF_HEAL_FRACTION);
        float damageBonus   = AbilityParams.f(params, "damage_bonus_pct",  DEF_DAMAGE_BONUS);
        float kbChance      = AbilityParams.f(params, "kb_ignore_chance",  DEF_KB_IGNORE_CHANCE);
        int coneCdBase      = AbilityParams.i(params, "cone_cd_ticks",     DEF_CONE_CD_TICKS);
        int coneCdReduction = AbilityParams.i(params, "cone_cd_reduction", DEF_CONE_CD_REDUCTION);

        long now = player.getWorld().getTime();

        if (isRaging(player, now)) {

            if (player.getRandom().nextFloat() < kbChance) {
                applyKbResistance(player);
                ResonanceState.set(player, KB_UNTIL, (float) (now + 3));
            }

            float cd = ResonanceState.get(player, CONE_CD_KEY);
            cd = Math.max(0f, cd - coneCdReduction);
            ResonanceState.set(player, CONE_CD_KEY, cd);

            if (cd <= 0f && player.getWorld() instanceof ServerWorld world) {
                fireCone(player, world, amount);
                ResonanceState.set(player, CONE_CD_KEY, coneCdBase);
            }

        } else {

            long last = getLastHitTime(player);
            float decayTicks = AbilityParams.f(params, "rage_decay_ticks", DEF_RAGE_DECAY_TICKS);
            if (now - last > (long) decayTicks) {
                ResonanceState.set(player, RAGE, 0f);
            }
            setLastHitTime(player, now);

            float rage = ResonanceState.get(player, RAGE) + 1f;
            ResonanceState.set(player, RAGE, rage);

            if (rage >= rageThreshold) {

                ResonanceState.set(player, RAGE, 0f);
                long activeUntil = now + warlustTicks;
                setActiveUntil(player, activeUntil);
                ResonanceState.set(player, HEALED_FLAG, 0f);
                ResonanceState.set(player, CONE_CD_KEY, 0f);

                applyDamageBonus(player, damageBonus);
                ResonanceState.set(player, APPLIED_FLAG, 1f);

                float maxHp = (float) player.getMaxHealth();
                APPLYING.set(true);
                try {
                    player.heal(maxHp * healFraction);
                } finally {
                    APPLYING.set(false);
                }
                ResonanceState.set(player, HEALED_FLAG, 1f);

                if (player.getWorld() instanceof ServerWorld world) {
                    world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_WITHER_SPAWN,
                            SoundCategory.PLAYERS, 0.5f, 1.8f);
                    world.spawnParticles(ParticleTypes.TOTEM_OF_UNDYING,
                            player.getX(), player.getBodyY(0.5), player.getZ(),
                            30, 0.4, 0.6, 0.4, 0.3);
                }
            }
        }
    }

    public static void tick(PlayerEntity player, int tier, Map<String, Float> params) {
        long now = player.getWorld().getTime();
        if (!isRaging(player, now)) {

            if (ResonanceState.get(player, APPLIED_FLAG) != 0f) {
                removeDamageBonus(player);
                removeKbResistance(player);
                ResonanceState.set(player, APPLIED_FLAG, 0f);
            }
            return;
        }

        long kbUntil = (long) ResonanceState.get(player, KB_UNTIL);
        if (kbUntil > 0 && now >= kbUntil) {
            removeKbResistance(player);
            ResonanceState.set(player, KB_UNTIL, 0f);
        }

    }

    private static void fireCone(PlayerEntity player, ServerWorld world, float sourceDamage) {
        Vec3d look = player.getRotationVec(1f);
        Vec3d flatLook = new Vec3d(look.x, 0, look.z).normalize();

        AftershockEntity cone = new AftershockEntity(AftershockEntity.TYPE, world);

        double sx = player.getX() + flatLook.x * 1.5;
        double sz = player.getZ() + flatLook.z * 1.5;
        cone.refreshPositionAndAngles(sx, player.getY(), sz, 0f, 0f);

        float[] radii = {2f, 3.5f, 5f, 6.5f};
        float[] frac  = {0.6f, 0f, 0f, 0f};
        cone.configure(player, sourceDamage, radii.length, 3, radii, frac);
        cone.asCone(flatLook, 40f);
        world.spawnEntity(cone);

        world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP,
                SoundCategory.PLAYERS, 1.2f, 0.5f);
        world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_GENERIC_EXPLODE.value(),
                SoundCategory.PLAYERS, 0.7f, 1.4f);
    }

    private static void applyDamageBonus(PlayerEntity player, float bonus) {
        EntityAttributeInstance inst = player.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE);
        if (inst == null) return;
        inst.removeModifier(DAMAGE_MODIFIER_ID);
        inst.addTemporaryModifier(new EntityAttributeModifier(
                DAMAGE_MODIFIER_ID, bonus, EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
    }

    private static void removeDamageBonus(PlayerEntity player) {
        EntityAttributeInstance inst = player.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE);
        if (inst != null) inst.removeModifier(DAMAGE_MODIFIER_ID);
    }

    private static void applyKbResistance(PlayerEntity player) {
        EntityAttributeInstance inst = player.getAttributeInstance(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE);
        if (inst == null) return;
        inst.removeModifier(KB_MODIFIER_ID);
        inst.addTemporaryModifier(new EntityAttributeModifier(
                KB_MODIFIER_ID, 1.0, EntityAttributeModifier.Operation.ADD_VALUE));
    }

    private static void removeKbResistance(PlayerEntity player) {
        EntityAttributeInstance inst = player.getAttributeInstance(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE);
        if (inst != null) inst.removeModifier(KB_MODIFIER_ID);
    }

    private static boolean isRaging(PlayerEntity player, long now) {
        return getActiveUntil(player) > now;
    }

    private static void setActiveUntil(PlayerEntity player, long time) {
        ResonanceState.set(player, ACTIVE_UNTIL_HI, (float) (time >> 16));
        ResonanceState.set(player, ACTIVE_UNTIL_LO, (float) (time & 0xFFFFL));
    }

    private static long getActiveUntil(PlayerEntity player) {
        long hi = (long) ResonanceState.get(player, ACTIVE_UNTIL_HI);
        long lo = (long) ResonanceState.get(player, ACTIVE_UNTIL_LO);
        return (hi << 16) | (lo & 0xFFFFL);
    }

    private static void setLastHitTime(PlayerEntity player, long time) {
        ResonanceState.set(player, LAST_HIT_HI, (float) (time >> 16));
        ResonanceState.set(player, LAST_HIT_LO, (float) (time & 0xFFFFL));
    }

    private static long getLastHitTime(PlayerEntity player) {
        long hi = (long) ResonanceState.get(player, LAST_HIT_HI);
        long lo = (long) ResonanceState.get(player, LAST_HIT_LO);
        return (hi << 16) | (lo & 0xFFFFL);
    }

    public static boolean isActive(PlayerEntity player) {
        return isRaging(player, player.getWorld().getTime());
    }

    public static float fill(PlayerEntity player, Map<String, Float> params) {
        long now = player.getWorld().getTime();
        if (isRaging(player, now)) {
            int warlustTicks = AbilityParams.i(params, "warlust_ticks", DEF_WARLUST_TICKS);
            if (warlustTicks <= 0) return 1f;
            long remaining = getActiveUntil(player) - now;
            return Math.max(0f, Math.min(1f, (float) remaining / warlustTicks));
        }
        int threshold = AbilityParams.i(params, "rage_threshold", DEF_RAGE_THRESHOLD);
        if (threshold <= 0) return 0f;
        return Math.max(0f, Math.min(1f, ResonanceState.get(player, RAGE) / threshold));
    }
}
