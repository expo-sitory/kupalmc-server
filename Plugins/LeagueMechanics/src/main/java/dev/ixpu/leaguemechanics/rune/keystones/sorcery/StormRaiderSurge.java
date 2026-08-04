package dev.ixpu.leaguemechanics.rune.keystones.sorcery;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import dev.ixpu.leaguemechanics.LeagueMechanics;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import dev.ixpu.leaguemechanics.rune.BaseRune;
import dev.ixpu.leaguemechanics.rune.RunePath;
import dev.ixpu.leaguemechanics.rune.RuneSlot;
import net.kyori.adventure.text.Component;

public class StormRaiderSurge extends BaseRune {
    private double DAMAGE_THRESHOLD_PERCENTAGE = 0.80;
    private double MOVEMENT_SPEED_BONUS = 0.40;

    int COOLDOWN_SECONDS = 25;

    private static final int TRACKING_WINDOW_TICKS = 60;
    private static final int SPEED_DURATION_TICKS = 120;

    private final Map<UUID, Double> damageTracker = new HashMap<>();
    private final Map<UUID, Integer> windowTickCounter = new HashMap<>();
    private final Map<UUID, Integer> speedActiveDuration = new HashMap<>();
    private LeagueMechanics plugin;

    public StormRaiderSurge(ConfigurationSection config) {
        super("storm-raider-surge", RunePath.SORCERY, RuneSlot.KEYSTONE);
        ConfigurationSection section = config.getConfigurationSection("runes.keystones.sorcery.storm-raider-surge");
        if (section != null) {
            this.DAMAGE_THRESHOLD_PERCENTAGE = section.getDouble("damage-threshold", this.DAMAGE_THRESHOLD_PERCENTAGE);
            this.MOVEMENT_SPEED_BONUS = section.getDouble("movement-speed-bonus", this.MOVEMENT_SPEED_BONUS);
            this.COOLDOWN_SECONDS = section.getInt("cooldown", COOLDOWN_SECONDS);
        }
        this.setCooldownSeconds(COOLDOWN_SECONDS);
    }

    @Override
    public void onEnable(Player player) {
        UUID uuid = player.getUniqueId();
        damageTracker.put(uuid, 0.0);
        windowTickCounter.put(uuid, 0);
        speedActiveDuration.put(uuid, 0);
    }

    @Override
    public void onDisable(Player player) {
        UUID uuid = player.getUniqueId();
        clearPlayerCooldown(player);
        damageTracker.remove(uuid);
        windowTickCounter.remove(uuid);
        speedActiveDuration.remove(uuid);
    }

    @Override
    public void onAttack(Player attacker, Entity target, EntityDamageByEntityEvent event) {
        if (!(target instanceof LivingEntity livingTarget)) {
            return;
        }

        if (livingTarget.getMaxHealth() < 20) {
            return;
        }
        double estimatedDamage = event.getDamage();
        trackDamage(attacker, estimatedDamage);
    }

    @Override
    public void tick(Player player) {
        UUID playerUUID = player.getUniqueId();

        int windowTicks = windowTickCounter.getOrDefault(playerUUID, 0);
        windowTicks++;
        if (windowTicks >= TRACKING_WINDOW_TICKS) {
            damageTracker.put(playerUUID, 0.0);
            windowTicks = 0;
        }
        windowTickCounter.put(playerUUID, windowTicks);

        int speedDuration = speedActiveDuration.getOrDefault(playerUUID, 0);
        if (speedDuration > 0) {
            speedDuration--;
            speedActiveDuration.put(playerUUID, speedDuration);
            displayActiveState(player, speedDuration);
            return;
        }

        if (isOnCooldown(player)) {
            displayCooldownInfo(player);
            return;
        }

        double maxHp = player.getMaxHealth();
        double damageThreshold = maxHp * DAMAGE_THRESHOLD_PERCENTAGE;
        double currentDamage = damageTracker.getOrDefault(playerUUID, 0.0);

        if (currentDamage >= damageThreshold) {
            activateSurge(player);
            return;
        }
        displayIdleState(player);
    }

    private void trackDamage(Player player, double damage) {
        UUID playerUUID = player.getUniqueId();
        double currentDamage = damageTracker.getOrDefault(playerUUID, 0.0);
        damageTracker.put(playerUUID, currentDamage + damage);
    }

    private void activateSurge(Player player) {
        UUID playerUUID = player.getUniqueId();
        speedActiveDuration.put(playerUUID, SPEED_DURATION_TICKS);
        damageTracker.put(playerUUID, 0.0);

        player.addPotionEffect(new PotionEffect(
                PotionEffectType.SPEED,
                SPEED_DURATION_TICKS,
                1,
                false,
                false
        ));

        removeNegativeEffects(player);
        player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 2f);
    }

    private void removeNegativeEffects(Player player) {
        PotionEffectType[] negativeEffects = {
                PotionEffectType.SLOWNESS,
                PotionEffectType.POISON,
                PotionEffectType.WITHER,
                PotionEffectType.BLINDNESS,
                PotionEffectType.DARKNESS,
                PotionEffectType.HUNGER
        };

        for (PotionEffectType effect : negativeEffects) {
            if (player.hasPotionEffect(effect)) {
                player.removePotionEffect(effect);
            }
        }
    }

    private void displayIdleState(Player player) {
        player.sendActionBar(Component.text()
                .append(Component.text("§1👾"))
                .build());
    }

    private void displayActiveState(Player player, int remainingTicks) {
        double remainingSeconds = remainingTicks / 20.0;
        player.sendActionBar(Component.text()
                .append(Component.text(String.format("§9👾 (%.1fs)", remainingSeconds)))
                .build());
    }

    private void displayCooldownInfo(Player player) {
        String cooldownDisplay = getCooldownDisplay(player);
        player.sendActionBar(Component.text()
                .append(Component.text("§7👾 " + cooldownDisplay))
                .build());
    }
}