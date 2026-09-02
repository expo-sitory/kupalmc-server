package dev.ixpu.leaguemechanics.rune.slots.primary.slot_1.domination;

import dev.ixpu.leaguemechanics.LeagueMechanics;
import dev.ixpu.leaguemechanics.manager.StatScalingManager;
import dev.ixpu.leaguemechanics.player.PlayerRuneData;
import dev.ixpu.leaguemechanics.player.PlayerStats;
import dev.ixpu.leaguemechanics.rune.CooldownHandler;
import dev.ixpu.leaguemechanics.rune.RunePath;
import dev.ixpu.leaguemechanics.rune.RuneSlot;

import net.kyori.adventure.text.Component;

import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class TasteOfBlood extends CooldownHandler {

    private double missingHealthHealPercent = 5.0;
    private double adBonusPercent = 10.0;
    private double apBonusPercent = 5.0;
    private int cooldownSeconds = 20;

    public TasteOfBlood(ConfigurationSection config) {
        super("taste-of-blood", RunePath.DOMINATION, RuneSlot.PRIMARY_SLOT_1);
        this.setCooldownSeconds(cooldownSeconds);

        ConfigurationSection section = config.getConfigurationSection("runes.slots.primary.slot-1.domination.taste-of-blood");
        if (section != null) {
            this.missingHealthHealPercent = section.getDouble("missing-health-heal-percent", this.missingHealthHealPercent);
            this.adBonusPercent = section.getDouble("ad-bonus-percent", this.adBonusPercent);
            this.apBonusPercent = section.getDouble("ap-bonus-percent", this.apBonusPercent);
            this.cooldownSeconds = section.getInt("cooldown", this.cooldownSeconds);
            this.setCooldownSeconds(this.cooldownSeconds);
        }
    }

    @Override
    public void onEnable(Player player) {
    }

    @Override
    public void onDisable(Player player) {
    }

    @Override
    public void tick(Player player) {
        CooldownHandler keystone = getKeystone(player);
        String keystoneSection = keystone != null ? keystone.getDisplaySection(player) : "";
        String slotSection = isOnCooldown(player) ? "§7🫀" + getCooldownDisplay(player) : "§c🫀";
        String combined = keystoneSection + " " + slotSection + " " + PlayerStats.getOrCreate(player).getActionBarSections(player);
        player.sendActionBar(Component.text(combined));
    }

    @Override
    public void onAttack(Player attacker, Entity target) {
        if (!(target instanceof Player)) {
            return;
        }
        if (isOnCooldown(attacker)) {
            return;
        }
        resetCooldown(attacker);

        double maxHealth = attacker.getAttribute(Attribute.MAX_HEALTH).getValue();
        double currentHealth = attacker.getHealth();
        double missingHealth = Math.max(0, maxHealth - currentHealth);
        if (missingHealth <= 0) {
            return;
        }

        double baseHeal = missingHealth * (missingHealthHealPercent / 100.0);
        double totalHeal = new StatScalingManager().calculateScaledValue(attacker, baseHeal, adBonusPercent, apBonusPercent);

        double newHealth = Math.min(currentHealth + totalHeal, maxHealth);
        attacker.setHealth(newHealth);
        attacker.playSound(attacker.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
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
