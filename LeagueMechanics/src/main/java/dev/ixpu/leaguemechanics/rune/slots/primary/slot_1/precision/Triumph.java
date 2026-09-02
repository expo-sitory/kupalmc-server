package dev.ixpu.leaguemechanics.rune.slots.primary.slot_1.precision;

import dev.ixpu.leaguemechanics.LeagueMechanics;
import dev.ixpu.leaguemechanics.player.PlayerRuneData;
import dev.ixpu.leaguemechanics.player.PlayerStats;
import dev.ixpu.leaguemechanics.rune.CooldownHandler;
import dev.ixpu.leaguemechanics.rune.RunePath;
import dev.ixpu.leaguemechanics.rune.RuneSlot;

import net.kyori.adventure.text.Component;

import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Triumph extends CooldownHandler {

    private double maxHealthHealPercent = 0.025;
    private double missingHealthHealPercent = 0.05;
    private int bonusXpLevels = 20;

    private final Map<UUID, Double> lastHealAmount = new HashMap<>();

    public Triumph(ConfigurationSection config) {
        super("triumph", RunePath.PRECISION, RuneSlot.PRIMARY_SLOT_1);

        ConfigurationSection section = config.getConfigurationSection("runes.slots.primary.slot-1.precision.triumph");
        if (section != null) {
            this.maxHealthHealPercent = section.getDouble("max-health-heal-percent", this.maxHealthHealPercent);
            this.missingHealthHealPercent = section.getDouble("missing-health-heal-percent", this.missingHealthHealPercent);
            this.bonusXpLevels = section.getInt("bonus-xp-levels", this.bonusXpLevels);
        }
    }

    @Override
    public void onEnable(Player player) {
    }

    @Override
    public void onDisable(Player player) {
        lastHealAmount.remove(player.getUniqueId());
    }

    @Override
    public void tick(Player player) {
        CooldownHandler keystone = getKeystone(player);
        String keystoneSection = keystone != null ? keystone.getDisplaySection(player) : "";
        Double heal = lastHealAmount.get(player.getUniqueId());
        String slotSection = heal != null ? "§e☠ " + String.format("%.1f", heal) : "§6☠";
        String combined = keystoneSection + " " + slotSection + " " + PlayerStats.getOrCreate(player).getActionBarSections(player);
        player.sendActionBar(Component.text(combined));
    }

    @Override
    public void onTakedown(Player attacker, Player victim, boolean isKill) {
        double maxHealth = attacker.getAttribute(Attribute.MAX_HEALTH).getValue();
        double currentHealth = attacker.getHealth();
        double missingHealth = Math.max(0, maxHealth - currentHealth);

        double healAmount = (maxHealth * maxHealthHealPercent) + (missingHealth * missingHealthHealPercent);
        if (healAmount > 0) {
            double newHealth = Math.min(currentHealth + healAmount, maxHealth);
            attacker.setHealth(newHealth);
        }

        lastHealAmount.put(attacker.getUniqueId(), healAmount);
        attacker.giveExpLevels(bonusXpLevels);
        attacker.playSound(attacker.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.5f);
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
