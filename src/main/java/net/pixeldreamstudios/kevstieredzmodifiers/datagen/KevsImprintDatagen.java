package net.pixeldreamstudios.kevstieredzmodifiers.datagen;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import draylar.tiered.api.imprint.EligibilityPredicate;
import draylar.tiered.api.imprint.ImprintDefinition;
import draylar.tiered.api.imprint.ability.AbilityBinding;
import draylar.tiered.api.imprint.ability.AbilityBinding.Plate;
import draylar.tiered.datagen.DefaultSkippingSerializer;

public final class KevsImprintDatagen {

    private static final String NS = "kevs-tieredz-modifiers";

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .registerTypeAdapter(ImprintDefinition.class, new DefaultSkippingSerializer<>(ImprintDefinition.class))
            .registerTypeAdapter(ImprintDefinition.TypeComponent.class, new DefaultSkippingSerializer<>(ImprintDefinition.TypeComponent.class))
            .create();

    private KevsImprintDatagen() {
    }

    public static void main(String[] args) throws IOException {
        Path dir = Path.of("generated", "data", NS, "imprint");
        Files.createDirectories(dir);

        write(dir, "resonance", base("resonance", "#E8C36B", "weapon")
                .combine("ADDITIVE").multiplicity("SLOT").reapply("HIGHEST")
                .abilities(List.of(

                        resAbilityTiered("tiered:honed", "warlust", "#B03030")
                                .tierParam(1, "rage_threshold", 20f).tierParam(1, "heal_fraction", 0.10f).tierParam(1, "damage_bonus_pct", 0.15f).tierParam(1, "kb_ignore_chance", 0.20f).tierParam(1, "cone_cd_ticks", 1200f).tierParam(1, "cone_cd_reduction", 10f).tierParam(1, "rage_decay_ticks", 160f).tierParam(1, "warlust_ticks", 1200f)
                                .tierParam(2, "rage_threshold", 15f).tierParam(2, "heal_fraction", 0.15f).tierParam(2, "damage_bonus_pct", 0.20f).tierParam(2, "kb_ignore_chance", 0.30f).tierParam(2, "cone_cd_ticks", 1000f).tierParam(2, "cone_cd_reduction", 20f).tierParam(2, "rage_decay_ticks", 160f).tierParam(2, "warlust_ticks", 1200f)
                                .tierParam(3, "rage_threshold", 10f).tierParam(3, "heal_fraction", 0.25f).tierParam(3, "damage_bonus_pct", 0.30f).tierParam(3, "kb_ignore_chance", 0.50f).tierParam(3, "cone_cd_ticks", 800f).tierParam(3, "cone_cd_reduction", 30f).tierParam(3, "rage_decay_ticks", 160f).tierParam(3, "warlust_ticks", 1200f)
                                .tierParam(4, "rage_threshold", 8f) .tierParam(4, "heal_fraction", 0.35f).tierParam(4, "damage_bonus_pct", 0.40f).tierParam(4, "kb_ignore_chance", 0.60f).tierParam(4, "cone_cd_ticks", 500f).tierParam(4, "cone_cd_reduction", 40f).tierParam(4, "rage_decay_ticks", 160f).tierParam(4, "warlust_ticks", 1200f),

                        resAbilityTiered("tiered:rending", "maul", "#C8772E")
                                .tierParam(1, "proc_every", 4f).tierParam(1, "bonus_fraction", 0.35f).tierParam(1, "stun_ticks", 16f).tierParam(1, "fissure_chance", 0.20f)
                                .tierParam(2, "proc_every", 4f).tierParam(2, "bonus_fraction", 0.45f).tierParam(2, "stun_ticks", 22f).tierParam(2, "fissure_chance", 0.30f)
                                .tierParam(3, "proc_every", 4f).tierParam(3, "bonus_fraction", 0.55f).tierParam(3, "stun_ticks", 30f).tierParam(3, "fissure_chance", 0.40f)
                                .tierParam(4, "proc_every", 3f).tierParam(4, "bonus_fraction", 0.65f).tierParam(4, "stun_ticks", 40f).tierParam(4, "fissure_chance", 0.50f),

                        resAbilityTiered("tiered:crushing", "aftershock", "#A07040")
                                .tierParam(1, "smash_threshold", 1.5f).tierParam(1, "wave_interval", 20f).tierParam(1, "wave1_frac", 0.15f).tierParam(1, "wave2_frac", 0.30f).tierParam(1, "wave3_frac", 0.60f).tierParam(1, "radius1", 1.5f).tierParam(1, "radius2", 3.0f).tierParam(1, "radius3", 4.5f).tierParam(1, "stun_ticks", 15f)
                                .tierParam(2, "smash_threshold", 1.5f).tierParam(2, "wave_interval", 20f).tierParam(2, "wave1_frac", 0.20f).tierParam(2, "wave2_frac", 0.40f).tierParam(2, "wave3_frac", 0.80f).tierParam(2, "radius1", 2.0f).tierParam(2, "radius2", 4.0f).tierParam(2, "radius3", 6.0f).tierParam(2, "stun_ticks", 20f)
                                .tierParam(3, "smash_threshold", 1.5f).tierParam(3, "wave_interval", 18f).tierParam(3, "wave1_frac", 0.25f).tierParam(3, "wave2_frac", 0.50f).tierParam(3, "wave3_frac", 1.00f).tierParam(3, "radius1", 2.5f).tierParam(3, "radius2", 5.0f).tierParam(3, "radius3", 7.5f).tierParam(3, "stun_ticks", 25f)
                                .tierParam(4, "smash_threshold", 1.0f).tierParam(4, "wave_interval", 16f).tierParam(4, "wave1_frac", 0.30f).tierParam(4, "wave2_frac", 0.60f).tierParam(4, "wave3_frac", 1.20f).tierParam(4, "radius1", 3.0f).tierParam(4, "radius2", 6.0f).tierParam(4, "radius3", 9.0f).tierParam(4, "stun_ticks", 30f),

                        resAbilityTiered("tiered:piercing", "tempest", "#5B9BD5")
                                .tierParam(1, "charge_required", 4f).tierParam(1, "homing_radius", 5.0f).tierParam(1, "max_chain", 2f).tierParam(1, "chain_damage", 4.0f)
                                .tierParam(2, "charge_required", 3f).tierParam(2, "homing_radius", 5.5f).tierParam(2, "max_chain", 3f).tierParam(2, "chain_damage", 5.0f)
                                .tierParam(3, "charge_required", 3f).tierParam(3, "homing_radius", 6.0f).tierParam(3, "max_chain", 3f).tierParam(3, "chain_damage", 6.0f)
                                .tierParam(4, "charge_required", 2f).tierParam(4, "homing_radius", 7.0f).tierParam(4, "max_chain", 4f).tierParam(4, "chain_damage", 8.0f),

                        resAbilityTiered("kevs-tieredz-modifiers:barbed", "siege_shot", "#7D9B5A")
                                .tierParam(1, "recharge_ticks", 700f).tierParam(1, "shot_reduction_ticks", 40f).tierParam(1, "damage_bonus", 2.5f).tierParam(1, "shockwave_radius", 3.0f).tierParam(1, "aoe_damage_fraction", 0.75f)
                                .tierParam(2, "recharge_ticks", 600f).tierParam(2, "shot_reduction_ticks", 50f).tierParam(2, "damage_bonus", 3.5f).tierParam(2, "shockwave_radius", 3.5f).tierParam(2, "aoe_damage_fraction", 0.85f)
                                .tierParam(3, "recharge_ticks", 500f).tierParam(3, "shot_reduction_ticks", 55f).tierParam(3, "damage_bonus", 5.0f).tierParam(3, "shockwave_radius", 4.5f).tierParam(3, "aoe_damage_fraction", 1.00f)
                                .tierParam(4, "recharge_ticks", 400f).tierParam(4, "shot_reduction_ticks", 60f).tierParam(4, "damage_bonus", 7.0f).tierParam(4, "shockwave_radius", 6.0f).tierParam(4, "aoe_damage_fraction", 1.00f),

                        resAbilityTiered("kevs-tieredz-modifiers:attuned", "overflow", "#8E44AD")
                                .tierParam(1, "max_stacks", 6f).tierParam(1, "per_stack", 0.02f).tierParam(1, "decay_ticks", 50f).tierParam(1, "halo_chance", 0.35f).tierParam(1, "halo_count", 3f).tierParam(1, "halo_life", 160f).tierParam(1, "orbit_radius", 1.4f).tierParam(1, "orbit_height", 2.0f).tierParam(1, "orbit_speed", 0.07f).tierParam(1, "release_range", 8.0f).tierParam(1, "launch_speed", 1.2f)
                                .tierParam(2, "max_stacks", 8f).tierParam(2, "per_stack", 0.03f).tierParam(2, "decay_ticks", 60f).tierParam(2, "halo_chance", 0.40f).tierParam(2, "halo_count", 3f).tierParam(2, "halo_life", 180f).tierParam(2, "orbit_radius", 1.5f).tierParam(2, "orbit_height", 2.1f).tierParam(2, "orbit_speed", 0.08f).tierParam(2, "release_range", 9.0f).tierParam(2, "launch_speed", 1.3f)
                                .tierParam(3, "max_stacks", 12f).tierParam(3, "per_stack", 0.04f).tierParam(3, "decay_ticks", 70f).tierParam(3, "halo_chance", 0.50f).tierParam(3, "halo_count", 4f).tierParam(3, "halo_life", 200f).tierParam(3, "orbit_radius", 1.6f).tierParam(3, "orbit_height", 2.2f).tierParam(3, "orbit_speed", 0.08f).tierParam(3, "release_range", 10.0f).tierParam(3, "launch_speed", 1.4f)
                                .tierParam(4, "max_stacks", 16f).tierParam(4, "per_stack", 0.05f).tierParam(4, "decay_ticks", 80f).tierParam(4, "halo_chance", 0.60f).tierParam(4, "halo_count", 5f).tierParam(4, "halo_life", 240f).tierParam(4, "orbit_radius", 1.8f).tierParam(4, "orbit_height", 2.4f).tierParam(4, "orbit_speed", 0.09f).tierParam(4, "release_range", 12.0f).tierParam(4, "launch_speed", 1.6f),

                        resAbilityTiered("kevs-tieredz-modifiers:sanctified", "requiem", "#F1C40F")
                                .tierParam(1, "stacks_required", 10f).tierParam(1, "window_ticks", 100f).tierParam(1, "heal_coeff", 0.5f).tierParam(1, "damage_cap", 12f).tierParam(1, "radius3", 4.5f).tierParam(1, "heal_buff_min", 0.05f).tierParam(1, "heal_buff_max", 0.20f).tierParam(1, "heal_buff_hold_ticks", 100f)
                                .tierParam(2, "stacks_required", 10f).tierParam(2, "window_ticks", 100f).tierParam(2, "heal_coeff", 0.6f).tierParam(2, "damage_cap", 16f).tierParam(2, "radius3", 5.0f).tierParam(2, "heal_buff_min", 0.05f).tierParam(2, "heal_buff_max", 0.25f).tierParam(2, "heal_buff_hold_ticks", 100f)
                                .tierParam(3, "stacks_required", 10f).tierParam(3, "window_ticks", 90f).tierParam(3, "heal_coeff", 0.7f).tierParam(3, "damage_cap", 22f).tierParam(3, "radius3", 5.5f).tierParam(3, "heal_buff_min", 0.05f).tierParam(3, "heal_buff_max", 0.28f).tierParam(3, "heal_buff_hold_ticks", 100f)
                                .tierParam(4, "stacks_required", 10f).tierParam(4, "window_ticks", 80f).tierParam(4, "heal_coeff", 0.75f).tierParam(4, "damage_cap", 30f).tierParam(4, "radius3", 6.0f).tierParam(4, "heal_buff_min", 0.05f).tierParam(4, "heal_buff_max", 0.30f).tierParam(4, "heal_buff_hold_ticks", 100f)))
                .types(List.of(new ImprintDefinition.TypeComponent()
                        .type("behavioral").behavior("kevs-tieredz-modifiers:resonance")
                        .value(1f).maxStacks(4).maxBonus(4f).valueDisplay("none")
                        .params(Map.of(
                                "target_hostiles", 1f, "target_neutrals", 1f, "target_players", 0f,
                                "target_owner_pets", 0f, "target_self", 0f)))));


        write(dir, "marksman", attr("marksman", "#8FBF6A", "ranged", "ranged_weapon:damage", "MULTIPLY_TOTAL", 0.01f, 0.05f, 0.30f));
        write(dir, "quickdraw", attr("quickdraw", "#B6D335", "ranged", "ranged_weapon:haste", "MULTIPLY_BASE", 0.01f, 0.03f, 0.12f));
        write(dir, "deadeye", attr("deadeye", "#E0C040", "combat", "critical_strike:chance", "MULTIPLY_BASE", 0.01f, 0.03f, 0.12f));
        write(dir, "hunters_focus", attr("hunters_focus", "#D08030", "combat", "critical_strike:damage", "MULTIPLY_BASE", 0.02f, 0.06f, 0.24f));

        write(dir, "pyromancy", attr("pyromancy", "#E2641E", "magic", "spell_power:fire", "MULTIPLY_TOTAL", 0.01f, 0.05f, 0.30f));
        write(dir, "cryomancy", attr("cryomancy", "#5FC9E6", "magic", "spell_power:frost", "MULTIPLY_TOTAL", 0.01f, 0.05f, 0.30f));
        write(dir, "arcanist", attr("arcanist", "#9B59D0", "magic", "spell_power:arcane", "MULTIPLY_TOTAL", 0.01f, 0.05f, 0.30f));
        write(dir, "galvanism", attr("galvanism", "#F2D03B", "magic", "spell_power:lightning", "MULTIPLY_TOTAL", 0.01f, 0.05f, 0.30f));
        write(dir, "soulreaper", attr("soulreaper", "#5A8C7A", "magic", "spell_power:soul", "MULTIPLY_TOTAL", 0.01f, 0.05f, 0.30f));
        write(dir, "druidry", attr("druidry", "#5FB04A", "magic", "spell_power:nature", "MULTIPLY_TOTAL", 0.01f, 0.05f, 0.30f));
        write(dir, "hydromancy", attr("hydromancy", "#3C9BE0", "magic", "spell_power:water", "MULTIPLY_TOTAL", 0.01f, 0.05f, 0.30f));
        write(dir, "aeromancy", attr("aeromancy", "#C8E0F0", "magic", "spell_power:air", "MULTIPLY_TOTAL", 0.01f, 0.05f, 0.30f));
        write(dir, "geomancy", attr("geomancy", "#9C7A4A", "magic", "spell_power:earth", "MULTIPLY_TOTAL", 0.01f, 0.05f, 0.30f));

        write(dir, "channeling", attr("channeling", "#A0E0FF", "magic", "spell_power:haste", "MULTIPLY_BASE", 0.01f, 0.03f, 0.12f));
        write(dir, "mending_light", attr("mending_light", "#C7D0DE", "magic", "spell_power:healing", "MULTIPLY_TOTAL", 0.01f, 0.05f, 0.30f));

        write(dir, "barbed", gatedDamage("barbed", "#7D9B5A", List.of("weapon", "ranged"), "tiered:universal_damage",
                List.of("#minecraft:enchantable/bow", "#minecraft:enchantable/crossbow")));

        write(dir, "attuned", gatedDamage("attuned", "#8E44AD", List.of("weapon", "magic"), "tiered:universal_damage",
                List.of("#rpg_series:weapon_type/damage_wand", "#rpg_series:weapon_type/damage_staff",
                        "#rpg_series:weapon_type/healing_wand", "#rpg_series:weapon_type/healing_staff",
                        "#bards_rpg:lutes", "#bards_rpg:lyres")));

        write(dir, "sanctified", gatedDamage("sanctified", "#F1C40F", List.of("weapon", "magic"), "kevs-tieredz-modifiers:wand_power",
                List.of("#rpg_series:weapon_type/healing_wand", "#rpg_series:weapon_type/healing_staff",
                        "#bards_rpg:lutes", "#bards_rpg:lyres", "#bards_rpg:harp_crossbows"),
                0.005f, 0.025f, 0.25f));

        write(dir, "culling_shot", behavioral("culling_shot", "#7FA646", "ranged", "kevs-tieredz-modifiers:projectile_execute",
                "HIGHEST", 0.5f, 1.0f, 2.0f, Map.of("heal_amount", 2.0f)));

        write(dir, "spell_surge", behavioral("spell_surge", "#7FE0FF", "magic", "kevs-tieredz-modifiers:spell_surge",
                "ADDITIVE", 0.025f, 0.05f, 0.25f, Map.of("refund_fraction", 0.05f)));

        write(dir, "mending_grace", behavioralPercent("mending_grace", "#D8E8C0", "magic", "kevs-tieredz-modifiers:mending_grace",
                "ADDITIVE", 0.05f, 0.075f, 0.50f));

        System.out.println("[KevsTieredZ] Generated imprint JSON under " + dir.toAbsolutePath());
    }

