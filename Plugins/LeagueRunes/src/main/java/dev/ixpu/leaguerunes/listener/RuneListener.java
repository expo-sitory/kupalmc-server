package dev.ixpu.leaguerunes.listener;

import dev.ixpu.leaguerunes.LeagueRunes;
import dev.ixpu.leaguerunes.RuneManager;
import dev.ixpu.leaguerunes.player.PlayerRuneData;
import dev.ixpu.leaguerunes.rune.BaseRune;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

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
        if (event.getDamager() instanceof Player) {
            Player attacker = (Player) event.getDamager();
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
}
