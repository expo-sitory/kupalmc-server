package dev.ixpu.leaguemechanics.manager;

import dev.ixpu.leaguemechanics.util.ItemLoreModifier;

import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Set;

public class ItemStatsManager {

    public double getItemHP(Player player) {
        double itemHP = 0;
        Set<String> countedItems = new HashSet<>();

        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && !item.getType().isAir()) {
                String itemId = ItemLoreModifier.getItemId(item);
                if (itemId != null && !countedItems.contains(itemId)) {
                    itemHP += ItemLoreModifier.getStat(item, "HP");
                    countedItems.add(itemId);
                }
            }
        }
        return itemHP;
    }

    public double getItemHR(Player player) {
        double itemHR = 0;
        Set<String> countedItems = new HashSet<>();

        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && !item.getType().isAir()) {
                String itemId = ItemLoreModifier.getItemId(item);
                if (itemId != null && !countedItems.contains(itemId)) {
                    itemHR += ItemLoreModifier.getStat(item, "HR");
                    countedItems.add(itemId);
                }
            }
        }
        return itemHR;
    }


    public double getItemAD(Player player) {
        double itemAD = 0;
        Set<String> countedItems = new HashSet<>();

        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && !item.getType().isAir()) {
                String itemId = ItemLoreModifier.getItemId(item);
                if (itemId != null && !countedItems.contains(itemId)) {
                    itemAD += ItemLoreModifier.getStat(item, "AD");
                    countedItems.add(itemId);
                }
            }
        }
        return itemAD;
    }

    public double getItemAP(Player player) {
        double itemAP = 0;
        Set<String> countedItems = new HashSet<>();

        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && !item.getType().isAir()) {
                String itemId = ItemLoreModifier.getItemId(item);
                if (itemId != null && !countedItems.contains(itemId)) {
                    itemAP += ItemLoreModifier.getStat(item, "AP");
                    countedItems.add(itemId);
                }
            }
        }
        return itemAP;
    }

    public double getItemTD(Player player) {
        double itemTD = 0;
        Set<String> countedItems = new HashSet<>();

        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && !item.getType().isAir()) {
                String itemId = ItemLoreModifier.getItemId(item);
                if (itemId != null && !countedItems.contains(itemId)) {
                    itemTD += ItemLoreModifier.getStat(item, "TD");
                    countedItems.add(itemId);
                }
            }
        }
        return itemTD;
    }

    public double getItemAS(Player player) {
        double itemAS = 0;
        Set<String> countedItems = new HashSet<>();

        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && !item.getType().isAir()) {
                String itemId = ItemLoreModifier.getItemId(item);
                if (itemId != null && !countedItems.contains(itemId)) {
                    itemAS += ItemLoreModifier.getStat(item, "AS");
                    countedItems.add(itemId);
                }
            }
        }
        return itemAS;
    }

    public double getItemAR(Player player) {
        double itemAR = 0;
        Set<String> countedItems = new HashSet<>();

        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && !item.getType().isAir()) {
                String itemId = ItemLoreModifier.getItemId(item);
                if (itemId != null && !countedItems.contains(itemId)) {
                    itemAR += ItemLoreModifier.getStat(item, "AR");
                    countedItems.add(itemId);
                }
            }
        }
        return itemAR;
    }

    public double getItemMR(Player player) {
        double totalMR = 0;
        Set<String> countedItems = new HashSet<>();

        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && !item.getType().isAir()) {
                String itemId = ItemLoreModifier.getItemId(item);
                if (itemId != null && !countedItems.contains(itemId)) {
                    totalMR += ItemLoreModifier.getStat(item, "MR");
                    countedItems.add(itemId);
                }
            }
        }
        return totalMR;
    }

    public double getItemSR(Player player) {
        double itemSR = 0;
        Set<String> countedItems = new HashSet<>();

        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && !item.getType().isAir()) {
                String itemId = ItemLoreModifier.getItemId(item);
                if (itemId != null && !countedItems.contains(itemId)) {
                    itemSR += ItemLoreModifier.getStat(item, "SR");
                    countedItems.add(itemId);
                }
            }
        }
        return itemSR;
    }

    public double getItemLS(Player player) {
        double itemLS = 0;
        Set<String> countedItems = new HashSet<>();

        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && !item.getType().isAir()) {
                String itemId = ItemLoreModifier.getItemId(item);
                if (itemId != null && !countedItems.contains(itemId)) {
                    itemLS += ItemLoreModifier.getStat(item, "LS");
                    countedItems.add(itemId);
                }
            }
        }
        return itemLS;
    }

    public double getItemCC(Player player) {
        double itemCC = 0;
        Set<String> countedItems = new HashSet<>();

        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && !item.getType().isAir()) {
                String itemId = ItemLoreModifier.getItemId(item);
                if (itemId != null && !countedItems.contains(itemId)) {
                    itemCC += ItemLoreModifier.getStat(item, "CC");
                    countedItems.add(itemId);
                }
            }
        }
        return itemCC;
    }

    public double getItemMS(Player player) {
        double itemMS = 0;
        Set<String> countedItems = new HashSet<>();

        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && !item.getType().isAir()) {
                String itemId = ItemLoreModifier.getItemId(item);
                if (itemId != null && !countedItems.contains(itemId)) {
                    itemMS += ItemLoreModifier.getStat(item, "MS");
                    countedItems.add(itemId);
                }
            }
        }
        return itemMS;
    }

    public double getItemAPen(Player player) {
        double ItemAPen = 0;
        Set<String> countedItems = new HashSet<>();

        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && !item.getType().isAir()) {
                String itemId = ItemLoreModifier.getItemId(item);
                if (itemId != null && !countedItems.contains(itemId)) {
                    ItemAPen += ItemLoreModifier.getStat(item, "APEN");
                    countedItems.add(itemId);
                }
            }
        }
        return ItemAPen;
    }

    public double getItemMPen(Player player) {
        double ItemMPen = 0;
        Set<String> countedItems = new HashSet<>();

        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && !item.getType().isAir()) {
                String itemId = ItemLoreModifier.getItemId(item);
                if (itemId != null && !countedItems.contains(itemId)) {
                    ItemMPen += ItemLoreModifier.getStat(item, "MPEN");
                    countedItems.add(itemId);
                }
            }
        }
        return ItemMPen;
    }
}