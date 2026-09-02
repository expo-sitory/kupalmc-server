package dev.ixpu.leaguemechanics.rune.slots.primary.slot_1.sorcery;

import dev.ixpu.leaguemechanics.LeagueMechanics;
import dev.ixpu.leaguemechanics.player.PlayerRuneData;
import dev.ixpu.leaguemechanics.player.PlayerStats;
import dev.ixpu.leaguemechanics.rune.CooldownHandler;
import dev.ixpu.leaguemechanics.rune.RunePath;
import dev.ixpu.leaguemechanics.rune.RuneSlot;

import net.kyori.adventure.text.Component;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NimbusCloak extends CooldownHandler {

    private double bonusMovementSpeedPercent = 60.0;
    private int decayTicks = 100;

    private final Map<UUID, Long> expiryTicks = new HashMap<>();

    public NimbusCloak(ConfigurationSection config) {
        super("nimbus-cloak", RunePath.SORCERY, RuneSlot.PRIMARY_SLOT_1);

        ConfigurationSection section = config.getConfigurationSection("runes.slots.primary.slot-1.sorcery.nimbus-cloak");
        if (section != null) {
            this.bonusMovementSpeedPercent = section.getDouble("movement-speed-percent", this.bonusMovementSpeedPercent);
            this.decayTicks = section.getInt("decay-ticks", this.decayTicks);
        }
    }

    @Override
    public void onEnable(Player player) {
    }

    @Override
    public void onDisable(Player player) {
        PlayerStats.getOrCreate(player).modifyMS(-bonusMovementSpeedPercent);
        expiryTicks.remove(player.getUniqueId());
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
                PlayerStats.getOrCreate(player).modifyMS(-bonusMovementSpeedPercent);
                expiryTicks.remove(uuid);
                slotSection = "§1🥷";
            } else {
                double seconds = remaining / 20.0;
                slotSection = String.format("§9🥷 (%.1fs)", seconds);
            }
        } else {
            slotSection = "§1🥷";
        }

        CooldownHandler keystone = getKeystone(player);
        String keystoneSection = keystone != null ? keystone.getDisplaySection(player) : "";
        String combined = keystoneSection + " " + slotSection + " " + PlayerStats.getOrCreate(player).getActionBarSections(player);
        player.sendActionBar(Component.text(combined));
    }

    @Override
    public void onPotionEffectGain(Player player, PotionEffect effect) {
        if (!PotionEffectType.SPEED.equals(effect.getType())) {
            return;
        }
        PlayerStats.getOrCreate(player).modifyMS(bonusMovementSpeedPercent);
        expiryTicks.put(player.getUniqueId(), player.getWorld().getGameTime() + decayTicks);
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
