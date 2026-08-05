package dev.ixpu.leaguemechanics.rune.keystones.precision;

import dev.ixpu.leaguemechanics.rune.RunePath;
import dev.ixpu.leaguemechanics.rune.RuneSlot;
import dev.ixpu.leaguemechanics.rune.StackingRune;

import java.util.*;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import net.kyori.adventure.text.Component;


public class LethalTempo extends StackingRune {

    private double BASE_ATTACK_SPEED = 0.8;

    private static final int MAXIMUM_STACKS = 6;
    private static final int ACTIVE_DURATION_TICKS = 60;

    int COOLDOWN_DURATION_SECONDS = 30;

    private final Map<UUID, RuneState> playerState = new HashMap<>();
    private final Map<UUID, Integer> activeState = new HashMap<>();
    private final Map<UUID, List<AttributeModifier>> activeModifiers = new HashMap<>();

    public LethalTempo(org.bukkit.configuration.ConfigurationSection config) {
        super("lethal-tempo", RunePath.PRECISION, RuneSlot.KEYSTONE, 6, 120);
        enablePerTargetStacking();

        ConfigurationSection section = config.getConfigurationSection("runes.keystones.precision.lethal-tempo");

        if (section != null) {
            this.BASE_ATTACK_SPEED = section.getDouble("attack-speed-bonus", this.BASE_ATTACK_SPEED);
            this.COOLDOWN_DURATION_SECONDS = section.getInt("cooldown", COOLDOWN_DURATION_SECONDS);
        }
        this.setCooldownSeconds(COOLDOWN_DURATION_SECONDS);
    }

    private enum RuneState {
        STACKING, ACTIVE
    }

    @Override
    public void onEnable(Player player) {
        UUID uuid = player.getUniqueId();
        playerState.put(uuid, RuneState.STACKING);
        activeState.put(uuid, 0);
    }

    @Override
    public void onDisable(Player player) {
        UUID uuid = player.getUniqueId();
        super.onDisable(player);
        playerState.remove(uuid);
        activeState.remove(uuid);
        clearPlayerCooldown(player);
        removeAllModifiers(player);
    }

    public void onAttack(Player attacker, Entity target, EntityDamageByEntityEvent event) {
        triggerLethalTempo(attacker, target, event);
    }

    private void triggerLethalTempo(Player player, Entity target, EntityDamageByEntityEvent event) {
        UUID playerUUID = player.getUniqueId();
        UUID targetUUID = target.getUniqueId();
        RuneState state = playerState.getOrDefault(playerUUID, RuneState.STACKING);

        if (!(target instanceof LivingEntity livingTarget)) {
            return;
        }

        if (livingTarget.getMaxHealth() < 20) {
            return;
        }

        switchTarget(player, targetUUID);

        if (state == RuneState.STACKING) {
            addStackForTarget(player, targetUUID);
        } else if (state == RuneState.ACTIVE) {
            refreshActiveTimer(player);
        }
    }

    @Override
    public void tick(Player player) {
        UUID playerUUID = player.getUniqueId();

        if (isOnCooldown(player)) {
            displayCooldownInfo(player);
            return;
        }

        RuneState state = playerState.getOrDefault(playerUUID, RuneState.STACKING);

        if (state == RuneState.STACKING) {
            tickStackExpiry(player);
            displayStackInfo(player);

        } else if (state == RuneState.ACTIVE) {
            int activeTime = activeState.get(playerUUID);
            activeTime--;
            activeState.put(playerUUID, activeTime);

            displayActiveInfo(player, activeTime);

            if (activeTime == 0) {
                resetCooldown(player);
                playerState.put(playerUUID, RuneState.STACKING);
                removeAllModifiers(player);
                resetStacks(player);
                player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_BEACON_DEACTIVATE, 1.0f, 1.2f);
            }
        }
    }

    private void addStackForTarget(Player player, UUID targetUUID) {
        addStack(player, targetUUID);

        int currentStacks = getStacks(player, targetUUID);
        applyProgressiveAttackSpeed(player, currentStacks);

        if (currentStacks == MAXIMUM_STACKS) {
            enterActiveState(player);
        }
    }

    private void removeAllModifiers(Player player) {
        UUID playerUUID = player.getUniqueId();
        List<AttributeModifier> mods = activeModifiers.getOrDefault(playerUUID, new ArrayList<>());
        for (AttributeModifier mod : mods) {
            try {
                Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_ATTACK_SPEED)).removeModifier(mod);
            } catch (Exception e) {
                //
            }
        }
        activeModifiers.put(playerUUID, new ArrayList<>());
    }

    @SuppressWarnings("removal")
    private void applyProgressiveAttackSpeed(Player player, int stackCount) {
        removeAllModifiers(player);

        double bonusAmount = (stackCount / 6.0) * BASE_ATTACK_SPEED;

        var modifier = new AttributeModifier(
                UUID.randomUUID(),
                "lethal-tempo-stack-" + stackCount,
                bonusAmount,
                AttributeModifier.Operation.ADD_SCALAR
        );

        var attackSpeedAttr = player.getAttribute(Attribute.GENERIC_ATTACK_SPEED);
        if (attackSpeedAttr != null) {
            attackSpeedAttr.addModifier(modifier);
            activeModifiers.computeIfAbsent(player.getUniqueId(), k -> new ArrayList<>()).add(modifier);
        }
    }

    private void enterActiveState(Player player) {
        UUID playerUUID = player.getUniqueId();
        playerState.put(playerUUID, RuneState.ACTIVE);
        activeState.put(playerUUID, ACTIVE_DURATION_TICKS);
        applyAttackSpeedBonus(player);
        player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.2f);
    }

    private void refreshActiveTimer(Player player) {
        UUID playerUUID = player.getUniqueId();
        activeState.put(playerUUID, ACTIVE_DURATION_TICKS);
    }

    @SuppressWarnings("removal")
    private void applyAttackSpeedBonus(Player player) {
        removeAllModifiers(player);

        var modifier = new AttributeModifier(
                UUID.randomUUID(),
                "lethal-tempo-active",
                BASE_ATTACK_SPEED,
                AttributeModifier.Operation.ADD_SCALAR
        );

        var attackSpeedAttr = player.getAttribute(Attribute.GENERIC_ATTACK_SPEED);
        if (attackSpeedAttr == null) {
            return;
        }

        attackSpeedAttr.addModifier(modifier);
        activeModifiers.computeIfAbsent(player.getUniqueId(), k -> new ArrayList<>()).add(modifier);
    }

    private void displayActiveInfo(Player player, int activeTime) {
        double remainingSeconds = activeTime / 20.0;

        player.sendActionBar(Component.text()
                .append(Component.text(String.format("§e⚚ " + "(%.1fs)", remainingSeconds), net.kyori.adventure.text.format.NamedTextColor.WHITE))
                .build());
    }

    private void displayCooldownInfo(Player player) {
        String cooldownDisplay = getCooldownDisplay(player);
        player.sendActionBar(Component.text("§7⚚ " + cooldownDisplay));
    }

    private void displayStackInfo(Player player) {
        UUID playerUUID = player.getUniqueId();
        UUID lastTargetUUID = lastTarget.getOrDefault(playerUUID, null);

        int currentStacks = 0;
        if (lastTargetUUID != null) {
            currentStacks = getStacks(player, lastTargetUUID);
        }

        if (currentStacks == 0) {
            player.sendActionBar(Component.text("§6⚚"));
        } else {
            player.sendActionBar(Component.text()
                    .append(Component.text("§6⚚ " + currentStacks + "/6"))
                    .build());
        }
    }
}