package dev.ixpu.leaguerunes.rune.keystones.precision;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
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
        ConfigurationSection section = config.getConfigurationSection("runes.keystones.precision.conqueror");
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
        UUID playerUUID = attacker.getUniqueId();

        if (!(target instanceof LivingEntity)) {
            return;
        }
        
        LivingEntity livingTarget = (LivingEntity) target;
        double maxHealth = livingTarget.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        
        if (maxHealth < 20) {
            return;
        }

        addStack(attacker);

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
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "conqueror-expired-sound " + player.getName());
            }
        }

        int stacks = playerStacks.getOrDefault(playerUUID, 0);
        displayStackInfo(player, stacks);
    }

    private void addStack(Player player) {
        UUID playerUUID = player.getUniqueId();
        int current = playerStacks.getOrDefault(playerUUID, 0);

        if (current < MAX_STACKS) {
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

    private void displayStackInfo(Player player, int stacks) {
        if (stacks == 0) {
            player.sendActionBar(Component.text("§6🪓"));
            return;
        } else {
            player.sendActionBar(Component.text()
                    .append(Component.text("§e🪓 " + stacks + "/" + MAX_STACKS, net.kyori.adventure.text.format.NamedTextColor.WHITE))
                    .build());
        }
    }
}