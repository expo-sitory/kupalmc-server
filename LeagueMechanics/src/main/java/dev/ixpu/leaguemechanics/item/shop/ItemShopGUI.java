package dev.ixpu.leaguemechanics.item.shop;

import dev.ixpu.leaguemechanics.item.passives.ItemPassive;
import dev.ixpu.leaguemechanics.item.passives.ItemPassivesRegistry;
import dev.ixpu.leaguemechanics.util.ItemModifier;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class ItemShopGUI {
    private static ItemShopGUI instance;
    private static final int INVENTORY_SIZE = 54;
    private static final String INVENTORY_TITLE = "§8§lɪᴛᴇᴍ ꜱʜᴏᴘ";

    private static final int CATEGORY_START = 0;
    private static final int CATEGORY_END = 8;
    private static final int FILLER_START = 9;
    private static final int FILLER_END = 15;
    private static final int ITEMS_START = 16;
    private static final int ITEMS_PER_PAGE = 38;

    private static final String CATEGORY_ALL = "all";
    private static final String CATEGORY_MAIN = "main";
    private static final String CATEGORY_MAGE = "mage";
    private static final String CATEGORY_FIGHTER = "fighter";
    private static final String CATEGORY_TANK = "tank";

    private final Map<UUID, Integer> playerPage = new HashMap<>();
    private final Map<UUID, String> playerCategory = new HashMap<>();

    public static ItemShopGUI getInstance() {
        if (instance == null) {
            instance = new ItemShopGUI();
        }
        return instance;
    }

    public static void openShop(Player player) {
        getInstance().openShopInstance(player);
    }

    public static void updateShopDisplay(Player player) {
        getInstance().updateShopDisplayInstance(player);
    }

    public static String getInventoryTitle() {
        return INVENTORY_TITLE;
    }

    public void openShopInstance(Player player) {
        Inventory shopInventory = Bukkit.createInventory(null, INVENTORY_SIZE, INVENTORY_TITLE);

        playerCategory.putIfAbsent(player.getUniqueId(), CATEGORY_ALL);
        playerPage.putIfAbsent(player.getUniqueId(), 0);

        buildGUI(shopInventory, player);

        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.5f);
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.5f);
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 1.0f);
        player.openInventory(shopInventory);
    }

    private void buildGUI(Inventory inventory, Player player) {
        String category = playerCategory.getOrDefault(player.getUniqueId(), CATEGORY_ALL);
        int currentPage = playerPage.getOrDefault(player.getUniqueId(), 0);

        setCategoryButtons(inventory, category);
        setFillers(inventory);
        setItems(inventory, player, category, currentPage);
    }

    private void setCategoryButtons(Inventory inventory, String activeCategory) {
        int[] categorySlots = {0, 1, 2, 3, 4, 5, 6, 7, 8};

        String[][] categories = {
            {CATEGORY_ALL, "§f§lᴀʟʟ", "§7View all items", "HOPPER"},
            {CATEGORY_MAIN, "§a§lꜱᴛᴀʀᴛᴇʀ", "§7Starting items", "WHEAT"},
            {CATEGORY_MAGE, "§9§lᴍᴀɢᴇ", "§7AP items", "BOOK"},
            {CATEGORY_FIGHTER, "§c§lғɪɢʜᴛᴇʀ", "§7AD items", "IRON_SWORD"},
            {CATEGORY_TANK, "§e§lᴛᴀɴᴋ", "§7Defensive items", "SHIELD"},
            {"page_prev", "§c§l◀", "§7Previous page", "ARROW"},
            {"page_1", "§f§l1", "§7Page 1", "PAPER"},
            {"page_2", "§f§l2", "§7Page 2", "PAPER"},
            {"page_next", "§a§l▶", "§7Next page", "ARROW"}
        };

        for (int i = 0; i < categories.length && i < categorySlots.length; i++) {
            String[] cat = categories[i];
            String categoryId = cat[0];
            String displayName = cat[1];
            String description = cat[2];
            String materialName = cat[3];

            Material material;
            try {
                material = Material.valueOf(materialName);
            } catch (IllegalArgumentException e) {
                material = Material.PAPER;
            }

            boolean isActive = categoryId.equals(activeCategory);
            boolean isPageButton = categoryId.startsWith("page_") || categoryId.equals("page_prev") || categoryId.equals("page_next");

            ItemStack item = new ItemStack(material);
            ItemMeta meta = item.getItemMeta();

            if (meta != null) {
                if (isActive && !isPageButton) {
                    meta.setDisplayName(displayName + " §a✓");
                } else {
                    meta.setDisplayName(displayName);
                }

                List<String> lore = new ArrayList<>();
                lore.add(description);
                if (isActive && !isPageButton) {
                    lore.add("§a§l▶ ᴄᴜʀʀᴇɴᴛ");
                }
                meta.setLore(lore);

                item.setItemMeta(meta);
                inventory.setItem(categorySlots[i], item);
            }
        }
    }

    private void setFillers(Inventory inventory) {
        ItemStack filler = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta meta = filler.getItemMeta();

        if (meta != null) {
            meta.setDisplayName("§r");
            filler.setItemMeta(meta);
        }

        for (int slot = FILLER_START; slot <= FILLER_END; slot++) {
            inventory.setItem(slot, filler);
        }
    }

    private void setItems(Inventory inventory, Player player, String category, int page) {
        ItemShopRegistry registry = ItemShopRegistry.getInstance();
        ItemShopData shopData = ItemShopData.getInstance();

        List<ItemShopRegistry.ShopItem> filteredItems = getSortedItems(registry.getAllShopItems(), category, shopData);

        int totalPages = Math.max(1, (int) Math.ceil((double) filteredItems.size() / ITEMS_PER_PAGE));
        int safePage = Math.min(page, totalPages - 1);
        int startIndex = safePage * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, filteredItems.size());

        for (int slot = ITEMS_START; slot < INVENTORY_SIZE; slot++) {
            inventory.setItem(slot, null);
        }

        int itemIndex = 0;
        for (int i = startIndex; i < endIndex; i++) {
            int slot = ITEMS_START + itemIndex;

            if (slot >= INVENTORY_SIZE) break;

            ItemShopRegistry.ShopItem shopItem = filteredItems.get(i);
            ItemStack displayItem = createItemDisplay(shopItem, player);
            inventory.setItem(slot, displayItem);
            itemIndex++;
        }
    }

    private List<ItemShopRegistry.ShopItem> getSortedItems(List<ItemShopRegistry.ShopItem> allItems,
                                                          String category, ItemShopData shopData) {
        List<ItemShopRegistry.ShopItem> filtered;

        if (category == null || category.equals(CATEGORY_ALL)) {
            filtered = new ArrayList<>(allItems);
        } else {
            filtered = new ArrayList<>();
            for (ItemShopRegistry.ShopItem item : allItems) {
                String itemCategory = shopData.getCategory(item.getId());
                if (category.equals(itemCategory)) {
                    filtered.add(item);
                }
            }
        }

        filtered.sort((a, b) -> {
            if (category == null || category.equals(CATEGORY_ALL)) {
                int catOrderA = getCategoryDisplayOrder(shopData.getCategory(a.getId()));
                int catOrderB = getCategoryDisplayOrder(shopData.getCategory(b.getId()));
                if (catOrderA != catOrderB) {
                    return Integer.compare(catOrderA, catOrderB);
                }
            }
            int orderA = shopData.getOrder(a.getId());
            int orderB = shopData.getOrder(b.getId());
            return Integer.compare(orderA, orderB);
        });

        return filtered;
    }

    private int getCategoryDisplayOrder(String category) {
        if (category == null) return 99;
        return switch (category) {
            case "main" -> 0;
            case "mage" -> 1;
            case "fighter" -> 2;
            case "tank" -> 3;
            default -> 99;
        };
    }

    private ItemStack createItemDisplay(ItemShopRegistry.ShopItem shopItem, Player player) {
        ItemStack item = new ItemStack(Material.NETHERITE_INGOT);

        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            String itemName = shopItem.getDisplayName();
            int price = shopItem.getPrice();

            String displayName = "§f" + itemName + " §a◎ " + price;
            meta.setDisplayName(displayName);

            boolean canBuy = canPlayerBuyItem(player, shopItem);

            if (canBuy) {
                meta.setEnchantmentGlintOverride(true);
            }

            List<String> lore = new ArrayList<>();

            if (shopItem.getStats().getAd() > 0) {
                lore.add("§6🗡 " + formatStat(shopItem.getStats().getAd()) + " §fAttack Damage");
            }
            if (shopItem.getStats().getAp() > 0) {
                lore.add("§9☄ " + formatStat(shopItem.getStats().getAp()) + " §fAbility Power");
            }
            if (shopItem.getStats().getAr() > 0) {
                lore.add("§e🛡 " + formatStat(shopItem.getStats().getAr()) + " §fArmor");
            }
            if (shopItem.getStats().getMr() > 0) {
                lore.add("§b⦿ " + formatStat(shopItem.getStats().getMr()) + " §fMagic Resist");
            }
            if (shopItem.getStats().getHp() > 0) {
                lore.add("§a❤ " + formatStat(shopItem.getStats().getHp()) + " §fHealth");
            }
            if (shopItem.getStats().getHr() > 0) {
                lore.add("§2❣ " + formatStat(shopItem.getStats().getHr()) + " §fHealth Regen per 15 sec.");
            }
            if (shopItem.getStats().getSr() > 0) {
                lore.add("§6🍖 " + formatStat(shopItem.getStats().getSr()) + " §fSaturation Regen per 25 sec.");
            }
            if (shopItem.getStats().getAs() > 0) {
                lore.add("§c➺ " + formatStat(shopItem.getStats().getAs()) + "% §fAttack Speed");
            }
            if (shopItem.getStats().getMs() > 0) {
                lore.add("§7👣 " + formatStat(shopItem.getStats().getMs()) + "% §fMovement Speed");
            }

            if (shopItem.getStats().hasPassive()) {
                ItemPassive passive = ItemPassivesRegistry.getInstance().getPassive(shopItem.getStats().getPassiveId());
                if (passive != null) {
                    lore.add("");
                    String[] passiveLines = passive.getDescription().split("\n");
                    for (String line : passiveLines) {
                        lore.add(line);
                    }
                }
            }
            lore.add("");
            if (!canBuy) {
                if (playerOwnsItem(player, shopItem)) {
                    lore.add("§7§lᴏᴡɴᴇᴅ");
                } else {
                    lore.add("§7§ʟᴏᴄᴋᴇᴅ");
                }
            } else {
                lore.add("§a§lᴘᴜʀᴄʜᴀꜱᴇ");
            }

            meta.setLore(lore);
            item.setItemMeta(meta);

            ItemShopData shopData = ItemShopData.getInstance();
            meta.setRarity(shopData.getRarity(shopItem.getId()));
            item.setItemMeta(meta);

            if (shopData.hasCustomModel(shopItem.getId())) {
                ItemModifier.setItemModel(item, shopData.getModel(shopItem.getId()));
            }
        }
        return item;
    }

    public boolean handleClick(Player player, int slot) {
        UUID playerId = player.getUniqueId();

        if (slot >= CATEGORY_START && slot <= CATEGORY_END) {
            int buttonIndex = slot - CATEGORY_START;

            String[] categoryIds = {CATEGORY_ALL, CATEGORY_MAIN, CATEGORY_MAGE, CATEGORY_FIGHTER, CATEGORY_TANK,
                                   "page_prev", "page_1", "page_2", "page_next"};

            if (buttonIndex < categoryIds.length) {
                String clicked = categoryIds[buttonIndex];

                if (clicked.startsWith("page_")) {
                    int currentPage = playerPage.getOrDefault(playerId, 0);
                    if (clicked.equals("page_prev")) {
                        playerPage.put(playerId, Math.max(0, currentPage - 1));
                    } else if (clicked.equals("page_next")) {
                        playerPage.put(playerId, currentPage + 1);
                    } else {
                        //
                        try {
                            int pageNum = Integer.parseInt(clicked.replace("page_", ""));
                            playerPage.put(playerId, pageNum - 1);
                        } catch (NumberFormatException ignored) {}
                    }
                    refreshGUI(player);
                } else {
                    playerCategory.put(playerId, clicked);
                    playerPage.put(playerId, 0);
                    refreshGUI(player);
                }
            }
            return true;
        }

        if (slot >= ITEMS_START && slot < INVENTORY_SIZE) {
            handleItemClick(player, slot);
            return true;
        }

        return false;
    }

    private void handleItemClick(Player player, int slot) {
        int itemIndex = slot - ITEMS_START;

        int currentPage = playerPage.getOrDefault(player.getUniqueId(), 0);
        String category = playerCategory.getOrDefault(player.getUniqueId(), CATEGORY_ALL);

        ItemShopRegistry registry = ItemShopRegistry.getInstance();
        ItemShopData shopData = ItemShopData.getInstance();
        List<ItemShopRegistry.ShopItem> sortedItems = getSortedItems(registry.getAllShopItems(), category, shopData);

        int actualIndex = (currentPage * ITEMS_PER_PAGE) + itemIndex;
        if (actualIndex < sortedItems.size()) {
            ItemShopRegistry.ShopItem clickedItem = sortedItems.get(actualIndex);
            handlePurchase(player, clickedItem);
        }
    }

    private void handlePurchase(Player player, ItemShopRegistry.ShopItem shopItem) {
        player.sendMessage("§aYou clicked: " + shopItem.getDisplayName());
    }

    private void refreshGUI(Player player) {
        org.bukkit.inventory.InventoryView view = player.getOpenInventory();

        if (view.getTitle().equals(INVENTORY_TITLE)) {
            Inventory inventory = view.getTopInventory();
            buildGUI(inventory, player);
        }
    }

    public void updateShopDisplayInstance(Player player) {
        refreshGUI(player);
    }

    private boolean canPlayerBuyItem(Player player, ItemShopRegistry.ShopItem shopItem) {
        return !playerOwnsItem(player, shopItem);
    }

    private boolean playerOwnsItem(Player player, ItemShopRegistry.ShopItem shopItem) {
        int ownedCount = 0;
        int limit = shopItem.getLimit();

        for (ItemStack inv : player.getInventory().getContents()) {
            if (inv != null && inv.hasItemMeta() && inv.getItemMeta().hasDisplayName()) {
                if (inv.getItemMeta().getDisplayName().contains(shopItem.getDisplayName())) {
                    ownedCount++;
                    if (ownedCount >= limit) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private String formatStat(double value) {
        if (value == (long) value) {
            return String.format("%d", (long) value);
        } else {
            return String.format("%.1f", value);
        }
    }

    public void cleanup(Player player) {
        playerPage.remove(player.getUniqueId());
        playerCategory.remove(player.getUniqueId());
    }

    public String getPlayerCategory(Player player) {
        return playerCategory.getOrDefault(player.getUniqueId(), CATEGORY_ALL);
    }
}
