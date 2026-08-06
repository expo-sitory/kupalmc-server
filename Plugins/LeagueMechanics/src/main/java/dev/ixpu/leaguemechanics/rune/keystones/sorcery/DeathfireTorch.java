package dev.ixpu.leaguemechanics.rune.keystones.sorcery;

import dev.ixpu.leaguemechanics.LeagueMechanics;
import dev.ixpu.leaguemechanics.rune.BaseRune;
import dev.ixpu.leaguemechanics.rune.RunePath;
import dev.ixpu.leaguemechanics.rune.RuneSlot;
import dev.ixpu.leaguemechanics.player.PlayerStats;


import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.entity.EntityDamageByEntityEvent;


import net.kyori.adventure.text.Component;

public class DeathfireTorch extends BaseRune {
    private double BASE_MAGIC_DAMAGE = 10.5;

    private static final int BURN_DURATION_TICKS = 100;

    private final Map<UUID, Map<UUID, Integer>> burnedPlayers = new HashMap<>();
    private final Map<UUID, Map<UUID, Double>> burnDamage = new HashMap<>();
    private final Map<UUID, Map<UUID, LivingEntity>> burnedTargets = new HashMap<>();
    private LeagueMechanics plugin;

    public DeathfireTorch(ConfigurationSection config) {
        super("deathfire-torch", RunePath.SORCERY, RuneSlot.KEYSTONE);
        ConfigurationSection section = config.getConfigurationSection("runes.keystones.sorcery.deathfire-torch");
        if (section != null) {
            this.BASE_MAGIC_DAMAGE = section.getDouble("damage-interval", this.BASE_MAGIC_DAMAGE);
        }
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
        burnedPlayers.remove(uuid);
        burnDamage.remove(uuid);
        burnedTargets.remove(uuid);
    }

    @Override
    public void onAttack(Player attacker, Entity target, EntityDamageByEntityEvent event) {
        triggerDeathFireTorch(attacker, target, event);
    }
    public void onProjectileHit(Player shooter, Entity target) {
        triggerDeathFireTorch(shooter, target, null);
    }

    public void triggerDeathFireTorch(Player player, Entity target, EntityDamageByEntityEvent event) {
        ItemStack weapon = player.getInventory().getItemInMainHand();

        if (!(target instanceof LivingEntity livingTarget)) {
            return;
        }
        if (livingTarget.getMaxHealth() < 20) {
            return;
        }
        if (!CheckEnchant(weapon, player)) {
            return;
        }

        double totalOutput = BASE_MAGIC_DAMAGE / 4;
        if (event != null) {
            applyBurn(player, livingTarget, totalOutput);
        }
        applyBurn(player, livingTarget, totalOutput);

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

    private boolean CheckEnchant(ItemStack item, Player player) {
        if (item == null || item.getType().isAir()) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }

        return meta.getEnchants().keySet().stream()
                .anyMatch(e -> {
                    String enchantName = e.toString().toLowerCase();
                    return enchantName.contains("fire_aspect") || enchantName.contains("flame");
                });
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


    @Override
    public void tick(Player player) {
        UUID playerUUID = player.getUniqueId();

        Map<UUID, Integer> burned = burnedPlayers.get(playerUUID);
        Map<UUID, Double> damages = burnDamage.get(playerUUID);
        Map<UUID, LivingEntity> targets = burnedTargets.get(playerUUID);

        if (burned == null || damages == null || targets == null) {
            String runeDisplay = getRuneDisplay(RuneState.IDLE, 0);
            setPlayerDisplay(player, runeDisplay);
            return;
        }

        java.util.ArrayList<UUID> toRemove = new java.util.ArrayList<>();
        for (UUID targetUUID : new java.util.ArrayList<>(burned.keySet())) {
            int duration = burned.getOrDefault(targetUUID, 0);

            if (duration > 0) {
                duration--;
                burned.put(targetUUID, duration);

                if (duration % BASE_MAGIC_DAMAGE == 0) {
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
        RuneState state = burnCount > 0 ? RuneState.ACTIVE : RuneState.IDLE;
        String runeDisplay = getRuneDisplay(state, burnCount);
        setPlayerDisplay(player, runeDisplay);
    }

    private void setPlayerDisplay(Player player, String runeDisplay) {
        PlayerStats playerStats = new PlayerStats();
        String statsDisplay = playerStats.getActionBarSections(player);
        
        String actionBarMessage = runeDisplay + " " + statsDisplay;
        player.sendActionBar(Component.text(actionBarMessage));
    }

    enum RuneState {
        ACTIVE, IDLE
    }

    private String getRuneDisplay(RuneState state, int victimCount) {
        return switch (state) {
            case ACTIVE -> "§9🔥 (" + victimCount + ")";
            case IDLE -> "§9🔥";
        };
    }

}