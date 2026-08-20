package dev.ixpu.leaguemechanics.manager;

import dev.ixpu.leaguemechanics.util.ItemLoreModifier;

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

    private double getStatWithLimit(Player player, String statType) {
        double total = 0;
        int count = 0;

        for (ItemStack item : player.getInventory().getContents()) {
            if (count >= 6) break;

            if (item != null && !item.getType().isAir()) {
                String itemId = ItemLoreModifier.getItemId(item);
                if (itemId != null) {
                    total += ItemLoreModifier.getStat(item, statType);
                    count++;
                }
            }
        }
        return total;
    }

    public double getItemHP(Player player) {
        return getStatWithLimit(player, "HP");
    }

    public double getItemHR(Player player) {
        return getStatWithLimit(player, "HR");
    }

    public double getItemAD(Player player) {
        return getStatWithLimit(player, "AD");
    }

    public double getItemAP(Player player) {
        return getStatWithLimit(player, "AP");
    }

    public double getItemTD(Player player) {
        return getStatWithLimit(player, "TD");
    }

    public double getItemAS(Player player) {
        return getStatWithLimit(player, "AS");
    }

    public double getItemAR(Player player) {
        return getStatWithLimit(player, "AR");
    }

    public double getItemMR(Player player) {
        return getStatWithLimit(player, "MR");
    }

    public double getItemSR(Player player) {
        return getStatWithLimit(player, "SR");
    }

    public double getItemLS(Player player) {
        return getStatWithLimit(player, "LS");
    }

    public double getItemCC(Player player) {
        return getStatWithLimit(player, "CC");
    }

    public double getItemMS(Player player) {
        return getStatWithLimit(player, "MS");
    }

    public double getItemAPen(Player player) {
        return getStatWithLimit(player, "APEN");
    }

    public double getItemMPen(Player player) {
        return getStatWithLimit(player, "MPEN");
    }
}