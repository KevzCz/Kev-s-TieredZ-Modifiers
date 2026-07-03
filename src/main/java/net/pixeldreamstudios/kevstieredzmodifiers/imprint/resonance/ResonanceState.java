package net.pixeldreamstudios.kevstieredzmodifiers.imprint.resonance;

import draylar.tiered.api.imprint.ImprintResource;
import net.minecraft.entity.player.PlayerEntity;

public final class ResonanceState {

    private ResonanceState() {
    }

    public static float get(PlayerEntity player, String key) {
        return ImprintResource.get(player, key);
    }

    public static void set(PlayerEntity player, String key, float value) {
        ImprintResource.set(player, key, value);
    }
}
