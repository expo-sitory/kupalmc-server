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
import net.kyori.adventure.text.Component;

public class Conqueror extends BaseRune {
    private static final int MAX_STACKS = 5;
    private static final int STACK_DURATION_TICKS = 100; 
    private static final double DAMAGE_PER_STACK = 0.5;

    private final Map<UUID, Integer> playerStacks = new HashMap<>();
    private final Map<UUID, Integer> stackExpiryTicks = new HashMap<>();

    public Conqueror() {
        super(
                "conqueror",
                RunePath.PRECISION,
                RuneSlot.KEYSTONE
        );
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

    @Override
    public void onAttack(Player attacker, Entity target, EntityDamageByEntityEvent event) {
        UUID playerUUID = attacker.getUniqueId();

        // Add stack
        addStack(attacker);

        // Apply damage bonus based on current stacks
        int stacks = playerStacks.getOrDefault(playerUUID, 0);
        double bonusDamage = stacks * DAMAGE_PER_STACK;
        event.setDamage(event.getDamage() + bonusDamage);

        displayStackInfo(attacker, stacks);
    }


    // Handle projectile hits for bow/crossbow
    public void onProjectileHit(Player shooter, Entity target) {
        UUID playerUUID = shooter.getUniqueId();

        // Add stack
        addStack(shooter);

        // Apply damage bonus via entity damage 
        int stacks = playerStacks.getOrDefault(playerUUID, 0);
        double bonusDamage = stacks * DAMAGE_PER_STACK;

        // Try to apply knockback or visual effect to indicate damage
        if (target instanceof Player) {
            ((Player) target).damage(bonusDamage);
        }

        displayStackInfo(shooter, stacks);
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
            }
        }
    }

    private void addStack(Player player) {
        UUID playerUUID = player.getUniqueId();
        int current = playerStacks.getOrDefault(playerUUID, 0);

        if (current < MAX_STACKS) {
            current++;
            playerStacks.put(playerUUID, current);
            stackExpiryTicks.put(playerUUID, STACK_DURATION_TICKS);
        } else {
            // Refresh timer on max stacks
            stackExpiryTicks.put(playerUUID, STACK_DURATION_TICKS);
        }
    }

    private void displayStackInfo(Player player, int stacks) {
        double totalDamage = stacks * DAMAGE_PER_STACK;

        player.sendActionBar(Component.text()
                .append(Component.text("§6☭ " + stacks + "/5 ", net.kyori.adventure.text.format.NamedTextColor.WHITE))
                .append(Component.text(String.format("(+%.1f dmg)", totalDamage), net.kyori.adventure.text.format.NamedTextColor.WHITE))
                .build());
    }
}