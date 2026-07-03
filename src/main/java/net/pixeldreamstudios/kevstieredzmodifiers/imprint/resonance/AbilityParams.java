package net.pixeldreamstudios.kevstieredzmodifiers.imprint.resonance;

import java.util.Map;

public final class AbilityParams {

    private AbilityParams() {
    }

    public static float f(Map<String, Float> params, String key, float def) {
        if (params == null) return def;
        Float v = params.get(key);
        return v == null ? def : v;
    }

    public static int i(Map<String, Float> params, String key, int def) {
        if (params == null) return def;
        Float v = params.get(key);
        return v == null ? def : Math.round(v);
    }
}
