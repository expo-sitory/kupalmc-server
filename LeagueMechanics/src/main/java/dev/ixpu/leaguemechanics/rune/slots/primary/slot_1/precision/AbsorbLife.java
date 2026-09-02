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

public class AbsorbLife extends CooldownHandler {

    private double missingHealthHealPercent = 0.20;

    private final Map<UUID, Double> lastHealAmount = new HashMap<>();

    public AbsorbLife(ConfigurationSection config) {
        super("absorb-life", RunePath.PRECISION, RuneSlot.PRIMARY_SLOT_1);

        ConfigurationSection section = config.getConfigurationSection("runes.slots.primary.slot-1.precision.absorb-life");
        if (section != null) {
            this.missingHealthHealPercent = section.getDouble("missing-health-heal-percent", this.missingHealthHealPercent);
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
        String slotSection = heal != null ? "§e☘ " + String.format("%.1f", heal) : "§6☘";
        String combined = keystoneSection + " " + slotSection + " " + PlayerStats.getOrCreate(player).getActionBarSections(player);
        player.sendActionBar(Component.text(combined));
    }

    @Override
    public void onTakedown(Player attacker, Player victim, boolean isKill) {
        if (!isKill) {
            return;
        }

        double maxHealth = attacker.getAttribute(Attribute.MAX_HEALTH).getValue();
        double currentHealth = attacker.getHealth();
        double missingHealth = Math.max(0, maxHealth - currentHealth);
        if (missingHealth <= 0) {
            return;
        }

        double healAmount = missingHealth * missingHealthHealPercent;
        double newHealth = Math.min(currentHealth + healAmount, maxHealth);
        attacker.setHealth(newHealth);
        lastHealAmount.put(attacker.getUniqueId(), healAmount);
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
