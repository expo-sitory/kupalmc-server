package dev.ixpu.leaguemechanics.listener;

import com.destroystokyo.paper.event.player.PlayerLaunchProjectileEvent;
import dev.ixpu.leaguemechanics.LeagueMechanics;
import dev.ixpu.leaguemechanics.item.*;
import dev.ixpu.leaguemechanics.manager.DamageManager;
import dev.ixpu.leaguemechanics.manager.ItemStatsManager;
import dev.ixpu.leaguemechanics.manager.RuneManager;

import dev.ixpu.leaguemechanics.player.PlayerRuneData;
import dev.ixpu.leaguemechanics.player.PlayerStats;

import dev.ixpu.leaguemechanics.rune.CooldownHandler;
import dev.ixpu.leaguemechanics.rune.RuneRegistry;
import dev.ixpu.leaguemechanics.rune.keystones.domination.HailOfBlades;
import dev.ixpu.leaguemechanics.rune.keystones.resolve.GraspOfTheUndying;

import dev.ixpu.leaguemechanics.util.DebugLogger;
import dev.ixpu.leaguemechanics.util.ItemLoreModifier;
import dev.ixpu.leaguemechanics.util.RunePersistence;

import io.papermc.paper.event.player.PlayerArmSwingEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.*;
import org.bukkit.Sound;
import org.bukkit.util.Vector;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;

import java.util.*;


public class PlayerEventListener implements Listener {
    private static final String LEAGUE_HP_MODIFIER = "league_hp";
    private static final String LEAGUE_MS_MODIFIER = "league_ms";
    private static final long TITLE_COOLDOWN_MS = 1500;

    private final LeagueMechanics plugin;
    private final RuneManager runeManager;
    private final RuneRegistry runeRegistry;
    private final RunePersistence runePersistence;

    private final Map<UUID, UUID> lastAttacker = new HashMap<>();
    private final Map<UUID, Long> titleCooldown = new HashMap<>();

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
            CooldownHandler keystone = runeRegistry.getRune(keystoneRuneId);
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

        ItemStack currentItem = event.getCurrentItem();

        if (currentItem != null && !currentItem.getType().isAir() && event.getClickedInventory() != player.getInventory()) {
            String itemId = ItemLoreModifier.getItemId(currentItem);
            if (itemId != null) {
                ItemStatsManager statsManager = plugin.getStatsManager();
                if (statsManager.countLeagueItems(player) > 5) {
                    event.setCancelled(true);
                    player.sendMessage(Component.text("§cLeague Items Count: 6/6"));
                    return;
                }
            }
        }

