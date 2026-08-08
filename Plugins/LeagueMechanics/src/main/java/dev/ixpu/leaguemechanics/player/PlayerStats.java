package dev.ixpu.leaguemechanics.player;

import dev.ixpu.leaguemechanics.LeagueMechanics;
import dev.ixpu.leaguemechanics.manager.ItemStatsManager;
import org.bukkit.entity.Player;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.inventory.ItemStack;
import org.bukkit.enchantments.Enchantment;

public class PlayerStats {
    double BASE_ATTACK_DAMAGE = 2.0;
    double BASE_PHYSICAL_ARMOR = 5.0;
    double BASE_ABILITY_POWER = 12.0;
    double BASE_MAGIC_RESIST = 5.0;

    public void tick(Player player) {
        getPlayerAD(player);

    }

    public double getPlayerWeaponSharpnessEnchant(Player player) {
        ItemStack weapon = player.getInventory().getItemInMainHand();
        if (weapon.getType() == Material.AIR) {
            return 0;
        }

        int sharpnessLevel = weapon.getEnchantmentLevel(Enchantment.SHARPNESS);
        if (sharpnessLevel == 0) {
            return 0;
        }
        return 0.5 + (sharpnessLevel * 0.5);
    }
    public double getPlayerArmorProtectionEnchant(Player player) {
        ItemStack helmet = player.getInventory().getHelmet();
        ItemStack chestplate = player.getInventory().getChestplate();
        ItemStack leggings = player.getInventory().getLeggings();
        ItemStack boots = player.getInventory().getBoots();

        int totalBonusProtectionLevel = 0;

        if (helmet != null && helmet.getType() != Material.AIR) {
            totalBonusProtectionLevel += helmet.getEnchantmentLevel(Enchantment.PROTECTION);
        }
        if (chestplate != null && chestplate.getType() != Material.AIR) {
            totalBonusProtectionLevel += chestplate.getEnchantmentLevel(Enchantment.PROTECTION);
        }
        if (leggings != null && leggings.getType() != Material.AIR) {
            totalBonusProtectionLevel += leggings.getEnchantmentLevel(Enchantment.PROTECTION);
        }
        if (boots != null && boots.getType() != Material.AIR) {
            totalBonusProtectionLevel += boots.getEnchantmentLevel(Enchantment.PROTECTION);
        }

        return totalBonusProtectionLevel * 0.4;
    }

    public double getPlayerAD(Player player) {
        double totalAD = 0;
        ItemStatsManager itemStatsManager = LeagueMechanics.getInstance().getStatsManager();
        if (itemStatsManager != null) {
            totalAD = BASE_ATTACK_DAMAGE + itemStatsManager.getItemAD(player);
        }
        return totalAD;
    }

    public double getPlayerAR(Player player) {
        double totalAR = 0;
        ItemStatsManager itemStatsManager = LeagueMechanics.getInstance().getStatsManager();
        if (itemStatsManager != null) {
            totalAR = BASE_PHYSICAL_ARMOR + itemStatsManager.getItemAR(player);
        }
        return totalAR;
    }

    public double getPlayerAP(Player player) {
        ItemStatsManager itemStatsManager = LeagueMechanics.getInstance().getStatsManager();
        double totalAP = 0;
        if (itemStatsManager != null) {
            totalAP = BASE_ABILITY_POWER + itemStatsManager.getItemAP(player);
        }
        return totalAP;
    }

    public double getPlayerMR(Player player) {
        ItemStatsManager itemStatsManager = LeagueMechanics.getInstance().getStatsManager();
        double totalMR = 0;
        if (itemStatsManager != null) {
            totalMR = BASE_MAGIC_RESIST + itemStatsManager.getItemMR(player);
        }
        return totalMR;
    }


    public String getActionBarSections(Player player) {
        var playerAD = player.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE);
        var playerAR = player.getAttribute(Attribute.GENERIC_ARMOR);
        assert playerAD != null;
        assert playerAR != null;

        double SHARPNESS = getPlayerWeaponSharpnessEnchant(player);
        double PROTECTION = getPlayerArmorProtectionEnchant(player);


        double AD = getPlayerAD(player) + playerAD.getValue();
        double AR = getPlayerAR(player) + playerAR.getValue();
        double AP = getPlayerAP(player);
        double MR = getPlayerMR(player);


        String adDisplay = " §6🗡 §f" + String.format("%.1f", AD);
        if (SHARPNESS > 0) {
            adDisplay = " §6🗡 §f" + String.format("%.1f", AD) + "§f(+" + String.format("%.1f", SHARPNESS) + ")";
        }
        String arDisplay = " §e🛡 §f" + String.format("%.1f", AR);
        if (PROTECTION > 0) {
            arDisplay = " §e🛡 §f" + String.format("%.1f", AR) + " §f(+" + String.format("%.1f", PROTECTION) + ")";
        }

        String apDisplay = " §9☄ §f" + String.format("%.1f", AP);
        String mrDisplay = " §b⦿ §f" + String.format("%.1f", MR) + ")";

        return adDisplay + arDisplay + apDisplay + mrDisplay;
    }
}