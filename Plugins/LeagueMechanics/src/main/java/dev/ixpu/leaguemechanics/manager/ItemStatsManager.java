package dev.ixpu.leaguemechanics.manager;

import dev.ixpu.leaguemechanics.util.ItemStatHelper;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Set;

public class ItemStatsManager {

    public double getItemAP(Player player) {
        double totalAP = 0;
        Set<String> countedItems = new HashSet<>();

        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && !item.getType().isAir()) {
                String itemId = ItemStatHelper.getItemId(item);
                if (itemId != null && !countedItems.contains(itemId)) {
                    totalAP += ItemStatHelper.getStat(item, "AP");
                    countedItems.add(itemId);
                }
            }
        }
        return totalAP;
    }

    public double getItemAD(Player player) {
        double totalAD = 0;
        Set<String> countedItems = new HashSet<>();

        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && !item.getType().isAir()) {
                String itemId = ItemStatHelper.getItemId(item);
                if (itemId != null && !countedItems.contains(itemId)) {
                    totalAD += ItemStatHelper.getStat(item, "AD");
                    countedItems.add(itemId);
                }
            }
        }
        return totalAD;
    }

    public double getItemAR(Player player) {
        double totalAR = 0;
        Set<String> countedItems = new HashSet<>();

        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && !item.getType().isAir()) {
                String itemId = ItemStatHelper.getItemId(item);
                if (itemId != null && !countedItems.contains(itemId)) {
                    totalAR += ItemStatHelper.getStat(item, "AR");
                    countedItems.add(itemId);
                }
            }
        }
        return totalAR;
    }

    public double getItemMR(Player player) {
        double totalMR = 0;
        Set<String> countedItems = new HashSet<>();

        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && !item.getType().isAir()) {
                String itemId = ItemStatHelper.getItemId(item);
                if (itemId != null && !countedItems.contains(itemId)) {
                    totalMR += ItemStatHelper.getStat(item, "MR");
                    countedItems.add(itemId);
                }
            }
        }
        return totalMR;
    }
}