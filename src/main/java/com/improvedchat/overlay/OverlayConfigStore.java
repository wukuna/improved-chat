package com.improvedchat.overlay;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Distinguishes first use from an intentionally empty saved list. */
public final class OverlayConfigStore {
    private OverlayConfigStore() { }

    public static List<OverlayConfig> defaults() {
        return new ArrayList<>(Arrays.asList(OverlayConfig.defaultPrivateOverlay(), OverlayConfig.defaultAllOverlay()));
    }

    public static List<OverlayConfig> read(Gson gson, String json) {
        if (json == null || json.trim().isEmpty()) return defaults();
        List<OverlayConfig> loaded = gson.fromJson(json, new TypeToken<List<OverlayConfig>>() { }.getType());
        if (loaded == null || loaded.contains(null)) throw new JsonParseException("Expected an overlay list");
        Set<String> ids = new HashSet<>();
        for (OverlayConfig config : loaded) {
            if (!ids.add(config.getId())) throw new JsonParseException("Duplicate overlay ID");
            config.normalize();
        }
        return loaded;
    }
}
