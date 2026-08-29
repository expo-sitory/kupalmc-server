package dev.ixpu.leaguemechanics.manager;

import dev.ixpu.leaguemechanics.LeagueMechanics;
import dev.ixpu.leaguemechanics.item.shop.ItemShopData;
import dev.ixpu.leaguemechanics.item.shop.ItemShopGUI;
import dev.ixpu.leaguemechanics.item.shop.ItemShopRegistry;
import dev.ixpu.leaguemechanics.util.ItemModifier;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.List;
import java.util.UUID;

public class ItemShopManager implements Listener {
    private static ItemShopManager instance;
    public static final String SHOP_INVENTORY_TITLE = "§8§lɪᴛᴇᴍ ꜱʜᴏᴘ";

    private ItemShopManager() {
    }

    public static ItemShopManager getInstance() {
        if (instance == null) {
            instance = new ItemShopManager();
        }
        return instance;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onShopInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        if (!event.getView().getTitle().equals(SHOP_INVENTORY_TITLE)) {
            return;
        }
        ItemStack cursor = event.getCursor();
        ItemStack currentItem = event.getCurrentItem();
        InventoryType.SlotType slotType = event.getSlotType();

        if (event.getClickedInventory() == player.getInventory() && event.isShiftClick()
                && currentItem != null && !currentItem.getType().isAir()
                && ItemModifier.getItemId(currentItem) != null) {
            event.setCancelled(true);
            sellItem(player, currentItem, event.getSlot());
            ItemShopGUI.updateShopDisplay(player);
            return;
        }
        if (event.getClickedInventory() == player.getInventory()) {
            return;
        }
        if (event.getClickedInventory() == null || slotType == InventoryType.SlotType.OUTSIDE) {
            return;
        }
        event.setCancelled(true);

        if (currentItem == null || currentItem.getType().isAir()) {
            return;
        }

        int clickedSlot = event.getSlot();
        ItemShopRegistry registry = ItemShopRegistry.getInstance();
        ItemShopRegistry.ShopItem shopItem = null;
        ItemShopData shopData = ItemShopData.getInstance();

        String currentCategory = ItemShopGUI.getInstance().getPlayerCategory(player);

        List<ItemShopRegistry.ShopItem> sortedItems = new java.util.ArrayList<ItemShopRegistry.ShopItem>();
        for (ItemShopRegistry.ShopItem item : registry.getAllShopItems()) {
            if (currentCategory.equals("all") || currentCategory.equals(shopData.getCategory(item.getId()))) {
                sortedItems.add(item);
            }
        }

        final String cat = currentCategory;
        sortedItems.sort((a, b) -> {
            if (cat.equals("all")) {
                int catOrderA = getCategoryOrder(shopData.getCategory(a.getId()));
                int catOrderB = getCategoryOrder(shopData.getCategory(b.getId()));
                if (catOrderA != catOrderB) {
                    return Integer.compare(catOrderA, catOrderB);
                }
            }
            return Integer.compare(shopData.getOrder(a.getId()), shopData.getOrder(b.getId()));
        });

        int ITEMS_START = 16;
        if (clickedSlot >= ITEMS_START && clickedSlot < 54) {
            int itemIndex = clickedSlot - ITEMS_START;
            if (itemIndex < sortedItems.size()) {
                shopItem = sortedItems.get(itemIndex);
            }
        }

        if (shopItem != null) {
            if (purchaseItem(player, shopItem)) {
                ItemShopGUI.updateShopDisplay(player);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onShopInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        if (!event.getView().getTitle().equals(SHOP_INVENTORY_TITLE)) {
            return;
        }

        ItemStack cursor = event.getCursor();
        if (cursor == null || cursor.getType().isAir() || ItemModifier.getItemId(cursor) == null) {
            return;
        }
        for (int slot : event.getRawSlots()) {
            if (slot < 54) {
                event.setCancelled(true);
                return;
            }
        }
    }

    private boolean purchaseItem(Player player, ItemShopRegistry.ShopItem shopItem) {
        ItemStatsManager itemStatsManager = LeagueMechanics.getInstance().getStatsManager();
        int price = shopItem.getPrice();
        int playerLevel = player.getLevel();

        if (playerLevel < price) {
            player.sendMessage(Component.text("§cInsufficient levels."));
            return false;
        }
        if (playerOwnsConflictingItem(player, shopItem)) {
            ItemShopData shopData = ItemShopData.getInstance();
            String group = shopData.getGroup(shopItem.getId());
            player.sendMessage(Component.text("§cYou can only apply one (1) " + group + " item to your build"));
            return false;
        }
        if (!hasInventorySpace(player)) {
            player.sendMessage(Component.text("§cInventory full. Need space in main inventory to purchase."));
            return false;
        }
        if (itemStatsManager.countLeagueItems(player) > 5) {
            player.sendMessage(Component.text("§cLeague Items Count: 6/6"));
            return false;
        }
        if (playerOwnsItem(player, shopItem)) {
            return false;
        }

        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.5f);
        player.setLevel(playerLevel - price);
        ItemStack purchasedItem = createPurchaseItem(shopItem);
        addItemToInventory(player, purchasedItem);

        return true;
    }

    private ItemStack createPurchaseItem(ItemShopRegistry.ShopItem shopItem) {
        ItemStack item = new ItemStack(Material.NETHERITE_INGOT);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName("§f" + shopItem.getDisplayName());
            meta.setMaxStackSize(1);
            ItemShopData shopData = ItemShopData.getInstance();
            meta.setRarity(shopData.getRarity(shopItem.getId()));

            item.setItemMeta(meta);
        }
        ItemModifier.setItemId(item, shopItem.getId());
        ItemModifier.syncItemStats(item);

        ItemShopData shopData = ItemShopData.getInstance();
        if (shopData.hasCustomModel(shopItem.getId())) {
            ItemModifier.setItemModel(item, shopData.getModel(shopItem.getId()));
        }

        return item;
    }