        ItemStack cursor = event.getCursor();
        if (!cursor.getType().isAir()) {
            ItemLoreModifier.syncItemStats(cursor);
        }
        if (currentItem != null && !currentItem.getType().isAir()) {
            ItemLoreModifier.syncItemStats(currentItem);
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
    public void onProjectileDamage(ProjectileHitEvent event) {
        if (!(event.getEntity().getShooter() instanceof Player)) {
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler
    public void onAttackDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) {
            return;
        }
        event.setDamage(0);
    }

    @EventHandler
    public void onProjectileDraw(PlayerLaunchProjectileEvent event) {
        Player shooter = event.getPlayer();
        if (isAnyHotbarOnCooldown(shooter)) {
            event.setCancelled(true);
        }
        setAttackCooldown(shooter);
    }

    @EventHandler
    public void onBowShoot(EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Player shooter)) {
            return;
        }
        if (isAnyHotbarOnCooldown(shooter)) {
            event.setCancelled(true);
        }
        setAttackCooldown(shooter);
    }

    @EventHandler
    public void onArmSwing(PlayerArmSwingEvent event) {
        Player attacker = event.getPlayer();
        if (isAnyHotbarOnCooldown(attacker)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (!(event.getEntity().getShooter() instanceof Player shooter)) {
            return;
        }
        if (event.getHitEntity() == null || !(event.getHitEntity() instanceof LivingEntity target)) {
            return;
        }

        if (event.getEntity() instanceof Arrow) {
            ItemStack bow = shooter.getInventory().getItemInMainHand();

            if (bow.containsEnchantment(Enchantment.FLAME)) {
                target.setFireTicks(8 * 20);
            }

            if (!target.getUniqueId().equals(shooter.getUniqueId())) {
                Vector direction = target.getLocation().toVector().subtract(shooter.getLocation().toVector()).normalize();
                double knockbackPower = 0.5;

                if (bow.containsEnchantment(Enchantment.KNOCKBACK)) {
                    int knockbackLevel = bow.getEnchantmentLevel(Enchantment.KNOCKBACK);
                    knockbackPower = knockbackLevel * 0.5;
                }

                target.setVelocity(direction.multiply(knockbackPower));
            }
        }
        if (target instanceof Creature creature) {
            creature.setTarget(shooter);
        }

        shooter.getWorld().playSound(shooter.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 1.0f, 1.0f);

        event.getEntity().remove();
        damageEvent(shooter, target, "Projectile Hit Event");
    }

    @EventHandler
    public void onAttack(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker)) {
            return;
        }
        if (!(event.getEntity() instanceof LivingEntity target)) {
            return;
        }
        if (isAnyHotbarOnCooldown(attacker)) {
            event.setCancelled(true);
            return;
        }
        if (target instanceof Creature creature) {
            creature.setTarget(attacker);
        }
        damageEvent(attacker, target, "Melee Hit Event");
        setAttackCooldown(attacker);
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

        for (CooldownHandler rune : runeData.getAllRunes()) {
            if (rune instanceof GraspOfTheUndying grasp) {
                grasp.onCombat(player);
            }
            if (rune instanceof HailOfBlades hailOfBlades) {
                hailOfBlades.onCombat(player);
            }
        }
    }


    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityDeath(EntityDeathEvent event) {
        Entity killer = event.getEntity().getKiller();
        if (!(killer instanceof Player player)) {
            return;
        }

        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null || item.getType().isAir()) {
                continue;
            }

            String itemId = ItemLoreModifier.getItemId(item);
            if (itemId == null) {
                continue;
            }

            ItemStatsRegistry itemData = ItemStatsData.getInstance().getItem(itemId);
            if (itemData == null || !itemData.hasPassive()) {
                continue;
            }

            ItemPassive passive = ItemPassivesRegistry.getInstance().getPassive(itemData.getPassiveId());
            if (passive != null) {
                passive.onEntityKill(player, item);
                ItemLoreModifier.syncItemStats(item);
            }
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
        ItemStack item = event.getItem().getItemStack();
        String itemId = ItemLoreModifier.getItemId(item);

        if (itemId == null) {
            return;
        }

        ItemStatsManager statsManager = plugin.getStatsManager();
        if (statsManager.countLeagueItems(player) > 5) {
            event.setCancelled(true);
            UUID uuid = player.getUniqueId();
            long now = System.currentTimeMillis();
            if (!titleCooldown.containsKey(uuid) || now - titleCooldown.get(uuid) >= TITLE_COOLDOWN_MS) {
                player.showTitle(Title.title(
                        Component.text("§c§l✗ ʙᴜɪʟᴅ ꜱʟᴏᴛꜱ ꜰᴜʟʟ"),
                        Component.text("§7ʟᴇᴀɢᴜᴇ ɪᴛᴇᴍꜱ ᴄᴏᴜɴᴛ: 6/6")
                ));
                titleCooldown.put(uuid, now);
            }
            return;
        }
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            applyPlayerStats(player);
        }, 1L);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        List<ItemStack> leagueItems = new ArrayList<>();
        for (ItemStack drop : event.getDrops()) {
            if (drop != null && !drop.getType().isAir()) {
                String itemId = ItemLoreModifier.getItemId(drop);
                if (itemId != null) {
                    leagueItems.add(drop.clone());
                }
            }
        }

        for (ItemStack leagueItem : leagueItems) {
            event.getDrops().remove(leagueItem);
        }

        if (!leagueItems.isEmpty()) {
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                for (ItemStack leagueItem : leagueItems) {
                    player.getInventory().addItem(leagueItem);
                }
            }, 1L);
        }

        for (int i = 0; i < 9; i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (item == null || item.getType().isAir()) {
                continue;
            }

            String itemId = ItemLoreModifier.getItemId(item);
            if (itemId == null || !itemId.equals("dark-seal")) {
                continue;
            }

            dev.ixpu.leaguemechanics.item.passives.dark_seal darkSeal =
                    (dev.ixpu.leaguemechanics.item.passives.dark_seal) ItemPassivesRegistry.getInstance().getPassive("dark-seal");
            if (darkSeal != null) {
                darkSeal.clearStacks(player);
                ItemLoreModifier.syncItemStats(item);
            }
        }
    }

    public void applyPlayerStats(Player player) {
        syncItemStats(player);
        applyHealthModifier(player);
        applyMovementSpeedModifier(player);
    }

    @SuppressWarnings("removal")
    private void syncItemStats(Player player) {
        ItemStack mainHand = player.getInventory().getItemInMainHand();

        if (!mainHand.getType().isAir()) {
            ItemLoreModifier.syncItemStats(mainHand);
        }

        ItemStack offHand = player.getInventory().getItemInOffHand();
        if (!offHand.getType().isAir()) {
            ItemLoreModifier.syncItemStats(offHand);
        }

        for (ItemStack armor : player.getInventory().getArmorContents()) {
            if (armor != null && !armor.getType().isAir()) {
                ItemLoreModifier.syncItemStats(armor);
            }
        }

        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && !item.getType().isAir()) {
                ItemLoreModifier.syncItemStats(item);
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

    private void setAttackCooldown(Player player) {
        PlayerStats stats = new PlayerStats();
        double attackSpeed = 35 * (1.0 - (stats.getPlayerAS(player) / 100.0));

        for (int i = 0; i < 9; i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (item != null && !item.getType().isAir()) {
                player.setCooldown(item.getType(), (int) attackSpeed);
            }
        }
    }

    public boolean isAnyHotbarOnCooldown(Player player) {
        for (int i = 0; i < 9; i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (item != null && !item.getType().isAir()) {
                if (player.getCooldown(item.getType()) > 0) {
                    return true;
                }
            }
        }
        return false;
    }

    private void damageEvent(Player player, LivingEntity target, String type) {
        DamageManager damage = new DamageManager();
        PlayerStats stats = new PlayerStats();

        double attackerAD = stats.getPlayerAD(player) + damage.getPlayerAdaptiveAD(player);
        double attackerAP = stats.getPlayerAP(player) + damage.getPlayerAdaptiveAP(player);
        double targetAR = damage.getTargetAR(target);
        double targetMR = damage.getTargetMR(target);

        double statsDamage = damage.DamageCalculation(player, target, 0, 0, 0);
        double newHealth = Math.clamp(target.getHealth() - statsDamage, 0, target.getMaxHealth());

        for (ItemStack armor : target.getEquipment().getArmorContents()) {
            if (armor != null && !armor.getType().isAir()) {
                armor.damage((short) 1, target);
            }
        }

        DebugLogger.debug(player, "§7----------- §f[ §dDEBUG MODE §f] §7-----------");
        DebugLogger.debug(player, "§aTrigger Type: " + type);
        DebugLogger.debug(player, "§7[Debug] §f[§dAttacker§f] Total AD = §d" + Math.ceil(attackerAD * 100) / 100.0);
        DebugLogger.debug(player, "§7[Debug] §f[§dAttacker§f] Total AP = §d" + Math.ceil(attackerAP * 100) / 100.0);
        DebugLogger.debug(player, "§7[Debug] §f[§dTarget§f] Total AR = §d" + Math.ceil(targetAR * 100) / 100.0);
        DebugLogger.debug(player, "§7[Debug] §f[§dTarget§f] Total MR = §d" + Math.ceil(targetMR * 100) / 100.0);

        DebugLogger.debug(player, "§7[Debug] §f[§dAttacker§f] Stats Damage = §d" + Math.ceil(statsDamage * 100) / 100.0);
        DebugLogger.debug(player, "§7[Debug] §f[§dTarget§f] Target New HP = §d" + Math.ceil(newHealth * 100) / 100.0);

        target.damage(0.001);
        if (newHealth <= 0) {
            target.setHealth(0);
            target.damage(1000, player);
        } else {
            target.setHealth(newHealth);
        }
    }
}