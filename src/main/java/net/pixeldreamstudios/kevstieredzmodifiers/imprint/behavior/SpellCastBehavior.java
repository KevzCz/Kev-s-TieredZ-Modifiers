package net.pixeldreamstudios.kevstieredzmodifiers.imprint.behavior;

import java.util.Map;

import draylar.tiered.api.imprint.behavior.ImprintBehavior;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.pixeldreamstudios.kevstieredzmodifiers.imprint.SpellCooldownRefund;

public class SpellCastBehavior extends ImprintBehavior {

    public static final String ID = "kevs-tieredz-modifiers:spell_surge";

    private static final boolean SPELL_ENGINE = FabricLoader.getInstance().isModLoaded("spell_engine");

    @Override
    public void onKill(PlayerEntity player, LivingEntity target, float resolvedValue, Map<String, Float> params) {
        if (player.getWorld().isClient()) return;
        if (!SPELL_ENGINE) return;
        float chance = Math.min(resolvedValue, 1.0f);
        if (chance <= 0f) return;
        if (player.getRandom().nextFloat() >= chance) return;
        float refund = param(params, "refund_fraction", 0.05f);
        SpellCooldownRefund.refundAll(player, refund);
    }
}
