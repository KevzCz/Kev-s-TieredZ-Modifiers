package net.pixeldreamstudios.kevstieredzmodifiers.imprint.resonance;

import java.util.Map;

import net.minecraft.entity.player.PlayerEntity;

public interface ResonanceStatus {

    float cooldownFill(PlayerEntity player, int tier, Map<String, Float> params);

    boolean active(PlayerEntity player, int tier, Map<String, Float> params);
}
