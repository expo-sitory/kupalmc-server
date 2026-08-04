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

import dev.ixpu.leaguemechanics.rune.BaseRune;
import dev.ixpu.leaguemechanics.rune.RunePath;
import dev.ixpu.leaguemechanics.rune.RuneSlot;
import net.kyori.adventure.text.Component;

public class ArcaneComet extends BaseRune {
    double BASE_MAGIC_DAMAGE = 4.5;

    int COOLDOWN_SECONDS = 20;

    private static final int COMET_FALL_TICKS = 30;

    private final Map<UUID, Long> lastCometTime = new HashMap<>();
    private LeagueMechanics plugin;

    public ArcaneComet(ConfigurationSection config) {
        super("arcane-comet", RunePath.SORCERY, RuneSlot.KEYSTONE);
        ConfigurationSection section = config.getConfigurationSection("runes.keystones.sorcery.arcane-comet");
        
        if (section != null) {
            this.COOLDOWN_SECONDS = section.getInt("cooldown", COOLDOWN_SECONDS);
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

        if (livingTarget.getMaxHealth() < 20) {
            return;
        }

        if (isOnCooldown(shooter)) {
            return;
        }

        double totalOutput = BASE_MAGIC_DAMAGE;
        triggerArcaneComet(shooter, (LivingEntity) target, totalOutput);
        resetCooldown(shooter);
    }

    private void triggerArcaneComet(Player shooter, LivingEntity target, double totalOutput) {
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
                    target.damage(totalOutput * 2.0);

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
        if (isOnCooldown(player)) {
            displayCooldownInfo(player);
            return;
        }
        displayIdleState(player);
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