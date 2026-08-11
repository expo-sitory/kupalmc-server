package dev.ixpu.leaguemechanics.util;

import dev.ixpu.leaguemechanics.item.ItemManager;
import dev.ixpu.leaguemechanics.item.ItemStatData;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

public class ItemStatHelper {
    private static NamespacedKey ITEM_ID_KEY;

    public static void initialize(Plugin plugin) {
        ITEM_ID_KEY = new NamespacedKey(plugin, "league_item_id");
    }

    public static void setItemId(ItemStack item, String itemId) {
        if (item == null || item.getType().isAir() || item.getItemMeta() == null) return;

        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer data = meta.getPersistentDataContainer();
        data.set(ITEM_ID_KEY, PersistentDataType.STRING, itemId);
        item.setItemMeta(meta);
    }

    public static String getItemId(ItemStack item) {
        if (item == null || item.getType().isAir() || item.getItemMeta() == null) return null;

        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer data = meta.getPersistentDataContainer();
        if (data.has(ITEM_ID_KEY, PersistentDataType.STRING)) {
            return data.get(ITEM_ID_KEY, PersistentDataType.STRING);
        }
        return null;
    }

    public static void syncItemStats(ItemStack item) {
        if (item == null || item.getType().isAir() || item.getItemMeta() == null) return;

        String itemId = getItemId(item);
        if (itemId == null) return;

        ItemStatData statData = ItemManager.getInstance().getItem(itemId);
        if (statData == null) return;

        updateItemLore(item, statData);
    }

    private static void updateItemLore(ItemStack item, ItemStatData statData) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        List<String> lore = new ArrayList<>();

        if (statData.getAd() > 0) {
            lore.add("§aAttack Damage: +" + formatStat(statData.getAd()));
        }
        if (statData.getAp() > 0) {
            lore.add("§aAbility Power: +" + formatStat(statData.getAp()));
        }
        if (statData.getAr() > 0) {
            lore.add("§aArmor: +" + formatStat(statData.getAr()));
        }
        if (statData.getMr() > 0) {
            lore.add("§aMagic Resist: +" + formatStat(statData.getMr()));
        }

        meta.setLore(lore);
        item.setItemMeta(meta);
    }

    private static String formatStat(double value) {
        if (value == (long) value) {
            return String.format("%d", (long) value);
        } else {
            return String.format("%.1f", value);
        }
    }

    public static double getStat(ItemStack item, String statType) {
        if (item == null || item.getType().isAir()) return 0;

        String itemId = getItemId(item);
        if (itemId == null) return 0;

        ItemStatData statData = ItemManager.getInstance().getItem(itemId);
        if (statData == null) return 0;

        return switch (statType.toUpperCase()) {
            case "AD" -> statData.getAd();
            case "AP" -> statData.getAp();
            case "AR" -> statData.getAr();
            case "MR" -> statData.getMr();
            default -> 0;
        };
    }
}