    private static AbilityBinding resAbilityTiered(String masteredWhen, String ability, String color) {
        String plateKey = ability.endsWith("_shot") ? ability.substring(0, ability.length() - "_shot".length()) : ability;
        return new AbilityBinding()
                .masteredWhen(masteredWhen)
                .ability(NS + ":" + ability)
                .plate(new Plate()
                        .nameKey("imprint." + NS + ".resonance." + plateKey + ".name")
                        .lineKey("imprint." + NS + ".resonance." + plateKey)
                        .color(color));
    }

    private static ImprintDefinition base(String name, String color, String group) {
        return new ImprintDefinition()
                .id(NS + ":" + name).color(color)
                .nameKey("imprint." + NS + "." + name + ".name")
                .lineKey("imprint." + NS + "." + name)
                .combine("ADDITIVE").multiplicity("SLOT").reapply("HIGHEST").group(group);
    }

    private static ImprintDefinition base(String name, String color, List<String> groups) {
        return new ImprintDefinition()
                .id(NS + ":" + name).color(color)
                .nameKey("imprint." + NS + "." + name + ".name")
                .lineKey("imprint." + NS + "." + name)
                .combine("ADDITIVE").multiplicity("SLOT").reapply("HIGHEST").groups(groups);
    }

    private static ImprintDefinition gatedDamage(String name, String color, List<String> groups, String behavior,
            List<String> gateAnyTags) {
        return gatedDamage(name, color, groups, behavior, gateAnyTags, 0.01f, 0.05f, 0.50f);
    }

