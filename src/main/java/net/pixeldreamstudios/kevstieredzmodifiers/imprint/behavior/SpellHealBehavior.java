package net.pixeldreamstudios.kevstieredzmodifiers.imprint.behavior;

import java.util.Map;

import draylar.tiered.api.imprint.behavior.ImprintBehavior;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;

public class SpellHealBehavior extends ImprintBehavior {

    public static final String ID = "kevs-tieredz-modifiers:mending_grace";

    @Override
    public void onSpellHeal(PlayerEntity player, LivingEntity target, float amount, float resolvedValue, Map<String, Float> params) {
        if (target == null || player.getWorld().isClient()) return;
        if (amount <= 0f) return;
        float chance = Math.min(resolvedValue, 1.0f);
        if (player.getRandom().nextFloat() < chance) {
            target.heal(amount);
        }
    }
}
