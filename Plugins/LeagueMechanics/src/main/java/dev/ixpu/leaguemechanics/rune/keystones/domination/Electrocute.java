package dev.ixpu.leaguemechanics.rune.keystones.domination;

import dev.ixpu.leaguemechanics.rune.RunePath;
import dev.ixpu.leaguemechanics.rune.RuneSlot;
import dev.ixpu.leaguemechanics.rune.StackingRune;

import java.util.UUID;

import org.bukkit.Sound;

import org.bukkit.entity.Player;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import net.kyori.adventure.text.Component;


public class Electrocute extends StackingRune {

    private double BASE_PHYSICAL_DAMAGE = 3.5;

    private static final int MAXIMUM_STACKS = 3;

    int COOLDOWN_DURATION_SECONDS = 25;

    public Electrocute(org.bukkit.configuration.ConfigurationSection config) {
        super("electrocute", RunePath.DOMINATION, RuneSlot.KEYSTONE, 3, 60);
        enablePerTargetStacking();
        enablePerTargetExpiry();

        ConfigurationSection section = config.getConfigurationSection("runes.keystones.domination.electrocute");
        if (section != null) {
            this.BASE_PHYSICAL_DAMAGE = section.getDouble("base-physical-damage", this.BASE_PHYSICAL_DAMAGE);
            this.COOLDOWN_DURATION_SECONDS = section.getInt("cooldown", COOLDOWN_DURATION_SECONDS);
        }
        this.setCooldownSeconds(COOLDOWN_DURATION_SECONDS);
    }

    @Override
    public void onEnable(Player player) {
        //
    }

    @Override
    public void onAttack(Player attacker, Entity target, EntityDamageByEntityEvent event) {
        triggerElectrocute(attacker, target, event);
    }

    public void onProjectileHit(Player shooter, Entity target) {
        triggerElectrocute(shooter, target, null);
    }

    private void triggerElectrocute(Player player, Entity target, EntityDamageByEntityEvent event) {
        UUID targetUUID = target.getUniqueId();

        if (!(target instanceof LivingEntity livingTarget)) {
            return;
        }
        if (livingTarget.getMaxHealth() < 20) {
            return;
        }
        if (isOnCooldown(player)) {
            return;
        }

        switchTarget(player, targetUUID);
        addStack(player, targetUUID);

        int stacks = getStacks(player, targetUUID);
        if (stacks >= MAXIMUM_STACKS) {
            target.getWorld().strikeLightning(target.getLocation());
            resetStacksForTarget(player, targetUUID);
            resetCooldown(player);

            player.playSound(player.getLocation(), Sound.ITEM_TRIDENT_THUNDER, 1.0f, 1.2f);
            double totalOutput = BASE_PHYSICAL_DAMAGE / 2;

            if (event != null) {
                event.setDamage(event.getDamage() + totalOutput);
            } else {
                livingTarget.damage(totalOutput, player);
            }
        }

    }

    @Override
    public void tick(Player player) {
        if (isOnCooldown(player)) {
            displayCooldownInfo(player);
            return;
        }

        tickStackExpiry(player);

        UUID lastTargetUUID = lastTarget.getOrDefault(player.getUniqueId(), null);
        int maxStacks = 0;
        if (lastTargetUUID != null) {
            maxStacks = getStacks(player, lastTargetUUID);
        }

        if (maxStacks > 0) {
            displayStackInfo(player, maxStacks);
        } else {
            displayIdleState(player);
        }
    }

    private void displayStackInfo(Player player, int stacks) {
        player.sendActionBar(Component.text()
                .append(Component.text("§c⚡ " + stacks + "/" + MAXIMUM_STACKS))
                .build());
    }

    private void displayCooldownInfo(Player player) {
        String cooldownDisplay = getCooldownDisplay(player);
        player.sendActionBar(Component.text("§7⚡ " + cooldownDisplay));
    }

    private void displayIdleState(Player player) {
        player.sendActionBar(Component.text("§4⚡"));
    }
}