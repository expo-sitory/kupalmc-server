package dev.ixpu.leaguemechanics.rune.keystones.precision;

import dev.ixpu.leaguemechanics.rune.RunePath;
import dev.ixpu.leaguemechanics.rune.RuneSlot;
import dev.ixpu.leaguemechanics.rune.StackingRune;
import dev.ixpu.leaguemechanics.player.PlayerStats;
import dev.ixpu.leaguemechanics.manager.DamageManager;

import java.util.*;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.configuration.ConfigurationSection;

import net.kyori.adventure.text.Component;


public class LethalTempo extends StackingRune {

    private double ATTACK_SPEED = 0.6;
    private double BASE_ADAPTIVE_DAMAGE = 4.5;

    private static final int MAXIMUM_STACKS = 6;
    private static final int ACTIVE_DURATION_TICKS = 60;
    private static final int STACK_DURATION_TICKS = 300;

    int COOLDOWN_DURATION_SECONDS = 30;

    private final Map<UUID, RuneState> playerState = new HashMap<>();
    private final Map<UUID, Integer> activeState = new HashMap<>();
    private final Map<UUID, List<AttributeModifier>> activeModifiers = new HashMap<>();
    private final Map<UUID, Map<UUID, List<Long>>> stackTimestamps = new HashMap<>();

    public LethalTempo(org.bukkit.configuration.ConfigurationSection config) {
        super("lethal-tempo", RunePath.PRECISION, RuneSlot.KEYSTONE, 6, 120);
        enablePerTargetStacking();

        ConfigurationSection section = config.getConfigurationSection("runes.keystones.precision.lethal-tempo");

        if (section != null) {
            this.ATTACK_SPEED = section.getDouble("attack-speed", this.ATTACK_SPEED);
            this.BASE_ADAPTIVE_DAMAGE = section.getDouble("base-adaptive-damage", this.BASE_ADAPTIVE_DAMAGE);
            this.COOLDOWN_DURATION_SECONDS = section.getInt("cooldown", COOLDOWN_DURATION_SECONDS);
        }
        this.setCooldownSeconds(COOLDOWN_DURATION_SECONDS);
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
        stackTimestamps.remove(uuid);
        clearPlayerCooldown(player);
        removeAllModifiers(player);
    }

    public void onProjectileHit(Player shooter, Entity target) {
        if (!(target instanceof LivingEntity livingTarget)) {
            return;
        }
        if (livingTarget.getMaxHealth() < 20) {
            return;
        }
        livingTarget.setHealth(Math.max(0, livingTarget.getHealth() - physicalDamage(shooter, target)));
    }

    public void onAttack(Player attacker, Entity target) {
        if (!(target instanceof LivingEntity livingTarget)) {
            return;
        }
        if (livingTarget.getMaxHealth() < 20) {
            return;
        }

        RuneState state = playerState.getOrDefault(attacker.getUniqueId(), RuneState.STACKING);

        if (state == RuneState.ACTIVE && !isOnCooldown(attacker)) {
            activateLethalTempo(attacker, target);
        } else {
            livingTarget.setHealth(Math.max(0, livingTarget.getHealth() - physicalDamage(attacker, target)));
            if (!isOnCooldown(attacker)) {
                activateLethalTempo(attacker, target);
            }
        }
    }

    public void activateLethalTempo(Player player, Entity target) {
        UUID targetUUID = target.getUniqueId();
        RuneState state = playerState.getOrDefault(player.getUniqueId(), RuneState.STACKING);

        if (!(target instanceof LivingEntity livingTarget)) {
            return;
        }
        if (livingTarget.getMaxHealth() < 20) {
            return;
        }

        switchTarget(player, targetUUID);

        if (state == RuneState.ACTIVE) {
            livingTarget.setHealth(Math.max(0, livingTarget.getHealth() - bonusDamage(player, target)));
            refreshActiveTimer(player);
            return;
        }

        if (state == RuneState.STACKING) {
            addStackForTarget(player, targetUUID);
        }
    }

    @SuppressWarnings("removal")
    private void applyAttackSpeedBonus(Player player) {
        removeAllModifiers(player);

        var modifier = new AttributeModifier(
                UUID.randomUUID(),
                "lethal-tempo-active",
                ATTACK_SPEED,
                AttributeModifier.Operation.ADD_SCALAR
        );

        var attackSpeedAttr = player.getAttribute(Attribute.GENERIC_ATTACK_SPEED);
        if (attackSpeedAttr == null) {
            return;
        }

        attackSpeedAttr.addModifier(modifier);
        activeModifiers.computeIfAbsent(player.getUniqueId(), k -> new ArrayList<>()).add(modifier);
    }

