package dev.ixpu.leaguemechanics.rune.slots.primary.slot_1.resolve;

import dev.ixpu.leaguemechanics.LeagueMechanics;
import dev.ixpu.leaguemechanics.player.PlayerRuneData;
import dev.ixpu.leaguemechanics.player.PlayerStats;
import dev.ixpu.leaguemechanics.rune.CooldownHandler;
import dev.ixpu.leaguemechanics.rune.RunePath;
import dev.ixpu.leaguemechanics.rune.RuneSlot;

import net.kyori.adventure.text.Component;

import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ShieldBash extends CooldownHandler {

    private double bonusAD = 15.0;
    private int bonusDurationTicks = 5 * 20;

    private final Map<UUID, Long> expiryTicks = new HashMap<>();

    public ShieldBash(ConfigurationSection config) {
        super("shield-bash", RunePath.RESOLVE, RuneSlot.PRIMARY_SLOT_1);

        ConfigurationSection section = config.getConfigurationSection("runes.slots.primary.slot-1.resolve.shield-bash");
        if (section != null) {
            this.bonusAD = section.getDouble("bonus-ad", this.bonusAD);
            this.bonusDurationTicks = section.getInt("bonus-duration-ticks", this.bonusDurationTicks);
        }
    }

    @Override
    public void onEnable(Player player) {
    }

    @Override
    public void onDisable(Player player) {
        UUID uuid = player.getUniqueId();
        if (expiryTicks.containsKey(uuid)) {
            PlayerStats.getOrCreate(player).modifyAD(-bonusAD);
            expiryTicks.remove(uuid);
        }
    }

    @Override
    public void onShieldBlock(Player player) {
        UUID uuid = player.getUniqueId();

        if (expiryTicks.containsKey(uuid)) {
            expiryTicks.put(uuid, player.getWorld().getGameTime() + bonusDurationTicks);
            return;
        }

        PlayerStats.getOrCreate(player).modifyAD(bonusAD);
        expiryTicks.put(uuid, player.getWorld().getGameTime() + bonusDurationTicks);
        player.playSound(player.getLocation(), Sound.BLOCK_BELL_USE, 0.8f, 1.6f);
    }

    @Override
    public void tick(Player player) {
        UUID uuid = player.getUniqueId();
        Long expiry = expiryTicks.get(uuid);

        String slotSection;
        if (expiry != null) {
            long now = player.getWorld().getGameTime();
            long remaining = expiry - now;
            if (remaining <= 0) {
                PlayerStats.getOrCreate(player).modifyAD(-bonusAD);
                expiryTicks.remove(uuid);
                slotSection = "§2🛡";
            } else {
                double seconds = remaining / 20.0;
                slotSection = String.format("§a🛡 (%.1fs)", seconds);
            }
        } else {
            slotSection = "§2🛡";
        }

        CooldownHandler keystone = getKeystone(player);
        String keystoneSection = keystone != null ? keystone.getDisplaySection(player) : "";
        String combined = keystoneSection + " " + slotSection + " " + PlayerStats.getOrCreate(player).getActionBarSections(player);
        player.sendActionBar(Component.text(combined));
    }

    private CooldownHandler getKeystone(Player player) {
        LeagueMechanics plugin = LeagueMechanics.getInstance();
        if (plugin == null || plugin.getRuneManager() == null) {
            return null;
        }
        PlayerRuneData runeData = plugin.getRuneManager().getPlayerRuneData(player);
        return runeData != null ? runeData.getKeystoneRune() : null;
    }
}
