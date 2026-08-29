package dev.ixpu.leaguemechanics.util;

import dev.ixpu.leaguemechanics.LeagueMechanics;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

public class DebugLogger {
    private static final LeagueMechanics plugin = LeagueMechanics.getInstance();

    public static void debug(Player player, String message) {
        if (plugin.isDebugMode()) {
            player.sendMessage(Component.text(message));
        }
    }
}
