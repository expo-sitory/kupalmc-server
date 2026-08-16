package dev.ixpu.leaguemechanics.item;

import java.util.HashMap;
import java.util.Map;

public class ItemStatsData {
    private static ItemStatsData instance;
    private final Map<String, ItemStatsRegistry> items = new HashMap<>();

    private ItemStatsData() {
        loadDefaultItems();
    }

    public static ItemStatsData getInstance() {
        if (instance == null) {
            instance = new ItemStatsData();
        }
        return instance;
    }

    private void loadDefaultItems() {

        // ABILITY POWER
        addItem("amplifying-tome", 0.0, 0.0, 0.0, 20.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
        addItem("blasting-wand", 0.0, 00.0, 0.0, 45.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
        addItem("needlessly-large-rod", 0.0, 0.0, 0.0, 65.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);

        // ATTACK DAMAGE
        addItem("cull", 0.0, 0.0, 7.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);

        addItem("long-sword", 0.0, 0.0, 10.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);

        addItem("pickaxe", 0.0, 0.0, 25.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
        addItem("b.f-sword", 0.0, 0.0, 45.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);

        // ARMOR
        addItem("cloth-armor", 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 15.0, 0.0, 0.0, 0.0, 0.0, 0.0);
        addItem("chain-vest", 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 40.0, 0.0, 0.0, 0.0, 0.0, 0.0);

        // MAGIC RESIST
        addItem("null-magic-mantle", 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 20.0, 0.0, 0.0, 0.0, 0.0);
        addItem("negatron-cloak", 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 45.0, 0.0, 0.0, 0.0, 0.0);

        // HEALTH
        addItem("ruby-crystal", 10.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);

        // HEALTH REGEN
        addItem("rejuvenation-bead", 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);

        // SATURATION REGEN
        addItem("faeri-charm", 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.5, 0.0);

        // ATTACK SPEED
        addItem("dagger", 0.0, 0.0, 0.0, 0.0, 0.0, 10.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);

        // MOVEMENT SPEED
        addItem("boots", 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 25.0);

        // HYBRID
        addItem("dorans-blade", 8.0, 0.0, 10.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
        addItem("dorans-bow", 0.0, 0.0, 8.0, 0.0, 0.0, 15.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
        addItem("dorans-helm", 10.0, 0.0, 0.0, 8.0, 0.0, 0.0, 8.0, 8.0, 0.0, 0.0, 0.0, 0.0);
        addItem("dorans-ring", 9.0, 0.0, 0.0, 18.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
        addItem("dorans-shield", 11.0, 0.5, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
        addItem("dark-seal", 5.0, 0.0, 0.0, 15.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
    }

    private void addItem(String id, double hp, double hr, double ad, double ap, double td,
                         double as, double ar, double mr, double ls, double cc, double sr, double ms) {
        items.put(id, new ItemStatsRegistry(id, "", hp, hr, ad, ap, td, as, ar, mr, ls, cc, sr, ms));
    }

    public ItemStatsRegistry getItem(String id) {
        return items.get(id);
    }
}