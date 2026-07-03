package net.pixeldreamstudios.kevstieredzmodifiers.mixin.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.pixeldreamstudios.kevstieredzmodifiers.client.ClientStunState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(Entity.class)
public abstract class EntityLookLockMixin {

    @Inject(method = "changeLookDirection", at = @At("HEAD"), cancellable = true)
    private void kevs$lookLock(double cursorDeltaX, double cursorDeltaY, CallbackInfo ci) {
        Entity self = (Entity) (Object) this;
        if (self != MinecraftClient.getInstance().player) return;
        if (ClientStunState.isStunned()) {
            ci.cancel();
        }
    }
}
