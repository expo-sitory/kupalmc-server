package dev.ixpu.leaguemechanics.rune;

import org.bukkit.entity.Player;

public interface RuneCooldownGate {
    boolean isAnyHotbarOnCooldown(Player player);
    boolean letRunesThrough(Player player);
}
