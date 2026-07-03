package net.pixeldreamstudios.kevstieredzmodifiers.imprint.resonance;

import java.util.Map;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;
import net.minecraft.block.Blocks;
import net.pixeldreamstudios.kevstieredzmodifiers.entity.AftershockEntity;

public final class MaulAbility {

    private static final String COUNT = "maul_count";

    private static final float DEF_BONUS_FRACTION = 0.5f;

    private static final int DEF_BASE_STUN_TICKS = 20;

    private static final int DEF_PROC_EVERY = 4;

    private static final float DEF_FISSURE_CHANCE = 0.10f;

    private MaulAbility() {
    }

    private static final ThreadLocal<Boolean> APPLYING_BONUS = ThreadLocal.withInitial(() -> false);

    public static void onMeleeHit(PlayerEntity player, LivingEntity target, float amount, int tier,
            Map<String, Float> params) {
        if (APPLYING_BONUS.get()) return;
        float bonusFraction = AbilityParams.f(params, "bonus_fraction", DEF_BONUS_FRACTION);
        int baseStun = AbilityParams.i(params, "stun_ticks", DEF_BASE_STUN_TICKS);
        int procEvery = Math.max(1, AbilityParams.i(params, "proc_every", DEF_PROC_EVERY));

        float count = ResonanceState.get(player, COUNT) + 1f;
        ResonanceState.set(player, COUNT, count);

        if (((int) count) % procEvery != 0) return;

        float bonus = amount * bonusFraction;
        if (bonus > 0f && target.isAlive()) {
            APPLYING_BONUS.set(true);
            try {
                target.damage(player.getDamageSources().magic(), bonus);
            } finally {
                APPLYING_BONUS.set(false);
            }
        }

        StunManager.stun(target, baseStun, player.getUuid(), "maul");

        if (player.getWorld() instanceof ServerWorld world) {
            world.spawnParticles(new BlockStateParticleEffect(ParticleTypes.BLOCK, Blocks.STONE.getDefaultState()),
                    target.getX(), target.getBodyY(0.5), target.getZ(), 18, 0.3, 0.3, 0.3, 0.05);
            world.spawnParticles(ParticleTypes.CRIT, target.getX(), target.getBodyY(0.6), target.getZ(),
                    10, 0.2, 0.2, 0.2, 0.2);
            world.playSound(null, target.getBlockPos(), SoundEvents.ITEM_MACE_SMASH_GROUND_HEAVY,
                    SoundCategory.PLAYERS, 0.9f, 0.8f);

            float fissureChance = AbilityParams.f(params, "fissure_chance", DEF_FISSURE_CHANCE);
            if (player.getRandom().nextFloat() < fissureChance) {
                spawnFissure(player, world, amount, params);
            }
        }
    }

    private static final double FISSURE_SPREAD_DEG = 45.0;

    private static void spawnFissure(PlayerEntity player, ServerWorld world, float sourceDamage, Map<String, Float> params) {

        Vec3d look = player.getRotationVec(1f);
        double baseYaw = Math.atan2(look.z, look.x);
        double jitter = (player.getRandom().nextDouble() * 2.0 - 1.0) * Math.toRadians(FISSURE_SPREAD_DEG);
        double ang = baseYaw + jitter;
        Vec3d dir = new Vec3d(Math.cos(ang), 0, Math.sin(ang));

        AftershockEntity fissure = new AftershockEntity(AftershockEntity.TYPE, world);

        double sx = player.getX() + dir.x * 1.5;
        double sz = player.getZ() + dir.z * 1.5;
        fissure.refreshPositionAndAngles(sx, player.getY(), sz, 0f, 0f);

        float[] radii = {2f, 3.5f, 5f, 7f};
        float[] frac  = {0.5f, 0f, 0f, 0f};
        fissure.configure(player, sourceDamage, radii.length, 3, radii, frac);
        fissure.asCone(dir, 12f);
        fissure.configureBehavior(TargetFilter.fromParams(params, TargetFilter.LEGACY), false, 2f, false);
        world.spawnEntity(fissure);

        world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_GENERIC_EXPLODE.value(),
                SoundCategory.PLAYERS, 0.6f, 0.7f);
    }

    public static float procFill(PlayerEntity player, Map<String, Float> params) {
        int procEvery = Math.max(1, AbilityParams.i(params, "proc_every", DEF_PROC_EVERY));
        if (procEvery <= 1) return 1f;
        int count = (int) ResonanceState.get(player, COUNT);
        if (count <= 0) return 0f;
        int within = count % procEvery;
        if (within == 0) return 0f;
        return Math.min(1f, (float) within / (procEvery - 1));
    }
}
