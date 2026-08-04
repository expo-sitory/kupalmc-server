package dev.ixpu.leaguemechanics.rune.keystones.inspiration;

import java.util.*;

import dev.ixpu.leaguemechanics.LeagueMechanics;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import dev.ixpu.leaguemechanics.rune.BaseRune;
import dev.ixpu.leaguemechanics.rune.RunePath;
import dev.ixpu.leaguemechanics.rune.RuneSlot;
import net.kyori.adventure.text.Component;

public class GlacialAugment extends BaseRune {
    private double DAMAGE_REDUCTION = -0.20;

    int COOLDOWN_DURATION_SECONDS = 45;

    private static final int FREEZE_DURATION_TICKS = 120;

    private final Map<UUID, Map<UUID, Integer>> frozenTargets = new HashMap<>();
    private final Map<UUID, Map<UUID, List<AttributeModifier>>> targetModifiers = new HashMap<>();
    private LeagueMechanics plugin;

    public GlacialAugment(ConfigurationSection config, LeagueMechanics plugin) {
        super("glacial-augment", RunePath.INSPIRATION, RuneSlot.KEYSTONE);
        this.plugin = plugin;
        ConfigurationSection section = config.getConfigurationSection("runes.keystones.inspiration.glacial-augment");

        if (section != null) {
            this.DAMAGE_REDUCTION = section.getDouble("damage-reduction", this.DAMAGE_REDUCTION);
            this.COOLDOWN_DURATION_SECONDS = section.getInt("cooldown", COOLDOWN_DURATION_SECONDS);
        }
        this.setCooldownSeconds(COOLDOWN_DURATION_SECONDS);
    }

    @Override
    public void onEnable(Player player) {
        UUID uuid = player.getUniqueId();
        frozenTargets.put(uuid, new HashMap<>());
        targetModifiers.put(uuid, new HashMap<>());
    }

    @Override
    public void onDisable(Player player) {
        UUID uuid = player.getUniqueId();
        clearPlayerCooldown(player);
        frozenTargets.remove(uuid);
        targetModifiers.remove(uuid);
    }

    public void onProjectileHit(Player shooter, Entity target) {
        triggerGlacialAugment(shooter, target);
    }
    private void triggerGlacialAugment(Player player, Entity target) {
        if (isOnCooldown(player)) {
            return;
        }
        if (!(target instanceof LivingEntity livingTarget)) {
            return;
        }

        if (livingTarget.getMaxHealth() < 20) {
            return;
        }
        applyFreeze(player, livingTarget);
        resetCooldown(player);
    }

    @Override
    public void tick(Player player) {
        UUID playerUUID = player.getUniqueId();

        if (isOnCooldown(player)) {
            displayCooldown(player);
            return;
        }

        Map<UUID, Integer> frozen = frozenTargets.get(playerUUID);
        if (frozen == null) {
            displayIdleState(player);
            return;
        }

        java.util.ArrayList<UUID> toRemove = new java.util.ArrayList<>();
        for (UUID targetUUID : new java.util.ArrayList<>(frozen.keySet())) {
            int duration = frozen.getOrDefault(targetUUID, 0);

            if (duration > 0) {
                duration--;
                frozen.put(targetUUID, duration);

                if (duration == 0) {
                    toRemove.add(targetUUID);
                }
            } else {
                toRemove.add(targetUUID);
            }
        }

        for (UUID targetUUID : toRemove) {
            frozen.remove(targetUUID);
            removeTargetModifiers(targetUUID);
            targetModifiers.get(playerUUID).remove(targetUUID);
        }

        displayIdleState(player);
    }

    private void applyFreeze(Player attacker, LivingEntity target) {
        UUID attackerUUID = attacker.getUniqueId();
        UUID targetUUID = target.getUniqueId();

        Map<UUID, Integer> frozen = frozenTargets.get(attackerUUID);
        if (frozen == null) {
            return;
        }

        frozen.put(targetUUID, FREEZE_DURATION_TICKS);

        org.bukkit.Location loc = target.getLocation();

        List<org.bukkit.Location> snowLocations = new ArrayList<>();
        snowLocations.add(loc.clone());
        snowLocations.add(loc.clone().add(1, 0, 0));
        snowLocations.add(loc.clone().add(2, 0, 0));
        snowLocations.add(loc.clone().add(-1, 0, 0));
        snowLocations.add(loc.clone().add(-2, 0, 0));
        snowLocations.add(loc.clone().add(0, 0, 1));
        snowLocations.add(loc.clone().add(0, 0, 2));
        snowLocations.add(loc.clone().add(0, 0, -1));
        snowLocations.add(loc.clone().add(0, 0, -2));

        for (org.bukkit.Location snowLoc : snowLocations) {
            if (snowLoc.getBlock().getType() == org.bukkit.Material.AIR) {
                snowLoc.getBlock().setType(org.bukkit.Material.POWDER_SNOW);
            }
        }

        target.addPotionEffect(new PotionEffect(
                PotionEffectType.SLOWNESS,
                FREEZE_DURATION_TICKS,
                2,
                false,
                false
        ));

        attacker.playSound(target.getLocation(), org.bukkit.Sound.BLOCK_POWDER_SNOW_BREAK, 1.0f, 1.0f);

        if (plugin != null) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                target.removePotionEffect(PotionEffectType.SLOWNESS);

                for (org.bukkit.Location snowLoc : snowLocations) {
                    if (snowLoc.getBlock().getType() == org.bukkit.Material.POWDER_SNOW) {
                        snowLoc.getBlock().setType(org.bukkit.Material.AIR);
                    }
                }
            }, FREEZE_DURATION_TICKS);
        }
    }

    private void removeTargetModifiers(UUID targetUUID) {
        for (UUID playerUUID : frozenTargets.keySet()) {
            Map<UUID, List<AttributeModifier>> playerModifiers = targetModifiers.get(playerUUID);
            if (playerModifiers != null) {
                List<AttributeModifier> mods = playerModifiers.get(targetUUID);
                if (mods != null) {
                    for (LivingEntity entity : getAllLivingEntities()) {
                        if (entity.getUniqueId().equals(targetUUID)) {
                            for (AttributeModifier mod : mods) {
                                try {
                                    Objects.requireNonNull(entity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)).removeModifier(mod);
                                    Objects.requireNonNull(entity.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE)).removeModifier(mod);
                                } catch (Exception e) {
                                    //
                                }
                            }
                            break;
                        }
                    }
                }
            }
        }
    }

    private java.util.List<LivingEntity> getAllLivingEntities() {
        java.util.List<LivingEntity> entities = new ArrayList<>();
        for (org.bukkit.World world : Bukkit.getWorlds()) {
            entities.addAll(world.getLivingEntities());
        }
        return entities;
    }

    private void displayCooldown(Player player) {
        String cooldownDisplay = getCooldownDisplay(player);
        player.sendActionBar(Component.text("§7❄ " + cooldownDisplay));
    }

    private void displayIdleState(Player player) {
        player.sendActionBar(Component.text("§3❄"));
    }
}