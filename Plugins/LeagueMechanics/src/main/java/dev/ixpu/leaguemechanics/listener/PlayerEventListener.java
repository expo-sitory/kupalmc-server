package dev.ixpu.leaguemechanics.listener;

import com.destroystokyo.paper.event.player.PlayerLaunchProjectileEvent;
import dev.ixpu.leaguemechanics.LeagueMechanics;
import dev.ixpu.leaguemechanics.item.*;
import dev.ixpu.leaguemechanics.item.passives.ItemPassive;
import dev.ixpu.leaguemechanics.item.passives.ItemPassivesRegistry;
import dev.ixpu.leaguemechanics.item.shop.ItemShopData;
import dev.ixpu.leaguemechanics.item.shop.ItemShopGUI;
import dev.ixpu.leaguemechanics.item.shop.ItemShopRegistry;
import dev.ixpu.leaguemechanics.manager.DamageManager;
import dev.ixpu.leaguemechanics.manager.ItemPassivesManager;
import dev.ixpu.leaguemechanics.manager.ItemStatsManager;
import dev.ixpu.leaguemechanics.manager.RuneManager;

import dev.ixpu.leaguemechanics.player.PlayerRuneData;
import dev.ixpu.leaguemechanics.player.PlayerStats;

import dev.ixpu.leaguemechanics.rune.CooldownHandler;
import dev.ixpu.leaguemechanics.rune.RuneRegistry;
import dev.ixpu.leaguemechanics.rune.keystones.domination.HailOfBlades;
import dev.ixpu.leaguemechanics.rune.keystones.resolve.GraspOfTheUndying;
import dev.ixpu.leaguemechanics.rune.keystones.resolve.Guardian;

import dev.ixpu.leaguemechanics.rune.keystones.sorcery.DeathfireTorch;
import dev.ixpu.leaguemechanics.util.DebugLogger;
import dev.ixpu.leaguemechanics.util.ItemModifier;
import dev.ixpu.leaguemechanics.util.RunePersistence;

