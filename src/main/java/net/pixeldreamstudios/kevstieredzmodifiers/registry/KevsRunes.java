package net.pixeldreamstudios.kevstieredzmodifiers.registry;

import draylar.tiered.registry.ModItems;

public final class KevsRunes {

    public static final String[] RUNE_NAMES = { "rune_azure", "rune_radiant", "rune_tidal" };

    private KevsRunes() {
    }

    public static void register() {
        for (String name : RUNE_NAMES) {
            ModItems.addRune(name);
        }
    }
}
