package dev.ixpu.leaguemechanics.item.passives;

import java.util.HashMap;
import java.util.Map;

public class ItemPassivesRegistry {
    private static ItemPassivesRegistry instance;
    private final Map<String, ItemPassive> passives = new HashMap<>();

    public static ItemPassivesRegistry getInstance() {
        if (instance == null) {
            instance = new ItemPassivesRegistry();
            instance.registerDefaults();
        }
        return instance;
    }

    private void registerDefaults() {
        register(new cull());
        register(new dark_seal());
        register(new dorans_ring());
        register(new dorans_shield());
    }

    public void register(ItemPassive passive) {
        passives.put(passive.getId(), passive);
    }

    public ItemPassive getPassive(String id) {
        return passives.get(id);
    }
}