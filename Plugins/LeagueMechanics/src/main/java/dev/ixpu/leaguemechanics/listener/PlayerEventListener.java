package dev.ixpu.leaguemechanics.listener;

import dev.ixpu.leaguemechanics.LeagueMechanics;
import dev.ixpu.leaguemechanics.manager.DamageManager;
import dev.ixpu.leaguemechanics.manager.ItemStatsManager;
import dev.ixpu.leaguemechanics.manager.RuneManager;

import dev.ixpu.leaguemechanics.player.PlayerRuneData;
import dev.ixpu.leaguemechanics.player.PlayerStats;

import dev.ixpu.leaguemechanics.rune.BaseRune;
import dev.ixpu.leaguemechanics.rune.RuneRegistry;
import dev.ixpu.leaguemechanics.rune.keystones.resolve.GraspOfTheUndying;

import dev.ixpu.leaguemechanics.util.DebugLogger;
import dev.ixpu.leaguemechanics.util.ItemStatHelper;
import dev.ixpu.leaguemechanics.util.RunePersistence;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.*;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;

import java.awt.*;
import java.util.*;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;


public class PlayerEventListener implements Listener {
    private static final String LEAGUE_HP_MODIFIER = "league_hp";
    private static final String LEAGUE_AS_MODIFIER = "league_as";
    private static final String LEAGUE_MS_MODIFIER = "league_ms";

    private final LeagueMechanics plugin;
    private final RuneManager runeManager;
    private final RuneRegistry runeRegistry;
    private final RunePersistence runePersistence;

    private final Map<UUID, UUID> lastAttacker = new HashMap<>();

