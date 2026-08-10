package dev.ixpu.leaguemechanics.rune.keystones.inspiration;

import dev.ixpu.leaguemechanics.LeagueMechanics;
import dev.ixpu.leaguemechanics.rune.BaseRune;
import dev.ixpu.leaguemechanics.rune.RunePath;
import dev.ixpu.leaguemechanics.rune.RuneSlot;
import dev.ixpu.leaguemechanics.player.PlayerStats;
import dev.ixpu.leaguemechanics.util.DebugLogger;
import dev.ixpu.leaguemechanics.manager.BuffManager;
import dev.ixpu.leaguemechanics.manager.DamageManager;

import java.util.*;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;


import net.kyori.adventure.text.Component;

public class GlacialAugment extends BaseRune {
    private int BASE_FREEZE_DURATION_TICKS = 120;

    private double AD_PERCENTAGE_MULTIPLIER = 0.07;
    private double AP_PERCENTAGE_MULTIPLIER = 0.06;

    int COOLDOWN_DURATION_SECONDS = 45;

    private final Map<UUID, Map<UUID, Integer>> frozenTargets = new HashMap<>();

    private LeagueMechanics plugin;

    public GlacialAugment(ConfigurationSection config, LeagueMechanics plugin) {
        super("glacial-augment", RunePath.INSPIRATION, RuneSlot.KEYSTONE);
        this.plugin = plugin;
        ConfigurationSection section = config.getConfigurationSection("runes.keystones.inspiration.glacial-augment");

        if (section != null) {
            this.COOLDOWN_DURATION_SECONDS = section.getInt("cooldown", COOLDOWN_DURATION_SECONDS);
            this.BASE_FREEZE_DURATION_TICKS = section.getInt("base-freeze-duration", this.BASE_FREEZE_DURATION_TICKS);
            this.AD_PERCENTAGE_MULTIPLIER = section.getDouble("ad-percentage-multiplier", this.AD_PERCENTAGE_MULTIPLIER);
            this.AP_PERCENTAGE_MULTIPLIER = section.getDouble("ap-percentage-multiplier", this.AP_PERCENTAGE_MULTIPLIER);
        }
        this.setCooldownSeconds(COOLDOWN_DURATION_SECONDS);
    }

    @Override
    public void onEnable(Player player) {
        UUID uuid = player.getUniqueId();
        frozenTargets.put(uuid, new HashMap<>());
    }

    @Override
    public void onDisable(Player player) {
        UUID uuid = player.getUniqueId();
        clearPlayerCooldown(player);
        frozenTargets.remove(uuid);
    }

    public void onProjectileHit(Player shooter, Entity target) {
        if (!(target instanceof LivingEntity livingTarget)) {
            return;
        }

        double statsDamage = playerDamage(shooter, target);
        double newHealth = Math.max(0, Math.min(livingTarget.getMaxHealth(), livingTarget.getHealth() - statsDamage));

        DebugLogger.debug(shooter, "§7[Debug] §f[§3Glacial Augment§f] (Projectile) Stats Damage = §d" + Math.ceil(statsDamage * 100) / 100.0);
        DebugLogger.debug(shooter, "§7[Debug] §f[§3Glacial Augment§f] (Projectile) Target New HP = §d" + Math.ceil(newHealth * 100) / 100.0);

        livingTarget.setHealth(newHealth);

        activateGlacialAugment(shooter, target);
    }

    public void onAttack(Player attacker, Entity target, EntityDamageByEntityEvent event) {
        if (!(target instanceof LivingEntity livingTarget)) {
            return;
        }

        double statsDamage = playerDamage(attacker, target);
        double newHealth = Math.max(0, Math.min(livingTarget.getMaxHealth(), livingTarget.getHealth() - statsDamage));

        DebugLogger.debug(attacker, "§7[Debug] §f[§3Glacial Augment§f] (Melee) Stats Damage = §d" + Math.ceil(statsDamage * 100) / 100.0);
        DebugLogger.debug(attacker, "§7[Debug] §f[§3Glacial Augment§f] (Melee) Target New HP = §d" + Math.ceil(newHealth * 100) / 100.0);

        livingTarget.setHealth(newHealth);
    }

