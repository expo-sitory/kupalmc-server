package dev.ixpu.leaguemechanics.player;

import dev.ixpu.leaguemechanics.LeagueMechanics;
import dev.ixpu.leaguemechanics.manager.StatsManager;
import org.bukkit.entity.LivingEntity;
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
        getAttackerAD(player);
        getTargetAR(player);
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

    public double getAttackerAD(Player player) {
        var playerAD = player.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE);
        assert playerAD != null;
        double totalAD = BASE_ATTACK_DAMAGE + playerAD.getValue();
        
        StatsManager statsManager = LeagueMechanics.getInstance().getStatsManager();
        if (statsManager != null) {
            totalAD += statsManager.getPlayerAD(player);
        }
        
        return totalAD;
    }

    public double getTargetAR(LivingEntity target) {
        var playerAR = target.getAttribute(Attribute.GENERIC_ARMOR);
        assert playerAR != null;
        double totalAR = BASE_PHYSICAL_ARMOR + playerAR.getValue();
        
        if (target instanceof Player player) {
            StatsManager statsManager = LeagueMechanics.getInstance().getStatsManager();
            if (statsManager != null) {
                totalAR += statsManager.getPlayerAR(player);
            }
        }
        
        return totalAR;
    }

    public String getActionBarSections(Player player) {
        double SHARPNESS = getPlayerWeaponSharpnessEnchant(player);
        double PROTECTION = getPlayerArmorProtectionEnchant(player);
        double AD = getAttackerAD(player);
        double AR = getTargetAR(player);

        StatsManager statsManager = LeagueMechanics.getInstance().getStatsManager();
        double itemAD = statsManager != null ? statsManager.getPlayerAD(player) : 0;
        double itemAR = statsManager != null ? statsManager.getPlayerAR(player) : 0;
        double itemAP = statsManager != null ? statsManager.getPlayerAP(player) : 0;
        double itemMR = statsManager != null ? statsManager.getPlayerMR(player) : 0;

        String adDisplay = " §6🗡 §f" + String.format("%.1f", AD + itemAD);
        if (SHARPNESS > 0) {
            adDisplay = " §6🗡 §f" + String.format("%.1f", AD +itemAD) + "§f(+" + String.format("%.1f", SHARPNESS) + ")";
        }
        String arDisplay = " §e🛡 §f" + String.format("%.1f", AR + itemAR);
        if (PROTECTION > 0) {
            arDisplay = " §e🛡 §f" + String.format("%.1f", AR + itemAR) + " §f(+" + String.format("%.1f", PROTECTION) + ")";
        }

        String apDisplay = " §9☄ §f" + String.format("%.1f", BASE_ABILITY_POWER);
        if (itemAP > 0) {
            apDisplay = " §9☄ §f" + String.format("%.1f", BASE_ABILITY_POWER + itemAP);
        }
        
        String mrDisplay = " §b⦿ §f" + String.format("%.1f", BASE_MAGIC_RESIST);
        if (itemMR > 0) {
            mrDisplay = " §b⦿ §f" + String.format("%.1f", BASE_MAGIC_RESIST + itemMR) + ")";
        }

        return adDisplay + arDisplay + apDisplay + mrDisplay;
    }
}