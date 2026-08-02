package dev.ixpu.leaguerunes.listener;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
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
        if (event.getDamager() instanceof Player attacker) {
            Entity target = event.getEntity();
            if (!runeManager.hasActiveRunes(attacker)) {
            } else {
                PlayerRuneData attackerRuneData = runeManager.getPlayerRuneData(attacker);
                if (attackerRuneData != null) {
                    for (BaseRune rune : attackerRuneData.getAllRunes()) {
                        if (rune != null) {
                            rune.onAttack(attacker, target, event);
                        }
                    }
                }
                double damage = event.getFinalDamage();
                for (BaseRune rune : attackerRuneData.getAllRunes()) {
                    if (rune != null) {
                        rune.onPlayerDealDamage(attacker, damage);
                    }
                }
            }
        }

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

        for (BaseRune rune : runeData.getAllRunes()) {
            if (rune != null) {
                rune.onPlayerDamage(victim, damage);
            }
        }
        BaseRune victimKeystone = runeData.getKeystoneRune();
        if (victimKeystone != null && victimKeystone.getId().equals("grasp-of-the-undying")) {
            try {
                dev.ixpu.leaguerunes.rune.keystones.resolve.GraspOfTheUndying grasp = 
                    (dev.ixpu.leaguerunes.rune.keystones.resolve.GraspOfTheUndying) victimKeystone;
                grasp.onCombat(victim);
            } catch (ClassCastException e) {}
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

        for (BaseRune rune : runeData.getAllRunes()) {
            if (rune != null) {
                rune.onPlayerDeath(victim);
            }
        }

        Player killer = victim.getKiller();
        if (killer != null && runeManager.hasActiveRunes(killer)) {
            PlayerRuneData killerRuneData = runeManager.getPlayerRuneData(killer);
            if (killerRuneData != null) {
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
            return;  
        }

        if (!runeManager.hasActiveRunes(shooter)) {
            return;
        }

        PlayerRuneData runeData = runeManager.getPlayerRuneData(shooter);
        if (runeData == null) {
            return;
        }

        BaseRune keystoneRune = runeData.getKeystoneRune();

        if (keystoneRune != null && keystoneRune.getId().equals("fleet-footwork")) {
            try {
                dev.ixpu.leaguerunes.rune.keystones.precision.FleetFootwork fleetFootwork = 
                    (dev.ixpu.leaguerunes.rune.keystones.precision.FleetFootwork) keystoneRune;
                fleetFootwork.onProjectileHit(shooter, hitEntity);
            } catch (ClassCastException e) {
                
            }
        }

        if (keystoneRune != null && keystoneRune.getId().equals("electrocute")) {
            try {
                dev.ixpu.leaguerunes.rune.keystones.domination.Electrocute electrocute = 
                    (dev.ixpu.leaguerunes.rune.keystones.domination.Electrocute) keystoneRune;
                electrocute.onProjectileHit(shooter, hitEntity);
            } catch (ClassCastException e) {
                
            }
        }
        
        if (keystoneRune != null && keystoneRune.getId().equals("dark-harvest")) {
            try {
                dev.ixpu.leaguerunes.rune.keystones.domination.DarkHarvest darkHarvest = 
                    (dev.ixpu.leaguerunes.rune.keystones.domination.DarkHarvest) keystoneRune;
                darkHarvest.onProjectileHit(shooter, hitEntity);
            } catch (ClassCastException e) {}
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
        if (runeData != null) {
            for (BaseRune rune : runeData.getAllRunes()) {
                if (rune != null) {
                    rune.onPlayerKill(killer, (Player) deadEntity);
                }
            }
        }
    }
}