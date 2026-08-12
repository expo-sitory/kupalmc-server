package dev.ixpu.leaguemechanics.rune.keystones.sorcery;

import dev.ixpu.leaguemechanics.LeagueMechanics;
import dev.ixpu.leaguemechanics.rune.BaseRune;
import dev.ixpu.leaguemechanics.rune.RunePath;
import dev.ixpu.leaguemechanics.rune.RuneSlot;
import dev.ixpu.leaguemechanics.player.PlayerStats;
import dev.ixpu.leaguemechanics.util.DebugLogger;
import dev.ixpu.leaguemechanics.manager.DamageManager;
import dev.ixpu.leaguemechanics.manager.BuffManager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.entity.Entity;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.configuration.ConfigurationSection;

import net.kyori.adventure.text.Component;

public class ArcaneComet extends BaseRune {
    double BASE_ADAPTIVE_DAMAGE = 20.5;

    private double AD_PERCENTAGE_MULTIPLIER = 0.05;
    private double AP_PERCENTAGE_MULTIPLIER = 0.15;

    int COOLDOWN_SECONDS = 20;

    private static final int COMET_FALL_TICKS = 30;

    private final Map<UUID, Long> lastCometTime = new HashMap<>();
    private LeagueMechanics plugin;

    public ArcaneComet(ConfigurationSection config, LeagueMechanics plugin) {
        super("arcane-comet", RunePath.SORCERY, RuneSlot.KEYSTONE);
        ConfigurationSection section = config.getConfigurationSection("runes.keystones.sorcery.arcane-comet");
        this.plugin = plugin;
        if (section != null) {
            this.BASE_ADAPTIVE_DAMAGE = section.getDouble("base-adaptive-damage", this.BASE_ADAPTIVE_DAMAGE);
            this.AD_PERCENTAGE_MULTIPLIER = section.getDouble("ad-percentage-multiplier", this.AD_PERCENTAGE_MULTIPLIER);
            this.AP_PERCENTAGE_MULTIPLIER = section.getDouble("ap-percentage-multiplier", this.AP_PERCENTAGE_MULTIPLIER);
            this.COOLDOWN_SECONDS = section.getInt("cooldown", this.COOLDOWN_SECONDS);
        }
        this.setCooldownSeconds(COOLDOWN_SECONDS);
    }

    @Override
    public void onEnable(Player player) {
        UUID uuid = player.getUniqueId();
        lastCometTime.put(uuid, 0L);
    }

    @Override
    public void onDisable(Player player) {
        UUID uuid = player.getUniqueId();
        clearPlayerCooldown(player);
        lastCometTime.remove(uuid);
    }

    public void onProjectileHit(Player shooter, Entity target) {
        if (!(target instanceof LivingEntity livingTarget)) {
            return;
        }

        double statsDamage = playerDamage(shooter, target);
        double newHealth = Math.clamp(livingTarget.getHealth() - statsDamage, 0, livingTarget.getMaxHealth());

        DebugLogger.debug(shooter, "§7[Debug] §f[§dAttacker§f] (Projectile) Stats Damage = §d" + Math.ceil(statsDamage * 100) / 100.0);
        DebugLogger.debug(shooter, "§7[Debug] §f[§dTarget§f] Target New HP = §d" + Math.ceil(newHealth * 100) / 100.0);

        livingTarget.setHealth(newHealth);

        triggerArcaneComet(shooter, target);
    }

    public void onAttack(Player attacker, Entity target) {
        if (!(target instanceof LivingEntity livingTarget)) {
            return;
        }

        double statsDamage = playerDamage(attacker, target);
        double newHealth = Math.clamp(livingTarget.getHealth() - statsDamage, 0, livingTarget.getMaxHealth());

        DebugLogger.debug(attacker, "§7[Debug] §f[§dAttacker§f] (Melee) Stats Damage = §d" + Math.ceil(statsDamage * 100) / 100.0);
        DebugLogger.debug(attacker, "§7[Debug] §f[§dTarget§f] Target New HP = §d" + Math.ceil(newHealth * 100) / 100.0);

        livingTarget.setHealth(newHealth);
    }

