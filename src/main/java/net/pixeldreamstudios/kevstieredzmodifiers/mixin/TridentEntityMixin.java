package net.pixeldreamstudios.kevstieredzmodifiers.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.TridentEntity;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import net.minecraft.util.hit.EntityHitResult;
import net.pixeldreamstudios.kevstieredzmodifiers.imprint.resonance.SiegeArrow;
import net.pixeldreamstudios.kevstieredzmodifiers.imprint.resonance.TempestAbility;
import net.pixeldreamstudios.kevstieredzmodifiers.imprint.resonance.TempestTrident;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TridentEntity.class)
public abstract class TridentEntityMixin implements TempestTrident {

    @Unique private boolean kevs$tempest = false;
    @Unique private boolean kevs$dischargedRing = false;
    @Unique private boolean kevs$hitSomething = false;
    @Unique private final Set<UUID> kevs$struckSet = new HashSet<>();

    @Override public Set<UUID> kevs$struck() { return kevs$struckSet; }

    @Override public void kevs$setTempest(boolean t) {
        this.kevs$tempest = t;

        if (t && (Object) this instanceof SiegeArrow sa) sa.kevs$setPierceLevel((byte) 4);
    }
    @Override public boolean kevs$isResolved() { return kevs$dischargedRing; }
    @Override public void kevs$setResolved() { this.kevs$dischargedRing = true; }

    @Inject(method = "onEntityHit", at = @At("TAIL"))
    private void kevs$tempestHit(EntityHitResult result, CallbackInfo ci) {
        TridentEntity self = (TridentEntity) (Object) this;
        if (self.getWorld().isClient() || !kevs$tempest) return;
        if (!(self.getOwner() instanceof PlayerEntity owner)) return;
        if (result.getEntity() instanceof LivingEntity hit) {
            kevs$hitSomething = true;

            TempestAbility.strikeHit(owner, (PersistentProjectileEntity) (Object) this, hit);
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void kevs$tempestTick(CallbackInfo ci) {
        TridentEntity self = (TridentEntity) (Object) this;
        if (self.getWorld().isClient() || !kevs$tempest) return;
        if (!(self.getOwner() instanceof PlayerEntity owner)) return;

        if (self.getVelocity().lengthSquared() > 0.05) {
            TempestAbility.homeToward((PersistentProjectileEntity) (Object) this, owner);
        }
    }

    @Inject(method = "onPlayerCollision", at = @At("HEAD"))
    private void kevs$tempestPickup(PlayerEntity player, CallbackInfo ci) {
        TridentEntity self = (TridentEntity) (Object) this;
        if (self.getWorld().isClient() || !kevs$tempest) return;
        if (kevs$dischargedRing || !kevs$hitSomething) return;

        if (self.getOwner() != player) return;
        kevs$dischargedRing = true;
        TempestAbility.dischargeRing(player);
    }
}
