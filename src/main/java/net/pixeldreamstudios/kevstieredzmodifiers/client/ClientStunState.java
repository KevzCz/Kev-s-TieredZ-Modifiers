package net.pixeldreamstudios.kevstieredzmodifiers.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;

@Environment(EnvType.CLIENT)
public final class ClientStunState {

    private static long stunnedUntil = 0L;

    private ClientStunState() {
    }

    public static void set(long until) {
        stunnedUntil = until;
    }

    public static boolean isStunned() {
        if (stunnedUntil <= 0L) return false;
        var world = MinecraftClient.getInstance().world;
        if (world == null) return false;
        if (world.getTime() >= stunnedUntil) {
            stunnedUntil = 0L;
            return false;
        }
        return true;
    }
}
