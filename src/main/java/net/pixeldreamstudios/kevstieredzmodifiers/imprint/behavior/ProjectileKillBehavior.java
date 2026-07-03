package net.pixeldreamstudios.kevstieredzmodifiers.imprint.behavior;

import java.util.Map;

import draylar.tiered.api.imprint.behavior.ImprintBehavior;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;

public class ProjectileKillBehavior extends ImprintBehavior {

    public static final String ID = "kevs-tieredz-modifiers:projectile_execute";

    @Override
    public void onProjectileKill(PlayerEntity player, LivingEntity target, float resolvedValue, Map<String, Float> params) {
        float heal = param(params, "heal_amount", 2.0f) * resolvedValue;
        if (heal > 0f) player.heal(heal);
    }
}
