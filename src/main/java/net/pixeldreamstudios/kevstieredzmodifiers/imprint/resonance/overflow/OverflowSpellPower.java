package net.pixeldreamstudios.kevstieredzmodifiers.imprint.resonance.overflow;

import java.util.Map;
import java.util.Optional;

import draylar.tiered.api.Cooldowns;
import draylar.tiered.api.imprint.ImprintResource;
import net.pixeldreamstudios.kevstieredzmodifiers.imprint.resonance.AbilityParams;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

public final class OverflowSpellPower {

    private static final String[] SCHOOLS = {
            "fire", "frost", "arcane", "air", "water", "earth", "healing", "soul", "lightning", "nature"
    };

    static final int   DEF_MAX_STACKS  = 10;
    static final float DEF_PER_STACK   = 0.03f;
    static final int   DEF_DECAY_TICKS = 60;

    static final String K_MAX   = "kevs-tieredz-modifiers:overflow_max";
    static final String K_PER   = "kevs-tieredz-modifiers:overflow_per";
    static final String K_DECAY = "kevs-tieredz-modifiers:overflow_decay_ticks";

    private static final String STACKS_KEY = "kevs-tieredz-modifiers:overflow_stacks";
    private static final String SCHOOL_KEY = "kevs-tieredz-modifiers:overflow_school";
    private static final String DECAY_CD   = "kevs-tieredz-modifiers:resonance/overflow_decay";
    private static final Identifier MODIFIER_ID = Identifier.of("kevs-tieredz-modifiers", "overflow");

    private OverflowSpellPower() {
    }

    private static final String K_STASH_TIER = "kevs-tieredz-modifiers:overflow_stash_tier";

    public static void tick(PlayerEntity player, int tier, Map<String, Float> params) {

        if ((int) get(player, K_STASH_TIER) != tier) {
            set(player, K_STASH_TIER, tier);

            set(player, K_MAX,   AbilityParams.i(params, "max_stacks",  DEF_MAX_STACKS));
            set(player, K_PER,   AbilityParams.f(params, "per_stack",   DEF_PER_STACK));
            set(player, K_DECAY, AbilityParams.i(params, "decay_ticks", DEF_DECAY_TICKS));

            set(player, OverflowHandler.K_HALO_CHANCE, AbilityParams.f(params, "halo_chance", OverflowHandler.DEF_HALO_CHANCE));
            set(player, OverflowHandler.K_HALO_COUNT,  AbilityParams.i(params, "halo_count",  OverflowHandler.DEF_HALO_COUNT));
            set(player, OverflowHandler.K_HALO_LIFE,   AbilityParams.i(params, "halo_life",   OverflowHandler.DEF_HALO_LIFE));

            set(player, OverflowHaloController.K_ORBIT_RADIUS,  AbilityParams.f(params, "orbit_radius",  OverflowHaloController.DEF_ORBIT_RADIUS));
            set(player, OverflowHaloController.K_ORBIT_HEIGHT,  AbilityParams.f(params, "orbit_height",  OverflowHaloController.DEF_ORBIT_HEIGHT));
            set(player, OverflowHaloController.K_ORBIT_SPEED,   AbilityParams.f(params, "orbit_speed",   OverflowHaloController.DEF_ORBIT_SPEED));
            set(player, OverflowHaloController.K_RELEASE_RANGE, AbilityParams.f(params, "release_range", OverflowHaloController.DEF_RELEASE_RANGE));
            set(player, OverflowHaloController.K_LAUNCH_SPEED,  AbilityParams.f(params, "launch_speed",  OverflowHaloController.DEF_LAUNCH_SPEED));
        }
        if (get(player, STACKS_KEY) > 0 && Cooldowns.ready(player, DECAY_CD)) clear(player);
    }

    public static void addStack(PlayerEntity player) {
        if (Cooldowns.ready(player, DECAY_CD)) clear(player);

        int schoolIdx = (int) get(player, SCHOOL_KEY) - 1;
        if (schoolIdx < 0) {
            schoolIdx = highestSchoolByBase(player);
            if (schoolIdx < 0) return;
            set(player, SCHOOL_KEY, schoolIdx + 1);
        }
        int maxStacks = stashedI(player, K_MAX, DEF_MAX_STACKS);
        float stacks = Math.min(maxStacks, get(player, STACKS_KEY) + 1);
        set(player, STACKS_KEY, stacks);
        applyModifier(player, SCHOOLS[schoolIdx], stacks);
        Cooldowns.start(player, DECAY_CD, stashedI(player, K_DECAY, DEF_DECAY_TICKS));
    }

    public static float stackFill(PlayerEntity player, Map<String, Float> params) {
        int maxStacks = AbilityParams.i(params, "max_stacks", DEF_MAX_STACKS);
        if (maxStacks <= 0) return 0f;
        return Math.max(0f, Math.min(1f, get(player, STACKS_KEY) / maxStacks));
    }

    private static void clear(PlayerEntity player) {
        int schoolIdx = (int) get(player, SCHOOL_KEY) - 1;
        if (schoolIdx >= 0) removeModifier(player, SCHOOLS[schoolIdx]);
        set(player, STACKS_KEY, 0f);
        set(player, SCHOOL_KEY, 0f);
    }

    private static int highestSchoolByBase(PlayerEntity player) {
        int best = -1;
        double bestVal = 0.0;
        for (int i = 0; i < SCHOOLS.length; i++) {
            EntityAttributeInstance inst = instance(player, SCHOOLS[i]);
            if (inst == null) continue;
            double base = inst.getBaseValue();
            if (base > bestVal) { bestVal = base; best = i; }
        }
        return best;
    }

    private static void applyModifier(PlayerEntity player, String school, float stacks) {
        EntityAttributeInstance inst = instance(player, school);
        if (inst == null) return;
        inst.removeModifier(MODIFIER_ID);
        inst.addTemporaryModifier(new EntityAttributeModifier(
                MODIFIER_ID, stashedF(player, K_PER, DEF_PER_STACK) * stacks,
                EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
    }

    private static void removeModifier(PlayerEntity player, String school) {
        EntityAttributeInstance inst = instance(player, school);
        if (inst != null) inst.removeModifier(MODIFIER_ID);
    }

    private static EntityAttributeInstance instance(PlayerEntity player, String school) {
        Optional<RegistryEntry.Reference<EntityAttribute>> attr =
                Registries.ATTRIBUTE.getEntry(Identifier.of("spell_power", school));
        return attr.map(player::getAttributeInstance).orElse(null);
    }

    static float stashedF(PlayerEntity p, String key, float def) {
        float v = get(p, key);
        return v > 0f ? v : def;
    }

    static int stashedI(PlayerEntity p, String key, int def) {
        float v = get(p, key);
        return v > 0f ? (int) v : def;
    }

    static float get(PlayerEntity p, String key) { return ImprintResource.get(p, key); }
    static void  set(PlayerEntity p, String key, float v) { ImprintResource.set(p, key, v); }
}
