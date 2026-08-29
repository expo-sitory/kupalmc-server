package dev.ixpu.leaguemechanics.manager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import dev.ixpu.leaguemechanics.player.PlayerRuneData;
import dev.ixpu.leaguemechanics.rune.CooldownHandler;
import dev.ixpu.leaguemechanics.util.RuneDetector;

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

        for (CooldownHandler rune : runeData.getAllRunes()) {
            if (rune != null) {
                rune.onEnable(player);
            }
        }
    }

    public void reloadPlayerRunes(Player player) {
        unloadPlayerRunes(player);
        loadPlayerRunes(player);
    }

    public void unloadPlayerRunes(Player player) {
        UUID uuid = player.getUniqueId();
        PlayerRuneData runeData = playerRuneData.get(uuid);

        if (runeData != null) {
            for (CooldownHandler rune : runeData.getAllRunes()) {
                if (rune != null) {
                    rune.onDisable(player);
                }
            }
        }

        playerRuneData.remove(uuid);
        playerRunesActive.remove(uuid);
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

        for (CooldownHandler rune : runeData.getAllRunes()) {
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

    public JavaPlugin getPlugin() {
        return plugin;
    }

    public void setPlayerKeystoneRune(Player player, CooldownHandler rune) {
        UUID uuid = player.getUniqueId();
        PlayerRuneData runeData = playerRuneData.getOrDefault(uuid, new PlayerRuneData(player));

        CooldownHandler oldRune = runeData.getKeystoneRune();
        if (oldRune != null) {
            oldRune.onDisable(player);
        }

        runeData.setKeystoneRune(rune);
        rune.onEnable(player);
        playerRuneData.put(uuid, runeData);
    }

    public void clearPlayerRunes(Player player) {
        UUID uuid = player.getUniqueId();
        PlayerRuneData runeData = playerRuneData.get(uuid);

        if (runeData != null) {
            for (CooldownHandler rune : runeData.getAllRunes()) {
                if (rune != null) {
                    rune.onDisable(player);
                }
            }

            runeData.setKeystoneRune(null);
            runeData.setPrimaryPath(null);
            runeData.setSecondaryPath(null);
            runeData.setPrimarySlot1Rune(null);
            runeData.setPrimarySlot2Rune(null);
            runeData.setPrimarySlot3Rune(null);
            runeData.setSecondarySlot1Rune(null);
            runeData.setSecondarySlot2Rune(null);
        }
    }
}