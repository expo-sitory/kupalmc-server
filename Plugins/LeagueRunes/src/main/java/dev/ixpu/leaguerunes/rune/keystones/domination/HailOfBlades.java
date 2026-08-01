package dev.ixpu.leaguerunes.rune.keystones.domination;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import dev.ixpu.leaguerunes.rune.BaseRune;
import dev.ixpu.leaguerunes.rune.RunePath;
import dev.ixpu.leaguerunes.rune.RuneSlot;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class HailOfBlades extends BaseRune {
    private double ATTACK_SPEED_BONUS = 0.10; 
    private double TRUE_DAMAGE_MULTIPLIER = 0.20;
    private int WINDUP_TICKS = 200; 
    private int STACK_DURATION_TICKS = 60; 
    private int INACTIVITY_TIMEOUT_TICKS = 60; 
    private int INITIAL_STACKS = 2;
    private int WINDUP_COOLDOWN_TICKS = 300;

    private final Map<UUID, Boolean> windupActive = new HashMap<>();
    private final Map<UUID, Integer> windupTicks = new HashMap<>();
    private final Map<UUID, Integer> windupCooldownTicks = new HashMap<>();
    private final Map<UUID, Integer> lastWindupStage = new HashMap<>(); // Track stage to detect changes
    private final Map<UUID, Boolean> effectActive = new HashMap<>();
    private final Map<UUID, List<Integer>> stackDurationTicks = new HashMap<>(); // Per-stack duration tracking
    private final Map<UUID, Integer> lastAttackTick = new HashMap<>(); // Global inactivity timer
    private final Map<UUID, Integer> currentStacks = new HashMap<>();
    private final Map<UUID, List<AttributeModifier>> activeModifiers = new HashMap<>();

    public HailOfBlades(ConfigurationSection config) {
        super("hail-of-blades", RunePath.DOMINATION, RuneSlot.KEYSTONE);
        ConfigurationSection section = config.getConfigurationSection("runes.keystones.domination.hail-of-blades");
        if (section != null) {
            this.ATTACK_SPEED_BONUS = section.getDouble("attack-speed-bonus", this.ATTACK_SPEED_BONUS);
            this.TRUE_DAMAGE_MULTIPLIER = section.getDouble("true-damage-multiplier", this.TRUE_DAMAGE_MULTIPLIER);
            this.WINDUP_TICKS = section.getInt("windup-ticks", this.WINDUP_TICKS);
            this.STACK_DURATION_TICKS = section.getInt("stack-duration", this.STACK_DURATION_TICKS);
            this.INACTIVITY_TIMEOUT_TICKS = section.getInt("inactivity-timeout", this.INACTIVITY_TIMEOUT_TICKS);
            this.INITIAL_STACKS = section.getInt("initial-stacks", this.INITIAL_STACKS);
            this.WINDUP_COOLDOWN_TICKS = section.getInt("cooldown", this.WINDUP_COOLDOWN_TICKS);
        }
    }

    @Override
    public void onEnable(Player player) {
        UUID uuid = player.getUniqueId();
        windupActive.put(uuid, false);
        windupTicks.put(uuid, 0);
        windupCooldownTicks.put(uuid, 0);
        lastWindupStage.put(uuid, 0);
        effectActive.put(uuid, false);
        stackDurationTicks.put(uuid, new ArrayList<>());
        lastAttackTick.put(uuid, 0);
        currentStacks.put(uuid, 0);
        activeModifiers.put(uuid, new ArrayList<>());
    }

    @Override
    public void onDisable(Player player) {
        UUID uuid = player.getUniqueId();
        removeAllModifiers(player);
        windupActive.remove(uuid);
        windupTicks.remove(uuid);
        windupCooldownTicks.remove(uuid);
        lastWindupStage.remove(uuid);
        effectActive.remove(uuid);
        stackDurationTicks.remove(uuid);
        lastAttackTick.remove(uuid);
        currentStacks.remove(uuid);
        activeModifiers.remove(uuid);
    }

    @Override
    public void onAttack(Player attacker, Entity target, EntityDamageByEntityEvent event) {
        UUID playerUUID = attacker.getUniqueId();

        if (!(target instanceof LivingEntity)) {
            return;
        }

        if (effectActive.getOrDefault(playerUUID, false)) {
            // Reset global inactivity timer
            lastAttackTick.put(playerUUID, 0);

            // Apply true damage bonus
            double trueDamageBonus = event.getDamage() * TRUE_DAMAGE_MULTIPLIER;
            event.setDamage(event.getDamage() + trueDamageBonus);

            // Refresh the duration of all stacks
            List<Integer> durations = stackDurationTicks.getOrDefault(playerUUID, new ArrayList<>());
            for (int i = 0; i < durations.size(); i++) {
                durations.set(i, STACK_DURATION_TICKS);
            }

            displayEffectInfo(attacker, currentStacks.getOrDefault(playerUUID, 0), true);
            return;
        }

        if (windupActive.getOrDefault(playerUUID, false) || windupCooldownTicks.getOrDefault(playerUUID, 0) > 0) {
            return;
        }

        // Start windup
        windupActive.put(playerUUID, true);
        windupTicks.put(playerUUID, WINDUP_TICKS);
        lastWindupStage.put(playerUUID, 0); 
        displayWindupMessage(attacker, WINDUP_TICKS);
    }

    @Override
    public void tick(Player player) {
        UUID playerUUID = player.getUniqueId();

        // Handle windup cooldown
        int cooldownTicks = windupCooldownTicks.getOrDefault(playerUUID, 0);
        if (cooldownTicks > 0) {
            cooldownTicks--;
            windupCooldownTicks.put(playerUUID, cooldownTicks);
        }

        // Handle windup
        if (windupActive.getOrDefault(playerUUID, false)) {
            int windupCount = windupTicks.getOrDefault(playerUUID, 0);
            windupCount--;
            windupTicks.put(playerUUID, windupCount);

            displayWindupMessage(player, windupCount);

            if (windupCount <= 0) {
                windupActive.put(playerUUID, false);
                windupCooldownTicks.put(playerUUID, WINDUP_COOLDOWN_TICKS);
                activateEffect(player);
            }
            return;
        }

        // Handle active effect
        if (effectActive.getOrDefault(playerUUID, false)) {
            List<Integer> durations = stackDurationTicks.getOrDefault(playerUUID, new ArrayList<>());

            // Tick global inactivity timer
            int inactivityCount = lastAttackTick.getOrDefault(playerUUID, 0);
            inactivityCount++;
            lastAttackTick.put(playerUUID, inactivityCount);

            // If 3 seconds of inactivity, consume one stack and reset timer
            if (inactivityCount >= INACTIVITY_TIMEOUT_TICKS) {
                if (!durations.isEmpty()) {
                    durations.remove(0); // Remove first stack
                    currentStacks.put(playerUUID, currentStacks.getOrDefault(playerUUID, 0) - 1);
                    
                    // Refresh remaining stacks' durations
                    for (int i = 0; i < durations.size(); i++) {
                        durations.set(i, STACK_DURATION_TICKS);
                    }
                    
                    player.playSound(player.getLocation(), org.bukkit.Sound.ITEM_TRIDENT_HIT_GROUND, 1.0f, 0.5f);
                }
                lastAttackTick.put(playerUUID, 0);
            }

            // Tick all stack durations independently
            List<Integer> expiredIndices = new ArrayList<>();
            for (int i = 0; i < durations.size(); i++) {
                int duration = durations.get(i);
                duration--;
                durations.set(i, duration);

                if (duration <= 0) {
                    expiredIndices.add(i);
                }
            }

            // Remove expired stacks in reverse order to maintain indices
            for (int i = expiredIndices.size() - 1; i >= 0; i--) {
                durations.remove((int) expiredIndices.get(i));
                currentStacks.put(playerUUID, currentStacks.getOrDefault(playerUUID, 0) - 1);
                player.playSound(player.getLocation(), org.bukkit.Sound.ITEM_TRIDENT_HIT_GROUND, 1.0f, 0.5f);
            }

            // If all stacks are gone, deactivate effect
            if (currentStacks.getOrDefault(playerUUID, 0) <= 0) {
                deactivateEffect(player);
                return;
            }

            displayEffectInfo(player, currentStacks.getOrDefault(playerUUID, 0), true);
        }
    }

    private void activateEffect(Player player) {
        UUID playerUUID = player.getUniqueId();
        effectActive.put(playerUUID, true);
        lastAttackTick.put(playerUUID, 0);
        currentStacks.put(playerUUID, INITIAL_STACKS);

        // Create duration trackers for each stack
        List<Integer> durations = new ArrayList<>();
        for (int i = 0; i < INITIAL_STACKS; i++) {
            durations.add(STACK_DURATION_TICKS);
        }
        stackDurationTicks.put(playerUUID, durations);

        // Apply attack speed bonus
        applyAttackSpeedBonus(player);

        player.playSound(player.getLocation(), org.bukkit.Sound.ITEM_TRIDENT_THROW, 1.0f, 2.0f);
        displayEffectInfo(player, INITIAL_STACKS, true);
    }

    private void deactivateEffect(Player player) {
        UUID playerUUID = player.getUniqueId();
        effectActive.put(playerUUID, false);
        currentStacks.put(playerUUID, 0);
        windupTicks.put(playerUUID, 0);
        windupActive.put(playerUUID, false);
        lastWindupStage.put(playerUUID, 0);
        lastAttackTick.put(playerUUID, 0);
        stackDurationTicks.put(playerUUID, new ArrayList<>());

        windupCooldownTicks.put(playerUUID, WINDUP_COOLDOWN_TICKS);
        removeAllModifiers(player);
        player.sendActionBar(Component.empty());
    }

    private void applyAttackSpeedBonus(Player player) {
        removeAllModifiers(player);
        UUID modifierUUID = UUID.nameUUIDFromBytes(("hail-of-blades-" + player.getUniqueId()).getBytes());
        
        AttributeModifier modifier = new AttributeModifier(
            modifierUUID,
            "Hail of Blades Attack Speed",
            ATTACK_SPEED_BONUS,
            AttributeModifier.Operation.ADD_SCALAR
        );

        player.getAttribute(Attribute.GENERIC_ATTACK_SPEED).addModifier(modifier);
        activeModifiers.get(player.getUniqueId()).add(modifier);
    }

    private void removeAllModifiers(Player player) {
        List<AttributeModifier> modifiers = activeModifiers.getOrDefault(player.getUniqueId(), new ArrayList<>());
        for (AttributeModifier modifier : modifiers) {
            player.getAttribute(Attribute.GENERIC_ATTACK_SPEED).removeModifier(modifier);
        }
        modifiers.clear();
    }

    private void displayWindupMessage(Player player, int remainingTicks) {
        UUID playerUUID = player.getUniqueId();
        
        int currentStage;
        String message;

        if (remainingTicks > (WINDUP_TICKS * 2 / 3)) {
            currentStage = 1;
            message = "§c❛§7❟❛";
        } else if (remainingTicks > (WINDUP_TICKS / 3)) {
            currentStage = 2;
            message = "§c❛❟§7❛";
        } else {
            currentStage = 3;
            message = "§c❛❟❛";
        }

        int lastStage = lastWindupStage.getOrDefault(playerUUID, 0);
        if (currentStage != lastStage) {
            player.playSound(player.getLocation(), org.bukkit.Sound.ITEM_TRIDENT_HIT, 1.0f, 2.0f);
            lastWindupStage.put(playerUUID, currentStage);
        }

        player.sendActionBar(Component.text(message));
    }

    private void displayEffectInfo(Player player, int stacks, boolean active) {
        if (!active) {
            player.sendActionBar(Component.empty());
            return;
        }
        double damageBonus = TRUE_DAMAGE_MULTIPLIER * 100;

        player.sendActionBar(Component.text()
            .append(Component.text("§c❛❟❛ " + stacks + "/" + INITIAL_STACKS, NamedTextColor.RED))
            .append(Component.text(String.format(" (+%d%% ats / +%.0f%% t-dmg)", 
                (int)(ATTACK_SPEED_BONUS * 100), damageBonus), NamedTextColor.WHITE))
            .build());
    }
}