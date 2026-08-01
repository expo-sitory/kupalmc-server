package dev.ixpu.leaguerunes.rune.keystones.precision;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import dev.ixpu.leaguerunes.rune.BaseRune;
import dev.ixpu.leaguerunes.rune.RunePath;
import dev.ixpu.leaguerunes.rune.RuneSlot;
import net.kyori.adventure.text.Component;

public class Conqueror extends BaseRune {
    private int MAX_STACKS = 12;
    private int STACK_DURATION_TICKS = 100; 
    private double DAMAGE_PER_STACK = 0.2;

    private final Map<UUID, Integer> playerStacks = new HashMap<>();
    private final Map<UUID, Integer> stackExpiryTicks = new HashMap<>();

    public Conqueror(org.bukkit.configuration.ConfigurationSection config) {
        super("conqueror", RunePath.PRECISION, RuneSlot.KEYSTONE);
<<<<<<< HEAD
        ConfigurationSection section = config.getConfigurationSection("runes.keystones.precision.conqueror");
=======
        ConfigurationSection section = config.getConfigurationSection("runes.keystones.precision.lethal-tempo");
>>>>>>> 87f6dcf62ac7990354dcef85cb2c12776c9fa559
        if (section != null) {
            this.MAX_STACKS = section.getInt("max-stacks", this.MAX_STACKS);
            this.STACK_DURATION_TICKS = section.getInt("stack-duration", this.STACK_DURATION_TICKS);
            this.DAMAGE_PER_STACK = section.getDouble("damage-per-stack", this.DAMAGE_PER_STACK);
        }
    }

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
        // Unused 
    }


    // Handle player damage events (melee and projectiles)
    public void onPlayerDamage(Player attacker, EntityDamageByEntityEvent event) {
        UUID playerUUID = attacker.getUniqueId();

        // Add stack
        addStack(attacker);

        // Apply damage bonus based on current stacks
        int stacks = playerStacks.getOrDefault(playerUUID, 0);
        double bonusDamage = stacks * DAMAGE_PER_STACK;
        event.setDamage(event.getDamage() + bonusDamage);

        displayStackInfo(attacker, stacks);
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
            player.playSound(player.getLocation(), Sound.ITEM_WOLF_ARMOR_CRACK, 1.0f, 1.2f);
        } else {
            // Refresh timer on max stacks
            stackExpiryTicks.put(playerUUID, STACK_DURATION_TICKS);
            player.playSound(player.getLocation(), Sound.ITEM_WOLF_ARMOR_BREAK, 1.0f, 1.2f);
        }
    }

    private void displayStackInfo(Player player, int stacks) {
        double totalDamage = stacks * DAMAGE_PER_STACK;

        player.sendActionBar(Component.text()
                .append(Component.text("§6☭ " + stacks + "/" + MAX_STACKS, net.kyori.adventure.text.format.NamedTextColor.WHITE))
                .append(Component.text(String.format(" (+%.1f dmg)", totalDamage), net.kyori.adventure.text.format.NamedTextColor.WHITE))
                .build());
    }
}
