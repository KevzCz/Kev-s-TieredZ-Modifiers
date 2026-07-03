package net.pixeldreamstudios.kevstieredzmodifiers.imprint.resonance;

import draylar.tiered.api.imprint.ImprintResolver;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.spell_engine.api.spell.event.SpellEvents;

/** Feeds Spell Engine HEAL events into tiered's (previously undispatched) onSpellHeal path. */
public final class RequiemHealHandler {

    private RequiemHealHandler() {
    }

    public static void register() {
        SpellEvents.HEAL.register(args -> {
            if (!(args.caster() instanceof PlayerEntity player)) return;
            LivingEntity target = args.target();
            if (target == null || player.getWorld().isClient()) return;
            ImprintResolver.dispatchSpellHeal(player, target, args.amount());
        });
    }
}
