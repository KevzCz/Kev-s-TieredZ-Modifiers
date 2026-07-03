package net.pixeldreamstudios.kevstieredzmodifiers.imprint.resonance;

import java.util.Map;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.pixeldreamstudios.kevstieredzmodifiers.entity.AftershockEntity;

public final class AftershockAbility {

    private static final float DEF_SMASH_THRESHOLD = 1.5f;

    private static final float[] DEF_WAVE_FRACTIONS = {0.25f, 0.5f, 1.0f};

    private static final float[] DEF_BASE_RADII = {2f, 4f, 6f};
    private static final int DEF_WAVE_INTERVAL = 20;
    private static final int DEF_STUN_TICKS = 20;

    private AftershockAbility() {
    }

    public static void onMeleeHit(PlayerEntity player, LivingEntity target, float amount, int tier,
            Map<String, Float> params) {
        float threshold = AbilityParams.f(params, "smash_threshold", DEF_SMASH_THRESHOLD);
        if (player.fallDistance <= threshold) return;
        if (!(player.getWorld() instanceof ServerWorld world)) return;

        int waveInterval = AbilityParams.i(params, "wave_interval", DEF_WAVE_INTERVAL);

        float[] fractions = {
                AbilityParams.f(params, "wave1_frac", DEF_WAVE_FRACTIONS[0]),
                AbilityParams.f(params, "wave2_frac", DEF_WAVE_FRACTIONS[1]),
                AbilityParams.f(params, "wave3_frac", DEF_WAVE_FRACTIONS[2]),
        };
        float[] baseRadii = {
                AbilityParams.f(params, "radius1", DEF_BASE_RADII[0]),
                AbilityParams.f(params, "radius2", DEF_BASE_RADII[1]),
                AbilityParams.f(params, "radius3", DEF_BASE_RADII[2]),
        };

        float fallScale = Math.min(1.6f, 1.0f + (player.fallDistance - threshold) * 0.06f);
        float[] radii = new float[baseRadii.length];
        for (int i = 0; i < radii.length; i++) radii[i] = baseRadii[i] * fallScale;

        AftershockEntity quake = new AftershockEntity(AftershockEntity.TYPE, world);

        quake.refreshPositionAndAngles(target.getX(), target.getY(), target.getZ(), 0f, 0f);
        quake.configure(player, amount, 3, waveInterval, radii, fractions);
        quake.configureBehavior(TargetFilter.fromParams(params, TargetFilter.LEGACY), false, 2f, false);
        world.spawnEntity(quake);

        int stun = AbilityParams.i(params, "stun_ticks", DEF_STUN_TICKS);
        if (stun > 0) StunManager.stun(target, stun, player.getUuid(), "aftershock");
    }
}
