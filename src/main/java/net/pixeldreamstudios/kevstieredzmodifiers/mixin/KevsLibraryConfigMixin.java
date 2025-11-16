package net.pixeldreamstudios.kevstieredzmodifiers.mixin;

import net.pixeldreamstudios.kevslibrary.config.KevsLibraryConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(KevsLibraryConfig.class)
public class KevsLibraryConfigMixin {

    @Inject(method = "isAttributeEnabled", at = @At("HEAD"), cancellable = true, remap = false)
    private void disableCritAttributes(String attributeName, CallbackInfoReturnable<Boolean> cir) {
        if (attributeName.equals("crit_chance") || attributeName.equals("crit_damage")) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "isSystemEnabled", at = @At("HEAD"), cancellable = true, remap = false)
    private void disableCritSystem(String systemName, CallbackInfoReturnable<Boolean> cir) {
        if (systemName.equals("crit")) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "isCritEnabled", at = @At("HEAD"), cancellable = true, remap = false)
    private void disableCrit(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }
}