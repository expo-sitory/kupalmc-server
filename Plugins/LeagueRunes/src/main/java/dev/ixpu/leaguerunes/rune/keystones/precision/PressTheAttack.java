package dev.ixpu.leaguerunes.rune.keystones.precision;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import dev.ixpu.leaguerunes.rune.BaseRune;
import dev.ixpu.leaguerunes.rune.RunePath;
import dev.ixpu.leaguerunes.rune.RuneSlot;


public class PressTheAttack extends BaseRune {
    private static final int MAX_STACKS = 5;
    private static final int STACK_DURATION_TICKS = 40;
    private static final int DAMAGE_PER_HEART = 2;

    private final Map<UUID, Map<UUID, Integer>> playerStacks = new HashMap<>();
    private final Map<UUID, UUID> lastTarget = new HashMap<>();
    private final Map<UUID, Map<UUID, Integer>> stackTimers = new HashMap<>();

    public PressTheAttack() {
        super("press_the_attack", RunePath.PRECISION, RuneSlot.KEYSTONE);
        this.hasStacking = false;
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
    }

    @Override
    public void onAttack(Player attacker, Entity target, EntityDamageByEntityEvent event) {
        UUID playerUUID = attacker.getUniqueId();
        UUID targetUUID = target.getUniqueId();

        // Check if this is a different target 
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

        // Check if we're about to trigger the effect (5th stack)
        if (currentStacks == 4) {
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

        // Decrease all timers and remove expired stacks
        timers.entrySet().removeIf(entry -> {
            int newTime = entry.getValue() - 1;
            if (newTime <= 0) {
                stacks.remove(entry.getKey());
                return true;
            }
            entry.setValue(newTime);
            return false;
        });

        // Clear last target if no stacks remain
        if (stacks.isEmpty()) {
            lastTarget.put(playerUUID, null);
        }
    }

    private void dealBonusDamage(Player attacker, Entity target, EntityDamageByEntityEvent event) {
        // Calculate bonus damage based on XP level
        int playerLevel = attacker.getLevel();
        double bonusDamage = getBonusDamageByLevel(playerLevel);
        double bonusDamageHearts = bonusDamage * DAMAGE_PER_HEART;

        // Add to existing damage
        event.setDamage(event.getDamage() + bonusDamageHearts);

        // Visual/audio feedback
        displayActivationFeedback(attacker, target);
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
        String stackBar = "█".repeat(stacks) + "░".repeat(MAX_STACKS - stacks);
        player.sendActionBar("§e§lPress the Attack §r[" + stackBar + "] §7" + stacks + "/" + MAX_STACKS);
    }

    private void displayActivationFeedback(Player player, Entity target) {
        player.sendActionBar("§c§lPress the Attack ACTIVATED!");
    }
}
