package dev.ixpu.leaguemechanics.rune.keystones.inspiration;

import dev.ixpu.leaguemechanics.LeagueMechanics;
import dev.ixpu.leaguemechanics.rune.CooldownHandler;
import dev.ixpu.leaguemechanics.rune.RunePath;
import dev.ixpu.leaguemechanics.rune.RuneSlot;
import dev.ixpu.leaguemechanics.player.PlayerStats;
import dev.ixpu.leaguemechanics.manager.BuffManager;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import net.kyori.adventure.text.Component;

public class GlacialAugment extends CooldownHandler {
    private int BASE_FREEZE_DURATION_TICKS = 60;

    private double AD_PERCENTAGE_MULTIPLIER = 7.0;
    private double AP_PERCENTAGE_MULTIPLIER = 6.0;

    private int COOLDOWN_DURATION_SECONDS = 45;

    private static final double DEBUFF_REDUCTION_PERCENT = 15.0;

    // Track frozen targets with their remaining freeze duration (ticks)
    // Key: attacker UUID -> Map<target UUID, remaining freeze ticks>
    private final Map<UUID, Map<UUID, Integer>> frozenTargets = new ConcurrentHashMap<>();

    // Track debuffed targets with their remaining debuff duration (ticks)
    // Key: target UUID -> remaining debuff ticks
    private final Map<UUID, Integer> debuffedTargets = new ConcurrentHashMap<>();

    // Store the AD/AP reduction amounts for each target so we can restore them
    // Key: target UUID -> Map<"AD", reductionAmount> or Map<"AP", reductionAmount>
    private final Map<UUID, Map<String, Double>> debuffAmounts = new ConcurrentHashMap<>();

    private LeagueMechanics plugin;

    public GlacialAugment(ConfigurationSection config, LeagueMechanics plugin) {
        super("glacial-augment", RunePath.INSPIRATION, RuneSlot.KEYSTONE);
        this.plugin = plugin;
        ConfigurationSection section = config.getConfigurationSection("runes.keystones.inspiration.glacial-augment");

        if (section != null) {
            this.COOLDOWN_DURATION_SECONDS = section.getInt("cooldown", 45);
            this.BASE_FREEZE_DURATION_TICKS = section.getInt("base-freeze-duration", this.BASE_FREEZE_DURATION_TICKS);
            this.AD_PERCENTAGE_MULTIPLIER = section.getDouble("ad-percentage-multiplier", this.AD_PERCENTAGE_MULTIPLIER);
            this.AP_PERCENTAGE_MULTIPLIER = section.getDouble("ap-percentage-multiplier", this.AP_PERCENTAGE_MULTIPLIER);
        }
        this.setCooldownSeconds(COOLDOWN_DURATION_SECONDS);
    }

    @Override
    public void onEnable(Player player) {
        UUID uuid = player.getUniqueId();
        frozenTargets.put(uuid, new ConcurrentHashMap<>());
    }

    @Override
    public void onDisable(Player player) {
        UUID uuid = player.getUniqueId();
        clearPlayerCooldown(player);
        frozenTargets.remove(uuid);
        // Don't clear debuffedTargets here - they might be from other attackers
        // The debuffs will naturally expire or be cleared when the target's debuff ends
    }

    public void onProjectileHit(Player shooter, Entity target) {
        activateGlacialAugment(shooter, target);
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
            frozen = new ConcurrentHashMap<>();
            frozenTargets.put(attackerUUID, frozen);
        }

        int scaledFreezeDuration = getScaledFreezeDuration(player);
        frozen.put(targetUUID, scaledFreezeDuration);
        debuffedTargets.put(targetUUID, scaledFreezeDuration);

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

        // Apply AD/AP debuff to target (only works for Players who have PlayerStats)
        applyDebuffToTarget(livingTarget, targetUUID, scaledFreezeDuration);

        target.getWorld().playSound(target.getLocation(), Sound.ENTITY_PLAYER_HURT_FREEZE, 1.0f, 1.0f);

