package net.pixeldreamstudios.kevstieredzmodifiers.mixin;

import net.minecraft.entity.LivingEntity;
import net.pixeldreamstudios.kevstieredzmodifiers.imprint.resonance.StunManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityStunMixin {

    @Inject(method = "tick", at = @At("TAIL"))
    private void kevs$stunFreeze(CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!self.getWorld().isClient()) {
            StunManager.tickFreeze(self);
        }
    }
}
