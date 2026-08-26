package dev.ixpu.leaguemechanics.player;

import dev.ixpu.leaguemechanics.LeagueMechanics;
import dev.ixpu.leaguemechanics.manager.ItemStatsManager;
import dev.ixpu.leaguemechanics.rune.keystones.precision.LethalTempo;
import dev.ixpu.leaguemechanics.rune.keystones.domination.HailOfBlades;
import dev.ixpu.leaguemechanics.rune.CooldownHandler;
import dev.ixpu.leaguemechanics.item.passives.ItemPassivesRegistry;
import dev.ixpu.leaguemechanics.item.passives.dark_seal;

import org.bukkit.entity.Player;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.inventory.ItemStack;
import org.bukkit.enchantments.Enchantment;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static dev.ixpu.leaguemechanics.player.PlayerOrigin.getPlayerCountryBaseAS;
import static dev.ixpu.leaguemechanics.player.PlayerOrigin.getPlayerRuneRatioAS;

public class PlayerStats {
    private static final Map<UUID, PlayerStats> INSTANCE_CACHE = new ConcurrentHashMap<>();

    double BASE_ATTACK_DAMAGE = 3.0;
    double BASE_ABILITY_POWER = 4.0;
    double BASE_ARMOR = 15.0;
    double BASE_MAGIC_RESIST = 15.0;

    public static PlayerStats getOrCreate(Player player) {
        UUID uuid = player.getUniqueId();
        PlayerStats stats = INSTANCE_CACHE.get(uuid);
        if (stats == null) {
            stats = new PlayerStats();
            INSTANCE_CACHE.put(uuid, stats);
        }
        return stats;
    }

    public static void invalidateCache(UUID uuid) {
        INSTANCE_CACHE.remove(uuid);
    }

    public double getPlayerHP(Player player) {
        ItemStatsManager itemStatsManager = LeagueMechanics.getInstance().getStatsManager();
        double itemHP = 0;
        double baseHP = player.getAttribute(Attribute.MAX_HEALTH).getValue();
        if (itemStatsManager != null) {
            itemHP += itemStatsManager.getItemHP(player);
        }
        return baseHP + itemHP;
    }

    public double getPlayerHR(Player player) {
        ItemStatsManager itemStatsManager = LeagueMechanics.getInstance().getStatsManager();
        return itemStatsManager.getItemHR(player);
    }


    public double getPlayerAD(Player player) {
        ItemStatsManager itemStatsManager = LeagueMechanics.getInstance().getStatsManager();
        ItemStack weapon = player.getInventory().getItemInMainHand();
        double itemAD = 0;
        double baseAD = BASE_ATTACK_DAMAGE + player.getAttribute(Attribute.ATTACK_DAMAGE).getValue();
        double enchantAD = getWeaponEnchant(player);
        if (isWeapon(weapon)) {
            baseAD++;
        }
        if (itemStatsManager != null) {
            itemAD += itemStatsManager.getItemAD(player);
        }
        return baseAD + itemAD + enchantAD;
    }

    public double getPlayerAP(Player player) {
        ItemStatsManager itemStatsManager = LeagueMechanics.getInstance().getStatsManager();
        double itemAP = 0;
        double baseAP = BASE_ABILITY_POWER;
        if (itemStatsManager != null) {
            itemAP += itemStatsManager.getItemAP(player);
        }

        dark_seal darkSeal = (dark_seal) ItemPassivesRegistry.getInstance().getPassive("dark-seal");
        if (darkSeal != null) {
            itemAP += darkSeal.getAbilityPower(player);
        }

        return baseAP + itemAP;
    }

    public double getPlayerAF(Player player) {
        ItemStatsManager itemStatsManager = LeagueMechanics.getInstance().getStatsManager();
        double bonusAD = itemStatsManager.getItemAD(player);
        double bonusAP = itemStatsManager.getItemAP(player);
        if (bonusAD > bonusAP) {
            return 0.6;
        } else if (bonusAP > bonusAD) {
            return 0.8;
        }
        return 0;
    }

    public double getPlayerTD(Player player) {
        ItemStatsManager itemStatsManager = LeagueMechanics.getInstance().getStatsManager();
        double itemTD = 0;
        double baseTD = 0;
        double bonusTD = levelBasedTD(player);
        if (itemStatsManager != null) {
            itemTD += itemStatsManager.getItemTD(player);
        }
        return baseTD + itemTD + bonusTD;
    }