    private void addStackForTarget(Player player, UUID targetUUID) {
        addStack(player, targetUUID);
        recordStackTimestamp(player, targetUUID);

        int currentStacks = getActiveStacks(player, targetUUID);

        if (currentStacks == MAXIMUM_STACKS) {
            enterActiveState(player);
        }
    }

    private void recordStackTimestamp(Player player, UUID targetUUID) {
        UUID playerUUID = player.getUniqueId();
        Map<UUID, List<Long>> playerTimestamps = stackTimestamps.computeIfAbsent(playerUUID, k -> new HashMap<>());
        List<Long> targetTimestamps = playerTimestamps.computeIfAbsent(targetUUID, k -> new ArrayList<>());
        targetTimestamps.add(System.currentTimeMillis());
    }

    private int getActiveStacks(Player player, UUID targetUUID) {
        expireOldStacks(player, targetUUID);
        return getStacks(player, targetUUID);
    }

    private void expireOldStacks(Player player, UUID targetUUID) {
        UUID playerUUID = player.getUniqueId();
        Map<UUID, List<Long>> playerTimestamps = stackTimestamps.get(playerUUID);

        if (playerTimestamps == null) {
            return;
        }

        List<Long> targetTimestamps = playerTimestamps.get(targetUUID);
        if (targetTimestamps == null || targetTimestamps.isEmpty()) {
            return;
        }

        long currentTime = System.currentTimeMillis();
        long stackDurationMs = STACK_DURATION_TICKS * 50L;

        targetTimestamps.removeIf(timestamp -> (currentTime - timestamp) > stackDurationMs);
    }

    private double bonusDamage(Player player, Entity target) {
        DamageManager damageManager = new DamageManager();
        return damageManager.totalBonusDamage(player, target, 0);
    }
    private double physicalDamage(Player player, Entity target) {
        DamageManager damageManager = new DamageManager();
        damageManager.enableOnlyAD();
        return damageManager.totalBonusDamage(player, target, 0);
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

    private int trackActiveStacks(Player player) {
        UUID lastTargetUUID = lastTarget.getOrDefault(player.getUniqueId(), null);
        int currentStacks = 0;
        if (lastTargetUUID != null) {
            currentStacks = getActiveStacks(player, lastTargetUUID);
        }
        return currentStacks;
    }

    @Override
    public void tick(Player player) {
        UUID playerUUID = player.getUniqueId();

        if (isOnCooldown(player)) {
            String runeDisplay = getRuneDisplay(RuneState.COOLDOWN, player, 0);
            setPlayerDisplay(player, runeDisplay);
            return;
        }

        RuneState state = playerState.getOrDefault(playerUUID, RuneState.STACKING);

        if (state == RuneState.STACKING) {
            int currentStacks = trackActiveStacks(player);
            String runeDisplay = getRuneDisplay(RuneState.STACKING, player, currentStacks);
            setPlayerDisplay(player, runeDisplay);

        } else if (state == RuneState.ACTIVE) {
            int activeTime = activeState.get(playerUUID);
            activeTime--;
            activeState.put(playerUUID, activeTime);

            String runeDisplay = getRuneDisplay(RuneState.ACTIVE, player, activeTime);
            setPlayerDisplay(player, runeDisplay);

            if (activeTime == 0) {
                resetCooldown(player);
                playerState.put(playerUUID, RuneState.STACKING);
                removeAllModifiers(player);
                resetStacks(player);
                clearPlayerTimestamps(player);
                player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_BEACON_DEACTIVATE, 1.0f, 1.2f);
            }
        }
    }

    private void clearPlayerTimestamps(Player player) {
        stackTimestamps.remove(player.getUniqueId());
    }

    private void setPlayerDisplay(Player player, String runeDisplay) {
        PlayerStats playerStats = new PlayerStats();
        String statsDisplay = playerStats.getActionBarSections(player);

        String actionBarMessage = runeDisplay + " " + statsDisplay;
        player.sendActionBar(Component.text(actionBarMessage));
    }

    enum RuneState {
        COOLDOWN, STACKING, ACTIVE
    }

    private String getRuneDisplay(RuneState state, Player player, int value) {
        return switch (state) {
            case COOLDOWN -> "§7⚚ " + getCooldownDisplay(player);
            case ACTIVE -> {
                double remainingSeconds = value / 20.0;
                yield String.format("§e⚚ (%.1fs)", remainingSeconds);
            }
            case STACKING -> {
                if (value == 0) {
                    yield "§6⚚";
                } else {
                    yield "§6⚚ " + value + "/6";
                }
            }
        };
    }
}