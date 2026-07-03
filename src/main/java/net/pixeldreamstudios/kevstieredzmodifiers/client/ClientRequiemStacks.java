package net.pixeldreamstudios.kevstieredzmodifiers.client;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/** Healer-only client store of current Requiem stack counts by target entity id. */
@Environment(EnvType.CLIENT)
public final class ClientRequiemStacks {

    private ClientRequiemStacks() {
    }

    private static final Map<Integer, Integer> STACKS = new HashMap<>();

    public static void set(List<Integer> ids, List<Integer> stacks) {
        STACKS.clear();
        int n = Math.min(ids.size(), stacks.size());
        for (int i = 0; i < n; i++) STACKS.put(ids.get(i), stacks.get(i));
    }

    public static boolean isEmpty() {
        return STACKS.isEmpty();
    }

    public static Map<Integer, Integer> all() {
        return STACKS;
    }
}