        if (plugin != null) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                livingTarget.removePotionEffect(PotionEffectType.SLOWNESS);

                // Remove powder snow blocks
                for (org.bukkit.Location snowLoc : snowLocations) {
                    if (snowLoc.getBlock().getType() == org.bukkit.Material.POWDER_SNOW) {
                        snowLoc.getBlock().setType(org.bukkit.Material.AIR);
                    }
                }
            }, scaledFreezeDuration);
        }
        resetCooldown(player);
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

        trackDebuffTimers();
    }

    private void trackDebuffTimers() {
        java.util.ArrayList<UUID> toRemove = new java.util.ArrayList<>();
        for (UUID targetUUID : new java.util.ArrayList<>(debuffedTargets.keySet())) {
            int duration = debuffedTargets.getOrDefault(targetUUID, 0);

            if (duration > 0) {
                duration--;
                debuffedTargets.put(targetUUID, duration);

                if (duration == 0) {
                    toRemove.add(targetUUID);
                }
            } else {
                toRemove.add(targetUUID);
            }
        }

        // Restore stats for expired debuffs
        for (UUID targetUUID : toRemove) {
            restoreDebuff(targetUUID);
            debuffedTargets.remove(targetUUID);
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

    private void applyDebuffToTarget(LivingEntity target, UUID targetUUID, int duration) {
        if (target instanceof Player player) {
            PlayerStats stats = PlayerStats.getOrCreate(player);
            BuffManager buffManager = new BuffManager();

            double currentAD = stats.getPlayerAD(player);
            double currentAP = stats.getPlayerAP(player);

            double reducedAD = buffManager.calculateDebuffValue(currentAD, DEBUFF_REDUCTION_PERCENT);
            double reducedAP = buffManager.calculateDebuffValue(currentAP, DEBUFF_REDUCTION_PERCENT);

            double adReduction = currentAD - reducedAD;
            double apReduction = currentAP - reducedAP;

            // Apply the debuff (negative modification)
            stats.modifyAD(-adReduction);
            stats.modifyAP(-apReduction);

            // Store the reduction amounts so we can restore them later
            Map<String, Double> amounts = new HashMap<>();
            amounts.put("AD", adReduction);
            amounts.put("AP", apReduction);
            debuffAmounts.put(targetUUID, amounts);
        }
    }

    private void restoreDebuff(UUID targetUUID) {
        Map<String, Double> amounts = debuffAmounts.get(targetUUID);
        if (amounts != null) {
            // Try to get the player and restore their stats
            // Note: Player might be offline, so we need to handle that
            org.bukkit.entity.Player targetPlayer = Bukkit.getPlayer(targetUUID);
            if (targetPlayer != null && targetPlayer.isOnline()) {
                PlayerStats stats = PlayerStats.getOrCreate(targetPlayer);
                // Restore the AD/AP (positive modification to cancel out the negative)
                stats.modifyAD(amounts.getOrDefault("AD", 0.0));
                stats.modifyAP(amounts.getOrDefault("AP", 0.0));
            }
            debuffAmounts.remove(targetUUID);
        }
    }

    public boolean isTargetDebuffed(UUID targetUUID) {
        return debuffedTargets.containsKey(targetUUID) && debuffedTargets.get(targetUUID) > 0;
    }

    public double getDebuffReduction(UUID targetUUID) {
        if (isTargetDebuffed(targetUUID)) {
            return DEBUFF_REDUCTION_PERCENT / 100.0;
        }
        return 0.0;
    }

    @Override
    public void tick(Player player) {
        RuneState state = isOnCooldown(player) ? RuneState.COOLDOWN : RuneState.IDLE;
        String runeDisplay = getRuneDisplay(player, state);
        setPlayerDisplay(player, runeDisplay);
        trackFreeze(player);
    }

    private void setPlayerDisplay(Player player, String runeDisplay) {
        PlayerStats playerStats = PlayerStats.getOrCreate(player);
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