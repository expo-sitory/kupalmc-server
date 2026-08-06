package dev.ixpu.leaguemechanics.player;

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
        return BASE_ATTACK_DAMAGE + playerAD.getValue();
    }

    public double getTargetAR(LivingEntity target) {
        var playerAR = target.getAttribute(Attribute.GENERIC_ARMOR);
        assert playerAR != null;
        return BASE_PHYSICAL_ARMOR + playerAR.getValue();
    }

    public String getActionBarSections(Player player) {
        double SHARPNESS = getPlayerWeaponSharpnessEnchant(player);
        double PROTECTION = getPlayerArmorProtectionEnchant(player);
        double AD = getAttackerAD(player);
        double AR = getTargetAR(player);

        String adDisplay = " §6🗡 §f" + AD;
        if (SHARPNESS > 0) {
            adDisplay = " §6🗡 §f" + + AD + "§f(+" + String.format("%.1f", SHARPNESS) + ")";
        }
        String arDisplay = " §7🛡 §f" + AR;
        if (PROTECTION > 0) {
            arDisplay = " §7🛡 §f" + AR + " §f(+" + String.format("%.1f", PROTECTION) + ")";
        }

        String apDisplay = " §9☄ §f" + BASE_ABILITY_POWER;
        String mrDisplay = " §b⦿ §f" + BASE_MAGIC_RESIST;

        return adDisplay + arDisplay + apDisplay + mrDisplay;
    }
}