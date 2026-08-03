package dev.ixpu.leaguemechanics.rune.keystones.sorcery;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import dev.ixpu.leaguemechanics.LeagueMechanics;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import dev.ixpu.leaguemechanics.rune.BaseRune;
import dev.ixpu.leaguemechanics.rune.RunePath;
import dev.ixpu.leaguemechanics.rune.RuneSlot;
import net.kyori.adventure.text.Component;

public class DeathfireTorch extends BaseRune {
    private int BURN_DURATION_TICKS = 100;
    private int DAMAGE_INTERVAL_TICKS = 10;

    private final Map<UUID, Map<UUID, Integer>> burnedPlayers = new HashMap<>();
    private final Map<UUID, Map<UUID, Double>> burnDamage = new HashMap<>();
    private final Map<UUID, Map<UUID, LivingEntity>> burnedTargets = new HashMap<>();
    private LeagueMechanics plugin;

    public DeathfireTorch(ConfigurationSection config) {
        super("deathfire-torch", RunePath.SORCERY, RuneSlot.KEYSTONE);
        ConfigurationSection section = config.getConfigurationSection("runes.keystones.sorcery.deathfire-torch");
        if (section != null) {
            this.BURN_DURATION_TICKS = section.getInt("burn-duration", this.BURN_DURATION_TICKS);
            this.DAMAGE_INTERVAL_TICKS = section.getInt("damage-interval", this.DAMAGE_INTERVAL_TICKS);
        }
    }

    public void setPlugin(LeagueMechanics plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onEnable(Player player) {
        UUID uuid = player.getUniqueId();
        burnedPlayers.put(uuid, new HashMap<>());
        burnDamage.put(uuid, new HashMap<>());
        burnedTargets.put(uuid, new HashMap<>());
    }

    @Override
    public void onDisable(Player player) {
        UUID uuid = player.getUniqueId();
        if (burnedPlayers != null) burnedPlayers.remove(uuid);
        if (burnDamage != null) burnDamage.remove(uuid);
        if (burnedTargets != null) burnedTargets.remove(uuid);
    }

    @Override
    public void onAttack(Player attacker, Entity target, EntityDamageByEntityEvent event) {
        if (!(target instanceof LivingEntity livingTarget)) {
            return;
        }

        if (livingTarget.getMaxHealth() < 20) {
            return;
        }

        ItemStack weapon = attacker.getInventory().getItemInMainHand();
        if (!hasFireAspect(weapon, attacker)) {
            return;
        }

        double burnDamagePerTick = getBonusDamageByLevel(attacker.getLevel()) / 2.0;
        applyBurn(attacker, livingTarget, burnDamagePerTick);
    }


    public void onProjectileHit(Player shooter, Entity target) {
        if (!(target instanceof LivingEntity livingTarget)) {
            return;
        }

        if (livingTarget.getMaxHealth() < 20) {
            return;
        }

        double burnDamagePerTick = getBonusDamageByLevel(shooter.getLevel()) / 2.0;
        applyBurn(shooter, livingTarget, burnDamagePerTick);
    }

    @Override
    public void tick(Player player) {
        UUID playerUUID = player.getUniqueId();

        Map<UUID, Integer> burned = burnedPlayers.get(playerUUID);
        Map<UUID, Double> damages = burnDamage.get(playerUUID);
        Map<UUID, LivingEntity> targets = burnedTargets.get(playerUUID);

        if (burned == null || damages == null || targets == null) {
            displayIdleState(player);
            return;
        }

        java.util.ArrayList<UUID> toRemove = new java.util.ArrayList<>();
        for (UUID targetUUID : new java.util.ArrayList<>(burned.keySet())) {
            int duration = burned.getOrDefault(targetUUID, 0);

            if (duration > 0) {
                duration--;
                burned.put(targetUUID, duration);

                if (duration % DAMAGE_INTERVAL_TICKS == 0) {
                    LivingEntity target = targets.get(targetUUID);
                    if (target != null && target.isValid()) {
                        double damagePerTick = damages.getOrDefault(targetUUID, 0.0);
                        target.damage(damagePerTick);
                        spawnBurnParticles(target);
                    }
                }

                if (duration <= 0) {
                    toRemove.add(targetUUID);
                }
            } else {
                toRemove.add(targetUUID);
            }
        }

        for (UUID targetUUID : toRemove) {
            burned.remove(targetUUID);
            damages.remove(targetUUID);
            targets.remove(targetUUID);
        }

        int burnCount = burned.size();
        if (burnCount > 0) {
            displayBurnState(player, burnCount);
        } else {
            displayIdleState(player);
        }
    }

    private void applyBurn(Player attacker, LivingEntity victim, double burnDamagePerTick) {
        UUID attackerUUID = attacker.getUniqueId();
        UUID victimUUID = victim.getUniqueId();

        Map<UUID, Integer> burned = burnedPlayers.get(attackerUUID);
        Map<UUID, Double> damages = burnDamage.get(attackerUUID);
        Map<UUID, LivingEntity> targets = burnedTargets.get(attackerUUID);

        if (burned == null || damages == null || targets == null) {
            return;
        }

        burned.put(victimUUID, BURN_DURATION_TICKS);
        damages.put(victimUUID, burnDamagePerTick);
        targets.put(victimUUID, victim);

        attacker.playSound(victim.getLocation(), org.bukkit.Sound.BLOCK_FIRE_AMBIENT, 0.5f, 0.8f);
    }

    private boolean hasFireAspect(ItemStack item, Player player) {
        if (item == null || item.getType().isAir()) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }

        return meta.getEnchants().keySet().stream()
                .anyMatch(e -> e.toString().toLowerCase().contains("fire"));
    }

    private void spawnBurnParticles(LivingEntity victim) {
        Location loc = victim.getLocation().add(0, 1, 0);
        victim.getWorld().spawnParticle(
                Particle.DUST,
                loc,
                20,
                0.2, 0.2, 0.2,
                new Particle.DustOptions(Color.fromRGB(0, 150, 255), 1.0f)
        );
    }

    private double getBonusDamageByLevel(int totalLevel) {
        if (totalLevel >= 70) {
            return 2.5;
        } else if (totalLevel >= 51) {
            return 2.0;
        } else if (totalLevel >= 31) {
            return 1.5;
        } else if (totalLevel >= 20) {
            return 1.0;
        } else {
            return 0.5;
        }
    }

    private void displayBurnState(Player player, int victimCount) {
        player.sendActionBar(Component.text()
                .append(Component.text("§9🔥 (" + victimCount + ")"))
                .build());
    }

    private void displayIdleState(Player player) {
        player.sendActionBar(Component.text("§1🔥"));
    }
}