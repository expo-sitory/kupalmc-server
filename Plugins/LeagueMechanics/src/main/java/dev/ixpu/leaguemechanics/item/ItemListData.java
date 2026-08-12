package dev.ixpu.leaguemechanics.item;

import java.util.HashMap;
import java.util.Map;

public class ItemListData {
    private static ItemListData instance;
    private final Map<String, ItemStatData> items = new HashMap<>();

    private ItemListData() {
        loadDefaultItems();
    }

    public static ItemListData getInstance() {
        if (instance == null) {
            instance = new ItemListData();
        }
        return instance;
    }

    private void loadDefaultItems() {

        // ABILITY POWER
        addItem("amplifying-tome", 0.0, 20.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
        addItem("blasting-wand", 0.0, 45.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
        addItem("needlessly-large-rod", 0.0, 65.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);

        // ATTACK DAMAGE
        addItem("cull", 7.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);

        addItem("long-sword", 10.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);

        addItem("pickaxe", 25.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
        addItem("b.f-sword", 45.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);

        // ARMOR
        addItem("cloth-armor", 0.0, 0.0, 15.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
        addItem("chain-vest", 0.0, 0.0, 40, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);

        // MAGIC RESIST
        addItem("null-magic-mantle", 0.0, 0.0, 0.0, 20.0, 0.0, 0.0, 0.0, 0.0, 0.0);
        addItem("negatron-cloak", 0.0, 0.0, 0.0, 45.0, 0.0, 0.0, 0.0, 0.0, 0.0);

        // HEALTH
        addItem("ruby-crystal", 0.0, 0.0, 0.0, 0.0, 10.0, 0.0, 0.0, 0.0, 0.0);

        // HEALTH REGEN
        addItem("rejuvenation-bead", 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0);

        // SATURATION REGEN
        addItem("faeri-charm", 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.5, 0.0, 0.0);

        // ATTACK SPEED
        addItem("dagger", 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 10.0, 0.0);

        // MOVEMENT SPEED
        addItem("boots", 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 25.0);

        // HYBRID
        addItem("dorans-blade", 10.0, 0.0, 0.0, 0.0, 8.0, 0.0, 0.0, 0.0, 0.0);
        addItem("dorans-bow", 8.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 15.0, 0.0);
        addItem("dorans-helm", 0.0, 0.0, 8.0, 8.0, 10.0, 0.0, 0.0, 0.0, 0.0);
        addItem("dorans-ring", 0.0, 18.0, 0.0, 0.0, 9.0, 0.0, 0.0, 0.0, 0.0);
        addItem("dorans-shield", 0.0, 0.0, 0.0, 0.0, 11.0, 0.5, 0.0, 0.0, 0.0);
        addItem("dark-seal", 0.0, 15.0, 0.0, 0.0, 5.0, 0.0, 0.0, 0.0, 0.0);
    }

    private void addItem(String id, double ad, double ap, double ar, double mr, double hp, double hr, double sr, double as, double ms) {
        items.put(id, new ItemStatData(id, "", ad, ap, ar, mr, hp, hr, sr, as, ms));
    }

    public ItemStatData getItem(String id) {
        return items.get(id);
    }

    public void updateItemStats(String id, String name, double ad, double ap, double ar, double mr, double hp, double hr, double sr, double as, double ms) {
        ItemStatData data = items.get(id);
        if (data != null) {
            data.setAd(ad);
            data.setAp(ap);
            data.setAr(ar);
            data.setMr(mr);
            data.setHp(hp);
            data.setHr(hr);
            data.setSr(sr);
            data.setAs(as);
            data.setMs(ms);
        }
    }

    public Map<String, ItemStatData> getAllItems() {
        return new HashMap<>(items);
    }

    public void addCustomItem(String id, String name, double ad, double ap, double ar, double mr, double hp, double hr, double sr, double as, double ms) {
        items.put(id, new ItemStatData(id, name, ad, ap, ar, mr, hp, hr, sr, as, ms));
    }
}