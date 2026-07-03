package net.pixeldreamstudios.kevstieredzmodifiers.imprint;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.api.spell.container.SpellContainer;
import net.spell_engine.api.spell.container.SpellContainerHelper;
import net.spell_engine.api.spell.registry.SpellRegistry;
import net.spell_engine.internals.SpellCooldownManager;
import net.spell_engine.internals.casting.SpellCasterEntity;

public final class SpellCooldownRefund {

    private SpellCooldownRefund() {
    }

    public static void refundAll(PlayerEntity player, float fraction) {
        if (fraction <= 0f) return;
        if (!(player instanceof SpellCasterEntity caster)) return;
        SpellCooldownManager cm = caster.getCooldownManager();
        if (cm == null) return;

        Registry<Spell> registry = SpellRegistry.from(player.getWorld());
        if (registry == null) return;

        Set<String> seen = new HashSet<>();
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            refundFromStack(player.getEquippedStack(slot), registry, cm, fraction, seen);
        }

    }

    private static void refundFromStack(ItemStack stack, Registry<Spell> registry, SpellCooldownManager cm,
            float fraction, Set<String> seen) {
        if (stack == null || stack.isEmpty()) return;
        SpellContainer container = SpellContainerHelper.containerFromItemStack(stack);
        if (container == null || !container.isValid()) return;
        for (String spellId : container.spell_ids()) {
            if (spellId == null || !seen.add(spellId)) continue;
            Identifier id = Identifier.tryParse(spellId);
            if (id == null) continue;
            RegistryEntry<Spell> entry = registry.getEntry(id).orElse(null);
            if (entry == null) continue;
            if (!cm.isCoolingDown(entry)) continue;
            int duration = cm.getCooldownDuration(entry);
            if (duration <= 0) continue;
            float progress = cm.getCooldownProgress(entry, 0f);
            int remaining = Math.round(duration * progress);
            int reduced = Math.max(0, remaining - Math.round(duration * fraction));
            cm.setDurationLeft(entry, reduced);
        }
    }
}