    public PlayerEventListener(LeagueMechanics plugin, RunePersistence runePersistence) {
        this.plugin = plugin;
        this.runeManager = plugin.getRuneManager();
        this.runeRegistry = plugin.getRuneRegistry();
        this.runePersistence = runePersistence;
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
        applyPlayerStats(player);
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

    @EventHandler(priority = EventPriority.NORMAL)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        ItemStack cursor = event.getCursor();
        if (!cursor.getType().isAir()) {
            ItemStatHelper.syncItemStats(cursor);
        }

        ItemStack clicked = event.getCurrentItem();
        if (clicked != null && !clicked.getType().isAir()) {
            ItemStatHelper.syncItemStats(clicked);
        }
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> applyPlayerStats(player), 1L);
    }


    @EventHandler
    public void onLightningDamage(EntityDamageEvent event) {
        if (event.getCause() == EntityDamageEvent.DamageCause.LIGHTNING) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onProjectileDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Projectile projectile)) {
            return;
        }

        if (!(projectile.getShooter() instanceof Player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (!(event.getEntity().getShooter() instanceof Player attacker)) {
            return;
        }

        if (event.getHitEntity() == null || !(event.getHitEntity() instanceof LivingEntity target)) {
            return;
        }

        DamageManager damage = new DamageManager();
        PlayerStats stats = new PlayerStats();

        if (event.getEntity() instanceof Arrow) {
            if (attacker.getInventory().getItemInMainHand().containsEnchantment(Enchantment.FLAME)) {
                target.setFireTicks(8 * 20);
            }
        }

        double attackerAD = stats.getPlayerAD(attacker);
        double attackerAP = stats.getPlayerAP(attacker);
        double targetAR = damage.getTargetAR(target);
        double targetMR = damage.getTargetMR(target);

        DebugLogger.debug(attacker, "§7[Debug] §f[§dAttacker Stats§f] (Projectile) Attacker AD = §d" + Math.ceil(attackerAD * 100) / 100.0);
        DebugLogger.debug(attacker, "§7[Debug] §f[§dAttacker Stats§f] (Projectile) Attacker AP = §d" + Math.ceil(attackerAP * 100) / 100.0);
        DebugLogger.debug(attacker, "§7[Debug] §f[§dTarget Stats§f] Target AR = §d" + Math.ceil(targetAR * 100) / 100.0);
        DebugLogger.debug(attacker, "§7[Debug] §f[§dTarget Stats§f] Target MR = §d" + Math.ceil(targetMR * 100) / 100.0);

        for (ItemStack armor : target.getEquipment().getArmorContents()) {
            if (armor != null && !armor.getType().isAir()) {
                armor.damage((short) 1, target);
            }
        }

        target.damage(0.00001);
        event.getEntity().remove();
    }

    @EventHandler
    public void onMeleeAttack(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker)) {
            return;
        }

        if (!(event.getEntity() instanceof Player target)) {
            return;
        }

        DamageManager damage = new DamageManager();
        PlayerStats stats = new PlayerStats();

        double attackerAD = stats.getPlayerAD(attacker);
        double attackerAP = stats.getPlayerAP(attacker);
        double targetAR = damage.getTargetAR(target);
        double targetMR = damage.getTargetMR(target);

        DebugLogger.debug(attacker, "§7[Debug] §f[§cMelee Attack§f] (Melee) Attacker AD = §d" + Math.ceil(attackerAD * 100) / 100.0);
        DebugLogger.debug(attacker, "§7[Debug] §f[§cMelee Attack§f] (Melee) Attacker AP = §d" + Math.ceil(attackerAP * 100) / 100.0);
        DebugLogger.debug(attacker, "§7[Debug] §f[§cMelee Attack§f] Target AR = §d" + Math.ceil(targetAR * 100) / 100.0);
        DebugLogger.debug(attacker, "§7[Debug] §f[§cMelee Attack§f] Target MR = §d" + Math.ceil(targetMR * 100) / 100.0);

        for (ItemStack armor : target.getEquipment().getArmorContents()) {
            if (armor != null && !armor.getType().isAir()) {
                armor.damage((short) 1, target);
            }
        }

        event.setDamage(0);
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

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player dead = event.getEntity();
        UUID attackerId = lastAttacker.remove(dead.getUniqueId());

        if (attackerId != null) {
            Player attacker = Bukkit.getPlayer(attackerId);
            if (attacker != null) {
                String killMessage = dead.getName() + " has been slain by " + attacker.getName();

                for (Player p : Bukkit.getOnlinePlayers()) {
                    showKillToast(p, killMessage);
                }
                event.deathMessage(null);
            }
        }
    }

    private void showKillToast(Player player, String message) {
        Component component = MiniMessage.miniMessage().deserialize("<gold>" + message + "</gold>");
        player.sendMessage(component);

        Advancement fakeAdv = Bukkit.getAdvancement(NamespacedKey.minecraft("story/root"));
        if (fakeAdv != null) {
            AdvancementProgress progress = player.getAdvancementProgress(fakeAdv);
            if (!progress.isDone()) {
                progress.awardCriteria("root");
            }
        }
    }

    public void applyPlayerStats(Player player) {
        syncItemStats(player);
        applyHealthModifier(player);
        applyAttackSpeedModifier(player);
        applyMovementSpeedModifier(player);
        applyAttackSpeedModifier(player);
    }

    @SuppressWarnings("removal")
    private void syncItemStats(Player player) {
        ItemStack mainHand = player.getInventory().getItemInMainHand();

        if (!mainHand.getType().isAir()) {
            ItemStatHelper.syncItemStats(mainHand);
        }

        ItemStack offHand = player.getInventory().getItemInOffHand();
        if (!offHand.getType().isAir()) {
            ItemStatHelper.syncItemStats(offHand);
        }

        for (ItemStack armor : player.getInventory().getArmorContents()) {
            if (armor != null && !armor.getType().isAir()) {
                ItemStatHelper.syncItemStats(armor);
            }
        }

        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && !item.getType().isAir()) {
                ItemStatHelper.syncItemStats(item);
            }
        }
    }

    @SuppressWarnings("removal")
    private void applyHealthModifier(Player player) {
        ItemStatsManager statsManager = plugin.getStatsManager();
        if (statsManager == null) return;

        double bonusHP = statsManager.getItemHP(player);
        var attr = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (attr != null) {
            new ArrayList<>(attr.getModifiers()).forEach(attr::removeModifier);
        }

        if (bonusHP > 0) {
            Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_MAX_HEALTH)).addModifier(
                    new AttributeModifier(UUID.randomUUID(), LEAGUE_HP_MODIFIER, bonusHP, AttributeModifier.Operation.ADD_NUMBER)
            );
        }

        if (player.getHealth() > player.getMaxHealth()) {
            player.setHealth(player.getMaxHealth());
        }
    }

    @SuppressWarnings("removal")
    private void applyAttackSpeedModifier(Player player) {
        ItemStatsManager statsManager = plugin.getStatsManager();
        if (statsManager == null) return;

        double bonusAS = statsManager.getItemAS(player);
        var attr = player.getAttribute(Attribute.GENERIC_ATTACK_SPEED);
        if (attr != null) {
            new ArrayList<>(attr.getModifiers()).forEach(attr::removeModifier);
        }
        if (bonusAS > 0) {
            Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_ATTACK_SPEED)).addModifier(
                    new AttributeModifier(UUID.randomUUID(), LEAGUE_AS_MODIFIER, bonusAS / 100.0, AttributeModifier.Operation.MULTIPLY_SCALAR_1)
            );
        }
    }

    @SuppressWarnings("removal")
    private void applyMovementSpeedModifier(Player player) {
        ItemStatsManager statsManager = plugin.getStatsManager();
        if (statsManager == null) return;

        double bonusMS = statsManager.getItemMS(player);

        var attr = player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
        if (attr != null) {
            new ArrayList<>(attr.getModifiers()).forEach(attr::removeModifier);
        }

        if (bonusMS > 0) {
            double speedBonus = 0.1 * (bonusMS / 100.0);
            Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)).addModifier(
                    new AttributeModifier(UUID.randomUUID(), LEAGUE_MS_MODIFIER, speedBonus, AttributeModifier.Operation.ADD_NUMBER)
            );
        }
    }

    @EventHandler
    public void onHealthRegen(EntityRegainHealthEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        if (event.getRegainReason() == EntityRegainHealthEvent.RegainReason.EATING ||
                event.getRegainReason() == EntityRegainHealthEvent.RegainReason.SATIATED) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            applyPlayerStats(player);
        }, 1L);
    }

    @EventHandler
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            applyPlayerStats(player);
        }, 1L);
    }

    private void removeModifier(Player player, Attribute attribute, String modifierName) {
        var attr = player.getAttribute(attribute);
        if (attr == null) return;

        var modifiersToRemove = attr.getModifiers().stream()
                .filter(m -> m.getName().equals(modifierName))
                .toList();

        for (AttributeModifier modifier : modifiersToRemove) {
            attr.removeModifier(modifier);
        }
    }
}