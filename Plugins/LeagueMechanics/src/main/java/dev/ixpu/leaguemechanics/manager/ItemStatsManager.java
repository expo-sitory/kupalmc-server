package dev.ixpu.leaguemechanics.manager;

import dev.ixpu.leaguemechanics.util.ItemStatHelper;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ItemStatsManager {

    public double getItemAP(Player player) {
        double totalAP = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && !item.getType().isAir()) {
                totalAP += ItemStatHelper.getStat(item, "AP");
            }
        }
        return totalAP;
    }

    public double getItemAD(Player player) {
        double totalAD = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && !item.getType().isAir()) {
                totalAD += ItemStatHelper.getStat(item, "AD");
            }
        }
        return totalAD;
    }

    public double getItemAR(Player player) {
        double totalAR = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && !item.getType().isAir()) {
                totalAR += ItemStatHelper.getStat(item, "AR");
            }
        }
        return totalAR;
    }

    public double getItemMR(Player player) {
        double totalMR = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && !item.getType().isAir()) {
                totalMR += ItemStatHelper.getStat(item, "MR");
            }
        }
        return totalMR;
    }
}