package dev.ixpu.leaguemechanics.manager;

import dev.ixpu.leaguemechanics.util.ItemLoreModifier;

import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ItemStatsManager {

    public int countLeagueItems(Player player) {
        int count = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && !item.getType().isAir()) {
                String itemId = ItemLoreModifier.getItemId(item);
                if (itemId != null) {
                    count++;
                }
            }
        }
        return count;
    }

    public double getItemHP(Player player) {
        double itemHP = 0;

        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && !item.getType().isAir()) {
                String itemId = ItemLoreModifier.getItemId(item);
                if (itemId != null) {
                    itemHP += ItemLoreModifier.getStat(item, "HP");
                }
            }
        }
        return itemHP;
    }

    public double getItemHR(Player player) {
        double itemHR = 0;

        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && !item.getType().isAir()) {
                String itemId = ItemLoreModifier.getItemId(item);
                if (itemId != null) {
                    itemHR += ItemLoreModifier.getStat(item, "HR");
                }
            }
        }
        return itemHR;
    }


    public double getItemAD(Player player) {
        double itemAD = 0;

        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && !item.getType().isAir()) {
                String itemId = ItemLoreModifier.getItemId(item);
                if (itemId != null) {
                    itemAD += ItemLoreModifier.getStat(item, "AD");
                }
            }
        }
        return itemAD;
    }

    public double getItemAP(Player player) {
        double itemAP = 0;

        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && !item.getType().isAir()) {
                String itemId = ItemLoreModifier.getItemId(item);
                if (itemId != null) {
                    itemAP += ItemLoreModifier.getStat(item, "AP");
                }
            }
        }
        return itemAP;
    }

    public double getItemTD(Player player) {
        double itemTD = 0;

        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && !item.getType().isAir()) {
                String itemId = ItemLoreModifier.getItemId(item);
                if (itemId != null) {
                    itemTD += ItemLoreModifier.getStat(item, "TD");
                }
            }
        }
        return itemTD;
    }

    public double getItemAS(Player player) {
        double itemAS = 0;

        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && !item.getType().isAir()) {
                String itemId = ItemLoreModifier.getItemId(item);
                if (itemId != null) {
                    itemAS += ItemLoreModifier.getStat(item, "AS");
                }
            }
        }
        return itemAS;
    }

    public double getItemAR(Player player) {
        double itemAR = 0;

        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && !item.getType().isAir()) {
                String itemId = ItemLoreModifier.getItemId(item);
                if (itemId != null) {
                    itemAR += ItemLoreModifier.getStat(item, "AR");
                }
            }
        }
        return itemAR;
    }

    public double getItemMR(Player player) {
        double totalMR = 0;

        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && !item.getType().isAir()) {
                String itemId = ItemLoreModifier.getItemId(item);
                if (itemId != null) {
                    totalMR += ItemLoreModifier.getStat(item, "MR");
                }
            }
        }
        return totalMR;
    }

    public double getItemSR(Player player) {
        double itemSR = 0;

        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && !item.getType().isAir()) {
                String itemId = ItemLoreModifier.getItemId(item);
                if (itemId != null) {
                    itemSR += ItemLoreModifier.getStat(item, "SR");
                }
            }
        }
        return itemSR;
    }

    public double getItemLS(Player player) {
        double itemLS = 0;

        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && !item.getType().isAir()) {
                String itemId = ItemLoreModifier.getItemId(item);
                if (itemId != null) {
                    itemLS += ItemLoreModifier.getStat(item, "LS");
                }
            }
        }
        return itemLS;
    }

    public double getItemCC(Player player) {
        double itemCC = 0;

        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && !item.getType().isAir()) {
                String itemId = ItemLoreModifier.getItemId(item);
                if (itemId != null) {
                    itemCC += ItemLoreModifier.getStat(item, "CC");
                }
            }
        }
        return itemCC;
    }

    public double getItemMS(Player player) {
        double itemMS = 0;

        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && !item.getType().isAir()) {
                String itemId = ItemLoreModifier.getItemId(item);
                if (itemId != null) {
                    itemMS += ItemLoreModifier.getStat(item, "MS");
                }
            }
        }
        return itemMS;
    }

    public double getItemAPen(Player player) {
        double ItemAPen = 0;

        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && !item.getType().isAir()) {
                String itemId = ItemLoreModifier.getItemId(item);
                if (itemId != null) {
                    ItemAPen += ItemLoreModifier.getStat(item, "APEN");
                }
            }
        }
        return ItemAPen;
    }

    public double getItemMPen(Player player) {
        double ItemMPen = 0;

        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && !item.getType().isAir()) {
                String itemId = ItemLoreModifier.getItemId(item);
                if (itemId != null) {
                    ItemMPen += ItemLoreModifier.getStat(item, "MPEN");
                }
            }
        }
        return ItemMPen;
    }
}