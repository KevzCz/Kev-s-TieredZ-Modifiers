package net.pixeldreamstudios.kevstieredzmodifiers.imprint.resonance;

import java.util.Map;

import draylar.tiered.api.imprint.ability.ImprintAbility;
import draylar.tiered.api.imprint.ability.ImprintAbilityRegistry;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.pixeldreamstudios.kevstieredzmodifiers.imprint.resonance.overflow.OverflowSpellPower;

public final class ResonanceAbilityDefs {

    public static final String WARLUST   = "kevs-tieredz-modifiers:warlust";
    public static final String MAUL = "kevs-tieredz-modifiers:maul";
    public static final String AFTERSHOCK = "kevs-tieredz-modifiers:aftershock";
    public static final String TEMPEST = "kevs-tieredz-modifiers:tempest";
    public static final String SIEGE = "kevs-tieredz-modifiers:siege_shot";
    public static final String OVERFLOW = "kevs-tieredz-modifiers:overflow";
    public static final String REQUIEM = "kevs-tieredz-modifiers:requiem";

    private ResonanceAbilityDefs() {
    }

    public static void register() {
        ImprintAbilityRegistry.register(WARLUST, new Warlust());
        ImprintAbilityRegistry.register(MAUL, new Maul());
        ImprintAbilityRegistry.register(AFTERSHOCK, new Aftershock());
        ImprintAbilityRegistry.register(TEMPEST, new Tempest());
        ImprintAbilityRegistry.register(SIEGE, new Siege());
        ImprintAbilityRegistry.register(OVERFLOW, new Overflow());
        ImprintAbilityRegistry.register(REQUIEM, new Requiem());
    }

    static final class Warlust implements ImprintAbility, ResonanceStatus {
        @Override
        public void onHit(PlayerEntity player, LivingEntity target, float amount, int tier, Map<String, Float> params) {
            WarlustAbility.onMeleeHit(player, target, amount, tier, params);
        }
        @Override
        public void tick(PlayerEntity player, int tier, Map<String, Float> params) {
            WarlustAbility.tick(player, tier, params);
        }
        @Override
        public float cooldownFill(PlayerEntity player, int tier, Map<String, Float> params) {
            return WarlustAbility.fill(player, params);
        }
        @Override
        public boolean active(PlayerEntity player, int tier, Map<String, Float> params) {
            return WarlustAbility.isActive(player);
        }
    }

    static final class Maul implements ImprintAbility, ResonanceStatus {
        @Override
        public void onHit(PlayerEntity player, LivingEntity target, float amount, int tier, Map<String, Float> params) {
            MaulAbility.onMeleeHit(player, target, amount, tier, params);
        }
        @Override
        public float cooldownFill(PlayerEntity player, int tier, Map<String, Float> params) {
            return MaulAbility.procFill(player, params);
        }
        @Override
        public boolean active(PlayerEntity player, int tier, Map<String, Float> params) {
            return MaulAbility.procFill(player, params) >= 1f;
        }
    }

    static final class Aftershock implements ImprintAbility, ResonanceStatus {
        @Override
        public void onHit(PlayerEntity player, LivingEntity target, float amount, int tier, Map<String, Float> params) {
            AftershockAbility.onMeleeHit(player, target, amount, tier, params);
        }
        @Override
        public float cooldownFill(PlayerEntity player, int tier, Map<String, Float> params) {
            return 1f;
        }
        @Override
        public boolean active(PlayerEntity player, int tier, Map<String, Float> params) {
            return true;
        }
    }

    static final class Tempest implements ImprintAbility, ResonanceStatus {
        @Override
        public void onHit(PlayerEntity player, LivingEntity target, float amount, int tier, Map<String, Float> params) {
            TempestAbility.onMeleeHit(player, tier, params);
        }
        @Override
        public float cooldownFill(PlayerEntity player, int tier, Map<String, Float> params) {
            return TempestAbility.chargeFill(player);
        }
        @Override
        public boolean active(PlayerEntity player, int tier, Map<String, Float> params) {
            return TempestAbility.isCharged(player);
        }
    }

    static final class Siege implements ImprintAbility, ResonanceStatus {
        @Override
        public void onEquip(PlayerEntity player, int tier, Map<String, Float> params) {
            SiegeShot.seedCharge(player, tier, params);
        }
        @Override
        public float cooldownFill(PlayerEntity player, int tier, Map<String, Float> params) {
            return SiegeShot.rechargeFill(player, params);
        }
        @Override
        public boolean active(PlayerEntity player, int tier, Map<String, Float> params) {
            return SiegeShot.rechargeFill(player, params) >= 1f;
        }
    }

    static final class Overflow implements ImprintAbility, ResonanceStatus {
        @Override
        public void tick(PlayerEntity player, int tier, Map<String, Float> params) {
            OverflowSpellPower.tick(player, tier, params);
        }
        @Override
        public float cooldownFill(PlayerEntity player, int tier, Map<String, Float> params) {
            return OverflowSpellPower.stackFill(player, params);
        }
        @Override
        public boolean active(PlayerEntity player, int tier, Map<String, Float> params) {
            return OverflowSpellPower.stackFill(player, params) > 0f;
        }
    }

    static final class Requiem implements ImprintAbility, ResonanceStatus {
        @Override
        public void onHeal(PlayerEntity healer, LivingEntity healed, float amount, int tier, Map<String, Float> params) {
            RequiemAbility.onHeal(healer, healed, amount, tier, params);
        }
        @Override
        public void tick(PlayerEntity player, int tier, Map<String, Float> params) {
            RequiemAbility.tick(player, tier, params);
        }
        @Override
        public float cooldownFill(PlayerEntity player, int tier, Map<String, Float> params) {
            return RequiemAbility.buffFill(player, params);
        }
        @Override
        public boolean active(PlayerEntity player, int tier, Map<String, Float> params) {
            return RequiemAbility.buffActive(player);
        }
    }
}
