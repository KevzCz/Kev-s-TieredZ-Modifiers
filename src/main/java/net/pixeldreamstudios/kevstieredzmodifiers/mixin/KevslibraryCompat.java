package net.pixeldreamstudios.kevstieredzmodifiers.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.text.DecimalFormat;
import java.util.Set;
import java.util.function.Consumer;

@Mixin(ItemStack.class)
@Environment(EnvType.CLIENT)
public abstract class KevslibraryCompat {
    private static final Identifier CRIT_CHANCE = Identifier.of("kevslibrary", "crit_chance");
    private static final Identifier MULTISTRIKE_CHANCE = Identifier.of("kevslibrary", "multistrike_chance");
    private static final Identifier CHAIN_LIGHTNING_CHANCE = Identifier.of("kevslibrary", "chain_lightning_chance");
    private static final Identifier CHAIN_LIGHTNING_OVERLOAD_CHANCE = Identifier.of("kevslibrary", "chain_lightning_overload_chance");
    private static final Identifier FIRE_TORNADO_CHANCE = Identifier.of("kevslibrary", "fire_tornado_chance");
    private static final Identifier FIRE_TORNADO_OVERLOAD_CHANCE = Identifier.of("kevslibrary", "fire_tornado_overload_chance");
    private static final Identifier FROST_NOVA_CHANCE = Identifier.of("kevslibrary", "frost_nova_chance");
    private static final Identifier SOUL_LINK_CHANCE = Identifier.of("kevslibrary", "soul_link_chance");
    private static final Identifier ARCANE_RUPTURE_CHANCE = Identifier.of("kevslibrary", "arcane_rupture_chance");
    private static final Identifier DAMAGE = Identifier.of("kevslibrary", "damage");
    private static final Identifier CRIT_DAMAGE = Identifier.of("kevslibrary", "crit_damage");
    private static final Identifier MULTISTRIKE_DAMAGE = Identifier.of("kevslibrary", "multistrike_damage");
    private static final Identifier MULTISTRIKE_COUNT = Identifier.of("kevslibrary", "multistrike_count");
    private static final Identifier FROST_NOVA_COUNT = Identifier.of("kevslibrary", "frost_nova_count");
    private static final Identifier CHAIN_LIGHTNING_COUNT = Identifier.of("kevslibrary", "chain_lightning_count");
    private static final Identifier ARCANE_RUPTURE_DAMAGE = Identifier.of("kevslibrary", "arcane_rupture_damage");
    private static final Identifier SOUL_LINK_DAMAGE = Identifier.of("kevslibrary", "soul_link_damage");
    private static final Identifier PET_INHERITANCE_RATIO = Identifier.of("kevslibrary", "pet_inheritance_ratio");

    private static final Set<Identifier> PERCENT_ATTRIBUTES = Set.of(
            DAMAGE, CRIT_DAMAGE, MULTISTRIKE_DAMAGE, CRIT_CHANCE, MULTISTRIKE_CHANCE, CHAIN_LIGHTNING_CHANCE, CHAIN_LIGHTNING_OVERLOAD_CHANCE,
            FIRE_TORNADO_CHANCE, FIRE_TORNADO_OVERLOAD_CHANCE, FROST_NOVA_CHANCE,ARCANE_RUPTURE_DAMAGE, SOUL_LINK_DAMAGE,
            SOUL_LINK_CHANCE, ARCANE_RUPTURE_CHANCE, PET_INHERITANCE_RATIO
    );

    private static final Set<Identifier> FLAT_ATTRIBUTES = Set.of(
            MULTISTRIKE_COUNT,
            FROST_NOVA_COUNT, CHAIN_LIGHTNING_COUNT,
            SOUL_LINK_DAMAGE
    );

    @Inject(
            method = "appendAttributeModifierTooltip(Ljava/util/function/Consumer;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/registry/entry/RegistryEntry;Lnet/minecraft/entity/attribute/EntityAttributeModifier;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void injectKevsTooltip(Consumer<Text> textConsumer, @Nullable PlayerEntity player,
                                   RegistryEntry<EntityAttribute> attribute,
                                   EntityAttributeModifier modifier, CallbackInfo ci) {

        Identifier id = Registries.ATTRIBUTE.getId(attribute.value());
        if (id == null || !id.getNamespace().equals("kevslibrary")) return;

        double value = modifier.value();
        MutableText tooltip = null;

        if (PERCENT_ATTRIBUTES.contains(id)) {
            DecimalFormat df = new DecimalFormat("#.##");
            String formatted = "+" + df.format(value * 100) + "% ";
            tooltip = Text.literal(formatted).append(Text.translatable(attribute.value().getTranslationKey())).formatted(Formatting.BLUE);
        }
        else if (FLAT_ATTRIBUTES.contains(id)) {
            tooltip = Text.literal("+" + value + " ").append(Text.translatable(attribute.value().getTranslationKey())).formatted(Formatting.BLUE);
        }

        if (tooltip != null) {
            textConsumer.accept(tooltip);
            ci.cancel();
        }
    }
}
