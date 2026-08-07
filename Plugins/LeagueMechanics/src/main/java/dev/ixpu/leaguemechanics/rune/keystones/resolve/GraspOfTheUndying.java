package dev.ixpu.leaguemechanics.rune.keystones.resolve;

import dev.ixpu.leaguemechanics.rune.RunePath;
import dev.ixpu.leaguemechanics.rune.RuneSlot;
import dev.ixpu.leaguemechanics.rune.StackingRune;
import dev.ixpu.leaguemechanics.player.PlayerStats;

import java.util.*;

import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import net.kyori.adventure.text.Component;



public class GraspOfTheUndying extends StackingRune {
    private double BASE_PHYSICAL_DAMAGE_PERCENT = 0.05;
    private double HEAL_PERCENT = 0.15;

    int COOLDOWN_DURATION_SECONDS = 60;

    private static final int ATTACK_WINDOW_TICKS = 100;
    private static final int ABSORPTION_DURATION_TICKS = Integer.MAX_VALUE;

    private final Map<UUID, Integer> activationState = new HashMap<>();
    private final Map<UUID, Integer> totalAbsorptionHearts = new HashMap<>();
    private final Map<UUID, Boolean> activeStateActive = new HashMap<>();

    public GraspOfTheUndying(org.bukkit.configuration.ConfigurationSection config) {
        super("grasp-of-the-undying", RunePath.RESOLVE, RuneSlot.KEYSTONE, 4, 60);
        ConfigurationSection section = config.getConfigurationSection("runes.keystones.resolve.grasp-of-the-undying");

        if (section != null) {
            this.BASE_PHYSICAL_DAMAGE_PERCENT = section.getDouble("damage-percent", this.BASE_PHYSICAL_DAMAGE_PERCENT);
            this.HEAL_PERCENT = section.getDouble("heal-percent", this.HEAL_PERCENT);
            this.COOLDOWN_DURATION_SECONDS = section.getInt("cooldown", COOLDOWN_DURATION_SECONDS);
        }
        this.setCooldownSeconds(COOLDOWN_DURATION_SECONDS);
    }

    @Override
    public void onEnable(Player player) {
        UUID uuid = player.getUniqueId();
        activationState.put(uuid, 0);
        totalAbsorptionHearts.put(uuid, 0);
        activeStateActive.put(uuid, false);
    }

    @Override
    public void onDisable(Player player) {
        UUID uuid = player.getUniqueId();
        super.onDisable(player);
        activationState.remove(uuid);
        totalAbsorptionHearts.remove(uuid);
        activeStateActive.remove(uuid);
    }

    public void onCombat(Player player) {
        if (isOnCooldown(player)) {
            return;
        }

        addStack(player);

        if (getStacks(player) == maxStacks) {
            activationState.put(player.getUniqueId(), ATTACK_WINDOW_TICKS);
        }
    }

    @Override
    public void onAttack(Player attacker, Entity target, EntityDamageByEntityEvent event) {
        triggerGraspOfTheUndying(attacker, target, event);
    }

    private void triggerGraspOfTheUndying(Player player, Entity target, EntityDamageByEntityEvent event) {
        UUID playerUUID = player.getUniqueId();
        int stacks = getStacks(player);
        int attackWindow = activationState.getOrDefault(playerUUID, 0);

        if (!(target instanceof LivingEntity livingTarget)) {
            return;
        }
        if (livingTarget.getMaxHealth() < 20) {
            return;
        }

        if (stacks >= maxStacks && attackWindow > 0 && !activeStateActive.getOrDefault(playerUUID, false)) {
            enterActiveState(player, event);
            resetStacks(player);
            activationState.put(playerUUID, 0);
            resetCooldown(player);
        } else {
            onCombat(player);
        }
    }

    private void enterActiveState(Player player, EntityDamageByEntityEvent event) {
        UUID playerUUID = player.getUniqueId();
        int absorptionHearts = totalAbsorptionHearts.getOrDefault(playerUUID, 0);

        double bonusDamage = absorptionHearts * BASE_PHYSICAL_DAMAGE_PERCENT;

        event.setDamage(event.getDamage() + bonusDamage);

        var maxHealthAttr = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (maxHealthAttr != null) {
            double maxHealth = maxHealthAttr.getValue();
            double healAmount = maxHealth * HEAL_PERCENT;
            double currentHealth = player.getHealth();
            player.setHealth(Math.min(maxHealth, currentHealth + healAmount));
        }

        activateEffects(player);

        player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_WITHER_AMBIENT, 1.0f, 1.0f);
    }

    private void activateEffects(Player player) {
        UUID playerUUID = player.getUniqueId();
        int currentAbsorptionHearts = totalAbsorptionHearts.getOrDefault(playerUUID, 0);
        currentAbsorptionHearts++;
        totalAbsorptionHearts.put(playerUUID, currentAbsorptionHearts);

        player.addPotionEffect(new PotionEffect(
                PotionEffectType.ABSORPTION,
                ABSORPTION_DURATION_TICKS,
                currentAbsorptionHearts - 1,
                false,
                false
        ));
    }

    public void resetAbsorption(Player player) {
        UUID playerUUID = player.getUniqueId();
        totalAbsorptionHearts.put(playerUUID, 0);
        player.removePotionEffect(PotionEffectType.ABSORPTION);
    }

    @Override
    public void tick(Player player) {
        UUID playerUUID = player.getUniqueId();

        if (isOnCooldown(player)) {
            String runeDisplay = getRuneDisplay(RuneState.COOLDOWN, player, 0, 0);
            setPlayerDisplay(player, runeDisplay);
            return;
        }

        tickStackExpiry(player);

        if (player.getAbsorptionAmount() <= 0 && totalAbsorptionHearts.getOrDefault(playerUUID, 0) > 0) {
            resetAbsorption(player);
        }

        int attackWindow = activationState.getOrDefault(playerUUID, 0);
        if (attackWindow > 0) {
            attackWindow--;
            activationState.put(playerUUID, attackWindow);

            if (attackWindow == 0) {
                resetStacks(player);
                activeStateActive.put(playerUUID, false);
            }
        }

        int stacks = getStacks(player);

        RuneState state;
        if (stacks >= maxStacks && attackWindow > 0) {
            state = RuneState.ACTIVE;
        } else if (stacks > 0) {
            state = RuneState.STACKING;
        } else {
            state = RuneState.IDLE;
        }

        String runeDisplay = getRuneDisplay(state, player, stacks, attackWindow);
        setPlayerDisplay(player, runeDisplay);
    }

    private void setPlayerDisplay(Player player, String runeDisplay) {
        PlayerStats playerStats = new PlayerStats();
        String statsDisplay = playerStats.getActionBarSections(player);

        String actionBarMessage = runeDisplay + " " + statsDisplay;
        player.sendActionBar(Component.text(actionBarMessage));
    }

    enum RuneState {
        COOLDOWN, STACKING, ACTIVE, IDLE
    }

    private String getRuneDisplay(RuneState state, Player player, int stacks, int attackWindow) {
        return switch (state) {
            case COOLDOWN -> "§7🥊 " + getCooldownDisplay(player);
            case ACTIVE -> {
                double remainingSeconds = attackWindow / 20.0;
                yield String.format("§a🥊 (%.1fs)", remainingSeconds);
            }
            case STACKING -> "§2🥊 " + stacks + "/" + maxStacks;
            case IDLE -> "§2🥊";
        };
    }

}