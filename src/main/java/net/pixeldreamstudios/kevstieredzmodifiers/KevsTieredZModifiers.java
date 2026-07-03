package net.pixeldreamstudios.kevstieredzmodifiers;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;
import net.pixeldreamstudios.kevstieredzmodifiers.entity.ModEntities;
import net.pixeldreamstudios.kevstieredzmodifiers.net.ModNetworking;

public class KevsTieredZModifiers implements ModInitializer {
	public static final String MOD_ID = "kevs-tieredz-modifiers";

	@Override
	public void onInitialize() {

		ModEntities.init();
		ModNetworking.init();

		FabricLoader.getInstance().getModContainer(MOD_ID).ifPresent(modContainer -> {
			ResourceManagerHelper.registerBuiltinResourcePack(
					Identifier.of(MOD_ID, "a_tiered_overwrite"),
					modContainer,
					ResourcePackActivationType.ALWAYS_ENABLED
			);
		});
		FabricLoader.getInstance().getModContainer(MOD_ID).ifPresent(modContainer -> {
			ResourceManagerHelper.registerBuiltinResourcePack(
					Identifier.of(MOD_ID, "tiered_more_overwrite_res"),
					modContainer,
					ResourcePackActivationType.ALWAYS_ENABLED
			);
		});

		FabricLoader.getInstance().getModContainer(MOD_ID).ifPresent(modContainer -> {
			ResourceManagerHelper.registerBuiltinResourcePack(
					Identifier.of(MOD_ID, "kevstieredzmodifiers"),
					modContainer,
					ResourcePackActivationType.ALWAYS_ENABLED
			);
		});

		if (FabricLoader.getInstance().isModLoaded("spell_power")) {
			FabricLoader.getInstance().getModContainer(MOD_ID).ifPresent(modContainer -> {
				ResourceManagerHelper.registerBuiltinResourcePack(
						Identifier.of(MOD_ID, "spellblades_compat"),
						modContainer,
						ResourcePackActivationType.ALWAYS_ENABLED
				);
			});
			FabricLoader.getInstance().getModContainer(MOD_ID).ifPresent(modContainer -> {
				ResourceManagerHelper.registerBuiltinResourcePack(
						Identifier.of(MOD_ID, "wandstaves_compat"),
						modContainer,
						ResourcePackActivationType.ALWAYS_ENABLED
				);
			});
			FabricLoader.getInstance().getModContainer(MOD_ID).ifPresent(modContainer -> {
				ResourceManagerHelper.registerBuiltinResourcePack(
						Identifier.of(MOD_ID, "magic_armor_compat"),
						modContainer,
						ResourcePackActivationType.ALWAYS_ENABLED
				);
			});
		}

		if (FabricLoader.getInstance().isModLoaded("spell_engine")) {
			FabricLoader.getInstance().getModContainer(MOD_ID).ifPresent(modContainer -> {
				ResourceManagerHelper.registerBuiltinResourcePack(
						Identifier.of(MOD_ID, "b_spellengine_compat"),
						modContainer,
						ResourcePackActivationType.ALWAYS_ENABLED
				);
			});
		}

		if (FabricLoader.getInstance().isModLoaded("druids")) {
			FabricLoader.getInstance().getModContainer(MOD_ID).ifPresent(modContainer -> {
				ResourceManagerHelper.registerBuiltinResourcePack(
						Identifier.of(MOD_ID, "druids_compat"),
						modContainer,
						ResourcePackActivationType.ALWAYS_ENABLED
				);
			});
		}

		if (FabricLoader.getInstance().isModLoaded("bards_rpg")) {
			FabricLoader.getInstance().getModContainer(MOD_ID).ifPresent(modContainer -> {
				ResourceManagerHelper.registerBuiltinResourcePack(
						Identifier.of(MOD_ID, "bards_compat"),
						modContainer,
						ResourcePackActivationType.ALWAYS_ENABLED
				);
			});
		}

		if (FabricLoader.getInstance().isModLoaded("wands")) {
			FabricLoader.getInstance().getModContainer(MOD_ID).ifPresent(modContainer -> {
				ResourceManagerHelper.registerBuiltinResourcePack(
						Identifier.of(MOD_ID, "buildingwands_compat"),
						modContainer,
						ResourcePackActivationType.ALWAYS_ENABLED
				);
			});
		}

		if (FabricLoader.getInstance().isModLoaded("mythicmetals")) {
			FabricLoader.getInstance().getModContainer(MOD_ID).ifPresent(modContainer -> {
				ResourceManagerHelper.registerBuiltinResourcePack(
						Identifier.of(MOD_ID, "b_mythicmetals_compat"),
						modContainer,
						ResourcePackActivationType.ALWAYS_ENABLED
				);
			});
		}

		if (FabricLoader.getInstance().isModLoaded("shyvvtrials")) {
			FabricLoader.getInstance().getModContainer(MOD_ID).ifPresent(modContainer -> {
				ResourceManagerHelper.registerBuiltinResourcePack(
						Identifier.of(MOD_ID, "shyvv_compat"),
						modContainer,
						ResourcePackActivationType.ALWAYS_ENABLED
				);
			});
		}
	}
}
