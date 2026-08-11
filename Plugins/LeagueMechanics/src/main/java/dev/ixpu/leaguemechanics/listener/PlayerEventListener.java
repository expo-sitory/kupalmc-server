package dev.ixpu.leaguemechanics.listener;

import dev.ixpu.leaguemechanics.LeagueMechanics;
import dev.ixpu.leaguemechanics.manager.RuneManager;
import dev.ixpu.leaguemechanics.player.PlayerRuneData;
import dev.ixpu.leaguemechanics.rune.BaseRune;
import dev.ixpu.leaguemechanics.rune.RuneRegistry;
import dev.ixpu.leaguemechanics.rune.keystones.resolve.GraspOfTheUndying;
import dev.ixpu.leaguemechanics.util.RunePersistence;

import org.bukkit.entity.*;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerEventListener implements Listener {
    private final LeagueMechanics plugin;
    private final RuneManager runeManager;
    private final RuneRegistry runeRegistry;
    private final RunePersistence runePersistence;

    public PlayerEventListener(LeagueMechanics plugin, RunePersistence runePersistence) {
        this.plugin = plugin;
        this.runeManager = plugin.getRuneManager();
        this.runeRegistry = plugin.getRuneRegistry();
        this.runePersistence = runePersistence;
    }


    @EventHandler
    public void onLightningDamage(EntityDamageEvent event) {
        if (event.getCause() == EntityDamageEvent.DamageCause.LIGHTNING) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onProjectileDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Projectile) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (event.getHitEntity() == null || !(event.getHitEntity() instanceof LivingEntity target)) {
            return;
        }

        if (!(event.getEntity().getShooter() instanceof Player attacker)) {
            return;
        }

        if (event.getEntity() instanceof Arrow) {
            if (attacker.getInventory().getItemInMainHand().containsEnchantment(Enchantment.FLAME)) {
                target.setFireTicks(8 * 20);
            }
        }
        target.damage(0.00001);
        event.getEntity().remove();
    }

    @EventHandler
    public void onAttack(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDamaged(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        if (!(event.getDamager() instanceof Player)) {
            return;
        }

        PlayerRuneData runeData = runeManager.getPlayerRuneData(player);
        if (runeData == null) {
            return;
        }

        for (BaseRune rune : runeData.getAllRunes()) {
            if (rune instanceof GraspOfTheUndying grasp) {
                grasp.onCombat(player);
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        runeManager.loadPlayerRunes(player);

        String keystoneRuneId = runePersistence.loadKeystoneRune(player.getUniqueId());
        if (keystoneRuneId != null) {
            BaseRune keystone = runeRegistry.getRune(keystoneRuneId);
            if (keystone != null) {
                runeManager.setPlayerKeystoneRune(player, keystone);
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        GraspOfTheUndying grasp = (GraspOfTheUndying) runeRegistry.getRune("grasp-of-the-undying");
        if (grasp != null) {
            grasp.resetAbsorption(player);
        }

        runeManager.unloadPlayerRunes(player);
    }

//    @EventHandler(priority = EventPriority.NORMAL)
//    public void onPlayerDeath(PlayerDeathEvent event) {
//        Player player = event.getEntity();
//       class nick = (name) runeRegistry.getRune("id");
//
//        if (nick != null) {
//           nick.event(player);
//       }
//    }
}