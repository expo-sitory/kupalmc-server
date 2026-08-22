package dev.ixpu.leaguemechanics.player;

import dev.ixpu.leaguemechanics.LeagueMechanics;
import dev.ixpu.leaguemechanics.manager.DamageManager;
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


public class PlayerStats {
    private static final Map<UUID, PlayerStats> INSTANCE_CACHE = new ConcurrentHashMap<>();
    private static final long CACHE_TTL_MS = 100;

    double BASE_HEALTH = 0.0;
    double BASE_HEALTH_REGEN = 0.0;

    double BASE_ATTACK_DAMAGE = 3.0;
    double BASE_ABILITY_POWER = 4.0;
    double BASE_ADAPTIVE_FORCE = 4.0;

    double BASE_TRUE_DAMAGE = 0.0;
    double BASE_ATTACK_SPEED = 0.0;
    double BASE_ARMOR = 15.0;
    double BASE_MAGIC_RESIST = 15.0;

    private final Player player;
    private long lastCacheTime = 0;

    private PlayerStats(Player player) {
        this.player = player;
    }

    public static PlayerStats getOrCreate(Player player) {
        UUID uuid = player.getUniqueId();
        PlayerStats stats = INSTANCE_CACHE.get(uuid);
        if (stats == null) {
            stats = new PlayerStats(player);
            INSTANCE_CACHE.put(uuid, stats);
        }
        return stats;
    }

    public static void invalidateCache(UUID uuid) {
        INSTANCE_CACHE.remove(uuid);
    }

    public static void invalidateAll() {
        INSTANCE_CACHE.clear();
    }


    public double getPlayerHP(Player player) {
        ItemStatsManager itemStatsManager = LeagueMechanics.getInstance().getStatsManager();
        double itemHP = 0;
        double baseHP = BASE_HEALTH;
        if (itemStatsManager != null) {
            itemHP += itemStatsManager.getItemHP(player);
        }
        return baseHP + itemHP;
    }

    public double getPlayerHR(Player player) {
        ItemStatsManager itemStatsManager = LeagueMechanics.getInstance().getStatsManager();
        double itemHR = 0;
        double baseHR = BASE_HEALTH_REGEN;
        if (itemStatsManager != null) {
            itemHR += itemStatsManager.getItemHR(player);
        }
        return baseHR + itemHR;
    }


    public double getPlayerAD(Player player) {
        ItemStatsManager itemStatsManager = LeagueMechanics.getInstance().getStatsManager();
        ItemStack weapon = player.getInventory().getItemInMainHand();
        double itemAD = 0;
        double baseAD = BASE_ATTACK_DAMAGE + player.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).getValue();
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
        return BASE_ADAPTIVE_FORCE;
    }

    public double getPlayerTD(Player player) {
        ItemStatsManager itemStatsManager = LeagueMechanics.getInstance().getStatsManager();
        double itemTD = 0;
        double baseTD = BASE_TRUE_DAMAGE;
        double bonusTD = levelBasedTD(player);
        if (itemStatsManager != null) {
            itemTD += itemStatsManager.getItemTD(player);
        }
        return baseTD + itemTD + bonusTD;
    }

    public double getPlayerAS(Player player) {
        ItemStatsManager itemStatsManager = LeagueMechanics.getInstance().getStatsManager();
        double itemAS = 0;
        double baseAS = BASE_ATTACK_SPEED;
        double runeAS = 0;

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

        return baseAS + itemAS + runeAS;
    }

    public double getPlayerAR(Player player) {
        ItemStatsManager itemStatsManager = LeagueMechanics.getInstance().getStatsManager();
        double itemAR = 0;
        double baseAR = BASE_ARMOR + player.getAttribute(Attribute.GENERIC_ARMOR).getValue();
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
        ItemStatsManager statsManager = LeagueMechanics.getInstance().getStatsManager();
        DamageManager damage = new DamageManager(statsManager);

        double enchantmentAD = getWeaponEnchant(player);
        double enchantmentAR = getArmorEnchant(player);

        double adaptiveAD = damage.getPlayerAdaptiveAD(player);
        double adaptiveAP = damage.getPlayerAdaptiveAP(player);

        double AD = getPlayerAD(player) - enchantmentAD;
        double AR = getPlayerAR(player) - enchantmentAR;
        double AP = getPlayerAP(player);
        double MR = getPlayerMR(player);

        String adDisplay = " §6🗡 §f" + String.format("%.1f", AD);
        if (enchantmentAD > 0 || adaptiveAD > BASE_ADAPTIVE_FORCE && adaptiveAD > adaptiveAP) {
            adDisplay = " §6🗡 §f" + String.format("%.1f", AD) + "§f(+" + String.format("%.1f", adaptiveAD + enchantmentAD) + ")";
        }

        String apDisplay = " §9☄ §f" + String.format("%.1f", AP);
        if (adaptiveAP > BASE_ADAPTIVE_FORCE && adaptiveAP > adaptiveAD) {
            apDisplay = " §9☄ §f" + String.format("%.1f", AP) + "§f(+" + String.format("%.1f", adaptiveAP) + ")";
        }

        String arDisplay = " §e🛡 §f" + String.format("%.1f", AR);
        if (enchantmentAR > 0) {
            arDisplay = " §e🛡 §f" + String.format("%.1f", AR) + " §f(+" + String.format("%.1f", enchantmentAR) + ")";
        }

        String mrDisplay = " §b⦿ §f" + String.format("%.1f", MR);

        return adDisplay + apDisplay + arDisplay + mrDisplay;
    }
}