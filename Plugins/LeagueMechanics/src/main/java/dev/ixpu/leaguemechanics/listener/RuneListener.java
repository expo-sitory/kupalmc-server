package dev.ixpu.leaguemechanics.listener;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;

import dev.ixpu.leaguemechanics.LeagueMechanics;
import dev.ixpu.leaguemechanics.RuneManager;
import dev.ixpu.leaguemechanics.player.PlayerRuneData;
import dev.ixpu.leaguemechanics.rune.BaseRune;

public class RuneListener implements Listener {
    private final RuneManager runeManager;

    public RuneListener(LeagueMechanics plugin) {
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

        if (!(event.getEntity() instanceof Player victim)) {
            return;
        }

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
                dev.ixpu.leaguemechanics.rune.keystones.resolve.GraspOfTheUndying grasp = 
                    (dev.ixpu.leaguemechanics.rune.keystones.resolve.GraspOfTheUndying) victimKeystone;
                grasp.onCombat(victim);
            } catch (ClassCastException e) {
                //
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onProjectileHit(ProjectileHitEvent event) {
        if (!(event.getEntity().getShooter() instanceof Player shooter)) {
            return;
        }

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
                dev.ixpu.leaguemechanics.rune.keystones.precision.FleetFootwork fleetFootwork = 
                    (dev.ixpu.leaguemechanics.rune.keystones.precision.FleetFootwork) keystoneRune;
                fleetFootwork.onProjectileHit(shooter, hitEntity);
            } catch (ClassCastException e) {
                //
            }
        }

        if (keystoneRune != null && keystoneRune.getId().equals("press-the-attack")) {
            try {
                dev.ixpu.leaguemechanics.rune.keystones.precision.PressTheAttack pressTheAttack =
                        (dev.ixpu.leaguemechanics.rune.keystones.precision.PressTheAttack) keystoneRune;
                pressTheAttack.onProjectileHit(shooter, hitEntity);
            } catch (ClassCastException e) {
                //
            }
        }

        if (keystoneRune != null && keystoneRune.getId().equals("electrocute")) {
            try {
                dev.ixpu.leaguemechanics.rune.keystones.domination.Electrocute electrocute = 
                    (dev.ixpu.leaguemechanics.rune.keystones.domination.Electrocute) keystoneRune;
                electrocute.onProjectileHit(shooter, hitEntity);
            } catch (ClassCastException e) {
                //
            }
        }
        
        if (keystoneRune != null && keystoneRune.getId().equals("dark-harvest")) {
            try {
                dev.ixpu.leaguemechanics.rune.keystones.domination.DarkHarvest darkHarvest = 
                    (dev.ixpu.leaguemechanics.rune.keystones.domination.DarkHarvest) keystoneRune;
                darkHarvest.onProjectileHit(shooter, hitEntity);
            } catch (ClassCastException e) {
                //
            }
        }

        if (keystoneRune != null && keystoneRune.getId().equals("arcane-comet")) {
            try {
                dev.ixpu.leaguemechanics.rune.keystones.sorcery.ArcaneComet arcaneComet =
                        (dev.ixpu.leaguemechanics.rune.keystones.sorcery.ArcaneComet) keystoneRune;
                arcaneComet.onProjectileHit(shooter, hitEntity);
            } catch (ClassCastException e) {
                //
            }
        }

        if (keystoneRune != null && keystoneRune.getId().equals("deathfire-torch")) {
            try {
                dev.ixpu.leaguemechanics.rune.keystones.sorcery.DeathfireTorch deathfire =
                        (dev.ixpu.leaguemechanics.rune.keystones.sorcery.DeathfireTorch) keystoneRune;
                deathfire.onProjectileHit(shooter, hitEntity);
            } catch (ClassCastException e) {
                //
            }
        }

        if (keystoneRune != null && keystoneRune.getId().equals("glacial-augment")) {
            try {
                dev.ixpu.leaguemechanics.rune.keystones.inspiration.GlacialAugment glacialAugment =
                        (dev.ixpu.leaguemechanics.rune.keystones.inspiration.GlacialAugment) keystoneRune;
                glacialAugment.onProjectileHit(shooter, hitEntity);
            } catch (ClassCastException e) {
                //
            }
        }

    }
}
