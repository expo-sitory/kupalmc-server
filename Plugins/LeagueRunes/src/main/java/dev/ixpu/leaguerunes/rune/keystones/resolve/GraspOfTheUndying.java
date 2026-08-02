package dev.ixpu.leaguerunes.rune.keystones.resolve;

import dev.ixpu.leaguerunes.rune.BaseRune;
import dev.ixpu.leaguerunes.rune.RunePath;
import dev.ixpu.leaguerunes.rune.RuneSlot;
import net.kyori.adventure.text.Component;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


public class GraspOfTheUndying extends BaseRune {
    private int MAX_STACKS = 4;
    private int COMBAT_DURATION_TICKS = 60;
    private int ATTACK_WINDOW_TICKS = 100;
    private int COOLDOWN_DURATION_TICKS = 600;
    private double DAMAGE_PERCENT = 0.05;
    private double HEAL_PERCENT = 0.15;
    private int BONUS_HEARTS = 1;
    private int MAX_BONUS_HEARTS = 10;

    private final Map<UUID, Integer> playerStacks = new HashMap<>();
    private final Map<UUID, Integer> combatTimers = new HashMap<>();
    private final Map<UUID, Integer> attackWindowTicks = new HashMap<>();
    private final Map<UUID, Integer> totalBonusHearts = new HashMap<>();
    private final Map<UUID, List<AttributeModifier>> activeModifiers = new HashMap<>();

    public GraspOfTheUndying(org.bukkit.configuration.ConfigurationSection config) {
        super("grasp-of-the-undying", RunePath.RESOLVE, RuneSlot.KEYSTONE);
        ConfigurationSection section = config.getConfigurationSection("runes.keystones.resolve.grasp-of-the-undying");
        if (section != null) {
            this.MAX_STACKS = section.getInt("max-stacks", 4);
            this.COMBAT_DURATION_TICKS = section.getInt("combat-duration", 60);
            this.ATTACK_WINDOW_TICKS = section.getInt("attack-window", 100);
            this.COOLDOWN_DURATION_TICKS = section.getInt("cooldown-duration", 600);
            this.DAMAGE_PERCENT = section.getDouble("damage-percent", 0.05);
            this.HEAL_PERCENT = section.getDouble("heal-percent", 0.15);
            this.BONUS_HEARTS = section.getInt("bonus-hearts", 1);
            this.MAX_BONUS_HEARTS = section.getInt("max-bonus-hearts", 10);
        }
        this.setCooldownSeconds(COOLDOWN_DURATION_TICKS / 20.0);
    }

    public GraspOfTheUndying() {
        super("grasp-of-the-undying", RunePath.RESOLVE, RuneSlot.KEYSTONE);
        this.setCooldownSeconds(COOLDOWN_DURATION_TICKS / 20.0);
    }

    @Override
    public void onEnable(Player player) {
        UUID uuid = player.getUniqueId();
        playerStacks.put(uuid, 0);
        combatTimers.put(uuid, 0);
        attackWindowTicks.put(uuid, 0);
        totalBonusHearts.put(uuid, 0);
    }

    @Override
    public void onDisable(Player player) {
        UUID uuid = player.getUniqueId();
        clearPlayerCooldown(player);
        playerStacks.remove(uuid);
        combatTimers.remove(uuid);
        attackWindowTicks.remove(uuid);
        totalBonusHearts.remove(uuid);
        removeAllModifiers(player);
    }

    public void onCombat(Player player) {
        UUID playerUUID = player.getUniqueId();
        
        if (isOnCooldown(player)) {
            return;
        }
        
        combatTimers.put(playerUUID, COMBAT_DURATION_TICKS);
        
        int stacks = playerStacks.getOrDefault(playerUUID, 0);
        if (stacks < MAX_STACKS) {
            stacks++;
            playerStacks.put(playerUUID, stacks);
            
            if (stacks == MAX_STACKS) {
                attackWindowTicks.put(playerUUID, ATTACK_WINDOW_TICKS);
            }
        }
    }

