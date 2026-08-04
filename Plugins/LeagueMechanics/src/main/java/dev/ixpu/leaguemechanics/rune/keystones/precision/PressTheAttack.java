package dev.ixpu.leaguemechanics.rune.keystones.precision;

import java.util.*;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import dev.ixpu.leaguemechanics.rune.BaseRune;
import dev.ixpu.leaguemechanics.rune.RunePath;
import dev.ixpu.leaguemechanics.rune.RuneSlot;
import net.kyori.adventure.text.Component;


public class PressTheAttack extends BaseRune {
    private static final int MAX_STACKS = 3;
    private double BASE_PHYSICAL_DAMAGE = 4.5;

    int COOLDOWN_DURATION_SECONDS = 200;

    private static final int STACK_DURATION_TICKS = 60;

    private final Map<UUID, Map<UUID, Integer>> playerStacks = new HashMap<>();
    private final Map<UUID, UUID> lastTarget = new HashMap<>();
    private final Map<UUID, Map<UUID, Integer>> stackTimers = new HashMap<>();

    public PressTheAttack(org.bukkit.configuration.ConfigurationSection config) {
        super("press-the-attack", RunePath.PRECISION, RuneSlot.KEYSTONE);
        ConfigurationSection section = config.getConfigurationSection("runes.keystones.precision.press-the-attack");

        if (section != null) {
            this.BASE_PHYSICAL_DAMAGE = section.getDouble("base-physical-damage", this.BASE_PHYSICAL_DAMAGE);
            this.COOLDOWN_DURATION_SECONDS = section.getInt("cooldown", COOLDOWN_DURATION_SECONDS);
        }
        this.setCooldownSeconds(COOLDOWN_DURATION_SECONDS);
    }

    @Override
    public void onEnable(Player player) {
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
        triggerPressTheAttack(attacker, target, event);
    }
    public void onProjectileHit(Player attacker, Entity target) {
        triggerPressTheAttack(attacker, target, null);
    }
    private void triggerPressTheAttack(Player attacker, Entity target, EntityDamageByEntityEvent event) {
        UUID playerUUID = attacker.getUniqueId();
        UUID targetUUID = target.getUniqueId();
        UUID previousTarget = lastTarget.get(playerUUID);

        if (!(target instanceof LivingEntity livingTarget)) {
            return;
        }

        if (livingTarget.getMaxHealth() < 20) {
            return;
        }
        if (isOnCooldown(attacker)) {
            return;
        }
        if (previousTarget != null && !previousTarget.equals(targetUUID)) {
            playerStacks.get(playerUUID).clear();
            stackTimers.get(playerUUID).clear();
        }
        lastTarget.put(playerUUID, targetUUID);

        Map<UUID, Integer> stacks = playerStacks.get(playerUUID);
        int currentStacks = stacks.getOrDefault(targetUUID, 0);

        if (currentStacks == 2) {
            double totalOutput = BASE_PHYSICAL_DAMAGE / 2;
            event.setDamage(event.getDamage() + totalOutput);
            stacks.remove(targetUUID);
            stackTimers.get(playerUUID).remove(targetUUID);
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "press-the-attack-stack-sound " + attacker.getName());
            attacker.playSound(attacker.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 0.5f);
        } else {
            currentStacks++;
            stacks.put(targetUUID, currentStacks);
            stackTimers.get(playerUUID).put(targetUUID, STACK_DURATION_TICKS);
        }
        resetCooldown(attacker);
    }

    @Override
    public void tick(Player player) {
        UUID playerUUID = player.getUniqueId();
        Map<UUID, Integer> timers = stackTimers.get(playerUUID);
        Map<UUID, Integer> stacks = playerStacks.get(playerUUID);

        if (timers == null || stacks == null) return;

        List<UUID> expiredTargets = new ArrayList<>();
        
        for (Map.Entry<UUID, Integer> entry : timers.entrySet()) {
            int newTime = entry.getValue() - 1;
            if (newTime <= 0) {
                expiredTargets.add(entry.getKey());
            } else {
                entry.setValue(newTime);
            }
        }

        for (UUID targetUUID : expiredTargets) {
            timers.remove(targetUUID);
            stacks.remove(targetUUID);
        }

        if (stacks.isEmpty()) {
            lastTarget.put(playerUUID, null);
        }
        
        displayStackInfo(player);
    }
    
    private void displayStackInfo(Player player) {
        UUID playerUUID = player.getUniqueId();
        Map<UUID, Integer> stacks = playerStacks.get(playerUUID);

        String cooldownDisplay = getCooldownDisplay(player);
        if (!cooldownDisplay.isEmpty()) {
            player.sendActionBar(Component.text("§7✽ " + cooldownDisplay));
            return;
        }
        
        if (stacks == null || stacks.isEmpty()) {
            player.sendActionBar(Component.text("§6✽"));
            return;
        }
        
        UUID lastTargetUUID = lastTarget.getOrDefault(playerUUID, null);
        int currentStacks = 0;
        if (lastTargetUUID != null) {
            currentStacks = stacks.getOrDefault(lastTargetUUID, 0);
        }
        
        player.sendActionBar(Component.text()
                .append(Component.text("§e✽ " + currentStacks + "/" + MAX_STACKS))
                .build());
    }
}