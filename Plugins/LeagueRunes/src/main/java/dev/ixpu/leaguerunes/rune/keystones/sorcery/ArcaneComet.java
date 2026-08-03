package dev.ixpu.leaguerunes.rune.keystones.sorcery;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import dev.ixpu.leaguerunes.LeagueRunes;
import dev.ixpu.leaguerunes.rune.BaseRune;
import dev.ixpu.leaguerunes.rune.RunePath;
import dev.ixpu.leaguerunes.rune.RuneSlot;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class ArcaneComet extends BaseRune {
    private int COMET_FALL_TICKS = 30;

    private final Map<UUID, Long> lastCometTime = new HashMap<>();
    private LeagueRunes plugin;

    public ArcaneComet(ConfigurationSection config) {
        super("arcane-comet", RunePath.SORCERY, RuneSlot.KEYSTONE);
        ConfigurationSection section = config.getConfigurationSection("runes.keystones.sorcery.arcane-comet");
        int COOLDOWN_SECONDS = 20;
        if (section != null) {
            this.COMET_FALL_TICKS = section.getInt("comet-fall-ticks", this.COMET_FALL_TICKS);
            COOLDOWN_SECONDS = section.getInt("cooldown", COOLDOWN_SECONDS);
        }
        this.setCooldownSeconds(COOLDOWN_SECONDS);
    }

    public void setPlugin(LeagueRunes plugin) {
        this.plugin = plugin;
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

        if (livingTarget.getMaxHealth() < 20) {
            return;
        }

        if (isOnCooldown(shooter)) {
            return;
        }

        double bonusDamage = getBonusDamageByLevel(shooter.getLevel());
        fireCometStrike(shooter, (LivingEntity) target, bonusDamage);
        resetCooldown(shooter);
    }

    @Override
    public void tick(Player player) {
        if (isOnCooldown(player)) {
            displayCooldownInfo(player);
            return;
        }
        displayIdleState(player);
    }

    private void fireCometStrike(Player shooter, LivingEntity target, double bonusDamage) {
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
                    target.damage(bonusDamage * 2.0);

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

    private double getBonusDamageByLevel(int totalLevel) {
        if (totalLevel >= 100) {
            return 3.8;
        } else if (totalLevel >= 71) {
            return 3.0;
        } else if (totalLevel >= 51) {
            return 2.5;
        } else if (totalLevel >= 30) {
            return 2.0;
        } else {
            return 1.5;
        }
    }

    private void displayCooldownInfo(Player player) {
        String cooldownDisplay = getCooldownDisplay(player);
        player.sendActionBar(Component.text()
                .append(Component.text("§7☄ " + cooldownDisplay))
                .build());
    }

    private void displayIdleState(Player player) {
        player.sendActionBar(Component.text("§9☄"));
    }
}