package net.pixeldreamstudios.kevstieredzmodifiers.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.TridentItem;
import net.pixeldreamstudios.kevstieredzmodifiers.imprint.behavior.ResonanceBehavior;
import net.pixeldreamstudios.kevstieredzmodifiers.imprint.resonance.ResonanceAbilityDefs;
import net.pixeldreamstudios.kevstieredzmodifiers.imprint.resonance.TempestAbility;
import net.pixeldreamstudios.kevstieredzmodifiers.imprint.resonance.TempestTrident;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(TridentItem.class)
public abstract class TridentItemMixin {

    @ModifyArg(
            method = "onStoppedUsing",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;spawnEntity(Lnet/minecraft/entity/Entity;)Z"))
    private Entity kevs$markTempest(Entity thrown) {
        if (thrown instanceof TempestTrident t && thrown instanceof ProjectileEntity proj
                && proj.getOwner() instanceof PlayerEntity player && !thrown.getWorld().isClient()) {

            if (ResonanceBehavior.activeAbilityIs(player, ResonanceAbilityDefs.TEMPEST) && TempestAbility.consumeCharge(player)) {
                t.kevs$setTempest(true);
            }
        }
        return thrown;
    }
}
