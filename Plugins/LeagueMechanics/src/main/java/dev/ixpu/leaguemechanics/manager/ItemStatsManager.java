package dev.ixpu.leaguemechanics.manager;

import dev.ixpu.leaguemechanics.util.ItemStatHelper;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ItemStatsManager {

    public double getItemAP(Player player) {
        double totalAP = 0;
        totalAP += ItemStatHelper.getStat(player.getInventory().getItemInMainHand(), "AP");
        totalAP += ItemStatHelper.getStat(player.getInventory().getItemInOffHand(), "AP");

        for (ItemStack armor : player.getInventory().getArmorContents()) {
            totalAP += ItemStatHelper.getStat(armor, "AP");
        }

        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null || item.getType().isAir()) continue;
            if (ItemStatHelper.isBuildMode(item)) {
                totalAP += ItemStatHelper.getStat(item, "AP");
            }
        }

        return totalAP;
    }

    public double getItemAD(Player player) {
        double totalAD = 0;
        totalAD += ItemStatHelper.getStat(player.getInventory().getItemInMainHand(), "AD");
        totalAD += ItemStatHelper.getStat(player.getInventory().getItemInOffHand(), "AD");

        for (ItemStack armor : player.getInventory().getArmorContents()) {
            totalAD += ItemStatHelper.getStat(armor, "AD");
        }

        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null || item.getType().isAir()) continue;
            if (ItemStatHelper.isBuildMode(item)) {
                totalAD += ItemStatHelper.getStat(item, "AD");
            }
        }

        return totalAD;
    }

    public double getItemAR(Player player) {
        double totalAR = 0;
        totalAR += ItemStatHelper.getStat(player.getInventory().getItemInMainHand(), "AR");
        totalAR += ItemStatHelper.getStat(player.getInventory().getItemInOffHand(), "AR");

        for (ItemStack armor : player.getInventory().getArmorContents()) {
            totalAR += ItemStatHelper.getStat(armor, "AR");
        }

        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null || item.getType().isAir()) continue;
            if (ItemStatHelper.isBuildMode(item)) {
                totalAR += ItemStatHelper.getStat(item, "AR");
            }
        }

        return totalAR;
    }

    public double getItemMR(Player player) {
        double totalMR = 0;
        totalMR += ItemStatHelper.getStat(player.getInventory().getItemInMainHand(), "MR");
        totalMR += ItemStatHelper.getStat(player.getInventory().getItemInOffHand(), "MR");

        for (ItemStack armor : player.getInventory().getArmorContents()) {
            totalMR += ItemStatHelper.getStat(armor, "MR");
        }

        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null || item.getType().isAir()) continue;
            if (ItemStatHelper.isBuildMode(item)) {
                totalMR += ItemStatHelper.getStat(item, "MR");
            }
        }

        return totalMR;
    }
}