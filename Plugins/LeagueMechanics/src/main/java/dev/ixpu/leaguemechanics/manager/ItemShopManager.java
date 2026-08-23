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
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemShopManager implements Listener {
    private static ItemShopManager instance;
    private static final String SHOP_INVENTORY_TITLE = "§8§lɪᴛᴇᴍ ꜱʜᴏᴘ";

    private ItemShopManager() {
    }

    public static ItemShopManager getInstance() {
        if (instance == null) {
            instance = new ItemShopManager();
        }
        return instance;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onShopInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        if (!event.getView().getTitle().equals(SHOP_INVENTORY_TITLE)) {
            return;
        }

        event.setCancelled(true);

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType().isAir()) {
            return;
        }

        int clickedSlot = event.getSlot();
        ItemShopRegistry registry = ItemShopRegistry.getInstance();
        ItemShopRegistry.ShopItem shopItem = null;
        ItemShopData shopData = ItemShopData.getInstance();

        for (ItemShopRegistry.ShopItem item : registry.getAllShopItems()) {
            int itemSlot = shopData.hasCustomSlot(item.getId())
                    ? shopData.getSlot(item.getId())
                    : -1;

            if (itemSlot == clickedSlot) {
                shopItem = item;
                break;
            }
        }

        if (shopItem != null) {
            if (purchaseItem(player, shopItem)) {
                ItemShopGUI.updateShopDisplay(player);
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
        if (playerOwnsItem(player, shopItem)) {
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
            meta.setDisplayName("§e" + shopItem.getDisplayName());
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
}