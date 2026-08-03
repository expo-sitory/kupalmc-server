package dev.ixpu.leaguemechanics.rune.keystones.domination;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.attribute.Attribute;

import dev.ixpu.leaguemechanics.rune.BaseRune;
import dev.ixpu.leaguemechanics.rune.RunePath;
import dev.ixpu.leaguemechanics.rune.RuneSlot;
import net.kyori.adventure.text.Component;

public class Electrocute extends BaseRune {
    private int MAX_STACKS = 3;
    private int STACK_DURATION_TICKS = 60;

    private final Map<UUID, Map<UUID, Integer>> playerEntityStacks = new HashMap<>();
    private final Map<UUID, Map<UUID, Integer>> stackExpiryTicks = new HashMap<>();

    public Electrocute(org.bukkit.configuration.ConfigurationSection config) {
        super("electrocute", RunePath.DOMINATION, RuneSlot.KEYSTONE);
        ConfigurationSection section = config.getConfigurationSection("runes.keystones.domination.electrocute");
        int COOLDOWN_DURATION_SECONDS = 25;
        if (section != null) {
            this.MAX_STACKS = section.getInt("max-stacks", this.MAX_STACKS);
            this.STACK_DURATION_TICKS = section.getInt("stack-duration", this.STACK_DURATION_TICKS);
            COOLDOWN_DURATION_SECONDS = section.getInt("cooldown", COOLDOWN_DURATION_SECONDS);
        }
        this.setCooldownSeconds(COOLDOWN_DURATION_SECONDS);
    }

    @Override
    public void onEnable(Player player) {
        UUID uuid = player.getUniqueId();
        playerEntityStacks.put(uuid, new HashMap<>());
        stackExpiryTicks.put(uuid, new HashMap<>());
    }

    @Override
    public void onDisable(Player player) {
        UUID uuid = player.getUniqueId();
        clearPlayerCooldown(player);
        playerEntityStacks.remove(uuid);
        stackExpiryTicks.remove(uuid);
    }

    @Override
    public void onAttack(Player attacker, Entity target, EntityDamageByEntityEvent event) {
        UUID playerUUID = attacker.getUniqueId();
        UUID targetUUID = target.getUniqueId();

        if (!(target instanceof LivingEntity livingTarget)) {
            return;
        }

        if (livingTarget.getMaxHealth() < 20) {
            return;
        }

        if (isOnCooldown(attacker)) {
            return;
        }

        addStack(attacker, targetUUID);

        int stacks = playerEntityStacks.get(playerUUID).getOrDefault(targetUUID, 0);
        if (stacks >= MAX_STACKS) {
            triggerLightning(attacker, target);
            resetCooldown(attacker);
            playerEntityStacks.get(playerUUID).remove(targetUUID);
            stackExpiryTicks.get(playerUUID).remove(targetUUID);
        }
    }

    public void onProjectileHit(Player shooter, Entity target) {
        UUID playerUUID = shooter.getUniqueId();
        UUID targetUUID = target.getUniqueId();

        if (!(target instanceof LivingEntity livingTarget)) {
            return;
        }

        if (livingTarget.getMaxHealth() < 20) {
            return;
        }

        if (isOnCooldown(shooter)) {
            return;
        }

        addStack(shooter, targetUUID);

        int stacks = playerEntityStacks.get(playerUUID).getOrDefault(targetUUID, 0);
        if (stacks >= MAX_STACKS) {
            triggerLightning(shooter, target);
            shooter.playSound(shooter.getLocation(), Sound.ITEM_TRIDENT_THUNDER, 1.0f, 1.2f);
            resetCooldown(shooter);
            playerEntityStacks.get(playerUUID).remove(targetUUID);
            stackExpiryTicks.get(playerUUID).remove(targetUUID);
        }
    }

    @Override
    public void tick(Player player) {
        UUID playerUUID = player.getUniqueId();

        if (isOnCooldown(player)) {
            displayCooldownInfo(player);
            return;
        }

        Map<UUID, Integer> entityStacks = playerEntityStacks.get(playerUUID);
        Map<UUID, Integer> expiries = stackExpiryTicks.get(playerUUID);

        int maxStacks = 0;
        for (UUID targetUUID : new java.util.ArrayList<>(entityStacks.keySet())) {
            int expiry = expiries.getOrDefault(targetUUID, 0);

            if (expiry > 0) {
                expiry--;
                expiries.put(targetUUID, expiry);
                maxStacks = Math.max(maxStacks, entityStacks.getOrDefault(targetUUID, 0));

                if (expiry == 0) {
                    entityStacks.remove(targetUUID);
                    expiries.remove(targetUUID);
                }
            }
        }

        if (maxStacks > 0) {
            displayStackInfo(player, maxStacks);
        } else {
            displayIdleState(player);
        }
    }

    private void addStack(Player player, UUID targetUUID) {
        UUID playerUUID = player.getUniqueId();
        Map<UUID, Integer> stacks = playerEntityStacks.get(playerUUID);
        int current = stacks.getOrDefault(targetUUID, 0);

        if (current < MAX_STACKS) {
            current++;
            stacks.put(targetUUID, current);
            stackExpiryTicks.get(playerUUID).put(targetUUID, STACK_DURATION_TICKS);
        }
    }

    private void triggerLightning(Player attacker, Entity target) {
        double damage = getBonusDamageByLevel(attacker.getLevel());

        target.getWorld().strikeLightning(target.getLocation());

        if (target instanceof org.bukkit.entity.LivingEntity living) {
            double currentHealth = living.getHealth();
            living.setHealth(Math.max(0, currentHealth - (damage * 2)));
        }
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

    private void displayStackInfo(Player player, int stacks) {
        player.sendActionBar(Component.text()
                .append(Component.text("§c⚡ " + stacks + "/" + MAX_STACKS))
                .build());
    }

    private void displayCooldownInfo(Player player) {
        String cooldownDisplay = getCooldownDisplay(player);
        player.sendActionBar(Component.text("§7⚡ " + cooldownDisplay));
    }

    private void displayIdleState(Player player) {
        player.sendActionBar(Component.text("§4⚡"));
    }
}