    public double getPlayerAS(Player player) {
        ItemStatsManager itemStatsManager = LeagueMechanics.getInstance().getStatsManager();

        double itemAS = 0;
        double runeAS = 0;
        double baseAS = getPlayerCountryBaseAS(player);

        if (itemStatsManager != null) {
            itemAS += itemStatsManager.getItemAS(player);
        }

        PlayerRuneData runeData = LeagueMechanics.getInstance().getRuneManager().getPlayerRuneData(player);
        if (runeData != null) {
            for (CooldownHandler rune : runeData.getAllRunes()) {
                if (rune instanceof LethalTempo lethalTempo) {
                    runeAS += lethalTempo.getActiveASBonus(player);
                }
                if (rune instanceof HailOfBlades hailOfBlades) {
                    runeAS += hailOfBlades.getActiveASBonus(player);
                }
            }
        }
        double asRatio = getPlayerRuneRatioAS(player);
        double bonusAS = (itemAS + runeAS) / 100;
        return bonusAS * (asRatio / baseAS);
    }

    public double getPlayerAR(Player player) {
        ItemStatsManager itemStatsManager = LeagueMechanics.getInstance().getStatsManager();
        double itemAR = 0;
        double baseAR = BASE_ARMOR + player.getAttribute(Attribute.ARMOR).getValue();
        double enchantAR = getArmorEnchant(player);
        if (itemStatsManager != null) {
            itemAR += itemStatsManager.getItemAR(player);
        }
        return baseAR + itemAR + enchantAR;
    }

    public double getPlayerMR(Player player) {
        ItemStatsManager itemStatsManager = LeagueMechanics.getInstance().getStatsManager();
        double itemMR = 0;
        double baseMR = BASE_MAGIC_RESIST;
        if (itemStatsManager != null) {
            itemMR += itemStatsManager.getItemMR(player);
        }
        return baseMR + itemMR;
    }

    public double getPlayerMS(Player player) {
        try {
            double baseMS = 0.1;
            double attributeMS = player.getAttribute(Attribute.MOVEMENT_SPEED).getValue();
            double speedEffectBonus = 0;
            if (player.hasPotionEffect(org.bukkit.potion.PotionEffectType.SPEED)) {
                org.bukkit.potion.PotionEffect speedEffect = player.getPotionEffect(org.bukkit.potion.PotionEffectType.SPEED);
                if (speedEffect != null) {
                    speedEffectBonus = 0.2 * (speedEffect.getAmplifier() + 1);
                }
            }
            ItemStatsManager itemStatsManager = LeagueMechanics.getInstance().getStatsManager();
            double itemMS = itemStatsManager != null ? itemStatsManager.getItemMS(player) : 0;
            double totalMS = (baseMS + attributeMS) * 100;
            totalMS += (speedEffectBonus * 100);
            totalMS += itemMS;

            return totalMS;
        } catch (Exception e) {
            return 0.0;
        }
    }

    public double getPlayerCC(Player player) {
        ItemStatsManager itemStatsManager = LeagueMechanics.getInstance().getStatsManager();
        return itemStatsManager.getItemCC(player);
    }

    private double levelBasedTD(Player player) {
        double playerLevel = player.getLevel();
        if (playerLevel >= 300) {
            return 20;
        } else if (playerLevel >= 200) {
            return 16;
        } else if (playerLevel >= 100) {
            return 8;
        } else if (playerLevel >= 50) {
            return 4;
        } else {
            return 2;
        }
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

    public double getWeaponEnchant(Player player) {
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

    public double getArmorEnchant(Player player) {
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


    public String getActionBarSections(Player player) {

//        double enchantmentAD = getWeaponEnchant(player);
//        double enchantmentAR = getArmorEnchant(player);
//
//        double AD = getPlayerAD(player) + enchantmentAD;
//        double AR = getPlayerAR(player) + enchantmentAR;
//        double AP = getPlayerAP(player);
//        double MR = getPlayerMR(player);
//
//        String adDisplay = " §6🗡 §f" + String.format("%.1f", AD);
//
//        String apDisplay = " §9☄ §f" + String.format("%.1f", AP);
//
//        String arDisplay = " §e🛡 §f" + String.format("%.1f", AR);
//
//        String mrDisplay = " §b⦿ §f" + String.format("%.1f", MR);

        return "";
    }
}