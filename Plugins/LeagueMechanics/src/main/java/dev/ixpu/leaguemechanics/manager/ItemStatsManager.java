package dev.ixpu.leaguemechanics.manager;

import dev.ixpu.leaguemechanics.util.ItemStatHelper;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ItemStatsManager {

    public double getItemAP(Player player) {
        double totalAP = 0;
        if (!ItemStatHelper.isBuildMode(player.getInventory().getItemInMainHand())) {
            totalAP += ItemStatHelper.getStat(player.getInventory().getItemInMainHand(), "AP");
        }
        if (!ItemStatHelper.isBuildMode(player.getInventory().getItemInOffHand())) {
            totalAP += ItemStatHelper.getStat(player.getInventory().getItemInOffHand(), "AP");
        }

        for (ItemStack armor : player.getInventory().getArmorContents()) {
            if (!ItemStatHelper.isBuildMode(armor)) {
                totalAP += ItemStatHelper.getStat(armor, "AP");
            }
        }

        return totalAP;
    }

    public double getItemAD(Player player) {
        double totalAD = 0;
        if (!ItemStatHelper.isBuildMode(player.getInventory().getItemInMainHand())) {
            totalAD += ItemStatHelper.getStat(player.getInventory().getItemInMainHand(), "AD");
        }
        if (!ItemStatHelper.isBuildMode(player.getInventory().getItemInOffHand())) {
            totalAD += ItemStatHelper.getStat(player.getInventory().getItemInOffHand(), "AD");
        }

        for (ItemStack armor : player.getInventory().getArmorContents()) {
            if (!ItemStatHelper.isBuildMode(armor)) {
                totalAD += ItemStatHelper.getStat(armor, "AD");
            }
        }

        return totalAD;
    }

    public double getItemAR(Player player) {
        double totalAR = 0;
        if (!ItemStatHelper.isBuildMode(player.getInventory().getItemInMainHand())) {
            totalAR += ItemStatHelper.getStat(player.getInventory().getItemInMainHand(), "AR");
        }
        if (!ItemStatHelper.isBuildMode(player.getInventory().getItemInOffHand())) {
            totalAR += ItemStatHelper.getStat(player.getInventory().getItemInOffHand(), "AR");
        }

        for (ItemStack armor : player.getInventory().getArmorContents()) {
            if (!ItemStatHelper.isBuildMode(armor)) {
                totalAR += ItemStatHelper.getStat(armor, "AR");
            }
        }

        return totalAR;
    }

    public double getItemMR(Player player) {
        double totalMR = 0;
        if (!ItemStatHelper.isBuildMode(player.getInventory().getItemInMainHand())) {
            totalMR += ItemStatHelper.getStat(player.getInventory().getItemInMainHand(), "MR");
        }
        if (!ItemStatHelper.isBuildMode(player.getInventory().getItemInOffHand())) {
            totalMR += ItemStatHelper.getStat(player.getInventory().getItemInOffHand(), "MR");
        }

        for (ItemStack armor : player.getInventory().getArmorContents()) {
            if (!ItemStatHelper.isBuildMode(armor)) {
                totalMR += ItemStatHelper.getStat(armor, "MR");
            }
        }

        return totalMR;
    }
}