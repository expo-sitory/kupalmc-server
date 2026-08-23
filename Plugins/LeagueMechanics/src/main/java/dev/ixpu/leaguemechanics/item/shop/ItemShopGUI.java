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

import java.util.ArrayList;
import java.util.List;

public class ItemShopGUI {
    private static final int INVENTORY_SIZE = 54;
    private static final String INVENTORY_TITLE = "§8§lɪᴛᴇᴍ ꜱʜᴏᴘ";

    public static void openShop(Player player) {
        Inventory shopInventory = Bukkit.createInventory(null, INVENTORY_SIZE, INVENTORY_TITLE);

        ItemShopRegistry registry = ItemShopRegistry.getInstance();
        List<ItemShopRegistry.ShopItem> shopItems = registry.getAllShopItems();

        int slotIndex = 0;
        for (ItemShopRegistry.ShopItem shopItem : shopItems) {
            ItemShopData shopData = ItemShopData.getInstance();
            int displaySlot = shopData.hasCustomSlot(shopItem.getId())
                    ? shopData.getSlot(shopItem.getId())
                    : slotIndex;

            if (displaySlot >= INVENTORY_SIZE) break;

            ItemStack displayItem = createDisplayItem(shopItem, player);
            shopInventory.setItem(displaySlot, displayItem);
            slotIndex++;
        }
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.5f);
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.5f);
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 1.0f);
        player.openInventory(shopInventory);
    }

    private static ItemStack createDisplayItem(ItemShopRegistry.ShopItem shopItem, Player player) {
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
            } else if (playerOwnsConflictingItem(player, shopItem)) {
                //
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
                lore.add("§c⚔ " + formatStat(shopItem.getStats().getAs()) + "% §fAttack Speed");
            }
            if (shopItem.getStats().getMs() > 0) {
                lore.add("§7👣 " + formatStat(shopItem.getStats().getMs()) + "% §fMovement Speed");
            }

            if (shopItem.getStats().hasPassive()) {
                ItemPassive passive = ItemPassivesRegistry.getInstance().getPassive(shopItem.getStats().getPassiveId());
                if (passive != null) {
                    String[] passiveLines = passive.getDescription().split("\n");
                    for (String line : passiveLines) {
                        lore.add("\n");
                        lore.add(line);
                    }
                }
            }
            lore.add("\n");
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

    private static boolean canPlayerBuyItem(Player player, ItemShopRegistry.ShopItem shopItem) {
        return !playerOwnsItem(player, shopItem);
    }

    private static boolean playerOwnsItem(Player player, ItemShopRegistry.ShopItem shopItem) {
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

    public static void updateShopDisplay(Player player) {
        org.bukkit.inventory.InventoryView view = player.getOpenInventory();

        if (view.getTitle().equals(INVENTORY_TITLE)) {
            Inventory inventory = view.getTopInventory();
            ItemShopRegistry registry = ItemShopRegistry.getInstance();
            ItemShopData shopData = ItemShopData.getInstance();
            List<ItemShopRegistry.ShopItem> shopItems = registry.getAllShopItems();

            int slotIndex = 0;
            for (ItemShopRegistry.ShopItem shopItem : shopItems) {
                int displaySlot = shopData.hasCustomSlot(shopItem.getId())
                        ? shopData.getSlot(shopItem.getId())
                        : slotIndex;

                if (displaySlot >= INVENTORY_SIZE) break;

                ItemStack updatedItem = createDisplayItem(shopItem, player);
                inventory.setItem(displaySlot, updatedItem);
                slotIndex++;
            }
        }
    }

    private static String formatStat(double value) {
        if (value == (long) value) {
            return String.format("%d", (long) value);
        } else {
            return String.format("%.1f", value);
        }
    }

    private static boolean playerOwnsConflictingItem(Player player, ItemShopRegistry.ShopItem shopItem) {
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