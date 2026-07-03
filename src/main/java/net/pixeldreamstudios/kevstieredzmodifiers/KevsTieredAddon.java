package net.pixeldreamstudios.kevstieredzmodifiers;

import draylar.tiered.api.TieredAddon;
import draylar.tiered.api.imprint.ImprintParamDisplay;
import draylar.tiered.api.imprint.behavior.ImprintBehaviorRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.pixeldreamstudios.kevstieredzmodifiers.imprint.resonance.overflow.OverflowHandler;
import net.pixeldreamstudios.kevstieredzmodifiers.imprint.behavior.ProjectileKillBehavior;
import net.pixeldreamstudios.kevstieredzmodifiers.imprint.behavior.ResonanceBehavior;
import net.pixeldreamstudios.kevstieredzmodifiers.imprint.behavior.SpellCastBehavior;
import net.pixeldreamstudios.kevstieredzmodifiers.imprint.behavior.SpellHealBehavior;
import net.pixeldreamstudios.kevstieredzmodifiers.imprint.behavior.WandPowerBehavior;
import net.pixeldreamstudios.kevstieredzmodifiers.imprint.resonance.RequiemHealHandler;
import net.pixeldreamstudios.kevstieredzmodifiers.imprint.resonance.ResonanceAbilityDefs;
import net.pixeldreamstudios.kevstieredzmodifiers.registry.KevsRunes;

public final class KevsTieredAddon implements TieredAddon {

    @Override
    public void onTieredInit() {

        ImprintParamDisplay.register("rage_threshold",    "raw");
        ImprintParamDisplay.register("heal_fraction",     "percent");
        ImprintParamDisplay.register("damage_bonus_pct",  "percent");
        ImprintParamDisplay.register("kb_ignore_chance",  "percent");
        ImprintParamDisplay.register("cone_cd_ticks",     "seconds");
        ImprintParamDisplay.register("cone_cd_reduction", "seconds");
        ImprintParamDisplay.register("rage_decay_ticks",  "seconds");
        ImprintParamDisplay.register("warlust_ticks",     "seconds");

        ImprintParamDisplay.register("max_stacks",       "raw");
        ImprintParamDisplay.register("decay_ticks",      "seconds");

        ImprintParamDisplay.register("proc_every",       "raw");
        ImprintParamDisplay.register("bonus_fraction",   "percent");
        ImprintParamDisplay.register("stun_ticks",       "seconds");
        ImprintParamDisplay.register("fissure_chance",   "percent");

        ImprintParamDisplay.register("smash_threshold",  "raw");
        ImprintParamDisplay.register("wave_interval",    "seconds");
        ImprintParamDisplay.register("wave1_frac",       "percent");
        ImprintParamDisplay.register("wave2_frac",       "percent");
        ImprintParamDisplay.register("wave3_frac",       "percent");
        ImprintParamDisplay.register("radius1",          "raw");
        ImprintParamDisplay.register("radius2",          "raw");
        ImprintParamDisplay.register("radius3",          "raw");

        ImprintParamDisplay.register("charge_required",  "raw");
        ImprintParamDisplay.register("homing_radius",    "raw");
        ImprintParamDisplay.register("max_chain",        "raw");
        ImprintParamDisplay.register("chain_damage",     "raw");

        ImprintParamDisplay.register("recharge_ticks",   "seconds");
        ImprintParamDisplay.register("shot_reduction_ticks", "seconds");
        ImprintParamDisplay.register("damage_bonus",     "raw");
        ImprintParamDisplay.register("shockwave_radius", "raw");
        ImprintParamDisplay.register("aoe_damage_fraction", "percent");

        ImprintParamDisplay.register("per_stack",        "percent");
        ImprintParamDisplay.register("halo_chance",      "percent");
        ImprintParamDisplay.register("halo_count",       "raw");
        ImprintParamDisplay.register("halo_life",        "seconds");
        ImprintParamDisplay.register("orbit_radius",     "raw");
        ImprintParamDisplay.register("orbit_height",     "raw");
        ImprintParamDisplay.register("orbit_speed",      "raw");
        ImprintParamDisplay.register("release_range",    "raw");
        ImprintParamDisplay.register("launch_speed",     "raw");

        ImprintBehaviorRegistry.register(ProjectileKillBehavior.ID, new ProjectileKillBehavior());
        ImprintBehaviorRegistry.register(SpellCastBehavior.ID, new SpellCastBehavior());
        ImprintBehaviorRegistry.register(SpellHealBehavior.ID, new SpellHealBehavior());
        ImprintBehaviorRegistry.register(WandPowerBehavior.ID, new WandPowerBehavior());
        ImprintBehaviorRegistry.register(ResonanceBehavior.ID, new ResonanceBehavior());

        ResonanceAbilityDefs.register();

        if (FabricLoader.getInstance().isModLoaded("spell_engine")) {
            OverflowHandler.register();
            RequiemHealHandler.register();
        }

        KevsRunes.register();
    }
}
