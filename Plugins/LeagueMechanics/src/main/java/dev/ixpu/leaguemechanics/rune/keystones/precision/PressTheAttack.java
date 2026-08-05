package dev.ixpu.leaguemechanics.rune.keystones.precision;

import dev.ixpu.leaguemechanics.rune.RunePath;
import dev.ixpu.leaguemechanics.rune.RuneSlot;
import dev.ixpu.leaguemechanics.rune.StackingRune;

import java.util.*;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import net.kyori.adventure.text.Component;


public class PressTheAttack extends StackingRune {

    private double BASE_PHYSICAL_DAMAGE = 4.5;

    private static final int MAX_STACKS = 3;

    int COOLDOWN_DURATION_SECONDS = 200;

    public PressTheAttack(org.bukkit.configuration.ConfigurationSection config) {
        super("press-the-attack", RunePath.PRECISION, RuneSlot.KEYSTONE, 3, 60);
        enablePerTargetStacking();
        enablePerTargetExpiry();

        ConfigurationSection section = config.getConfigurationSection("runes.keystones.precision.press-the-attack");

        if (section != null) {
            this.BASE_PHYSICAL_DAMAGE = section.getDouble("base-physical-damage", this.BASE_PHYSICAL_DAMAGE);
            this.COOLDOWN_DURATION_SECONDS = section.getInt("cooldown", COOLDOWN_DURATION_SECONDS);
        }
        this.setCooldownSeconds(COOLDOWN_DURATION_SECONDS);
    }

    @Override
    public void onAttack(Player attacker, Entity target, EntityDamageByEntityEvent event) {
        triggerPressTheAttack(attacker, target, event);
    }

    private void triggerPressTheAttack(Player player, Entity target, EntityDamageByEntityEvent event) {
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

        int currentStacks = getStacks(player, targetUUID);

        if (currentStacks == 2) {
            double totalOutput = BASE_PHYSICAL_DAMAGE / 2;

            event.setDamage(event.getDamage() + totalOutput);
            resetStacksForTarget(player, targetUUID);

            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "press-the-attack-stack-sound " + player.getName());
            player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 0.5f);
            resetCooldown(player);
        } else {
            addStack(player, targetUUID);
        }
    }

    @Override
    public void onEnable(Player player) {
        //
    }

    @Override
    public void tick(Player player) {
        tickStackExpiry(player);
        displayStackInfo(player);
    }

    private void displayStackInfo(Player player) {
        String cooldownDisplay = getCooldownDisplay(player);
        if (!cooldownDisplay.isEmpty()) {
            player.sendActionBar(Component.text("§7✽ " + cooldownDisplay));
            return;
        }

        UUID lastTargetUUID = lastTarget.getOrDefault(player.getUniqueId(), null);
        int currentStacks = 0;
        if (lastTargetUUID != null) {
            currentStacks = getStacks(player, lastTargetUUID);
        }

        if (currentStacks == 0) {
            player.sendActionBar(Component.text("§6✽"));
        } else {
            player.sendActionBar(Component.text()
                    .append(Component.text("§e✽ " + currentStacks + "/" + MAX_STACKS))
                    .build());
        }
    }
}