package net.pixeldreamstudios.kevstieredzmodifiers.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.pixeldreamstudios.kevstieredzmodifiers.imprint.behavior.ResonanceBehavior;
import net.pixeldreamstudios.kevstieredzmodifiers.imprint.resonance.ResonanceAbilityDefs;
import net.pixeldreamstudios.kevstieredzmodifiers.imprint.resonance.SiegeArrow;
import net.pixeldreamstudios.kevstieredzmodifiers.imprint.resonance.SiegeShot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PersistentProjectileEntity.class)
public abstract class PersistentProjectileEntityMixin implements SiegeArrow {

    @Shadow protected boolean inGround;

    @Unique private boolean kevs$siege = false;
    @Unique private boolean kevs$resolved = false;
    @Unique private boolean kevs$impacted = false;

    @Invoker("setPierceLevel")
    abstract void kevs$invokeSetPierceLevel(byte level);

    @Override public boolean kevs$isResolved() { return kevs$resolved; }
    @Override public void kevs$setResolved() { this.kevs$resolved = true; }
    @Override public boolean kevs$hasImpacted() { return kevs$impacted; }
    @Override public void kevs$setImpacted() { this.kevs$impacted = true; }
    @Override public void kevs$setPierceLevel(byte level) { kevs$invokeSetPierceLevel(level); }

    @Inject(method = "tick", at = @At("HEAD"))
    private void kevs$siegeTick(CallbackInfo ci) {
        PersistentProjectileEntity self = (PersistentProjectileEntity) (Object) this;
        if (self.getWorld().isClient()) return;

        if (!kevs$resolved) {
            kevs$resolved = true;
            if (self.getOwner() instanceof PlayerEntity owner
                    && ResonanceBehavior.activeAbilityIs(owner, ResonanceAbilityDefs.SIEGE)) {
                if (SiegeShot.consumeCharge(owner)) {
                    kevs$siege = true;
                    kevs$invokeSetPierceLevel((byte) 4);
                    SiegeShot.empower(owner, self);
                } else {

                    SiegeShot.onArrowFired(owner);
                }
            }
        }

        if (kevs$siege) SiegeShot.trail(self);

        // Fallback: some arrows settle into inGround without ever routing through onBlockHit's own
        // raycast (observed as "doesn't proc until the block below is broken"). If we're embedded and
        // never impacted, trigger the shockwave at the arrow's resting position instead of losing it.
        if (kevs$siege && !kevs$impacted && inGround) {
            kevs$impacted = true;
            SiegeShot.onEntityImpact(self, self.getPos());
        }
    }

    @Inject(method = "onBlockHit", at = @At("HEAD"))
    private void kevs$siegeBlockHit(BlockHitResult hit, CallbackInfo ci) {
        if (kevs$siege && !kevs$impacted && !((PersistentProjectileEntity) (Object) this).getWorld().isClient()) {
            kevs$impacted = true;
            SiegeShot.onBlockImpact((PersistentProjectileEntity) (Object) this, hit);
        }
    }

    @Inject(method = "onEntityHit", at = @At("TAIL"))
    private void kevs$siegeEntityHit(EntityHitResult hit, CallbackInfo ci) {
        PersistentProjectileEntity self = (PersistentProjectileEntity) (Object) this;
        if (kevs$siege && !kevs$impacted && !self.getWorld().isClient()) {
            kevs$impacted = true;
            SiegeShot.onEntityImpact(self, hit.getPos());
        }
    }
}