    private void  triggerArcaneComet(Player player, Entity target) {
        if (!(target instanceof LivingEntity livingTarget)) {
            return;
        }
        if (livingTarget.getMaxHealth() < 20) {
            return;
        }
        if (isOnCooldown(player)){
            return;
        }

        double newHealth = Math.clamp(livingTarget.getHealth() - keystoneDamage(player, target), 0, livingTarget.getMaxHealth());

        summonComet(player, livingTarget, newHealth);
        resetCooldown(player);
    }

    private double keystoneDamage(Player player, Entity target) {
        DamageManager damageManager = new DamageManager();
        damageManager.enableAdaptiveScaling();

        double baseDamage = damageManager.totalBonusDamage(player, target, 0);
        double scaledBonus = getScaledBonusDamage(player);

        return baseDamage + scaledBonus;
    }


    private double playerDamage(Player player, Entity target) {
        DamageManager damageManager = new DamageManager();
        return damageManager.totalBonusDamage(player, target, 0);
    }

    private double getScaledBonusDamage(Player player) {
        BuffManager buffManager = new BuffManager();
        return buffManager.calculateBuffValue(
                player,
                0,
                AD_PERCENTAGE_MULTIPLIER,
                AP_PERCENTAGE_MULTIPLIER
        );
    }

    private void summonComet(Player shooter, LivingEntity target, double newHealth) {
        if (plugin == null) {
            return;
        }

        Location targetLoc = target.getLocation().clone().add(0, 1, 0);
        Location skyLoc = targetLoc.clone().add(0, 50, 0);

        int[] taskId = {-1};

        taskId[0] = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
            int tick = 0;

            @Override
            public void run() {
                if (tick >= COMET_FALL_TICKS) {
                    target.damage(0.00001);
                    target.setHealth(newHealth);
                    DebugLogger.debug(shooter, "§7[Debug] §f[§dAttacker§f] §f[§9Arcane Comet§f] Keystone Damage = §d" + Math.ceil(keystoneDamage(shooter, target) * 100) / 100.0);
                    DebugLogger.debug(shooter, "§7[Debug] §f[§dTarget§f] Target New HP = §d" + newHealth);

                    targetLoc.getWorld().spawnParticle(
                            Particle.DUST,
                            targetLoc,
                            15,
                            0.5, 0.5, 0.5,
                            new Particle.DustOptions(Color.BLUE, 1.5f)
                    );

                    shooter.playSound(targetLoc, org.bukkit.Sound.ENTITY_GENERIC_EXPLODE, 0.8f, 1.5f);
                    plugin.getServer().getScheduler().cancelTask(taskId[0]);
                    return;
                }

                double progress = (double) tick / COMET_FALL_TICKS;
                Location cometLoc = skyLoc.clone().add(
                        (targetLoc.getX() - skyLoc.getX()) * progress,
                        (targetLoc.getY() - skyLoc.getY()) * progress,
                        (targetLoc.getZ() - skyLoc.getZ()) * progress
                );

                targetLoc.getWorld().spawnParticle(
                        Particle.DUST,
                        cometLoc,
                        3,
                        0.15, 0.15, 0.15,
                        new Particle.DustOptions(Color.BLUE, 1.0f)
                );

                tick++;
            }
        }, 0, 1);
    }

    @Override
    public void tick(Player player) {
        RuneState state = isOnCooldown(player) ? RuneState.COOLDOWN : RuneState.IDLE;
        String runeDisplay = getRuneDisplay(state, player);
        setPlayerDisplay(player, runeDisplay);
    }

    private void setPlayerDisplay(Player player, String runeDisplay) {
        PlayerStats playerStats = new PlayerStats();
        String statsDisplay = playerStats.getActionBarSections(player);

        String actionBarMessage = runeDisplay + " " + statsDisplay;
        player.sendActionBar(Component.text(actionBarMessage));
    }

    enum RuneState {
        COOLDOWN, IDLE
    }

    private String getRuneDisplay(RuneState state, Player player) {
        return switch (state) {
            case COOLDOWN -> "§7🌠" + getCooldownDisplay(player);
            case IDLE -> "§1🌠";
        };
    }

}