    @Override
    public void onAttack(Player attacker, Entity target, EntityDamageByEntityEvent event) {
        UUID playerUUID = attacker.getUniqueId();
        int stacks = playerStacks.getOrDefault(playerUUID, 0);
        int attackWindow = attackWindowTicks.getOrDefault(playerUUID, 0);

        if (!(target instanceof LivingEntity)) {
            return;
        }
        
        LivingEntity livingTarget = (LivingEntity) target;
        double maxHealth = livingTarget.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        
        if (maxHealth < 20) {
            return;
        }
        
        if (stacks >= MAX_STACKS && attackWindow > 0) {
            triggerGrasp(attacker, event);
            playerStacks.put(playerUUID, 0);
            attackWindowTicks.put(playerUUID, 0);
            combatTimers.put(playerUUID, 0);
            resetCooldown(attacker);
        } else {
            onCombat(attacker);
        }
    }

    @Override
    public void tick(Player player) {
        UUID playerUUID = player.getUniqueId();

        if (isOnCooldown(player)) {
            displayCooldown(player);
            return;
        }
        
        int combatTimer = combatTimers.getOrDefault(playerUUID, 0);
        if (combatTimer > 0) {
            combatTimer--;
            combatTimers.put(playerUUID, combatTimer);
            
            if (combatTimer == 0) {
                playerStacks.put(playerUUID, 0);
                attackWindowTicks.put(playerUUID, 0);
            }
        }

        int attackWindow = attackWindowTicks.getOrDefault(playerUUID, 0);
        if (attackWindow > 0) {
            attackWindow--;
            attackWindowTicks.put(playerUUID, attackWindow);
            
            if (attackWindow == 0) {
                playerStacks.put(playerUUID, 0);
            }
        }
        
        displayStackInfo(player);
    }

    private void triggerGrasp(Player player, EntityDamageByEntityEvent event) {
        var maxHealthAttr = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (maxHealthAttr == null) return;
        
        double maxHealth = maxHealthAttr.getValue();
        
        double bonusDamage = maxHealth * DAMAGE_PERCENT;
        event.setDamage(event.getDamage() + bonusDamage);
        
        double healAmount = maxHealth * HEAL_PERCENT;
        double currentHealth = player.getHealth();
        player.setHealth(Math.min(maxHealth, currentHealth + healAmount));
        
        applyBonusHealth(player);
        
        player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_WITHER_AMBIENT, 1.0f, 1.0f);
    }

    private void applyBonusHealth(Player player) {
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
        var maxHealthAttr = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        
        if (maxHealthAttr != null) {
            for (AttributeModifier mod : mods) {
                try {
                    maxHealthAttr.removeModifier(mod);
                } catch (Exception e) {}
            }
        }
        activeModifiers.put(playerUUID, new ArrayList<>());
    }

    private void displayCooldown(Player player) {
        String cooldownDisplay = getCooldownDisplay(player);
        player.sendActionBar(Component.text("§7🥊 " + cooldownDisplay));
    }

    private void displayReadyInfo(Player player, int attackWindow) {
        double remainingSeconds = attackWindow / 20.0;
        player.sendActionBar(Component.text()
                .append(Component.text(String.format("§a🥊 " + "(%.1fs)", remainingSeconds), net.kyori.adventure.text.format.NamedTextColor.WHITE))
                .build());
    }

    private void displayStackInfo(Player player) {
        UUID playerUUID = player.getUniqueId();
        int stacks = playerStacks.getOrDefault(playerUUID, 0);
        int attackWindow = attackWindowTicks.getOrDefault(playerUUID, 0);
        
        if (stacks >= MAX_STACKS && attackWindow > 0) {
            displayReadyInfo(player, attackWindow);
        }
        else if (stacks > 0) {
            player.sendActionBar(Component.text()
                    .append(Component.text(String.format("§2🥊 " + stacks + "/" + MAX_STACKS), net.kyori.adventure.text.format.NamedTextColor.WHITE))
                    .build());
        } else {
              player.sendActionBar(Component.text()
                    .append(Component.text(String.format("§2🥊"), net.kyori.adventure.text.format.NamedTextColor.WHITE))
                    .build());
        }
    }

    public void resetBonusHearts(Player player) {
        UUID playerUUID = player.getUniqueId();
        totalBonusHearts.put(playerUUID, 0);
        removeAllModifiers(player);
    }
}