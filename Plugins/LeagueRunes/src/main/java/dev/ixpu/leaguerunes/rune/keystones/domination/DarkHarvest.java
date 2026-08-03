package dev.ixpu.leaguerunes.rune.keystones.domination;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import dev.ixpu.leaguerunes.LeagueRunes;
import dev.ixpu.leaguerunes.rune.BaseRune;
import dev.ixpu.leaguerunes.rune.RunePath;
import dev.ixpu.leaguerunes.rune.RuneSlot;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;


public class DarkHarvest extends BaseRune {
    private double HEALTH_THRESHOLD = 0.50;
    private double BASE_BONUS_DAMAGE = 0.8;
    private double DAMAGE_PER_SOUL = 0.3;
    private int REAP_DELAY_TICKS = 35;
    private int MAX_SOULS = 12;
    private int LEVEL_COST_PER_SOUL = 5;

    private final Map<UUID, Integer> playerSouls = new HashMap<>();
    private LeagueRunes plugin;

    public DarkHarvest(ConfigurationSection config) {
        super("dark-harvest", RunePath.DOMINATION, RuneSlot.KEYSTONE);
        ConfigurationSection section = config.getConfigurationSection("runes.keystones.domination.dark-harvest");
        int SOUL_COOLDOWN_SECONDS = 60;
        if (section != null) {
            this.HEALTH_THRESHOLD = section.getDouble("health-threshold", this.HEALTH_THRESHOLD);
            this.BASE_BONUS_DAMAGE = section.getDouble("base-bonus-damage", this.BASE_BONUS_DAMAGE);
            this.DAMAGE_PER_SOUL = section.getDouble("damage-per-soul", this.DAMAGE_PER_SOUL);
            this.REAP_DELAY_TICKS = section.getInt("reap-delay", this.REAP_DELAY_TICKS);
            SOUL_COOLDOWN_SECONDS = section.getInt("cooldown", SOUL_COOLDOWN_SECONDS);
            this.MAX_SOULS = section.getInt("max-souls-stack", this.MAX_SOULS);
            this.LEVEL_COST_PER_SOUL = section.getInt("level-cost-per-soul", this.LEVEL_COST_PER_SOUL);
        }
        this.setCooldownSeconds(SOUL_COOLDOWN_SECONDS);
    }

    public void setPlugin(LeagueRunes plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onEnable(Player player) {
        UUID uuid = player.getUniqueId();
        playerSouls.put(uuid, 0);
    }

    @Override
    public void onDisable(Player player) {
        UUID uuid = player.getUniqueId();
        clearPlayerCooldown(player);
        playerSouls.remove(uuid);
    }

    @Override
    public void onAttack(Player attacker, Entity target, EntityDamageByEntityEvent event) {
        UUID playerUUID = attacker.getUniqueId();

        if (!(target instanceof LivingEntity livingTarget)) {
            return;
        }

        double targetHealth = livingTarget.getHealth();
        double maxHealth = livingTarget.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        double healthPercent = targetHealth / maxHealth;

        if (maxHealth < 20) {
            return;
        }

        if (healthPercent >= HEALTH_THRESHOLD) {
            return;
        }

        int souls = playerSouls.getOrDefault(playerUUID, 0);
        double bonusDamage = BASE_BONUS_DAMAGE + (souls * DAMAGE_PER_SOUL);
        event.setDamage(event.getDamage() + bonusDamage);

        if (isOnCooldown(attacker)) {
            return;
        }

        if (attacker.getLevel() < LEVEL_COST_PER_SOUL) {
            return;
        }

        scheduleReapSoul(attacker);
        resetCooldown(attacker);
    }

    public void onProjectileHit(Player shooter, Entity target) {
        if (!(target instanceof LivingEntity livingTarget)) {
            return;
        }

        double maxHealth = Objects.requireNonNull(livingTarget.getAttribute(Attribute.GENERIC_MAX_HEALTH)).getValue();

        if (maxHealth < 20) {
            return;
        }

        double targetHealth = livingTarget.getHealth();
        double healthPercent = targetHealth / maxHealth;

        if (healthPercent >= HEALTH_THRESHOLD) {
            return;
        }

        if (isOnCooldown(shooter)) {
            return;
        }

        if (shooter.getLevel() < LEVEL_COST_PER_SOUL) {
            return;
        }

        scheduleReapSoul(shooter);
        resetCooldown(shooter);
    }

    @Override
    public void tick(Player player) {
        UUID playerUUID = player.getUniqueId();
        int souls = playerSouls.getOrDefault(playerUUID, 0);

        if (isOnCooldown(player)) {
            displayCooldown(player, souls);
            return;
        }

        displaySoulInfo(player, souls);
    }

    private void scheduleReapSoul(Player attacker) {
        if (plugin == null) {
            reapSoul(attacker);
            return;
        }

        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> reapSoul(attacker), REAP_DELAY_TICKS);
    }

    private void reapSoul(Player player) {
        UUID playerUUID = player.getUniqueId();
        int current = playerSouls.getOrDefault(playerUUID, 0);

        if (current >= MAX_SOULS) {
            return;
        }

        player.setLevel(player.getLevel() - LEVEL_COST_PER_SOUL);
        current++;
        playerSouls.put(playerUUID, current);

        player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_WITHER_SHOOT, 1.0f, 1.5f);
    }

    private void displayCooldown(Player player, int souls) {
        String cooldownDisplay = getCooldownDisplay(player);
        player.sendActionBar(Component.text("§7👻 " + souls + "/" + MAX_SOULS + " | " + cooldownDisplay));
    }

    private void displaySoulInfo(Player player, int souls) {
        if (souls >= 1) {
                player.sendActionBar(Component.text()
                        .append(Component.text("§c👻 " + souls + "/" + MAX_SOULS))
                        .build());
        } else {
                player.sendActionBar(Component.text()
                        .append(Component.text("§c👻"))
                        .build());
        }
    }
}