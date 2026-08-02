package dev.ixpu.leaguerunes.rune.keystones.precision;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.attribute.Attribute;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import dev.ixpu.leaguerunes.rune.BaseRune;
import dev.ixpu.leaguerunes.rune.RunePath;
import dev.ixpu.leaguerunes.rune.RuneSlot;
import net.kyori.adventure.text.Component;


public class PressTheAttack extends BaseRune {
    private static final int MAX_STACKS = 3;
    private int COOLDOWN_DURATION_SECONDS = 200;
    private int STACK_DURATION_TICKS = 60;
    private double DAMAGE_PER_HEART = 1.5;

    private final Map<UUID, Map<UUID, Integer>> playerStacks = new HashMap<>();
    private final Map<UUID, UUID> lastTarget = new HashMap<>();
    private final Map<UUID, Map<UUID, Integer>> stackTimers = new HashMap<>();

    public PressTheAttack(org.bukkit.configuration.ConfigurationSection config) {
        super("press-the-attack", RunePath.PRECISION, RuneSlot.KEYSTONE);
        ConfigurationSection section = config.getConfigurationSection("runes.keystones.precision.press-the-attack");
        if (section != null) {
            this.STACK_DURATION_TICKS = section.getInt("stack-duration", this.STACK_DURATION_TICKS);
            this.DAMAGE_PER_HEART = section.getDouble("base-damage", this.DAMAGE_PER_HEART);
            this.COOLDOWN_DURATION_SECONDS = section.getInt("cooldown", this.COOLDOWN_DURATION_SECONDS);
        }
        this.setCooldownSeconds(COOLDOWN_DURATION_SECONDS);
    }

    public PressTheAttack() {
        super("press-the-attack", RunePath.PRECISION, RuneSlot.KEYSTONE);
        this.hasStacking = false;
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
        UUID playerUUID = attacker.getUniqueId();
        UUID targetUUID = target.getUniqueId();

        if (!(target instanceof LivingEntity)) {
            return;
        }
        
        LivingEntity livingTarget = (LivingEntity) target;
        double maxHealth = livingTarget.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        
        if (maxHealth < 20) {
            return;
        }

        if (isOnCooldown(attacker)) {
            return;
        }        

        UUID previousTarget = lastTarget.get(playerUUID);
        if (previousTarget != null && !previousTarget.equals(targetUUID)) {
            playerStacks.get(playerUUID).clear();
            stackTimers.get(playerUUID).clear();
        }

        lastTarget.put(playerUUID, targetUUID);

        Map<UUID, Integer> stacks = playerStacks.get(playerUUID);
        int currentStacks = stacks.getOrDefault(targetUUID, 0);


        if (currentStacks == 2) {
            dealBonusDamage(attacker, target, event);
            stacks.remove(targetUUID);
            stackTimers.get(playerUUID).remove(targetUUID);
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "press-the-attack-stack-sound " + attacker.getName());
            attacker.playSound(attacker.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 0.5f);
        } else if (currentStacks < MAX_STACKS) {
            currentStacks++;
            stacks.put(targetUUID, currentStacks);
            stackTimers.get(playerUUID).put(targetUUID, STACK_DURATION_TICKS);
        }
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

    private void dealBonusDamage(Player attacker, Entity target, EntityDamageByEntityEvent event) {
        if (isOnCooldown(attacker)) {
            return;
        }

        int playerLevel = attacker.getLevel();
        double bonusDamage = getBonusDamageByLevel(playerLevel);
        double bonusDamageHearts = bonusDamage * DAMAGE_PER_HEART;

        event.setDamage(event.getDamage() + bonusDamageHearts);

        resetCooldown(attacker);
    }

    private double getBonusDamageByLevel(int totalLevel) {
        if (totalLevel >= 100) {
            return 2.6;
        } else if (totalLevel >= 71) {
            return 2.3;
        } else if (totalLevel >= 51) {
            return 1.8;
        } else if (totalLevel >= 30) {
            return 1.3;
        } else {
            return 0.5;
        }
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
                .append(Component.text("§e✽ " + currentStacks + "/" + MAX_STACKS, net.kyori.adventure.text.format.NamedTextColor.WHITE))
                .build());
    }
}