    private void activateGlacialAugment(Player player, Entity target) {
        UUID attackerUUID = player.getUniqueId();
        UUID targetUUID = target.getUniqueId();
        ItemStack weapon = player.getInventory().getItemInMainHand();

        if (!(target instanceof LivingEntity livingTarget)) {
            return;
        }
        if (livingTarget.getMaxHealth() < 20) {
            return;
        }
        if (CheckEnchant(weapon)) {
            return;
        }
        if (isOnCooldown(player)) {
            return;
        }

        Map<UUID, Integer> frozen = frozenTargets.get(attackerUUID);
        if (frozen == null) {
            return;
        }

        int scaledFreezeDuration = getScaledFreezeDuration(player);
        frozen.put(targetUUID, scaledFreezeDuration);

        org.bukkit.Location loc = target.getLocation();

        List<org.bukkit.Location> snowLocations = new ArrayList<>();
        snowLocations.add(loc.clone());
        snowLocations.add(loc.clone().add(1, 0, 0));
        snowLocations.add(loc.clone().add(2, 0, 0));
        snowLocations.add(loc.clone().add(-1, 0, 0));
        snowLocations.add(loc.clone().add(-2, 0, 0));
        snowLocations.add(loc.clone().add(0, 0, 1));
        snowLocations.add(loc.clone().add(0, 0, 2));
        snowLocations.add(loc.clone().add(0, 0, -1));
        snowLocations.add(loc.clone().add(0, 0, -2));

        for (org.bukkit.Location snowLoc : snowLocations) {
            if (snowLoc.getBlock().getType() == org.bukkit.Material.AIR) {
                snowLoc.getBlock().setType(org.bukkit.Material.POWDER_SNOW);
            }
        }

        livingTarget.addPotionEffect(new PotionEffect(
                PotionEffectType.SLOWNESS,
                scaledFreezeDuration,
                2,
                false,
                false
        ));

        livingTarget.addPotionEffect(new PotionEffect(
                PotionEffectType.WEAKNESS,
                scaledFreezeDuration,
                0,
                false,
                false
        ));

        player.playSound(target.getLocation(), Sound.ENTITY_PLAYER_HURT_FREEZE, 1.0f, 1.0f);

        if (plugin != null) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                livingTarget.removePotionEffect(PotionEffectType.SLOWNESS);

                for (org.bukkit.Location snowLoc : snowLocations) {
                    if (snowLoc.getBlock().getType() == org.bukkit.Material.POWDER_SNOW) {
                        snowLoc.getBlock().setType(org.bukkit.Material.AIR);
                    }
                }
            }, scaledFreezeDuration);
        }
        resetCooldown(player);
    }

    private double playerDamage(Player player, Entity target) {
        DamageManager damageManager = new DamageManager();
        return damageManager.totalBonusDamage(player, target, 0);
    }


    private boolean CheckEnchant(ItemStack item) {
        ItemMeta meta = item.getItemMeta();

        if (item.getType().isAir()) {
            return false;
        }
        if (meta == null) {
            return false;
        }

        return meta.getEnchants().keySet().stream()
                .anyMatch(e -> {
                    String enchantName = e.toString().toLowerCase();
                    return enchantName.contains("flame");
                });
    }

    private void trackFreeze(Player player) {
        UUID playerUUID = player.getUniqueId();

        Map<UUID, Integer> frozen = frozenTargets.get(playerUUID);
        if (frozen == null) {
            String runeDisplay = getRuneDisplay(player, RuneState.IDLE);
            setPlayerDisplay(player, runeDisplay);
            return;
        }

        java.util.ArrayList<UUID> toRemove = new java.util.ArrayList<>();
        for (UUID targetUUID : new java.util.ArrayList<>(frozen.keySet())) {
            int duration = frozen.getOrDefault(targetUUID, 0);

            if (duration > 0) {
                duration--;
                frozen.put(targetUUID, duration);

                if (duration == 0) {
                    toRemove.add(targetUUID);
                }
            } else {
                toRemove.add(targetUUID);
            }
        }

        for (UUID targetUUID : toRemove) {
            frozen.remove(targetUUID);
        }
    }

    private int getScaledFreezeDuration(Player player) {
        BuffManager buffManager = new BuffManager();
        double scaledDuration = buffManager.calculateBuffValue(
                player,
                BASE_FREEZE_DURATION_TICKS,
                AD_PERCENTAGE_MULTIPLIER,
                AP_PERCENTAGE_MULTIPLIER
        );
        return (int) scaledDuration;
    }

    @Override
    public void tick(Player player) {
        RuneState state = isOnCooldown(player) ? RuneState.COOLDOWN : RuneState.IDLE;
        String runeDisplay = getRuneDisplay(player, state);
        setPlayerDisplay(player, runeDisplay);
        trackFreeze(player);
    }

    private void setPlayerDisplay(Player player, String runeDisplay) {
        PlayerStats playerStats = new PlayerStats();
        String statsDisplay = playerStats.getActionBarSections(player);

        String actionBarMessage = runeDisplay + " " + statsDisplay;
        player.sendActionBar(Component.text(actionBarMessage));
    }

    enum RuneState {
        COOLDOWN, IDLE
    }

    private String getRuneDisplay(Player player, RuneState state) {
        return switch (state) {
            case COOLDOWN -> "§7❄ " + getCooldownDisplay(player);
            case IDLE -> "§3❄";
        };
    }
}