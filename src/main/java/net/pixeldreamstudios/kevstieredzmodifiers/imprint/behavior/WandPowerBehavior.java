package net.pixeldreamstudios.kevstieredzmodifiers.imprint.behavior;

import java.util.Map;

import draylar.tiered.api.imprint.behavior.ImprintBehavior;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;

public class WandPowerBehavior extends ImprintBehavior {

    public static final String ID = "kevs-tieredz-modifiers:wand_power";

    @Override
    public float magicDamageFraction(PlayerEntity player, DamageSource source, float resolvedValue, Map<String, Float> params) {
        return resolvedValue;
    }

    @Override
    public void onSpellHeal(PlayerEntity player, LivingEntity target, float amount, float resolvedValue, Map<String, Float> params) {
        if (target == null || player.getWorld().isClient()) return;
        float bonus = amount * resolvedValue;
        if (bonus > 0f) target.heal(bonus);
    }
}
