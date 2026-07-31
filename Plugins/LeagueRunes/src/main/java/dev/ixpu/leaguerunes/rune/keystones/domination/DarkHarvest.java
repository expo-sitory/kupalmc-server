package dev.ixpu.leaguerunes.rune.keystones.domination;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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

public class DarkHarvest extends BaseRune {
    private static final int SOUL_COOLDOWN_TICKS = 1200;
    private static final int REAP_DELAY_TICKS = 35;      
    private static final int KILL_RESET_COOLDOWN_TICKS = 200;  
    private double HEALTH_THRESHOLD = 0.50; 
    private double BASE_BONUS_DAMAGE = 1.5; 
    private double DAMAGE_PER_SOUL = 0.2;
    private int MAX_SOULS = 9; 


    private final Map<UUID, Integer> playerSouls = new HashMap<>();
    private final Map<UUID, Map<UUID, Integer>> entitySoulCooldown = new HashMap<>(); 
    private LeagueRunes plugin;

    public DarkHarvest(org.bukkit.configuration.ConfigurationSection config) {
        super("dark-harvest", RunePath.DOMINATION, RuneSlot.KEYSTONE);
        ConfigurationSection section = config.getConfigurationSection("rune.keystone.domination.dark-harvest");
        this.HEALTH_THRESHOLD = section.getDouble("health-threshold", this.HEALTH_THRESHOLD);
        this.BASE_BONUS_DAMAGE = section.getDouble("base-bonus-damage", this.BASE_BONUS_DAMAGE);
        this.DAMAGE_PER_SOUL = section.getDouble("damage-per-soul", this.DAMAGE_PER_SOUL);
        this.MAX_SOULS = section.getInt("max-soul", this.MAX_SOULS);
    }

    public DarkHarvest() {
        super(
                "dark-harvest",
                RunePath.DOMINATION,
                RuneSlot.KEYSTONE
        );
    }

    public void setPlugin(LeagueRunes plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onEnable(Player player) {
        UUID uuid = player.getUniqueId();
        playerSouls.put(uuid, 0);
        entitySoulCooldown.put(uuid, new HashMap<>());
    }

    @Override
    public void onDisable(Player player) {
        UUID uuid = player.getUniqueId();
        playerSouls.remove(uuid);
        entitySoulCooldown.remove(uuid);
    }


    // Handle player damage events (melee and projectiles)
    public void onPlayerDamage(Player attacker, EntityDamageByEntityEvent event) {
        Entity target = event.getEntity();
        UUID playerUUID = attacker.getUniqueId();
        UUID targetUUID = target.getUniqueId();

        if (!(target instanceof LivingEntity)) {
            return;
        }

        LivingEntity livingTarget = (LivingEntity) target;
        double targetHealth = livingTarget.getHealth();
        double maxHealth = livingTarget.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue();
        double healthPercent = targetHealth / maxHealth;

        // Check if target is below 50% health
        if (healthPercent >= HEALTH_THRESHOLD) {
            return;
        }

        // Check soul cooldown for this entity
        Map<UUID, Integer> cooldowns = entitySoulCooldown.get(playerUUID);
        int cooldown = cooldowns.getOrDefault(targetUUID, 0);
        if (cooldown > 0) {
            return;
        }

        // Apply bonus damage based on soul stacks
        int souls = playerSouls.getOrDefault(playerUUID, 0);
        double bonusDamage = BASE_BONUS_DAMAGE + (souls * DAMAGE_PER_SOUL);
        event.setDamage(event.getDamage() + bonusDamage);

        // Schedule soul reaping after delay
        scheduleReapSoul(attacker, targetUUID);

        // Set entity cooldown
        cooldowns.put(targetUUID, SOUL_COOLDOWN_TICKS);

        displaySoulInfo(attacker, souls);
    }

    @Override
    public void tick(Player player) {
        UUID playerUUID = player.getUniqueId();
        Map<UUID, Integer> cooldowns = entitySoulCooldown.get(playerUUID);

        // Tick down entity cooldowns
        for (UUID entityUUID : new java.util.ArrayList<>(cooldowns.keySet())) {
            int cooldown = cooldowns.get(entityUUID);
            if (cooldown > 0) {
                cooldown--;
                if (cooldown == 0) {
                    cooldowns.remove(entityUUID);
                } else {
                    cooldowns.put(entityUUID, cooldown);
                }
            }
        }
        int souls = playerSouls.getOrDefault(playerUUID, 0);
        displaySoulInfo(player, souls);
    }

    @Override
    public void onPlayerDeath(Player player) {
        UUID uuid = player.getUniqueId();
        playerSouls.put(uuid, 0);
    }
    
    // Handle entity death to reset cooldown
    public void onEntityKill(Player killer, Entity deadEntity) {
        UUID playerUUID = killer.getUniqueId();
        UUID targetUUID = deadEntity.getUniqueId();

        Map<UUID, Integer> cooldowns = entitySoulCooldown.get(playerUUID);
        if (cooldowns != null) {
            // Reset cooldown to 10 seconds on entity death
            cooldowns.put(targetUUID, KILL_RESET_COOLDOWN_TICKS);
        }
    }

    private void scheduleReapSoul(Player attacker, UUID targetUUID) {
        if (plugin == null) {
            reapSoul(attacker);
            return;
        }

        // Schedule soul reap after delay using plugin scheduler
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> reapSoul(attacker), REAP_DELAY_TICKS);
    }

    private void reapSoul(Player player) {

        if (player.getLevel() < 2) {
            return;
        }

        // Consume 5 levels
        player.setLevel(player.getLevel() - 2);

        UUID playerUUID = player.getUniqueId();
        int current = playerSouls.getOrDefault(playerUUID, 0);
        
        // Cap at max stacks
        if (current >= MAX_SOULS) {
            return;
        }
        
        current++;
        playerSouls.put(playerUUID, current);

        player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_ITEM_PICKUP, 1.0f, 1.5f);
        displaySoulInfo(player, current);
    }
    private void displaySoulInfo(Player player, int souls) {
        double totalDamage = BASE_BONUS_DAMAGE + (souls * DAMAGE_PER_SOUL);
        
        player.sendActionBar(Component.text()
                .append(Component.text("👻 " + souls, net.kyori.adventure.text.format.NamedTextColor.RED))
                .append(Component.text(String.format("(+%.1f dmg)", totalDamage), net.kyori.adventure.text.format.NamedTextColor.WHITE))
                .build());
    }
}