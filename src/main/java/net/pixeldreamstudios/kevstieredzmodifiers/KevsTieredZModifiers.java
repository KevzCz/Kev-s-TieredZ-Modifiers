package net.pixeldreamstudios.kevstieredzmodifiers;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KevsTieredZModifiers implements ModInitializer {
	public static final String MOD_ID = "kevs-tieredz-modifiers";

	@Override
	public void onInitialize() {
		// Register default resource pack
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
