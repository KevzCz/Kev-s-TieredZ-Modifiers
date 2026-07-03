package net.pixeldreamstudios.kevstieredzmodifiers.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.pixeldreamstudios.kevstieredzmodifiers.imprint.resonance.RequiemAbility;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

// Requiem layer A: amplify all healing received by a Requiem-active player (universal self-heal buff).
@Mixin(LivingEntity.class)
public abstract class LivingEntityHealMixin {

    @ModifyVariable(method = "heal", at = @At("HEAD"), argsOnly = true)
    private float kevs$requiemHealBuff(float amount) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (self instanceof PlayerEntity player && !self.getWorld().isClient()) {
            float mult = RequiemAbility.healMultiplier(player.getUuid());
            if (mult != 1f) return amount * mult;
        }
        return amount;
    }
}
