package dev.ixpu.leaguemechanics.rune.keystones.precision;

import dev.ixpu.leaguemechanics.rune.RunePath;
import dev.ixpu.leaguemechanics.rune.RuneSlot;
import dev.ixpu.leaguemechanics.rune.StackingRune;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import net.kyori.adventure.text.Component;


public class Conqueror extends StackingRune {

    private double BASE_PHYSICAL_DAMAGE_PER_STACK = 2.5;

    private static final int MAXIMUM_STACKS = 12;

    public Conqueror(ConfigurationSection config) {
        super("conqueror", RunePath.PRECISION, RuneSlot.KEYSTONE, 12, 100);
        ConfigurationSection section = config.getConfigurationSection("runes.keystones.precision.conqueror");
        if (section != null) {
            this.BASE_PHYSICAL_DAMAGE_PER_STACK = section.getDouble("attack-damage-per-stack", this.BASE_PHYSICAL_DAMAGE_PER_STACK);
        }
    }

    @Override
    public void onEnable(Player player) {
        //
    }

    @Override
    public void onAttack(Player attacker, Entity target, EntityDamageByEntityEvent event) {
        if (!(target instanceof LivingEntity livingTarget)) {
            return;
        }
        if (livingTarget.getMaxHealth() < 20) {
            return;
        }

        addStack(attacker);

        int currentStacks = getStacks(attacker);
        double totalOutput = (currentStacks * BASE_PHYSICAL_DAMAGE_PER_STACK) / 2;
        event.setDamage(event.getDamage() + totalOutput);
    }

    @Override
    public void tick(Player player) {

        tickStackExpiry(player);

        int currentStacks = getStacks(player);
        displayStackInfo(player, currentStacks);
    }

    @Override
    protected void onStackAdded(Player player, int newStackCount) {
        if (newStackCount == 1) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "conqueror-stack-sound " + player.getName());
        } else if (newStackCount == MAXIMUM_STACKS) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "conqueror-max-stack-sound " + player.getName());
            player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 0.5f);
        }
    }

    @Override
    protected void onStacksExpired(Player player) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "conqueror-expired-sound " + player.getName());
    }

    private void displayStackInfo(Player player, int currentStacks) {
        if (currentStacks == 0) {
            player.sendActionBar(Component.text("§6🪓"));
        } else {
            player.sendActionBar(Component.text()
                    .append(Component.text("§e🪓 " + currentStacks + "/" + MAXIMUM_STACKS))
                    .build());
        }
    }
}