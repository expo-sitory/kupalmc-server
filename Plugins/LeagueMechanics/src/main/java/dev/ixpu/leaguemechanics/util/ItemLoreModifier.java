package dev.ixpu.leaguemechanics.util;

import dev.ixpu.leaguemechanics.item.ItemStatsData;
import dev.ixpu.leaguemechanics.item.ItemStatsRegistry;
import dev.ixpu.leaguemechanics.item.ItemPassive;
import dev.ixpu.leaguemechanics.item.ItemPassivesRegistry;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

public class ItemLoreModifier {
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

        ItemStatsRegistry statData = ItemStatsData.getInstance().getItem(itemId);
        if (statData == null) return;

        updateItemLore(item, statData);
    }

    private static void updateItemLore(ItemStack item, ItemStatsRegistry statData) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        List<String> lore = new ArrayList<>();

        if (statData.getAd() > 0) {
            lore.add("§6🗡 " + formatStat(statData.getAd()) + " §fAttack Damage");
        }
        if (statData.getAp() > 0) {
            lore.add("§9☄ " + formatStat(statData.getAp()) + " §fAbility Power");
        }
        if (statData.getAr() > 0) {
            lore.add("§e🛡 " + formatStat(statData.getAr()) + " §fArmor");
        }
        if (statData.getMr() > 0) {
            lore.add("§b⦿ " + formatStat(statData.getMr()) + " §fMagic Resist");
        }
        if (statData.getHp() > 0) {
            lore.add("§a❤ " + formatStat(statData.getHp()) + " §fHealth");
        }
        if (statData.getHr() > 0) {
            lore.add("§2❣ " + formatStat(statData.getHr()) + " §fHealth Regen per 15 sec.");
        }
        if (statData.getSr() > 0) {
            lore.add("§6🍖 " + formatStat(statData.getSr()) + " §fSaturation Regen per 25 sec.");
        }
        if (statData.getAs() > 0) {
            lore.add("§c⚔ " + formatStat(statData.getAs()) + "% §fAttack Speed");
        }
        if (statData.getMs() > 0) {
            lore.add("§7👣 " + formatStat(statData.getMs()) + "% §fMovement Speed");
        }

        if (statData.hasPassive()) {
            ItemPassive passive = ItemPassivesRegistry.getInstance().getPassive(statData.getPassiveId());
            if (passive != null) {
                lore.add("");
                for (String line : passive.getDescription().split("\n")) {
                    lore.add(line);
                }
            }
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

        ItemStatsRegistry statData = ItemStatsData.getInstance().getItem(itemId);
        if (statData == null) return 0;

        return switch (statType.toUpperCase()) {
            case "HP" -> statData.getHp();
            case "HR" -> statData.getHr();

            case "AD" -> statData.getAd();
            case "AP" -> statData.getAp();

            case "TD" -> statData.getTd();
            case "AS" -> statData.getAs();
            case "AR" -> statData.getAr();
            case "MR" -> statData.getMr();
            case "LS" -> statData.getLs();
            case "CC" -> statData.getCc();
            case "SR" -> statData.getSr();
            case "MS" -> statData.getMs();

            default -> 0;
        };
    }
}