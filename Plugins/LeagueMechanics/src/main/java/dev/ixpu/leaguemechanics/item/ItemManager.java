package dev.ixpu.leaguemechanics.item;

import java.util.HashMap;
import java.util.Map;

public class ItemManager {
    private static ItemManager instance;
    private final Map<String, ItemStatData> items = new HashMap<>();

    private ItemManager() {
        loadDefaultItems();
    }

    public static ItemManager getInstance() {
        if (instance == null) {
            instance = new ItemManager();
        }
        return instance;
    }

    private void loadDefaultItems() {
        // AP ITEMS
        addItem("amplifying-tome", "", 0.0, 20.0, 0.0, 0.0);

        // AD ITEMS
        addItem("long-sword", "", 10.0, 0.0, 0.0, 0.0);

        // AR ITEMS
        addItem("cloth-armor", "", 0.0, 0.0, 15.0, 0.0);

        // MR ITEMS
        // no mr for starter items (still gonna make recipes)
    }

    private void addItem(String id, String name, double ad, double ap, double ar, double mr) {
        items.put(id, new ItemStatData(id, name, ad, ap, ar, mr));
    }

    public ItemStatData getItem(String id) {
        return items.get(id);
    }

    public void updateItemStats(String id, double ad, double ap, double ar, double mr) {
        ItemStatData data = items.get(id);
        if (data != null) {
            data.setAd(ad);
            data.setAp(ap);
            data.setAr(ar);
            data.setMr(mr);
        }
    }

    public Map<String, ItemStatData> getAllItems() {
        return new HashMap<>(items);
    }

    public void addCustomItem(String id, String name, double ad, double ap, double ar, double mr) {
        items.put(id, new ItemStatData(id, name, ad, ap, ar, mr));
    }
}