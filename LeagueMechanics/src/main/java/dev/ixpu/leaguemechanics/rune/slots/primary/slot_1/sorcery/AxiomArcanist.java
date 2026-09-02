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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AxiomArcanist extends CooldownHandler {

    private double bonusCritDamage = 0.12;
    private int cooldownSeconds = 60;

    private final Map<UUID, Boolean> bonusActive = new HashMap<>();

    public AxiomArcanist(ConfigurationSection config) {
        super("axiom-arcanist", RunePath.SORCERY, RuneSlot.PRIMARY_SLOT_1);
        this.setCooldownSeconds(cooldownSeconds);

        ConfigurationSection section = config.getConfigurationSection("runes.slots.primary.slot-1.sorcery.axiom-arcanist");
        if (section != null) {
            this.bonusCritDamage = section.getDouble("bonus-crit-damage", this.bonusCritDamage);
            this.cooldownSeconds = section.getInt("cooldown", this.cooldownSeconds);
            this.setCooldownSeconds(this.cooldownSeconds);
        }
    }

    @Override
    public void onEnable(Player player) {
    }

    @Override
    public void onDisable(Player player) {
        UUID uuid = player.getUniqueId();
        if (Boolean.TRUE.equals(bonusActive.get(uuid))) {
            PlayerStats.getOrCreate(player).modifyCritDamage(-bonusCritDamage);
            bonusActive.remove(uuid);
        }
    }

    @Override
    public void onCrit(Player player) {
        UUID uuid = player.getUniqueId();

        if (isOnCooldown(player)) {
            return;
        }

        PlayerStats.getOrCreate(player).modifyCritDamage(bonusCritDamage);
        bonusActive.put(uuid, true);
        resetCooldown(player);
    }

    @Override
    public void tick(Player player) {
        UUID uuid = player.getUniqueId();

        if (Boolean.TRUE.equals(bonusActive.get(uuid)) && !isOnCooldown(player)) {
            PlayerStats.getOrCreate(player).modifyCritDamage(-bonusCritDamage);
            bonusActive.remove(uuid);
        }

        CooldownHandler keystone = getKeystone(player);
        String keystoneSection = keystone != null ? keystone.getDisplaySection(player) : "";
        String slotSection;
        if (isOnCooldown(player)) {
            slotSection = "§7༒ " + getCooldownDisplay(player);
        } else {
            slotSection = "§1༒";
        }
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
