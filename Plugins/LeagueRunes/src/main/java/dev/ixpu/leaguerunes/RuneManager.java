package dev.ixpu.leaguerunes;

import dev.ixpu.leaguerunes.player.PlayerRuneData;
import dev.ixpu.leaguerunes.rune.BaseRune;
import dev.ixpu.leaguerunes.util.RuneDetector;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RuneManager {
    private final JavaPlugin plugin;
    private final Map<UUID, PlayerRuneData> playerRuneData = new HashMap<>();
    private final Map<UUID, Boolean> playerRunesActive = new HashMap<>();

    public RuneManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void loadPlayerRunes(Player player) {
        UUID uuid = player.getUniqueId();
        PlayerRuneData runeData = RuneDetector.detectPlayerRunes(player);
        playerRuneData.put(uuid, runeData);
        playerRunesActive.put(uuid, true);

        // Enable all runes for the player
        for (BaseRune rune : runeData.getAllRunes()) {
            if (rune != null) {
                rune.onEnable(player);
            }
        }

        plugin.getLogger().info("Loaded runes for player: " + player.getName());
    }

    public void reloadPlayerRunes(Player player) {
        unloadPlayerRunes(player);
        loadPlayerRunes(player);
    }

    public void unloadPlayerRunes(Player player) {
        UUID uuid = player.getUniqueId();
        PlayerRuneData runeData = playerRuneData.get(uuid);

        if (runeData != null) {
            // Disable all runes for the player
            for (BaseRune rune : runeData.getAllRunes()) {
                if (rune != null) {
                    rune.onDisable(player);
                }
            }
        }

        playerRuneData.remove(uuid);
        playerRunesActive.remove(uuid);
        plugin.getLogger().info("Unloaded runes for player: " + player.getName());
    }

    public void tickAllPlayerRunes() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            tickPlayerRunes(player);
        }
    }

    public void tickPlayerRunes(Player player) {
        UUID uuid = player.getUniqueId();

        if (!playerRunesActive.getOrDefault(uuid, false)) {
            return;
        }

        PlayerRuneData runeData = playerRuneData.get(uuid);
        if (runeData == null) {
            return;
        }

        for (BaseRune rune : runeData.getAllRunes()) {
            if (rune != null) {
                rune.tick(player);
            }
        }
    }

    public PlayerRuneData getPlayerRuneData(Player player) {
        return playerRuneData.get(player.getUniqueId());
    }

    public boolean hasActiveRunes(Player player) {
        return playerRunesActive.getOrDefault(player.getUniqueId(), false);
    }

    public void setRunesActive(Player player, boolean active) {
        playerRunesActive.put(player.getUniqueId(), active);
    }

    public void clearAll() {
        playerRuneData.clear();
        playerRunesActive.clear();
    }
}
