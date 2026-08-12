package dev.ixpu.leaguemechanics.player;

import dev.ixpu.leaguemechanics.LeagueMechanics;
import dev.ixpu.leaguemechanics.manager.ItemStatsManager;
import org.bukkit.entity.Player;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.inventory.ItemStack;
import org.bukkit.enchantments.Enchantment;


public class PlayerStats {
    double BASE_ATTACK_DAMAGE = 0.0;
    double BASE_PHYSICAL_ARMOR = 0.0;
    double BASE_ABILITY_POWER = 12.0;
    double BASE_MAGIC_RESIST = 5.0;

    double BASE_BONUS_HEALTH = 0.0;
    double BASE_BONUS_ATTACK_SPEED = 0.0;
    double BASE_BONUS_HEALTH_REGEN = 0.0;
    double BASE_BONUS_SATURATION_REGEN = 0.0;


    public void tick(Player player) {
        getPlayerAD(player);
        getPlayerAP(player);
        getPlayerAR(player);
        getPlayerMR(player);
        getPlayerHP(player);
        getPlayerHR(player);
        getPlayerSR(player);
        getPlayerAS(player);
    }

    public double getPlayerWeapon(Player player) {
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

    private boolean isWeapon(ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return false;
        }

        String name = item.getType().toString().toLowerCase();
        return name.contains("sword") || name.contains("axe") || name.contains("trident") ||
                name.contains("pickaxe") || name.contains("shovel") || name.contains("hoe") ||
                name.contains("mace") || name.contains("spear");
    }

    public double getPlayerArmor(Player player) {
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
        ItemStack weapon = player.getInventory().getItemInMainHand();
        int baseAD = 0;
        if (isWeapon(weapon)) {
            baseAD++;
        }
        var itemHeldAD = player.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE);
        double enchantAD = getPlayerWeapon(player);
        double totalAD = BASE_ATTACK_DAMAGE + itemHeldAD.getValue();
        ItemStatsManager itemStatsManager = LeagueMechanics.getInstance().getStatsManager();
        if (itemStatsManager != null) {
            totalAD += itemStatsManager.getItemAD(player);
        }
        return totalAD + baseAD + enchantAD;
    }

    public double getPlayerAR(Player player) {
        var itemEquipedAR = player.getAttribute(Attribute.GENERIC_ARMOR);
        double totalAR = BASE_PHYSICAL_ARMOR + itemEquipedAR.getValue();
        double enchantAR = getPlayerArmor(player);
        ItemStatsManager itemStatsManager = LeagueMechanics.getInstance().getStatsManager();
        if (itemStatsManager != null) {
            totalAR += itemStatsManager.getItemAR(player);
        }
        return totalAR + enchantAR;
    }

    public double getPlayerAP(Player player) {
        ItemStatsManager itemStatsManager = LeagueMechanics.getInstance().getStatsManager();
        double totalAP = BASE_ABILITY_POWER;
        if (itemStatsManager != null) {
            totalAP += itemStatsManager.getItemAP(player);
        }
        return totalAP;
    }

    public double getPlayerMR(Player player) {
        ItemStatsManager itemStatsManager = LeagueMechanics.getInstance().getStatsManager();
        double totalMR = BASE_MAGIC_RESIST;
        if (itemStatsManager != null) {
            totalMR += itemStatsManager.getItemMR(player);
        }
        return totalMR;
    }

    public double getPlayerHP(Player player) {
        ItemStatsManager itemStatsManager = LeagueMechanics.getInstance().getStatsManager();
        double totalHP = BASE_BONUS_HEALTH;
        if (itemStatsManager != null) {
            totalHP += itemStatsManager.getItemHP(player);
        }
        return totalHP;
    }

    public double getPlayerHR(Player player) {
        ItemStatsManager itemStatsManager = LeagueMechanics.getInstance().getStatsManager();
        double totalHR = BASE_BONUS_HEALTH_REGEN;
        if (itemStatsManager != null) {
            totalHR += itemStatsManager.getItemHR(player);
        }
        return totalHR;
    }

    public double getPlayerAS(Player player) {
        ItemStatsManager itemStatsManager = LeagueMechanics.getInstance().getStatsManager();
        double totalAS = BASE_BONUS_ATTACK_SPEED;
        if (itemStatsManager != null) {
            totalAS += itemStatsManager.getItemAS(player);
        }
        return totalAS;
    }

    public double getPlayerSR(Player player) {
        ItemStatsManager itemStatsManager = LeagueMechanics.getInstance().getStatsManager();
        double totalSR = BASE_BONUS_SATURATION_REGEN;
        if (itemStatsManager != null) {
            totalSR += itemStatsManager.getItemSR(player);
        }
        return totalSR;
    }


    public String getActionBarSections(Player player) {
        double SHARPNESS = getPlayerWeapon(player);
        double PROTECTION = getPlayerArmor(player);

        double AD = getPlayerAD(player);
        double AR = getPlayerAR(player);
        double AP = getPlayerAP(player);
        double MR = getPlayerMR(player);

        double AS = getPlayerAS(player);
        double HP = getPlayerHP(player);
        double HR = getPlayerHR(player);
        double SR = getPlayerSR(player);


        String adDisplay = " §6🗡 §f" + String.format("%.1f", AD);
        if (SHARPNESS > 0) {
            adDisplay = " §6🗡 §f" + String.format("%.1f", AD) + "§f(+" + String.format("%.1f", SHARPNESS) + ")";
        }
        String arDisplay = " §e🛡 §f" + String.format("%.1f", AR);
        if (PROTECTION > 0) {
            arDisplay = " §e🛡 §f" + String.format("%.1f", AR) + " §f(+" + String.format("%.1f", PROTECTION) + ")";
        }

        String apDisplay = " §9☄ §f" + String.format("%.1f", AP);
        String mrDisplay = " §b⦿ §f" + String.format("%.1f", MR);

        return adDisplay + arDisplay + apDisplay + mrDisplay;
    }
}