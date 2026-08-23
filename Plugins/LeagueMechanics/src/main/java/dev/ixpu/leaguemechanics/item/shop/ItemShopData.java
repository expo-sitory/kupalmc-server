package dev.ixpu.leaguemechanics.item.shop;

import org.bukkit.inventory.ItemRarity;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

public class ItemShopData {
    private static ItemShopData instance;
    private final Map<String, Integer> itemPrices = new HashMap<>();
    private final Map<String, Integer> itemLimits = new HashMap<>();
    private final Map<String, Integer> itemSlots = new HashMap<>();
    private final Map<String, String> itemModels = new HashMap<>();
    private final Map<String, ItemRarity> itemRarities = new HashMap<>();
    private final Map<String, String> itemGroups = new HashMap<>(); // Item ID -> Group name

    private ItemShopData() {
        loadDefaultPrices();
        loadDefaultModels();
    }

    public static ItemShopData getInstance() {
        if (instance == null) {
            instance = new ItemShopData();
        }
        return instance;
    }

    private void loadDefaultPrices() {

        // STARTER ITEMS
        addItem("cull", 18, 1, 0, ItemRarity.COMMON, null);
        addItem("dark-seal", 15, 1, 1, ItemRarity.COMMON, null);
        addItem("dorans-blade", 18, 1, 2, ItemRarity.COMMON, "dorans");
        addItem("dorans-bow", 17, 1, 3, ItemRarity.COMMON, "dorans");
        addItem("dorans-helm", 18, 1, 4, ItemRarity.COMMON, "dorans");
        addItem("dorans-ring", 17, 1, 5, ItemRarity.COMMON, "dorans");
        addItem("dorans-shield", 18, 1, 6, ItemRarity.COMMON, "dorans");

        // BASIC ITEMS
        addItem("amplifying-tome", 17, 6, 27, ItemRarity.COMMON, null);
        addItem("blasting-wand", 24, 6, 28, ItemRarity.UNCOMMON, null);
        addItem("needlessly-large-rod", 28, 6, 29, ItemRarity.RARE, null);
        addItem("long-sword", 15, 6, 18, ItemRarity.COMMON, null);
        addItem("pickaxe", 24, 6, 19, ItemRarity.UNCOMMON, null);
        addItem("b.f-sword", 29, 6, 20, ItemRarity.EPIC, null);
        addItem("cloth-armor", 14, 6, 22, ItemRarity.COMMON, null);
        addItem("null-magic-mantle", 17, 6, 31, ItemRarity.UNCOMMON, null);
        addItem("ruby-crystal", 17, 6, 40, ItemRarity.COMMON, null);
        addItem("rejuvenation-bead", 14, 6, 24, ItemRarity.COMMON, null);
        addItem("faeri-charm", 11, 6, 25, ItemRarity.COMMON, null);
        addItem("dagger", 13, 6, 33, ItemRarity.COMMON, null);
        addItem("boots", 14, 1, 34, ItemRarity.UNCOMMON, null);
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
        itemModels.put("rejuvenation-bead", "minecraft:green_bundle");
        itemModels.put("faeri-charm", "minecraft:orange_bundle");
        itemModels.put("ruby-crystal", "minecraft:red_dye");
    }

    private void addItem(String itemId, int price, int limit, int slot, ItemRarity rarity, String group) {
        itemPrices.put(itemId, price);
        itemLimits.put(itemId, limit);
        itemSlots.put(itemId, slot);
        itemRarities.put(itemId, rarity);
        if (group != null) {
            itemGroups.put(itemId, group);
        }
    }

    public int getPrice(String itemId) {
        return itemPrices.getOrDefault(itemId, 0);
    }

    public int getLimit(String itemId) {
        return itemLimits.getOrDefault(itemId, 1);
    }

    public int getSlot(String itemId) {
        return itemSlots.getOrDefault(itemId, -1);
    }

    public boolean hasItem(String itemId) {
        return itemPrices.containsKey(itemId);
    }

    public boolean hasCustomSlot(String itemId) {
        return itemSlots.containsKey(itemId);
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

    public boolean hasRarity(String itemId) {
        return itemRarities.containsKey(itemId);
    }

    public String getGroup(String itemId) {
        return itemGroups.get(itemId);
    }

    public boolean hasGroup(String itemId) {
        return itemGroups.containsKey(itemId);
    }
}