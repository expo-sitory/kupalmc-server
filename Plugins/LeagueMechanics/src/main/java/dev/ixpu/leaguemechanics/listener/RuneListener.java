package dev.ixpu.leaguemechanics.listener;

import org.bukkit.entity.Player;
import org.bukkit.entity.Entity;

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
            if (runeManager.hasActiveRunes(attacker)) {
                PlayerRuneData attackerRuneData = runeManager.getPlayerRuneData(attacker);
                if (attackerRuneData != null) {
                    for (BaseRune rune : attackerRuneData.getAllRunes()) {
                        if (rune != null) {
                            rune.onAttack(attacker, target, event);
                        }
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
        if (keystoneRune != null) {
            keystoneRune.onProjectileHit(shooter, hitEntity);
        }
    }
}