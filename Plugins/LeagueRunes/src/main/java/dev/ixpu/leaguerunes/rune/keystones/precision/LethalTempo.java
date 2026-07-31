package dev.ixpu.leaguerunes.rune.keystones.precision;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import dev.ixpu.leaguerunes.rune.BaseRune;
import dev.ixpu.leaguerunes.rune.RunePath;
import dev.ixpu.leaguerunes.rune.RuneSlot;
import net.kyori.adventure.text.Component;

public class LethalTempo extends BaseRune {
    private static final int COOLDOWN_DURATION_TICKS = 600;
    private int MAX_STACKS = 6;
    private int STACK_DURATION_TICKS = 120;
    private int ACTIVE_DURATION_TICKS = 100;  
    private double ATTACK_SPEED_BONUS = 0.8;  

    private final Map<UUID, Map<UUID, Integer>> playerStacks = new HashMap<>();
    private final Map<UUID, UUID> lastTarget = new HashMap<>();
    private final Map<UUID, RuneState> playerState = new HashMap<>();
    private final Map<UUID, Integer> activeTimers = new HashMap<>();
    private final Map<UUID, Integer> cooldownTimers = new HashMap<>();
    private final Map<UUID, Integer> stackExpiryTicks = new HashMap<>();
    private final Map<UUID, List<AttributeModifier>> activeModifiers = new HashMap<>();

    public LethalTempo(org.bukkit.configuration.ConfigurationSection config) {
        super("lethal-tempo", RunePath.PRECISION, RuneSlot.KEYSTONE);
        ConfigurationSection section = config.getConfigurationSection("runes.keystones.precision.lethal-tempo");
        if (section != null) {
            this.MAX_STACKS = section.getInt("max-stacks", this.MAX_STACKS);
            this.STACK_DURATION_TICKS = section.getInt("stack-duration", this.STACK_DURATION_TICKS);
            this.ACTIVE_DURATION_TICKS = section.getInt("active-duration", this.ACTIVE_DURATION_TICKS);
            this.ATTACK_SPEED_BONUS = section.getDouble("attack-speed-bonus", this.ATTACK_SPEED_BONUS);
        }
    }

    private enum RuneState {
        STACKING, ACTIVE, COOLDOWN
    }

    public LethalTempo() {
        super("lethal-tempo", RunePath.PRECISION, RuneSlot.KEYSTONE);
        this.hasStacking = false;
    }

    @Override
    public void onEnable(Player player) {
        UUID uuid = player.getUniqueId();
        playerStacks.put(uuid, new HashMap<>());
        playerState.put(uuid, RuneState.STACKING);
        activeTimers.put(uuid, 0);
        cooldownTimers.put(uuid, 0);
        stackExpiryTicks.put(uuid, 0);
    }

    @Override
    public void onDisable(Player player) {
        UUID uuid = player.getUniqueId();
        playerStacks.remove(uuid);
        lastTarget.remove(uuid);
        playerState.remove(uuid);
        activeTimers.remove(uuid);
        cooldownTimers.remove(uuid);
        stackExpiryTicks.remove(uuid);
        removeAllModifiers(player);
    }

    @Override
    public void onAttack(Player attacker, Entity target, EntityDamageByEntityEvent event) {
        UUID playerUUID = attacker.getUniqueId();
        UUID targetUUID = target.getUniqueId();
        RuneState state = playerState.getOrDefault(playerUUID, RuneState.STACKING);

        if (state == RuneState.COOLDOWN) {
            return;
        }

        // Check target change
        UUID prevTarget = lastTarget.get(playerUUID);
        if (prevTarget != null && !prevTarget.equals(targetUUID)) {
            playerStacks.get(playerUUID).clear();
        }
        lastTarget.put(playerUUID, targetUUID);

        if (state == RuneState.STACKING) {
            addStack(attacker, targetUUID);
        } else if (state == RuneState.ACTIVE) {
            refreshActiveTimer(attacker);
        }
    }

    private void addStack(Player player, UUID targetUUID) {
        UUID playerUUID = player.getUniqueId();
        Map<UUID, Integer> stacks = playerStacks.get(playerUUID);
        int current = stacks.getOrDefault(targetUUID, 0);

        if (current < MAX_STACKS) {
            current++;
            stacks.put(targetUUID, current);
            stackExpiryTicks.put(playerUUID, STACK_DURATION_TICKS);

            displayStackInfo(player, current);

            // Apply progressive attack speed bonus during stacking
            applyProgressiveAttackSpeed(player, current);

            if (current == MAX_STACKS) {
                enterActiveState(player);
            }
        }
    }

