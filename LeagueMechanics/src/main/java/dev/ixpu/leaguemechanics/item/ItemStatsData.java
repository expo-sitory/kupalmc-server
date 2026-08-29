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
        addItem("amplifying-tome", "Amplifying Tome", 0.0, 0.0, 0.0, 20.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
        addItem("blasting-wand", "Blasting Wand", 0.0, 0.0, 0.0, 45.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
        addItem("needlessly-large-rod", "Needlessly Large Rod", 0.0, 0.0, 0.0, 65.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);

        // ATTACK DAMAGE
        addItem("cull", "Cull", 0.0, 0.0, 7.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, true, "cull");

        addItem("long-sword", "Long Sword", 0.0, 0.0, 10.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);

        addItem("pickaxe", "Pickaxe", 0.0, 0.0, 25.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
        addItem("b.f-sword", "B.F. Sword", 0.0, 0.0, 45.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);

        // ARMOR
        addItem("cloth-armor", "Cloth Armor", 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 15.0, 0.0, 0.0, 0.0, 0.0, 0.0);
        addItem("chain-vest", "Chain Vest", 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 40.0, 0.0, 0.0, 0.0, 0.0, 0.0);

        // MAGIC RESIST
        addItem("null-magic-mantle", "Null-Magic Mantle", 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 20.0, 0.0, 0.0, 0.0, 0.0);
        addItem("negatron-cloak", "Negatron Cloak", 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 45.0, 0.0, 0.0, 0.0, 0.0);

        // HEALTH
        addItem("ruby-crystal", "Ruby Crystal", 10.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);

        // HEALTH REGEN
        addItem("rejuvenation-bead", "Rejuvenation Bead", 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);

        // SATURATION REGEN
        addItem("faeri-charm", "Faeri Charm", 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.5, 0.0);

        // ATTACK SPEED
        addItem("dagger", "Dagger", 0.0, 0.0, 0.0, 0.0, 0.0, 10.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);

        // MOVEMENT SPEED
        addItem("boots", "Boots", 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 25.0);

        // HYBRID
        addItem("dorans-blade", "Doran's Blade", 8.0, 0.0, 10.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
        addItem("dorans-bow", "Doran's Bow", 0.0, 0.0, 8.0, 0.0, 0.0, 15.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
        addItem("dorans-helm", "Doran's Helm", 10.0, 0.0, 0.0, 8.0, 0.0, 0.0, 8.0, 8.0, 0.0, 0.0, 0.0, 0.0);
        addItem("dorans-ring", "Doran's Ring", 9.0, 0.0, 0.0, 18.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, true, "dorans-ring");
        addItem("dorans-shield", "Doran's Shield", 11.0, 0.5, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, true, "dorans-shield");
        addItem("dark-seal", "Dark Seal", 5.0, 0.0, 0.0, 15.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, true, "dark-seal");
    }

    private void addItem(String id, String name, double hp, double hr, double ad, double ap, double td,
                         double as, double ar, double mr, double ls, double cc, double sr, double ms) {
        addItem(id, name, hp, hr, ad, ap, td, as, ar, mr, ls, cc, sr, ms, false, null);
    }

    private void addItem(String id, String name, double hp, double hr, double ad, double ap, double td,
                         double as, double ar, double mr, double ls, double cc, double sr, double ms,
                         boolean hasPassive, String passiveId) {
        items.put(id, new ItemStatsRegistry(id, name, hp, hr, ad, ap, td, as, ar, mr, ls, cc, sr, ms, hasPassive, passiveId));
    }

    public ItemStatsRegistry getItem(String id) {
        return items.get(id);
    }
}