package dev.ixpu.leaguemechanics.rune.keystones.domination;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import dev.ixpu.leaguemechanics.rune.BaseRune;
import dev.ixpu.leaguemechanics.rune.RunePath;
import dev.ixpu.leaguemechanics.rune.RuneSlot;
import net.kyori.adventure.text.Component;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class Electrocute extends BaseRune {

    private double BASE_PHYSICAL_DAMAGE = 3.5;
    private int MAXIMUM_STACKS = 3;

    int COOLDOWN_DURATION_SECONDS = 25;

    private static final int STACK_DURATION_TICKS = 60;

    private final Map<UUID, Map<UUID, Integer>> playerEntityStacks = new HashMap<>();
    private final Map<UUID, Map<UUID, Integer>> stackExpiryTicks = new HashMap<>();

    public Electrocute(org.bukkit.configuration.ConfigurationSection config) {
        super("electrocute", RunePath.DOMINATION, RuneSlot.KEYSTONE);
        ConfigurationSection section = config.getConfigurationSection("runes.keystones.domination.electrocute");
        if (section != null) {
            this.BASE_PHYSICAL_DAMAGE = section.getDouble("base-physical-damage", this.BASE_PHYSICAL_DAMAGE);
            this.MAXIMUM_STACKS = section.getInt("maximum-stacks", this.MAXIMUM_STACKS);
            this.COOLDOWN_DURATION_SECONDS = section.getInt("cooldown", COOLDOWN_DURATION_SECONDS);
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
        triggerElectrocute(attacker, target, event);
    }

    public void onProjectileHit(Player shooter, Entity target) {
        triggerElectrocute(shooter, target, null);
    }
    private void triggerElectrocute(Player player, Entity target, EntityDamageByEntityEvent event) {
        UUID playerUUID = player.getUniqueId();
        UUID targetUUID = target.getUniqueId();

        if (!(target instanceof LivingEntity livingTarget)) {
            return;
        }
        if (livingTarget.getMaxHealth() < 20) {
            return;
        }
        if (isOnCooldown(player)) {
            return;
        }

        addStack(player, targetUUID);

        int stacks = playerEntityStacks.get(playerUUID).getOrDefault(targetUUID, 0);
        if (stacks >= MAXIMUM_STACKS) {
            target.getWorld().strikeLightning(target.getLocation());
            resetCooldown(player);
            playerEntityStacks.get(playerUUID).remove(targetUUID);
            stackExpiryTicks.get(playerUUID).remove(targetUUID);

            player.playSound(player.getLocation(), Sound.ITEM_TRIDENT_THUNDER, 1.0f, 1.2f);
            double totalOutput = BASE_PHYSICAL_DAMAGE / 2;

            if (event != null) {
                event.setDamage(event.getDamage() + totalOutput);
            } else {
                livingTarget.damage(totalOutput, player);
            }
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

        if (current < MAXIMUM_STACKS) {
            current++;
            stacks.put(targetUUID, current);
            stackExpiryTicks.get(playerUUID).put(targetUUID, STACK_DURATION_TICKS);
        }
    }

    private void displayStackInfo(Player player, int stacks) {
        player.sendActionBar(Component.text()
                .append(Component.text("§c⚡ " + stacks + "/" + MAXIMUM_STACKS))
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