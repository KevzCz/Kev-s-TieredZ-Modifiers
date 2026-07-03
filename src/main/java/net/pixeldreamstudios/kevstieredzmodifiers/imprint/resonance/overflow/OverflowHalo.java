package net.pixeldreamstudios.kevstieredzmodifiers.imprint.resonance.overflow;

import java.util.UUID;

import org.jetbrains.annotations.Nullable;

public interface OverflowHalo {

    boolean kevs$isHalo();

    void kevs$setHalo(boolean halo);

    @Nullable
    UUID kevs$haloOwner();

    void kevs$setHaloOwner(@Nullable UUID owner);

    float kevs$haloAngle();

    void kevs$setHaloAngle(float angle);

    int kevs$haloLife();

    void kevs$setHaloLife(int life);
}
