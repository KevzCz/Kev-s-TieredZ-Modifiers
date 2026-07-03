package net.pixeldreamstudios.kevstieredzmodifiers.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.pixeldreamstudios.kevstieredzmodifiers.KevsTieredZModifiers;

public final class ModEntities {

    private ModEntities() {
    }

    public static void init() {
        AftershockEntity.TYPE = Registry.register(
                Registries.ENTITY_TYPE,
                Identifier.of(KevsTieredZModifiers.MOD_ID, "aftershock"),
                EntityType.Builder.<AftershockEntity>create(AftershockEntity::new, SpawnGroup.MISC)
                        .dimensions(0.5f, 0.5f)
                        .maxTrackingRange(8)
                        .makeFireImmune()
                        .build("aftershock"));
    }
}
