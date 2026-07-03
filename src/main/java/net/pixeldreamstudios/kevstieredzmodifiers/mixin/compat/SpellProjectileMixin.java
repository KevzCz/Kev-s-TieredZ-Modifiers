package net.pixeldreamstudios.kevstieredzmodifiers.mixin.compat;

import java.util.UUID;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.Vec3d;
import net.spell_engine.entity.SpellProjectile;
import net.pixeldreamstudios.kevstieredzmodifiers.imprint.resonance.overflow.OverflowHalo;
import net.pixeldreamstudios.kevstieredzmodifiers.imprint.resonance.overflow.OverflowHaloController;
import org.jetbrains.annotations.Nullable;

@Mixin(SpellProjectile.class)
public abstract class SpellProjectileMixin implements OverflowHalo {

    @Shadow(remap = false) private boolean skipTravel;

    @Unique
    private static final TrackedData<Boolean> KEVS_ORBITING =
            DataTracker.registerData(SpellProjectile.class, TrackedDataHandlerRegistry.BOOLEAN);

    @Nullable private UUID kevs$haloOwner;
    private float kevs$haloAngle;
    private int kevs$haloLife;

    @Inject(method = "initDataTracker", at = @At("TAIL"))
    private void kevs$initOrbiting(DataTracker.Builder builder, CallbackInfo ci) {
        builder.add(KEVS_ORBITING, false);
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void kevs$haloTick(CallbackInfo ci) {
        SpellProjectile self = (SpellProjectile) (Object) this;

        if (self.getWorld().isClient()) {
            if (self.getDataTracker().get(KEVS_ORBITING)) {
                self.setVelocity(Vec3d.ZERO);
                this.skipTravel = true;
                ci.cancel();
            }
            return;
        }

        if (!kevs$isHalo()) return;
        boolean orbiting = OverflowHaloController.tickHalo(self);
        if (kevs$isHalo() && orbiting) {

            this.skipTravel = true;
            ci.cancel();
        } else {

            this.skipTravel = false;
        }
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void kevs$writeHalo(NbtCompound nbt, CallbackInfo ci) {
        if (!kevs$isHalo()) return;
        nbt.putBoolean("kevs_halo", true);
        nbt.putFloat("kevs_halo_angle", kevs$haloAngle);
        nbt.putInt("kevs_halo_life", kevs$haloLife);
        if (kevs$haloOwner != null) nbt.putUuid("kevs_halo_owner", kevs$haloOwner);
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void kevs$readHalo(NbtCompound nbt, CallbackInfo ci) {
        if (!nbt.getBoolean("kevs_halo")) return;
        kevs$setHalo(true);
        kevs$haloAngle = nbt.getFloat("kevs_halo_angle");
        kevs$haloLife = nbt.getInt("kevs_halo_life");
        if (nbt.containsUuid("kevs_halo_owner")) kevs$haloOwner = nbt.getUuid("kevs_halo_owner");
    }

    @Override public boolean kevs$isHalo() {
        return ((SpellProjectile) (Object) this).getDataTracker().get(KEVS_ORBITING);
    }
    @Override public void kevs$setHalo(boolean halo) {
        ((SpellProjectile) (Object) this).getDataTracker().set(KEVS_ORBITING, halo);
    }
    @Override @Nullable public UUID kevs$haloOwner() { return kevs$haloOwner; }
    @Override public void kevs$setHaloOwner(@Nullable UUID owner) { this.kevs$haloOwner = owner; }
    @Override public float kevs$haloAngle() { return kevs$haloAngle; }
    @Override public void kevs$setHaloAngle(float angle) { this.kevs$haloAngle = angle; }
    @Override public int kevs$haloLife() { return kevs$haloLife; }
    @Override public void kevs$setHaloLife(int life) { this.kevs$haloLife = life; }
}