    private boolean hasInventorySpace(Player player) {
        for (int i = 9; i < 36; i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (item == null || item.getType().isAir()) {
                return true;
            }
        }
        return false;
    }

    private void addItemToInventory(Player player, ItemStack item) {
        for (int i = 9; i < 36; i++) {
            ItemStack slot = player.getInventory().getItem(i);
            if (slot == null || slot.getType().isAir()) {
                player.getInventory().setItem(i, item);
                return;
            }
        }
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

    private boolean playerOwnsConflictingItem(Player player, ItemShopRegistry.ShopItem shopItem) {
        ItemShopData shopData = ItemShopData.getInstance();
        String itemGroup = shopData.getGroup(shopItem.getId());

        if (itemGroup == null) {
            return false;
        }

        for (ItemStack inv : player.getInventory().getContents()) {
            if (inv != null && !inv.getType().isAir()) {
                String itemId = ItemModifier.getItemId(inv);
                if (itemId != null && !itemId.equals(shopItem.getId())) {
                    String ownerGroup = shopData.getGroup(itemId);
                    if (ownerGroup != null && ownerGroup.equals(itemGroup)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private int getCategoryOrder(String category) {
        if (category == null) return 99;
        if (category.equals("main")) return 0;
        if (category.equals("mage")) return 1;
        if (category.equals("fighter")) return 2;
        if (category.equals("tank")) return 3;
        return 99;
    }

    public void sellItem(Player player, ItemStack item, int knownSlot) {
        String itemId = ItemModifier.getItemId(item);
        if (itemId == null) {
            return;
        }

        ItemShopRegistry registry = ItemShopRegistry.getInstance();
        ItemShopRegistry.ShopItem shopItem = registry.getShopItem(itemId);
        if (shopItem == null) {
            return;
        }

        int originalPrice = shopItem.getPrice();
        int refundAmount = (int) Math.floor(originalPrice * 0.70);

        if (knownSlot >= 0 && knownSlot < 36) {
            ItemStack inInv = player.getInventory().getItem(knownSlot);
            if (inInv != null && !inInv.getType().isAir()
                    && itemId.equals(ItemModifier.getItemId(inInv))) {
                if (inInv.getAmount() > 1) {
                    inInv.setAmount(inInv.getAmount() - 1);
                } else {
                    player.getInventory().clear(knownSlot);
                }
            }
        }

        player.setLevel(player.getLevel() + refundAmount);
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
        player.sendMessage(Component.text("§a✦ §fSold §a" + shopItem.getDisplayName() + " §ffor " + refundAmount + " levels §a(70%)"));

        LeagueMechanics plugin = LeagueMechanics.getInstance();
        if (plugin != null) {
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                UUID uuid = player.getUniqueId();
                dev.ixpu.leaguemechanics.player.PlayerStats.invalidateCache(uuid);
                dev.ixpu.leaguemechanics.manager.ItemStatsManager manager =
                        dev.ixpu.leaguemechanics.LeagueMechanics.getInstance().getStatsManager();
                if (manager != null) manager.invalidateCache(uuid);
            }, 1L);
        }
    }
}