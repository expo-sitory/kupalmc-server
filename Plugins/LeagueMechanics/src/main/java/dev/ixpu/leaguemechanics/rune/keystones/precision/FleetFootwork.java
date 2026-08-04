package dev.ixpu.leaguemechanics.rune.keystones.precision;

import dev.ixpu.leaguemechanics.rune.BaseRune;
import dev.ixpu.leaguemechanics.rune.RunePath;
import dev.ixpu.leaguemechanics.rune.RuneSlot;
import net.kyori.adventure.text.Component;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import java.util.*;


public class FleetFootwork extends BaseRune {
    private int MAXIMUM_STACKS = 100;
    private double BLOCKS_PER_STACK = 10.0;
    private int PROJECTILE_STACK_GAIN = 5;
    private double HEAL_PERCENT = 0.60;

    private static final double MOVEMENT_SPEED_BONUS = 0.20;
    private static final int SPEED_BUFF_DURATION_TICKS = 100;

    private final Map<UUID, Integer> playerStacks = new HashMap<>();
    private final Map<UUID, org.bukkit.Location> lastLocation = new HashMap<>();
    private final Map<UUID, Double> distanceAccumulator = new HashMap<>();
    private final Map<UUID, Integer> speedBuffTicks = new HashMap<>();
    private final Map<UUID, List<AttributeModifier>> activeModifiers = new HashMap<>();

    public FleetFootwork(org.bukkit.configuration.ConfigurationSection config) {
        super("fleet-footwork", RunePath.PRECISION, RuneSlot.KEYSTONE);
        ConfigurationSection section = config.getConfigurationSection("runes.keystones.precision.fleet-footwork");
        this.MAXIMUM_STACKS = section.getInt("maximum-stacks", this.MAXIMUM_STACKS);
        this.BLOCKS_PER_STACK = section.getDouble("blocks-per-stack", this.BLOCKS_PER_STACK);
        this.PROJECTILE_STACK_GAIN = section.getInt("projectile-stack-gain", this.PROJECTILE_STACK_GAIN);
        this.HEAL_PERCENT = section.getDouble("heal-percent", this.HEAL_PERCENT);
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

    public void onProjectileHit(Player shooter, Entity target) {
        UUID playerUUID = shooter.getUniqueId();
        int stacks = playerStacks.getOrDefault(playerUUID, 0);

        if (!(target instanceof LivingEntity livingTarget)) {
            return;
        }

        if (livingTarget.getMaxHealth() < 20) {
            return;
        }

        for (int i = 0; i < PROJECTILE_STACK_GAIN; i++) {
            if (stacks < MAXIMUM_STACKS) {
                addStack(shooter);
                stacks++;
            }
        }

        if (stacks >= MAXIMUM_STACKS) {
            double maxHealth = shooter.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
            double currentHealth = shooter.getHealth();
            double missingHealth = maxHealth - currentHealth;
            double healAmount = missingHealth * HEAL_PERCENT;

            shooter.setHealth(Math.min(maxHealth, currentHealth + healAmount));
            playerStacks.put(playerUUID, 0);
            applySpeedBuff(shooter);
            distanceAccumulator.put(playerUUID, 0.0);
        } else if (speedBuffTicks.getOrDefault(playerUUID, 0) > 0) {
            refreshSpeedBuff(shooter);
        }
    }

    @Override
    public void tick(Player player) {
        UUID playerUUID = player.getUniqueId();

        org.bukkit.Location currentLoc = player.getLocation();
        org.bukkit.Location prevLoc = lastLocation.get(playerUUID);

        if (prevLoc != null && currentLoc.getWorld().equals(prevLoc.getWorld())) {
            double distance = currentLoc.distance(prevLoc);
            double accumulated = distanceAccumulator.getOrDefault(playerUUID, 0.0);
            accumulated += distance;

            while (accumulated >= BLOCKS_PER_STACK) {
                addStack(player);
                accumulated -= BLOCKS_PER_STACK;
            }

            distanceAccumulator.put(playerUUID, accumulated);
        }

        lastLocation.put(playerUUID, currentLoc.clone());

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

    private void addStack(Player player) {
        UUID playerUUID = player.getUniqueId();
        int current = playerStacks.getOrDefault(playerUUID, 0);
        int buffTicks = speedBuffTicks.getOrDefault(playerUUID, 0);

        if (buffTicks > 0) {
            return;
        }

        if (current < MAXIMUM_STACKS) {
            playerStacks.put(playerUUID, current + 1);
        }
    }

    @SuppressWarnings("removal")
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
        player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_BREEZE_IDLE_AIR, 1.0f, 1.2f);
    }

    private void refreshSpeedBuff(Player player) {
        UUID playerUUID = player.getUniqueId();
        speedBuffTicks.put(playerUUID, SPEED_BUFF_DURATION_TICKS);
    }

    private void removeAllModifiers(Player player) {
        UUID playerUUID = player.getUniqueId();
        List<AttributeModifier> mods = activeModifiers.getOrDefault(playerUUID, new ArrayList<>());
        for (AttributeModifier mod : mods) {
            try {
                Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)).removeModifier(mod);
                player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_BREEZE_LAND, 1.0f, 1.2f );
            } catch (Exception e) {
                //
            }
        }
        activeModifiers.put(playerUUID, new ArrayList<>());
    }

    private void displayStackInfo(Player player) {
        UUID playerUUID = player.getUniqueId();
        int stacks = playerStacks.getOrDefault(playerUUID, 0);
        int buffTicks = speedBuffTicks.getOrDefault(playerUUID, 0);

        if (buffTicks > 0) {
            double remainingSeconds = buffTicks / 20.0;
            player.sendActionBar(Component.text()
                    .append(Component.text(String.format("§e👣 " + "(%.1fs) ", remainingSeconds)))
                    .build());
        } else {

            player.sendActionBar(Component.text()
                    .append(Component.text("§e👣 " + stacks + "/" + MAXIMUM_STACKS))
                    .build());
        }
    }
}