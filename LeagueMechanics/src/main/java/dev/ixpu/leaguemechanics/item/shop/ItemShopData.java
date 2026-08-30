package dev.ixpu.leaguemechanics.item.shop;

import org.bukkit.inventory.ItemRarity;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemShopData {
    private static ItemShopData instance;
    private final Map<String, Integer> itemPrices = new HashMap<>();
    private final Map<String, Integer> itemLimits = new HashMap<>();
    private final Map<String, Integer> itemOrders = new HashMap<>();
    private final Map<String, String> itemModels = new HashMap<>();
    private final Map<String, ItemRarity> itemRarities = new HashMap<>();
    private final Map<String, String> itemGroups = new HashMap<>();
    private final Map<String, String> itemCategories = new HashMap<>();
    private final Map<String, List<String>> itemRequiredItems = new HashMap<>();

    private ItemShopData() {
        loadDefaultItems();
        loadDefaultModels();
        loadDefaultRequiredItems();
    }

    public static ItemShopData getInstance() {
        if (instance == null) {
            instance = new ItemShopData();
        }
        return instance;
    }

    private void loadDefaultItems() {

        // --------------------------------- STARTER ITEMS (MAIN CATEGORY) ---------------------------------

        addItem("cull", 18, 1, 0, ItemRarity.COMMON, null, "main");
        addItem("dark-seal", 15, 1, 1, ItemRarity.COMMON, null, "main");
        addItem("dorans-blade", 18, 1, 2, ItemRarity.COMMON, "dorans", "main");
        addItem("dorans-bow", 17, 1, 3, ItemRarity.COMMON, "dorans", "main");
        addItem("dorans-helm", 18, 1, 4, ItemRarity.COMMON, "dorans", "main");
        addItem("dorans-ring", 17, 1, 5, ItemRarity.COMMON, "dorans", "main");
        addItem("dorans-shield", 18, 1, 6, ItemRarity.COMMON, "dorans", "main");
        addItem("boots", 14, 1, 7, ItemRarity.UNCOMMON, null, "main");
        addItem("ruby-crystal", 17, 6, 8, ItemRarity.UNCOMMON, null, "main");
        addItem("rejuvenation-bead", 14, 6, 9, ItemRarity.UNCOMMON, null, "main");
        addItem("faeri-charm", 11, 6, 10, ItemRarity.UNCOMMON, null, "main");
        addItem("dagger", 13, 6, 11, ItemRarity.UNCOMMON, null, "main");

        // --------------------------------- MAGE ITEMS ---------------------------------

        addItem("amplifying-tome", 17, 6, 0, ItemRarity.UNCOMMON, null, "mage");
        addItem("blasting-wand", 24, 6, 1, ItemRarity.UNCOMMON, null, "mage");
        addItem("needlessly-large-rod", 28, 6, 2, ItemRarity.UNCOMMON, null, "mage");

        // --------------------------------- FIGHTER ITEMS ---------------------------------

        addItem("long-sword", 15, 6, 0, ItemRarity.UNCOMMON, null, "fighter");
        addItem("pickaxe", 24, 6, 1, ItemRarity.UNCOMMON, null, "fighter");
        addItem("b.f-sword", 29, 6, 2, ItemRarity.UNCOMMON, null, "fighter");

        // --------------------------------- TANK ITEMS ---------------------------------

        addItem("cloth-armor", 14, 6, 0, ItemRarity.UNCOMMON, null, "tank");
        addItem("null-magic-mantle", 17, 6, 1, ItemRarity.UNCOMMON, null, "tank");
        addItem("chain-vest", 24, 6, 2, ItemRarity.UNCOMMON, null, "tank");
        addItem("negatron-cloak", 27, 6, 3, ItemRarity.UNCOMMON, null, "tank");

    }

    private void loadDefaultRequiredItems() {
        addRequiredItem("chain-vest", "cloth-armor");
        addRequiredItem("negatron-cloak", "null-magic-mantle");
    }

    private void loadDefaultModels() {
        itemModels.put("cull", "minecraft:netherite_hoe");
        itemModels.put("dorans-blade", "minecraft:copper_sword");
        itemModels.put("dorans-bow", "minecraft:bow");
        itemModels.put("dorans-ring", "minecraft:music_disc_11");
        itemModels.put("dorans-helm", "minecraft:iron_helmet");
        itemModels.put("dorans-shield", "minecraft:shield");
        itemModels.put("dark-seal", "minecraft:popped_chorus_fruit");
        itemModels.put("boots", "minecraft:leather_boots");
        itemModels.put("amplifying-tome", "minecraft:book");
        itemModels.put("blasting-wand", "minecraft:golden_spear");
        itemModels.put("needlessly-large-rod", "minecraft:breeze_rod");
        itemModels.put("long-sword", "minecraft:iron_sword");
        itemModels.put("pickaxe", "minecraft:iron_pickaxe");
        itemModels.put("dagger", "minecraft:wooden_sword");
        itemModels.put("b.f-sword", "minecraft:diamond_sword");
        itemModels.put("cloth-armor", "minecraft:leather_chestplate");
        itemModels.put("null-magic-mantle", "minecraft:copper_nautilus_armor");
        itemModels.put("chain-vest", "minecraft:chainmail_chestplate");
        itemModels.put("negatron-cloak", "minecraft:iron_nautilus_armor");
        itemModels.put("rejuvenation-bead", "minecraft:green_bundle");
        itemModels.put("faeri-charm", "minecraft:orange_bundle");
        itemModels.put("ruby-crystal", "minecraft:red_dye");
    }

    private void addItem(String itemId, int price, int limit, int order, ItemRarity rarity, String group, String category) {
        itemPrices.put(itemId, price);
        itemLimits.put(itemId, limit);
        itemOrders.put(itemId, order);
        itemRarities.put(itemId, rarity);
        if (group != null) {
            itemGroups.put(itemId, group);
        }
        if (category != null) {
            itemCategories.put(itemId, category);
        }
    }

    private void addRequiredItem(String itemId, String... requiredIds) {
        itemRequiredItems.put(itemId, List.of(requiredIds));
    }

    public int getPrice(String itemId) {
        return itemPrices.getOrDefault(itemId, 0);
    }

    public int getLimit(String itemId) {
        return itemLimits.getOrDefault(itemId, 1);
    }

    public int getOrder(String itemId) {
        return itemOrders.getOrDefault(itemId, 0);
    }

    public boolean hasItem(String itemId) {
        return itemPrices.containsKey(itemId);
    }

    public String getModel(String itemId) {
        return itemModels.get(itemId);
    }

    public boolean hasCustomModel(String itemId) {
        return itemModels.containsKey(itemId);
    }

    public ItemRarity getRarity(String itemId) {
        return itemRarities.getOrDefault(itemId, ItemRarity.COMMON);
    }

    public String getGroup(String itemId) {
        return itemGroups.get(itemId);
    }

    public String getCategory(String itemId) {
        return itemCategories.get(itemId);
    }

    public List<String> getRequiredItems(String itemId) {
        return itemRequiredItems.getOrDefault(itemId, List.of());
    }
}