    private static ImprintDefinition gatedDamage(String name, String color, List<String> groups, String behavior,
            List<String> gateAnyTags, float min, float max, float cap) {
        return base(name, color, groups)
                .activeWhen(EligibilityPredicate.withEquippedItem(
                        EligibilityPredicate.EquippedItemClause.ofAnyTags("mainhand", gateAnyTags)))
                .types(List.of(new ImprintDefinition.TypeComponent()
                        .type("behavioral").behavior(behavior)
                        .valueDisplay("percent").valueRange(min, max).maxBonus(cap)));
    }

    private static ImprintDefinition attr(String name, String color, String group, String attribute, String op,
            float min, float max, float maxBonus) {
        return base(name, color, group).types(List.of(new ImprintDefinition.TypeComponent()
                .type("attribute").attribute(attribute).operation(op).anyWorn(true)
                .valueDisplay("percent").valueRange(min, max).maxBonus(maxBonus)));
    }

    private static ImprintDefinition behavioral(String name, String color, String group, String behavior, String combine,
            float min, float max, float maxBonus, Map<String, Float> params) {
        ImprintDefinition.TypeComponent c = new ImprintDefinition.TypeComponent()
                .type("behavioral").behavior(behavior).valueDisplay("percent").valueRange(min, max).maxBonus(maxBonus);
        if (params != null && !params.isEmpty()) c.params(params);
        return base(name, color, group).combine(combine).types(List.of(c));
    }

    private static ImprintDefinition behavioralPercent(String name, String color, String group, String behavior,
            String combine, float min, float max, float maxBonus) {
        return behavioral(name, color, group, behavior, combine, min, max, maxBonus, null);
    }

    private static void write(Path dir, String name, Object def) throws IOException {
        Files.writeString(dir.resolve(name + ".json"), GSON.toJson(def) + "\n", StandardCharsets.UTF_8);
    }
}