    private void removeAllModifiers(Player player) {
        UUID playerUUID = player.getUniqueId();
        List<AttributeModifier> mods = activeModifiers.getOrDefault(playerUUID, new ArrayList<>());
        for (AttributeModifier mod : mods) {
            try {
                player.getAttribute(Attribute.GENERIC_ATTACK_SPEED).removeModifier(mod);
            } catch (Exception e) {
                // debug
            }
        }
        activeModifiers.put(playerUUID, new ArrayList<>());
    }

    @SuppressWarnings("removal")
    private void applyProgressiveAttackSpeed(Player player, int stackCount) {
        removeAllModifiers(player);
        
        double bonusAmount = (stackCount / 6.0) * ATTACK_SPEED_BONUS;

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
        activeTimers.put(playerUUID, ACTIVE_DURATION_TICKS);
        applyAttackSpeedBonus(player);
    }

    private void refreshActiveTimer(Player player) {
        UUID playerUUID = player.getUniqueId();
        activeTimers.put(playerUUID, ACTIVE_DURATION_TICKS);
    }

    @SuppressWarnings("removal")
    private void applyAttackSpeedBonus(Player player) {
        removeAllModifiers(player);

        var modifier = new AttributeModifier(
                UUID.randomUUID(),
                "lethal-tempo-active",
                ATTACK_SPEED_BONUS,
                AttributeModifier.Operation.ADD_SCALAR
        );
        
        var attackSpeedAttr = player.getAttribute(Attribute.GENERIC_ATTACK_SPEED);
        if (attackSpeedAttr == null) {
            return;
        }
        
        attackSpeedAttr.addModifier(modifier);
        activeModifiers.computeIfAbsent(player.getUniqueId(), k -> new ArrayList<>()).add(modifier);
    }

    @Override
    public void tick(Player player) {
        UUID playerUUID = player.getUniqueId();
        RuneState state = playerState.get(playerUUID);

        if (state == RuneState.STACKING) {
            int expiry = stackExpiryTicks.getOrDefault(playerUUID, 0);
            if (expiry > 0) {
                expiry--;
                stackExpiryTicks.put(playerUUID, expiry);
                if (expiry == 0) {
                    playerStacks.get(playerUUID).clear();
                    lastTarget.put(playerUUID, null);
                }
            }
        } else if (state == RuneState.ACTIVE) {
            int activeTime = activeTimers.get(playerUUID);
            activeTime--;
            activeTimers.put(playerUUID, activeTime);

            // Display active time indicator
            if (activeTime > 0) {
                double remainingSeconds = activeTime / 10.0;
            
                player.sendActionBar(Component.text()
                        .append(Component.text(String.format("§6⚚ " + "%.1fs " + "§f(+20%% ats)", remainingSeconds), net.kyori.adventure.text.format.NamedTextColor.WHITE))
                        .build());
            }

            if (activeTime == 0) {
                playerState.put(playerUUID, RuneState.COOLDOWN);
                cooldownTimers.put(playerUUID, COOLDOWN_DURATION_TICKS);
                removeAllModifiers(player);
                playerStacks.get(playerUUID).clear();
                lastTarget.put(playerUUID, null);
                player.sendActionBar(Component.text("ʟᴇᴛʜᴀʟ ᴛᴇᴍᴘᴏ ᴄᴏᴏʟᴅᴏᴡɴ - 30s")
                        .color(net.kyori.adventure.text.format.NamedTextColor.DARK_GRAY));
            }
        } else if (state == RuneState.COOLDOWN) {
            int cooldown = cooldownTimers.get(playerUUID);
            cooldown--;
            cooldownTimers.put(playerUUID, cooldown);

            if (cooldown == 0) {
                playerState.put(playerUUID, RuneState.STACKING);
                playerStacks.get(playerUUID).clear();
            }
        }
    }

    private void displayStackInfo(Player player, int stacks) {        
        double percent = (stacks / 6.0) * 20.0;
        player.sendActionBar(Component.text()
                .append(Component.text("§6⚚ " + stacks + "/6 ", net.kyori.adventure.text.format.NamedTextColor.WHITE))
                .append(Component.text(String.format("(+%.1f%% ats)", percent), net.kyori.adventure.text.format.NamedTextColor.WHITE))
                .build());
    }
}