package dev.ixpu.leaguerunes.listener;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;

import dev.ixpu.leaguerunes.LeagueRunes;
import dev.ixpu.leaguerunes.RuneManager;
import dev.ixpu.leaguerunes.player.PlayerRuneData;
import dev.ixpu.leaguerunes.rune.BaseRune;

public class RuneListener implements Listener {
    private final LeagueRunes plugin;
    private final RuneManager runeManager;

    public RuneListener(LeagueRunes plugin) {
        this.plugin = plugin;
        this.runeManager = plugin.getRuneManager();
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        // Handle player as attacker
        if (event.getDamager() instanceof Player attacker) {
            Entity target = event.getEntity();

            if (!runeManager.hasActiveRunes(attacker)) {
                // Still continue for victim damage handling below
            } else {
                PlayerRuneData attackerRuneData = runeManager.getPlayerRuneData(attacker);
                if (attackerRuneData != null) {
                    // Trigger attack effects
                    for (BaseRune rune : attackerRuneData.getAllRunes()) {
                        if (rune != null) {
                            rune.onAttack(attacker, target, event);
                        }
                    }
                }

                double damage = event.getFinalDamage();
                // Trigger damage dealt effects
                for (BaseRune rune : attackerRuneData.getAllRunes()) {
                    if (rune != null) {
                        rune.onPlayerDealDamage(attacker, damage);
                    }
                }
            }
        }

        // Handle victim (if player)
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player victim = (Player) event.getEntity();
        double damage = event.getFinalDamage();

        if (!runeManager.hasActiveRunes(victim)) {
            return;
        }

        PlayerRuneData runeData = runeManager.getPlayerRuneData(victim);
        if (runeData == null) {
            return;
        }

        // Trigger damage taken effects
        for (BaseRune rune : runeData.getAllRunes()) {
            if (rune != null) {
                rune.onPlayerDamage(victim, damage);
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();

        if (!runeManager.hasActiveRunes(victim)) {
            return;
        }

        PlayerRuneData runeData = runeManager.getPlayerRuneData(victim);
        if (runeData == null) {
            return;
        }

        // Trigger death effects
        for (BaseRune rune : runeData.getAllRunes()) {
            if (rune != null) {
                rune.onPlayerDeath(victim);
            }
        }

        // Handle kill
        Player killer = victim.getKiller();
        if (killer != null && runeManager.hasActiveRunes(killer)) {
            PlayerRuneData killerRuneData = runeManager.getPlayerRuneData(killer);
            if (killerRuneData != null) {
                // Trigger kill effects
                for (BaseRune rune : killerRuneData.getAllRunes()) {
                    if (rune != null) {
                        rune.onPlayerKill(killer, victim);
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onProjectileHit(ProjectileHitEvent event) {
        if (!(event.getEntity().getShooter() instanceof Player)) {
            return;
        }

        Player shooter = (Player) event.getEntity().getShooter();
        Entity hitEntity = event.getHitEntity();

        if (hitEntity == null) {
            return;  // Hit a block, not an entity
        }

        if (!runeManager.hasActiveRunes(shooter)) {
            return;
        }

        PlayerRuneData runeData = runeManager.getPlayerRuneData(shooter);
        if (runeData == null) {
            return;
        }

        // Call onProjectileHit
        BaseRune keystoneRune = runeData.getKeystoneRune();
        if (keystoneRune != null && keystoneRune.getId().equals("fleet-footwork")) {
            try {
                dev.ixpu.leaguerunes.rune.keystones.precision.FleetFootwork fleetFootwork = 
                    (dev.ixpu.leaguerunes.rune.keystones.precision.FleetFootwork) keystoneRune;
                fleetFootwork.onProjectileHit(shooter, hitEntity);
            } catch (ClassCastException e) {
                // skip
            }
        }

        if (keystoneRune != null && keystoneRune.getId().equals("electrocute")) {
            try {
                dev.ixpu.leaguerunes.rune.keystones.domination.Electrocute electrocute = 
                    (dev.ixpu.leaguerunes.rune.keystones.domination.Electrocute) keystoneRune;
                electrocute.onProjectileHit(shooter, hitEntity);
            } catch (ClassCastException e) {
                // skip
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerCausesDamage(EntityDamageByEntityEvent event) {
        Player attacker = null;

        // Check if direct player damage (melee)
        if (event.getDamager() instanceof Player player) {
            attacker = player;
        }
        // Check if projectile damage (bow/crossbow)
        else if (event.getDamager() instanceof Projectile projectile) {
            if (projectile.getShooter() instanceof Player player) {
                attacker = player;
            }
        }

        if (attacker == null) {
            return;
        }

        if (!runeManager.hasActiveRunes(attacker)) {
            return;
        }

        PlayerRuneData runeData = runeManager.getPlayerRuneData(attacker);
        if (runeData == null) {
            return;
        }

        // Call onPlayerDamage 
        BaseRune keystoneRune = runeData.getKeystoneRune();
        if (keystoneRune != null && keystoneRune.getId().equals("conqueror")) {
            try {
                dev.ixpu.leaguerunes.rune.keystones.precision.Conqueror conqueror = 
                    (dev.ixpu.leaguerunes.rune.keystones.precision.Conqueror) keystoneRune;
                conqueror.onPlayerDamage(attacker, event);
            } catch (ClassCastException e) {
                // skip
            }
        }

        if (keystoneRune != null && keystoneRune.getId().equals("dark-harvest")) {
            try {
                dev.ixpu.leaguerunes.rune.keystones.domination.DarkHarvest darkHarvest = 
                    (dev.ixpu.leaguerunes.rune.keystones.domination.DarkHarvest) keystoneRune;
                darkHarvest.onPlayerDamage(attacker, event);
            } catch (ClassCastException e) {
                // Not DarkHarvest, skip
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity deadEntity = event.getEntity();
        
        if (!(deadEntity instanceof Player)) {
            return;
        }

        Player killer = deadEntity.getKiller();

        if (killer == null) {
            return;
        }

        if (!runeManager.hasActiveRunes(killer)) {
            return;
        }

        PlayerRuneData runeData = runeManager.getPlayerRuneData(killer);
        if (runeData == null) {
            return;
        }

        // Call onEntityKill 
        BaseRune keystoneRune = runeData.getKeystoneRune();
        if (keystoneRune != null && keystoneRune.getId().equals("dark-harvest")) {
            try {
                dev.ixpu.leaguerunes.rune.keystones.domination.DarkHarvest darkHarvest = 
                    (dev.ixpu.leaguerunes.rune.keystones.domination.DarkHarvest) keystoneRune;
                darkHarvest.onEntityKill(killer, deadEntity);
            } catch (ClassCastException e) {
                // skip
            }
        }
    }
}