import io.papermc.paper.event.player.PlayerArmSwingEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.*;
import org.bukkit.Material;
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
    private final ItemStatsManager itemStatsManager;

    private final Map<UUID, Long> titleCooldown = new HashMap<>();
    private final Map<UUID, Long> attackCooldown = new HashMap<>();
    private final Map<UUID, Boolean> letRunesThroughMap = new HashMap<>();

    public PlayerEventListener(LeagueMechanics plugin, RunePersistence runePersistence) {
        this.plugin = plugin;
        this.runeManager = plugin.getRuneManager();
        this.runeRegistry = plugin.getRuneRegistry();
        this.runePersistence = runePersistence;
        this.itemStatsManager = plugin.getStatsManager();
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
        UUID uuid = player.getUniqueId();

        GraspOfTheUndying grasp = (GraspOfTheUndying) runeRegistry.getRune("grasp-of-the-undying");
        if (grasp != null) {
            grasp.resetAbsorption(player);
        }
        runeManager.unloadPlayerRunes(player);

        dev.ixpu.leaguemechanics.item.passives.dark_seal darkSeal =
                (dev.ixpu.leaguemechanics.item.passives.dark_seal) ItemPassivesRegistry.getInstance().getPassive("dark-seal");
        if (darkSeal != null) {
            darkSeal.clearStacks(player);
        }

        PlayerStats.invalidateCache(uuid);
        itemStatsManager.invalidateCache(uuid);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        ItemShopGUI.updateShopDisplay(player);

        ItemStack currentItem = event.getCurrentItem();
        ItemStack cursor = event.getCursor();

        if (preventBundleInsert(currentItem, cursor)) {
            event.setCancelled(true);
            player.sendMessage(Component.text("§cLeague items cannot be inserted into bundles"));
            return;
        }

        if (currentItem != null && !currentItem.getType().isAir() && event.getClickedInventory() != player.getInventory()) {
            String itemId = ItemModifier.getItemId(currentItem);
            if (itemId != null) {
                ItemStatsManager statsManager = plugin.getStatsManager();
                if (statsManager.countLeagueItems(player) > 5) {
                    event.setCancelled(true);
                    player.sendMessage(Component.text("§cLeague Items Count: 6/6"));
                    return;
                }
            }
        }

        if (!cursor.getType().isAir() && event.getClickedInventory() != player.getInventory()) {
            String cursorItemId = ItemModifier.getItemId(cursor);
            if (cursorItemId != null) {
                ItemShopData shopData = ItemShopData.getInstance();
                String itemGroup = shopData.getGroup(cursorItemId);

                if (itemGroup != null) {
                    for (ItemStack inv : player.getInventory().getContents()) {
                        if (inv != null && !inv.getType().isAir()) {
                            String invItemId = ItemModifier.getItemId(inv);
                            if (invItemId != null && !invItemId.equals(cursorItemId)) {
                                String ownerGroup = shopData.getGroup(invItemId);
                                if (ownerGroup != null && ownerGroup.equals(itemGroup)) {
                                    event.setCancelled(true);
                                    player.sendMessage(Component.text("§cYou can only apply one (1) " + itemGroup + " item to your build."));
                                    return;
                                }
                            }
                        }
                    }
                }
            }
        }

        if (!cursor.getType().isAir()) {
            ItemModifier.syncItemStats(cursor);
        }
        if (currentItem != null && !currentItem.getType().isAir()) {
            ItemModifier.syncItemStats(currentItem);
        }

        UUID uuid = player.getUniqueId();
        PlayerStats.invalidateCache(uuid);
        itemStatsManager.invalidateCache(uuid);

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> applyPlayerStats(player), 1L);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onSwapHandItems(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        if (ItemModifier.getItemId(event.getMainHandItem()) != null
                || ItemModifier.getItemId(event.getOffHandItem()) != null) {
            event.setCancelled(true);
            player.sendMessage(Component.text("§cLeague items cannot be swapped between hands"));
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerInteractEntity(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item != null && item.getType() == Material.BUNDLE) {
            ItemStack mainHand = player.getInventory().getItemInMainHand();
            ItemStack offHand = player.getInventory().getItemInOffHand();
            if (ItemModifier.getItemId(mainHand) != null || ItemModifier.getItemId(offHand) != null) {
                event.setCancelled(true);
                player.sendMessage(Component.text("§cLeague items cannot be inserted into bundles"));
            }
        }
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
        if (isPlayerOnAttackCooldown(shooter)) {
            event.setCancelled(true);
            return;
        }
        setAttackCooldown(shooter);
    }

    @EventHandler
    public void onBowShoot(EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Player shooter)) {
            return;
        }
        if (isPlayerOnAttackCooldown(shooter)) {
            event.setCancelled(true);
            return;
        }
        if (isAnyHotbarOnCooldown(shooter)) {
            event.setCancelled(true);
        }
        letRunesThroughMap.put(shooter.getUniqueId(), false);
        setAttackCooldown(shooter);
    }

    @EventHandler
    public void onArmSwing(PlayerArmSwingEvent event) {
        Player attacker = event.getPlayer();
        if (isPlayerOnAttackCooldown(attacker)) {
            event.setCancelled(true);
            return;
        }
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

        letRunesThroughMap.put(shooter.getUniqueId(), true);

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

        PlayerRuneData runeData = runeManager.getPlayerRuneData(shooter);
        if (runeData != null) {
            CooldownHandler keystoneRune = runeData.getKeystoneRune();
            if (keystoneRune != null) {
                keystoneRune.onProjectileHit(shooter, target);
            }
        }

        event.getEntity().remove();
        damageEvent(shooter, target, "Projectile Hit Event");
        letRunesThroughMap.put(shooter.getUniqueId(), false);
    }

    @EventHandler
    public void onAttack(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker)) {
            return;
        }
        if (!(event.getEntity() instanceof LivingEntity target)) {
            return;
        }
        if (isPlayerOnAttackCooldown(attacker)) {
            event.setCancelled(true);
            return;
        }
        if (isAnyHotbarOnCooldown(attacker)) {
            event.setCancelled(true);
            return;
        }
        if (target instanceof Creature creature) {
            creature.setTarget(attacker);
        }
        letRunesThroughMap.put(attacker.getUniqueId(), true);

        PlayerRuneData runeData = runeManager.getPlayerRuneData(attacker);
        if (runeData != null) {
            CooldownHandler keystoneRune = runeData.getKeystoneRune();
            if (keystoneRune != null) {
                keystoneRune.onAttack(attacker, target);
            }
        }

        if (target instanceof Player targetPlayer) {
            PlayerRuneData targetRuneData = runeManager.getPlayerRuneData(targetPlayer);
            if (targetRuneData != null) {
                CooldownHandler targetKeystoneRune = targetRuneData.getKeystoneRune();
                if (targetKeystoneRune instanceof Guardian guardian) {
                    guardian.onTakeDamage(targetPlayer);
                }
            }
        }

        damageEvent(attacker, target, "Melee Hit Event");
        setAttackCooldown(attacker);
        letRunesThroughMap.put(attacker.getUniqueId(), false);
    }

    @EventHandler
    public void onPlayerDamaged(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        PlayerRuneData runeData = runeManager.getPlayerRuneData(player);
        if (runeData == null) {
            return;
        }

        double damage = event.getDamage();
        for (CooldownHandler rune : runeData.getAllRunes()) {
            rune.onPlayerDamage(player, damage);
            if (rune instanceof GraspOfTheUndying grasp) {
                grasp.activateGraspOfTheUndying(player, null);
            }
            if (rune instanceof HailOfBlades hailOfBlades) {
                hailOfBlades.activateHailofBlades(player, null);
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityDeath(EntityDeathEvent event) {
        Entity killer = event.getEntity().getKiller();
        if (!(killer instanceof Player player)) {
            return;
        }

        DeathfireTorch deathfire = (DeathfireTorch) runeRegistry.getRune("deathfire-torch");
        if (deathfire != null) {
            deathfire.clearBurnForTarget(event.getEntity().getUniqueId());
        }

        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null || item.getType().isAir()) {
                continue;
            }

            String itemId = ItemModifier.getItemId(item);
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
                ItemModifier.syncItemStats(item);
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
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> applyPlayerStats(player), 1L);
    }

    @EventHandler
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();
        ItemShopData shopData = ItemShopData.getInstance();
        ItemStack item = event.getItem().getItemStack();
        String itemId = ItemModifier.getItemId(item);
        String itemGroup = shopData.getGroup(itemId);

        if (itemId == null) {
            return;
        }
        if (exceedsItemLimit(player, itemId)) {
            event.setCancelled(true);
            UUID uuid = player.getUniqueId();
            long now = System.currentTimeMillis();
            if (!titleCooldown.containsKey(uuid) || now - titleCooldown.get(uuid) >= TITLE_COOLDOWN_MS) {
                player.showTitle(Title.title(
                        Component.text("§c§l✗ ɪᴛᴇᴍ ʟɪᴍɪᴛ"),
                        Component.text("§7ʏᴏᴜ ᴀʟʀᴇᴀᴅʏ ʜᴀᴠᴇ ᴛʜɪꜱ ɪᴛᴇᴍ ᴏɴ ʏᴏᴜʀ ʙᴜɪʟᴅ")
                ));
                titleCooldown.put(uuid, now);
            }
            return;
        }
        if (!hasMainInventorySpace(player)) {
            event.setCancelled(true);
            UUID uuid = player.getUniqueId();
            long now = System.currentTimeMillis();
            if (!titleCooldown.containsKey(uuid) || now - titleCooldown.get(uuid) >= TITLE_COOLDOWN_MS) {
                player.showTitle(Title.title(
                        Component.text("§c§l✗ ɪɴᴠᴇɴᴛᴏʀʏ ꜰᴜʟʟ"),
                        Component.text("§7ɴᴏ ꜱᴘᴀᴄᴇ ɪɴ ᴍᴀɪɴ ɪɴᴠᴇɴᴛᴏʀʏ")
                ));
                titleCooldown.put(uuid, now);
            }
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
        if (itemGroup != null) {
            for (ItemStack inv : player.getInventory().getContents()) {
                if (inv != null && !inv.getType().isAir()) {
                    String invItemId = ItemModifier.getItemId(inv);
                    if (invItemId != null && !invItemId.equals(itemId)) {
                        String ownerGroup = shopData.getGroup(invItemId);
                        if (ownerGroup != null && ownerGroup.equals(itemGroup)) {
                            event.setCancelled(true);
                            UUID uuid = player.getUniqueId();
                            long now = System.currentTimeMillis();
                            if (!titleCooldown.containsKey(uuid) || now - titleCooldown.get(uuid) >= TITLE_COOLDOWN_MS) {
                                player.showTitle(Title.title(
                                        Component.text("§c§l✗ ɪᴛᴇᴍ ɢʀᴏᴜᴘ ʟɪᴍɪᴛ"),
                                        Component.text("§7ʏᴏᴜ ᴄᴀɴ ᴏɴʟʏ ᴀᴘᴘʟʏ 1 ᴏꜰ §aᴅᴏʀᴀɴ'ꜱ §7ɪᴛᴇᴍ ᴛᴏ ʏᴏᴜʀ ʙᴜɪʟᴅ")
                                ));
                                titleCooldown.put(uuid, now);
                            }
                            return;
                        }
                    }
                }
            }
        }
        event.setCancelled(true);
        for (int i = 9; i <= 35; i++) {
            ItemStack slot = player.getInventory().getItem(i);
            if (slot == null || slot.getType().isAir()) {
                player.getInventory().setItem(i, item.clone());
                event.getItem().remove();
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> applyPlayerStats(player), 1L);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        List<ItemStack> leagueItems = new ArrayList<>();
        for (ItemStack drop : event.getDrops()) {
            if (drop != null && !drop.getType().isAir()) {
                String itemId = ItemModifier.getItemId(drop);
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
                    moveLeagueItemToMainInventory(player, leagueItem);
                }
            }, 1L);
        }

        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null || item.getType().isAir()) {
                continue;
            }

            String itemId = ItemModifier.getItemId(item);
            if (itemId == null || !itemId.equals("dark-seal")) {
                continue;
            }

            dev.ixpu.leaguemechanics.item.passives.dark_seal darkSeal =
                    (dev.ixpu.leaguemechanics.item.passives.dark_seal) ItemPassivesRegistry.getInstance().getPassive("dark-seal");
            if (darkSeal != null) {
                int currentStacks = darkSeal.getStacks(player);
                int newStacks = Math.max(currentStacks - 2, 0);
                ItemPassivesManager.getInstance().setKillCount(player, "dark-seal", newStacks);
                ItemModifier.syncItemStats(item);
            }
        }
    }

    public void moveLeagueItemToMainInventory(Player player, ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return;
        }
        for (int i = 9; i <= 35; i++) {
            ItemStack slot = player.getInventory().getItem(i);
            if (slot == null || slot.getType().isAir()) {
                player.getInventory().setItem(i, item);
                return;
            }
        }
        player.getWorld().dropItemNaturally(player.getLocation(), item);
        dropLeagueItemExceededLimit(player);
    }

    private void dropLeagueItemExceededLimit(Player player) {
        ItemStatsManager statsManager = plugin.getStatsManager();
        int leagueItemCount = statsManager.countLeagueItems(player);

        if (!(leagueItemCount > 5)) {
            return;
        }

        int excessItems = leagueItemCount - 5;

        for (int i = 35; i >= 9 && excessItems > 0; i--) {
            ItemStack item = player.getInventory().getItem(i);
            if (item != null && !item.getType().isAir() && ItemModifier.getItemId(item) != null) {
                player.getWorld().dropItemNaturally(player.getLocation(), item);
                player.getInventory().setItem(i, null);
                excessItems--;
            }
        }
    }
    public void applyPlayerStats(Player player) {
        syncItemStats(player);
        applyHealthModifier(player);
        applyMovementSpeedModifier(player);
    }

    private void syncItemStats(Player player) {
        ItemStack mainHand = player.getInventory().getItemInMainHand();

        ItemModifier.syncItemStats(mainHand);

        ItemStack offHand = player.getInventory().getItemInOffHand();
        if (!offHand.getType().isAir()) {
            ItemModifier.syncItemStats(offHand);
        }

        for (ItemStack armor : player.getInventory().getArmorContents()) {
            if (armor != null && !armor.getType().isAir()) {
                ItemModifier.syncItemStats(armor);
            }
        }

        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && !item.getType().isAir()) {
                ItemModifier.syncItemStats(item);
            }
        }
    }

    @SuppressWarnings("removal")
    private void applyHealthModifier(Player player) {
        ItemStatsManager statsManager = plugin.getStatsManager();
        if (statsManager == null) return;

        double bonusHP = statsManager.getItemHP(player);
        var attr = player.getAttribute(Attribute.MAX_HEALTH);
        if (attr != null) {
            new ArrayList<>(attr.getModifiers()).forEach(attr::removeModifier);
        }

        if (bonusHP > 0) {
            Objects.requireNonNull(player.getAttribute(Attribute.MAX_HEALTH)).addModifier(
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

        var attr = player.getAttribute(Attribute.MOVEMENT_SPEED);
        if (attr != null) {
            new ArrayList<>(attr.getModifiers()).forEach(attr::removeModifier);
        }

        if (bonusMS > 0) {
            double speedBonus = 0.1 * (bonusMS / 100.0);
            Objects.requireNonNull(player.getAttribute(Attribute.MOVEMENT_SPEED)).addModifier(
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
        PlayerStats stats = PlayerStats.getOrCreate(player);

        double playerAS = stats.getPlayerAS(player);
        double attackSpeed = 35 * (1.0 - playerAS);

        UUID uuid = player.getUniqueId();
        long cooldownMs = (long) attackSpeed * 50;
        attackCooldown.put(uuid, System.currentTimeMillis() + cooldownMs);

        for (int i = 0; i < 9; i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (item != null && !item.getType().isAir()) {
                player.setCooldown(item.getType(), (int) attackSpeed);
            }
        }
    }

    public boolean letRunesThrough(Player player) {
        return letRunesThroughMap.getOrDefault(player.getUniqueId(), false);
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

    public boolean isPlayerOnAttackCooldown(Player player) {
        UUID uuid = player.getUniqueId();
        if (!attackCooldown.containsKey(uuid)) {
            return false;
        }

        long cooldownEnd = attackCooldown.get(uuid);
        if (System.currentTimeMillis() >= cooldownEnd) {
            attackCooldown.remove(uuid);
            return false;
        }

        return true;
    }

    public long getAttackCooldownRemaining(Player player) {
        UUID uuid = player.getUniqueId();
        if (!attackCooldown.containsKey(uuid)) {
            return 0;
        }

        long cooldownEnd = attackCooldown.get(uuid);
        long remaining = cooldownEnd - System.currentTimeMillis();
        return remaining > 0 ? remaining : 0;
    }

    private void damageEvent(Player player, LivingEntity target, String type) {
        DamageManager damage = new DamageManager(itemStatsManager);
        PlayerStats stats = PlayerStats.getOrCreate(player);

        double attackerAD = stats.getPlayerAD(player);
        double attackerAP = stats.getPlayerAP(player);
        double targetAR = damage.getTargetAR(target);
        double targetMR = damage.getTargetMR(target);

        double statsDamage = damage.DamageCalculation(player, target, 0, 0, 0);

        double newHealth = target.getHealth();
        if (target instanceof Player targetPlayer) {
            double absorption = targetPlayer.getAbsorptionAmount();
            if (statsDamage > absorption) {
                statsDamage -= absorption;
                targetPlayer.setAbsorptionAmount(0);
            } else {
                targetPlayer.setAbsorptionAmount(absorption - statsDamage);
                statsDamage = 0;
            }
        }

        newHealth = Math.clamp(newHealth - statsDamage, 0, target.getMaxHealth());

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

    private boolean preventBundleInsert(ItemStack currentItem, ItemStack cursor) {
        if (currentItem != null && !currentItem.getType().isAir()) {
            if (currentItem.getType() == Material.BUNDLE) {
                if (cursor != null && !cursor.getType().isAir() && ItemModifier.getItemId(cursor) != null) {
                    return true;
                }
            }
        }
        if (cursor != null && !cursor.getType().isAir() && cursor.getType() == Material.BUNDLE) {
            return currentItem != null && !currentItem.getType().isAir() && ItemModifier.getItemId(currentItem) != null;
        }

        return false;
    }

    private boolean hasMainInventorySpace(Player player) {
        for (int i = 9; i <= 35; i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (item == null || item.getType().isAir()) {
                return true;
            }
        }
        return false;
    }

    private boolean exceedsItemLimit(Player player, String itemId) {
        ItemShopRegistry registry = ItemShopRegistry.getInstance();
        ItemShopRegistry.ShopItem shopItem = null;

        for (ItemShopRegistry.ShopItem item : registry.getAllShopItems()) {
            if (item.getId().equals(itemId)) {
                shopItem = item;
                break;
            }
        }

        if (shopItem == null) {
            return false;
        }

        int limit = shopItem.getLimit();
        int ownedCount = 0;

        for (ItemStack inv : player.getInventory().getContents()) {
            if (inv != null && !inv.getType().isAir()) {
                String invItemId = ItemModifier.getItemId(inv);
                if (invItemId != null && invItemId.equals(itemId)) {
                    ownedCount++;
                }
            }
        }

        return ownedCount >= limit;
    }
}