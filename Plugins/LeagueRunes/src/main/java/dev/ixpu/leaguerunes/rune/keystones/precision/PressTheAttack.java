package dev.ixpu.leaguerunes.rune.keystones.precision;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import dev.ixpu.leaguerunes.rune.BaseRune;
import dev.ixpu.leaguerunes.rune.RunePath;
import dev.ixpu.leaguerunes.rune.RuneSlot;
import net.kyori.adventure.text.Component;


public class PressTheAttack extends BaseRune {
    private static final int MAX_STACKS = 3;
    private static final int STACK_DURATION_TICKS = 40;
    private static final int DAMAGE_PER_HEART = 2; 

    private final Map<UUID, Map<UUID, Integer>> playerStacks = new HashMap<>();
    private final Map<UUID, UUID> lastTarget = new HashMap<>();
    private final Map<UUID, Map<UUID, Integer>> stackTimers = new HashMap<>();

    public PressTheAttack() {
        super("press-the-attack", RunePath.PRECISION, RuneSlot.KEYSTONE);
        this.hasStacking = false;
        this.setCooldownSeconds(6.0);
    }

    @Override
    public void onEnable(Player player) {
        // Initialize player data
        playerStacks.putIfAbsent(player.getUniqueId(), new HashMap<>());
        lastTarget.putIfAbsent(player.getUniqueId(), null);
        stackTimers.putIfAbsent(player.getUniqueId(), new HashMap<>());
    }

    @Override
    public void onDisable(Player player) {
        UUID playerUUID = player.getUniqueId();
        playerStacks.remove(playerUUID);
        lastTarget.remove(playerUUID);
        stackTimers.remove(playerUUID);
        clearPlayerCooldown(player);
    }

    @Override
    public void onAttack(Player attacker, Entity target, EntityDamageByEntityEvent event) {
        UUID playerUUID = attacker.getUniqueId();
        UUID targetUUID = target.getUniqueId();

        if (isOnCooldown(attacker)) {
            return;
        }        

        // Check if this is a different target - reset stacks
        UUID previousTarget = lastTarget.get(playerUUID);
        if (previousTarget != null && !previousTarget.equals(targetUUID)) {
            playerStacks.get(playerUUID).clear();
            stackTimers.get(playerUUID).clear();
        }

        // Update last target
        lastTarget.put(playerUUID, targetUUID);

        // Get current stacks
        Map<UUID, Integer> stacks = playerStacks.get(playerUUID);
        int currentStacks = stacks.getOrDefault(targetUUID, 0);

        // Check if we're about to trigger the effect (3rd stack)
        if (currentStacks == 2) {
            // Consume all stacks and deal bonus damage
            dealBonusDamage(attacker, target, event);
            stacks.remove(targetUUID);
            stackTimers.get(playerUUID).remove(targetUUID);
        } else if (currentStacks < MAX_STACKS) {
            // Add a new stack
            currentStacks++;
            stacks.put(targetUUID, currentStacks);
            stackTimers.get(playerUUID).put(targetUUID, STACK_DURATION_TICKS);

            // Send action bar feedback
            displayStackFeedback(attacker, currentStacks);
        }
    }

    @Override
    public void tick(Player player) {
        UUID playerUUID = player.getUniqueId();
        Map<UUID, Integer> timers = stackTimers.get(playerUUID);
        Map<UUID, Integer> stacks = playerStacks.get(playerUUID);

        if (timers == null || stacks == null) return;

        // Safely iterate and collect expired entries
        List<UUID> expiredTargets = new ArrayList<>();
        
        for (Map.Entry<UUID, Integer> entry : timers.entrySet()) {
            int newTime = entry.getValue() - 1;
            if (newTime <= 0) {
                expiredTargets.add(entry.getKey());
            } else {
                entry.setValue(newTime);
            }
        }

        // Remove expired entries after iteration
        for (UUID targetUUID : expiredTargets) {
            timers.remove(targetUUID);
            stacks.remove(targetUUID);
        }

        // Clear last target if no stacks remain
        if (stacks.isEmpty()) {
            lastTarget.put(playerUUID, null);
        }
    }

    private void dealBonusDamage(Player attacker, Entity target, EntityDamageByEntityEvent event) {
        // Check cooldown
        if (isOnCooldown(attacker)) {
            return;
        }

        // Calculate bonus damage based on XP level
        int playerLevel = attacker.getLevel();
        double bonusDamage = getBonusDamageByLevel(playerLevel);
        double bonusDamageHearts = bonusDamage * DAMAGE_PER_HEART;

        // Add to existing damage
        event.setDamage(event.getDamage() + bonusDamageHearts);

        // Visual/audio feedback
        displayActivationFeedback(attacker, target);

        // Reset cooldown
        resetCooldown(attacker);
    }

    private double getBonusDamageByLevel(int totalLevel) {
        if (totalLevel >= 100) {
            return 3.5;
        } else if (totalLevel >= 71) {
            return 3.0;
        } else if (totalLevel >= 51) {
            return 2.5;
        } else if (totalLevel >= 30) {
            return 2.0;
        } else {
            return 1.5;
        }
    }

    private void displayStackFeedback(Player player, int stacks) {
        String stackBar = "§e✇".repeat(stacks) + "§7〇".repeat(MAX_STACKS - stacks);
        player.sendActionBar(Component.text()
                .append(Component.text(stackBar))
                .build());
    }

    private void displayActivationFeedback(Player player, Entity target) {
        player.sendActionBar(Component.text("✇")
                .color(net.kyori.adventure.text.format.NamedTextColor.RED)
                .decorate(net.kyori.adventure.text.format.TextDecoration.BOLD));
    }
}
