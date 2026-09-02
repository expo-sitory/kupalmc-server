package dev.ixpu.leaguemechanics.rune.slots.primary.slot_1.inspiration;

import dev.ixpu.leaguemechanics.LeagueMechanics;
import dev.ixpu.leaguemechanics.manager.ItemShopManager;
import dev.ixpu.leaguemechanics.player.PlayerRuneData;
import dev.ixpu.leaguemechanics.player.PlayerStats;
import dev.ixpu.leaguemechanics.rune.CooldownHandler;
import dev.ixpu.leaguemechanics.rune.RunePath;
import dev.ixpu.leaguemechanics.rune.RuneSlot;
import dev.ixpu.leaguemechanics.util.ItemModifier;

import net.kyori.adventure.text.Component;

import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CashBack extends CooldownHandler implements Listener {

    private double refundPercent = 7.5;
    private final Map<UUID, Double> totalRefundedXp = new HashMap<>();

    public CashBack(ConfigurationSection config) {
        super("cash-back", RunePath.INSPIRATION, RuneSlot.PRIMARY_SLOT_1);

        ConfigurationSection section = config.getConfigurationSection("runes.slots.primary.slot-1.inspiration.cash-back");
        if (section != null) {
            this.refundPercent = section.getDouble("refund-percent", this.refundPercent);
        }

        LeagueMechanics plugin = LeagueMechanics.getInstance();
        if (plugin != null) {
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
        }
    }

    @Override
    public void onEnable(Player player) {
        totalRefundedXp.putIfAbsent(player.getUniqueId(), 0.0);
    }

    @Override
    public void onDisable(Player player) {
        totalRefundedXp.remove(player.getUniqueId());
    }

    @Override
    public void tick(Player player) {
        CooldownHandler keystone = getKeystone(player);
        String keystoneSection = keystone != null ? keystone.getDisplaySection(player) : "";
        Double refunded = totalRefundedXp.get(player.getUniqueId());
        String slotSection = refunded != null ? "§b◎ " + String.format("%.1f", refunded) : "§3◎";
        String combined = keystoneSection + " " + slotSection + " " + PlayerStats.getOrCreate(player).getActionBarSections(player);
        player.sendActionBar(Component.text(combined));
    }

    @EventHandler
    public void onShopPurchase(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        if (!event.getView().getTitle().equals(ItemShopManager.SHOP_INVENTORY_TITLE)) {
            return;
        }

        if (!hasRuneEquipped(player)) {
            return;
        }

        ItemStack currentItem = event.getCurrentItem();
        if (currentItem == null || currentItem.getType().isAir()) {
            return;
        }

        String itemId = ItemModifier.getItemId(currentItem);
        if (itemId == null) {
            return;
        }

        dev.ixpu.leaguemechanics.item.shop.ItemShopData shopData = dev.ixpu.leaguemechanics.item.shop.ItemShopData.getInstance();
        org.bukkit.inventory.ItemRarity rarity = shopData.getRarity(itemId);
        if (rarity != org.bukkit.inventory.ItemRarity.EPIC) {
            return;
        }

        int price = shopData.getPrice(itemId);
        if (price <= 0) {
            return;
        }

        double refundAmount = price * (refundPercent / 100.0);
        UUID uuid = player.getUniqueId();
        double currentTotal = totalRefundedXp.getOrDefault(uuid, 0.0);
        totalRefundedXp.put(uuid, currentTotal + refundAmount);

        player.setLevel(player.getLevel() + (int) Math.floor(refundAmount));
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.5f);
        player.sendMessage(Component.text("§a✦ Cash Back: §fRefunded " + String.format("%.1f", refundAmount) + " xp (Total: " + String.format("%.1f", currentTotal + refundAmount) + ")"));
    }

    private boolean hasRuneEquipped(Player player) {
        LeagueMechanics plugin = LeagueMechanics.getInstance();
        if (plugin == null || plugin.getRuneManager() == null) {
            return false;
        }
        PlayerRuneData runeData = plugin.getRuneManager().getPlayerRuneData(player);
        return runeData != null && runeData.getPrimarySlot1Rune() instanceof CashBack;
    }

    private CooldownHandler getKeystone(Player player) {
        LeagueMechanics plugin = LeagueMechanics.getInstance();
        if (plugin == null || plugin.getRuneManager() == null) {
            return null;
        }
        PlayerRuneData runeData = plugin.getRuneManager().getPlayerRuneData(player);
        return runeData != null ? runeData.getKeystoneRune() : null;
    }

    public double getTotalRefundedXp(UUID uuid) {
        return totalRefundedXp.getOrDefault(uuid, 0.0);
    }
}