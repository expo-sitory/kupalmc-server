package dev.ixpu.leaguerunes.rune.keystones.precision;

import dev.ixpu.leaguerunes.rune.BaseRune;
import dev.ixpu.leaguerunes.rune.RunePath;
import dev.ixpu.leaguerunes.rune.RuneSlot;
import net.kyori.adventure.text.Component;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class FleetFootwork extends BaseRune {
    private static final int MAX_STACKS = 100;
    private static final double BLOCKS_PER_STACK = 10.0;
    private static final int PROJECTILE_STACK_GAIN = 5;
    private static final double HEAL_PERCENT = 0.40; 
    private static final double MOVEMENT_SPEED_BONUS = 0.20;  
    private static final int SPEED_BUFF_DURATION_TICKS = 100; 

    private final Map<UUID, Integer> playerStacks = new HashMap<>();
    private final Map<UUID, org.bukkit.Location> lastLocation = new HashMap<>();
    private final Map<UUID, Double> distanceAccumulator = new HashMap<>();
    private final Map<UUID, Integer> speedBuffTicks = new HashMap<>();
    private final Map<UUID, List<AttributeModifier>> activeModifiers = new HashMap<>();

    public FleetFootwork() {
        super(
                "fleet-footwork",
                RunePath.PRECISION,
                RuneSlot.KEYSTONE
        );
    }

    @Override
    public void onEnable(Player player) {
        UUID uuid = player.getUniqueId();
        playerStacks.put(uuid, 0);
        lastLocation.put(uuid, player.getLocation().clone());
        distanceAccumulator.put(uuid, 0.0);
        speedBuffTicks.put(uuid, 0);
    }

    @Override
    public void onDisable(Player player) {
        UUID uuid = player.getUniqueId();
        playerStacks.remove(uuid);
        lastLocation.remove(uuid);
        distanceAccumulator.remove(uuid);
        speedBuffTicks.remove(uuid);
        removeAllModifiers(player);
    }

    @Override
    public void tick(Player player) {
        UUID playerUUID = player.getUniqueId();

        // Handle movement stacking
        org.bukkit.Location currentLoc = player.getLocation();
        org.bukkit.Location prevLoc = lastLocation.get(playerUUID);

        if (prevLoc != null && currentLoc.getWorld().equals(prevLoc.getWorld())) {
            double distance = currentLoc.distance(prevLoc);
            double accumulated = distanceAccumulator.getOrDefault(playerUUID, 0.0);
            accumulated += distance;

            // Grant stacks per 5 blocks
            while (accumulated >= BLOCKS_PER_STACK) {
                addStack(player);
                accumulated -= BLOCKS_PER_STACK;
            }

            distanceAccumulator.put(playerUUID, accumulated);
        }

        lastLocation.put(playerUUID, currentLoc.clone());

        // Handle speed buff countdown
        int buffTicks = speedBuffTicks.getOrDefault(playerUUID, 0);
        if (buffTicks > 0) {
            buffTicks--;
            speedBuffTicks.put(playerUUID, buffTicks);

            if (buffTicks == 0) {
                removeAllModifiers(player);
            }
        }

        displayStackInfo(player);
    }

    @Override
    public void onAttack(Player attacker, Entity target, EntityDamageByEntityEvent event) {
        UUID playerUUID = attacker.getUniqueId();
        int stacks = playerStacks.getOrDefault(playerUUID, 0);

        if (stacks >= MAX_STACKS) {
            // Heal player for 30% missing HP
            double maxHealth = attacker.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
            double currentHealth = attacker.getHealth();
            double missingHealth = maxHealth - currentHealth;
            double healAmount = missingHealth * HEAL_PERCENT;

            attacker.setHealth(Math.min(maxHealth, currentHealth + healAmount));

            // Consume stacks and apply speed buff
            playerStacks.put(playerUUID, 0);
            applySpeedBuff(attacker);
            distanceAccumulator.put(playerUUID, 0.0);
        }
    }

    // Handle projectile hits for bow/crossbow
    public void onProjectileHit(Player shooter, Entity target) {
        UUID playerUUID = shooter.getUniqueId();
        int stacks = playerStacks.getOrDefault(playerUUID, 0);

        // Add stacks from projectile
        for (int i = 0; i < PROJECTILE_STACK_GAIN; i++) {
            if (stacks < MAX_STACKS) {
                addStack(shooter);
                stacks++;
            }
        }

        // Check if reached max stacks after projectile
        if (stacks >= MAX_STACKS) {
            // Heal player for 30% missing HP
            double maxHealth = shooter.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
            double currentHealth = shooter.getHealth();
            double missingHealth = maxHealth - currentHealth;
            double healAmount = missingHealth * HEAL_PERCENT;

            shooter.setHealth(Math.min(maxHealth, currentHealth + healAmount));

            // Consume stacks and apply speed buff
            playerStacks.put(playerUUID, 0);
            applySpeedBuff(shooter);
            distanceAccumulator.put(playerUUID, 0.0);
        }
    }

    private void addStack(Player player) {
        UUID playerUUID = player.getUniqueId();
        int current = playerStacks.getOrDefault(playerUUID, 0);

        if (current < MAX_STACKS) {
            playerStacks.put(playerUUID, current + 1);
        }
    }

    private void applySpeedBuff(Player player) {
        UUID playerUUID = player.getUniqueId();
        removeAllModifiers(player);

        var modifier = new AttributeModifier(
                UUID.randomUUID(),
                "fleet-footwork-speed",
                MOVEMENT_SPEED_BONUS,
                AttributeModifier.Operation.MULTIPLY_SCALAR_1
        );

        var movementAttr = player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
        if (movementAttr != null) {
            movementAttr.addModifier(modifier);
            activeModifiers.computeIfAbsent(playerUUID, k -> new ArrayList<>()).add(modifier);
        }

        speedBuffTicks.put(playerUUID, SPEED_BUFF_DURATION_TICKS);
    }

    private void removeAllModifiers(Player player) {
        UUID playerUUID = player.getUniqueId();
        List<AttributeModifier> mods = activeModifiers.getOrDefault(playerUUID, new ArrayList<>());
        for (AttributeModifier mod : mods) {
            try {
                player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).removeModifier(mod);
            } catch (Exception e) {
                // Modifier already removed
            }
        }
        activeModifiers.put(playerUUID, new ArrayList<>());
    }

    private void displayStackInfo(Player player) {
        UUID playerUUID = player.getUniqueId();
        int stacks = playerStacks.getOrDefault(playerUUID, 0);
        int buffTicks = speedBuffTicks.getOrDefault(playerUUID, 0);

        if (buffTicks > 0) {
            // Show speed buff timer
            double remainingSeconds = buffTicks / 20.0;
            player.sendActionBar(Component.text()
                    .append(Component.text(String.format("§e👣 " + "%.1fs " + "§f(+20%% mvs)", remainingSeconds), net.kyori.adventure.text.format.NamedTextColor.WHITE))
                    .build());
        } else {

            player.sendActionBar(Component.text()
                    .append(Component.text("§e👣 " + stacks + "/100", net.kyori.adventure.text.format.NamedTextColor.WHITE))
                    .build());
        }
    }
}
