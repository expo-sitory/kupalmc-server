package dev.ixpu.leaguemechanics.rune.keystones.resolve;

import dev.ixpu.leaguemechanics.rune.RunePath;
import dev.ixpu.leaguemechanics.rune.RuneSlot;
import dev.ixpu.leaguemechanics.rune.StackingRune;
import dev.ixpu.leaguemechanics.player.PlayerStats;

import java.util.*;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import net.kyori.adventure.text.Component;


public class GraspOfTheUndying extends StackingRune {
    private double BASE_PHYSICAL_DAMAGE_PERCENT = 0.05;
    private double HEAL_PERCENT = 0.15;

    int COOLDOWN_DURATION_SECONDS = 60;

    private static final int ATTACK_WINDOW_TICKS = 100;
    private static final int MAX_BONUS_HEARTS = 10;

    private final Map<UUID, Integer> activationState = new HashMap<>();
    private final Map<UUID, Integer> totalBonusHearts = new HashMap<>();
    private final Map<UUID, List<AttributeModifier>> activeModifiers = new HashMap<>();

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
        totalBonusHearts.put(uuid, 0);
    }

    @Override
    public void onDisable(Player player) {
        UUID uuid = player.getUniqueId();
        super.onDisable(player);
        activationState.remove(uuid);
        totalBonusHearts.remove(uuid);
        removeAllModifiers(player);
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

        if (stacks >= maxStacks && attackWindow > 0) {
            enterActiveState(player, event);
            resetStacks(player);
            activationState.put(playerUUID, 0);
            resetCooldown(player);
        } else {
            onCombat(player);
        }
    }

    private void enterActiveState(Player player, EntityDamageByEntityEvent event) {
        var maxHealthAttr = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (maxHealthAttr == null) return;

        double maxHealth = maxHealthAttr.getValue();

        double bonusDamage = maxHealth * (BASE_PHYSICAL_DAMAGE_PERCENT / 2);

        event.setDamage(event.getDamage() + bonusDamage);

        double healAmount = maxHealth * HEAL_PERCENT;
        double currentHealth = player.getHealth();
        player.setHealth(Math.min(maxHealth, currentHealth + healAmount));

        activateEffects(player);

        player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_WITHER_AMBIENT, 1.0f, 1.0f);
    }

    @SuppressWarnings("removal")
    private void activateEffects(Player player) {
        UUID playerUUID = player.getUniqueId();

        var maxHealthAttr = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (maxHealthAttr == null) return;

        int currentBonusHearts = totalBonusHearts.getOrDefault(playerUUID, 0);

        if (currentBonusHearts < MAX_BONUS_HEARTS) {
            currentBonusHearts++;
            totalBonusHearts.put(playerUUID, currentBonusHearts);

            removeAllModifiers(player);

            var modifier = new AttributeModifier(
                    java.util.UUID.randomUUID(),
                    "grasp-bonus-health",
                    currentBonusHearts * 2.0,
                    AttributeModifier.Operation.ADD_NUMBER
            );

            maxHealthAttr.addModifier(modifier);
            activeModifiers.computeIfAbsent(playerUUID, k -> new ArrayList<>()).add(modifier);
        }
    }

    private void removeAllModifiers(Player player) {
        UUID playerUUID = player.getUniqueId();
        List<AttributeModifier> mods = activeModifiers.getOrDefault(playerUUID, new ArrayList<>());

        try {
            var maxHealthAttr = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
            if (maxHealthAttr != null) {
                for (AttributeModifier mod : mods) {
                    try {
                        maxHealthAttr.removeModifier(mod);
                    } catch (Exception e) {
                        //
                    }
                }
            }
        } catch (NoSuchFieldError e) {
            //
        }

        activeModifiers.put(playerUUID, new ArrayList<>());
    }

    public void resetBonusHearts(Player player) {
        UUID playerUUID = player.getUniqueId();
        totalBonusHearts.put(playerUUID, 0);
        removeAllModifiers(player);
    }


    @Override
    public void tick(Player player) {
        UUID playerUUID = player.getUniqueId();

        if (isOnCooldown(player)) {
            String runeDisplay = getRuneDisplay(RuneState.COOLDOWN, player, 0, 0);
            player.sendActionBar(Component.text(runeDisplay));
            return;
        }

        tickStackExpiry(player);

        int attackWindow = activationState.getOrDefault(playerUUID, 0);
        if (attackWindow > 0) {
            attackWindow--;
            activationState.put(playerUUID, attackWindow);

            if (attackWindow == 0) {
                resetStacks(player);
            }
        }

        int stacks = getStacks(player);

        RuneState state;
        if (stacks >= maxStacks && attackWindow > 0) {
            state = RuneState.STACKING;
        } else if (stacks > 0) {
            state = RuneState.ACTIVE;
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
            case STACKING -> {
                double remainingSeconds = attackWindow / 20.0;
                yield String.format("§a🥊 (%.1fs)", remainingSeconds);
            }
            case ACTIVE -> "§a🥊 " + stacks + "/" + maxStacks;
            case IDLE -> "§a🥊";
        };
    }

}