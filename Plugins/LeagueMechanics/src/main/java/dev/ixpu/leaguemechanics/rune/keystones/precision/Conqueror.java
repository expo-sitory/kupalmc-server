package dev.ixpu.leaguemechanics.rune.keystones.precision;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import dev.ixpu.leaguemechanics.rune.BaseRune;
import dev.ixpu.leaguemechanics.rune.RunePath;
import dev.ixpu.leaguemechanics.rune.RuneSlot;
import net.kyori.adventure.text.Component;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class Conqueror extends BaseRune {
    private double BASE_PHYSICAL_DAMAGE_PER_STACK = 2.5;

    private int MAXIMUM_STACKS = 12;

    private static final int STACK_DURATION_TICKS = 100;

    private final Map<UUID, Integer> playerStacks = new HashMap<>();
    private final Map<UUID, Integer> stackExpiryTicks = new HashMap<>();

    public Conqueror(org.bukkit.configuration.ConfigurationSection config) {
        super("conqueror", RunePath.PRECISION, RuneSlot.KEYSTONE);
        ConfigurationSection section = config.getConfigurationSection("runes.keystones.precision.conqueror");
        if (section != null) {
            this.BASE_PHYSICAL_DAMAGE_PER_STACK = section.getDouble("attack-damage-per-stack", this.BASE_PHYSICAL_DAMAGE_PER_STACK);
            this.MAXIMUM_STACKS = section.getInt("maximum-stacks", this.MAXIMUM_STACKS);
        }
    }

    @Override
    public void onEnable(Player player) {
        UUID uuid = player.getUniqueId();
        playerStacks.put(uuid, 0);
        stackExpiryTicks.put(uuid, 0);
    }

    @Override
    public void onDisable(Player player) {
        UUID uuid = player.getUniqueId();
        playerStacks.remove(uuid);
        stackExpiryTicks.remove(uuid);
    }

    public void onAttack(Player attacker, Entity target, EntityDamageByEntityEvent event) {
        UUID playerUUID = attacker.getUniqueId();

        if (!(target instanceof LivingEntity livingTarget)) {
            return;
        }

        if (livingTarget.getMaxHealth() < 20) {
            return;
        }

        addStack(attacker);

        int CURRENT_STACKS = playerStacks.getOrDefault(playerUUID, 0);
        double totalOutput = CURRENT_STACKS * BASE_PHYSICAL_DAMAGE_PER_STACK;

        event.setDamage(event.getDamage() + totalOutput);

        displayStackInfo(attacker, CURRENT_STACKS);
    }

    @Override
    public void tick(Player player) {
        UUID playerUUID = player.getUniqueId();
        int expiry = stackExpiryTicks.getOrDefault(playerUUID, 0);

        if (expiry > 0) {
            expiry--;
            stackExpiryTicks.put(playerUUID, expiry);

            if (expiry == 0) {
                playerStacks.put(playerUUID, 0);
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "conqueror-expired-sound " + player.getName());
            }
        }

        int CURRENT_STACKS = playerStacks.getOrDefault(playerUUID, 0);
        displayStackInfo(player, CURRENT_STACKS);
    }

    private void addStack(Player player) {
        UUID playerUUID = player.getUniqueId();
        int current = playerStacks.getOrDefault(playerUUID, 0);

        if (current < MAXIMUM_STACKS) {
            current++;
            playerStacks.put(playerUUID, current);
            stackExpiryTicks.put(playerUUID, STACK_DURATION_TICKS);
            if (current == 1) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "conqueror-stack-sound " + player.getName());
            }
        } else {
            stackExpiryTicks.put(playerUUID, STACK_DURATION_TICKS);
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "conqueror-max-stack-sound " + player.getName());
            player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 0.5f);
        }
    }

    private void displayStackInfo(Player player, int CURRENT_STACKS) {
        if (CURRENT_STACKS == 0) {
            player.sendActionBar(Component.text("§6🪓"));
        } else {
            player.sendActionBar(Component.text()
                    .append(Component.text("§e🪓 " + CURRENT_STACKS + "/" + MAXIMUM_STACKS))
                    .build());
        }
    }
}