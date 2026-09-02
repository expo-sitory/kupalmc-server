package dev.ixpu.leaguemechanics.rune.slots.primary.slot_1.inspiration;

import dev.ixpu.leaguemechanics.LeagueMechanics;
import dev.ixpu.leaguemechanics.player.PlayerRuneData;
import dev.ixpu.leaguemechanics.player.PlayerStats;
import dev.ixpu.leaguemechanics.rune.CooldownHandler;
import dev.ixpu.leaguemechanics.rune.RunePath;
import dev.ixpu.leaguemechanics.rune.RuneSlot;

import net.kyori.adventure.text.Component;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;


public class MagicalFootwear extends CooldownHandler {

    private double bonusMovementSpeedPercent = 10.0;

    public MagicalFootwear(ConfigurationSection config) {
        super("magical-footwear", RunePath.INSPIRATION, RuneSlot.PRIMARY_SLOT_1);

        ConfigurationSection section = config.getConfigurationSection("runes.slots.primary.slot-1.inspiration.magical-footwear");
        if (section != null) {
            this.bonusMovementSpeedPercent = section.getDouble("movement-speed-percent", this.bonusMovementSpeedPercent);
        }
    }

    @Override
    public void onEnable(Player player) {
        PlayerStats.getOrCreate(player).modifyMS(bonusMovementSpeedPercent);
    }

    @Override
    public void onDisable(Player player) {
        PlayerStats.getOrCreate(player).modifyMS(-bonusMovementSpeedPercent);
    }

    @Override
    public void tick(Player player) {
        CooldownHandler keystone = getKeystone(player);
        String keystoneSection = keystone != null ? keystone.getDisplaySection(player) : "";
        String slotSection = "§b👣 +" + String.format("%.1f", bonusMovementSpeedPercent) + "%";
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