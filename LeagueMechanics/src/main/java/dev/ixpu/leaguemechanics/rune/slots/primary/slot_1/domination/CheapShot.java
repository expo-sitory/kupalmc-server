package dev.ixpu.leaguemechanics.rune.slots.primary.slot_1.domination;

import dev.ixpu.leaguemechanics.LeagueMechanics;
import dev.ixpu.leaguemechanics.player.PlayerRuneData;
import dev.ixpu.leaguemechanics.player.PlayerStats;
import dev.ixpu.leaguemechanics.rune.CooldownHandler;
import dev.ixpu.leaguemechanics.rune.RunePath;
import dev.ixpu.leaguemechanics.rune.RuneSlot;

import net.kyori.adventure.text.Component;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class CheapShot extends CooldownHandler {

    private double bonusTrueDamage = 10.0;
    private int cooldownSeconds = 10;

    public CheapShot(ConfigurationSection config) {
        super("cheap-shot", RunePath.DOMINATION, RuneSlot.PRIMARY_SLOT_1);
        this.setCooldownSeconds(cooldownSeconds);

        ConfigurationSection section = config.getConfigurationSection("runes.slots.primary.slot-1.domination.cheap-shot");
        if (section != null) {
            this.bonusTrueDamage = section.getDouble("bonus-true-damage", this.bonusTrueDamage);
            this.cooldownSeconds = section.getInt("cooldown", this.cooldownSeconds);
            this.setCooldownSeconds(this.cooldownSeconds);
        }
    }

    @Override
    public void onEnable(Player player) {
    }

    @Override
    public void onDisable(Player player) {
        PlayerStats.getOrCreate(player).modifyTD(-bonusTrueDamage);
    }

    @Override
    public void tick(Player player) {
        CooldownHandler keystone = getKeystone(player);
        String keystoneSection = keystone != null ? keystone.getDisplaySection(player) : "";
        String slotSection = isOnCooldown(player) ? "§7➵ " + getCooldownDisplay(player) : "§4➵";
        String combined = keystoneSection + " " + slotSection + " " + PlayerStats.getOrCreate(player).getActionBarSections(player);
        player.sendActionBar(Component.text(combined));
    }

    @Override
    public void onProjectileHit(Player shooter, org.bukkit.entity.Entity hitEntity) {
        if (!(hitEntity instanceof Player)) {
            return;
        }
        if (isOnCooldown(shooter)) {
            return;
        }
        resetCooldown(shooter);
        PlayerStats.getOrCreate(shooter).modifyTD(bonusTrueDamage);
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
