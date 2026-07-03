package net.pixeldreamstudios.kevstieredzmodifiers.imprint.resonance.overflow;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.pixeldreamstudios.kevstieredzmodifiers.imprint.behavior.ResonanceBehavior;
import net.pixeldreamstudios.kevstieredzmodifiers.imprint.resonance.ResonanceAbilityDefs;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.api.spell.event.SpellEvents;

public final class OverflowHandler {

    static final String K_HALO_CHANCE = "kevs-tieredz-modifiers:overflow_halo_chance";
    static final String K_HALO_COUNT  = "kevs-tieredz-modifiers:overflow_halo_count";
    static final String K_HALO_LIFE   = "kevs-tieredz-modifiers:overflow_halo_life";

    static final float DEF_HALO_CHANCE = 0.5f;
    static final int   DEF_HALO_COUNT  = 4;
    static final int   DEF_HALO_LIFE   = 200;

    private OverflowHandler() {
    }

    public static void register() {
        // PROJECTILE_SHOOT = normal cast-and-fire projectiles. PROJECTILE_FALL = meteor/falling-type
        // projectile spells (a separate Spell Engine event) — both must be covered or Overflow silently
        // misses any projectile spawned via SpellHelper.fallProjectile.
        SpellEvents.PROJECTILE_SHOOT.register(OverflowHandler::onProjectileLaunch);
        SpellEvents.PROJECTILE_FALL.register(OverflowHandler::onProjectileLaunch);

        SpellEvents.SPELL_CAST.register(args -> {
            PlayerEntity player = args.caster();
            if (player == null || !isWandActive(player)) return;
            if (isProjectile(args.spell())) return;
            OverflowSpellPower.addStack(player);
        });
    }

    private static void onProjectileLaunch(SpellEvents.ProjectileLaunchEvent event) {
        if (!(event.caster() instanceof PlayerEntity player)) return;
        if (!isWandActive(player)) return;
        float chance = OverflowSpellPower.stashedF(player, K_HALO_CHANCE, DEF_HALO_CHANCE);
        if (player.getRandom().nextFloat() < chance) {
            int count     = OverflowSpellPower.stashedI(player, K_HALO_COUNT, DEF_HALO_COUNT);
            int lifeTicks = OverflowSpellPower.stashedI(player, K_HALO_LIFE, DEF_HALO_LIFE);
            OverflowHaloController.spawnHalo(event, count, lifeTicks);
        } else {
            OverflowSpellPower.addStack(player);
        }
    }

    private static boolean isProjectile(RegistryEntry<Spell> entry) {
        try {
            Spell spell = entry.value();
            return spell != null && spell.deliver != null && spell.deliver.projectile != null;
        } catch (Throwable t) {
            return false;
        }
    }

    private static boolean isWandActive(PlayerEntity player) {
        return ResonanceBehavior.activeAbilityIs(player, ResonanceAbilityDefs.OVERFLOW);
    }
}
