package dev.ixpu.leaguemechanics.item.shop;

import dev.ixpu.leaguemechanics.item.ItemStatsData;
import dev.ixpu.leaguemechanics.item.ItemStatsRegistry;

import java.util.*;

public class ItemShopRegistry {
    private static ItemShopRegistry instance;
    private final List<ShopItem> shopItems = new ArrayList<>();

    private ItemShopRegistry() {
        registerItems();
    }

    public static ItemShopRegistry getInstance() {
        if (instance == null) {
            instance = new ItemShopRegistry();
        }
        return instance;
    }

    private void registerItems() {
        ItemStatsData statsData = ItemStatsData.getInstance();
        ItemShopData shopData = ItemShopData.getInstance();

        String[] itemIds = {
                "amplifying-tome", "blasting-wand", "needlessly-large-rod",
                "cull", "long-sword", "pickaxe", "b.f-sword",
                "cloth-armor",
                "null-magic-mantle",
                "ruby-crystal",
                "rejuvenation-bead",
                "faeri-charm",
                "dagger",
                "boots",
                "dorans-blade", "dorans-bow", "dorans-helm", "dorans-ring", "dorans-shield", "dark-seal"
        };

        for (String itemId : itemIds) {
            ItemStatsRegistry stats = statsData.getItem(itemId);
            if (stats != null && shopData.hasItem(itemId)) {
                shopItems.add(new ShopItem(stats, shopData.getPrice(itemId), shopData.getLimit(itemId)));
            }
        }
    }

    public List<ShopItem> getAllShopItems() {
        return new ArrayList<>(shopItems);
    }

    public static class ShopItem {
        private final ItemStatsRegistry stats;
        private final int price;
        private final int limit;

        public ShopItem(ItemStatsRegistry stats, int price, int limit) {
            this.stats = stats;
            this.price = price;
            this.limit = limit;
        }

        public ItemStatsRegistry getStats() {
            return stats;
        }

        public int getPrice() {
            return price;
        }

        public int getLimit() {
            return limit;
        }

        public String getDisplayName() {
            return stats.getName();
        }

        public String getId() {
            return stats.getId();
        }

    }
}