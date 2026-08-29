package dev.ixpu.leaguemechanics.manager;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ItemPassivesManager {
    private static ItemPassivesManager instance;

    private final Map<String, Map<UUID, Integer>> killCounts = new HashMap<>();
    private final Map<String, Map<UUID, Boolean>> passiveDisabled = new HashMap<>();

    public static ItemPassivesManager getInstance() {
        if (instance == null) {
            instance = new ItemPassivesManager();
        }
        return instance;
    }

    public int getKillCount(Player player, String itemId) {
        UUID uuid = player.getUniqueId();
        return killCounts.computeIfAbsent(itemId, k -> new HashMap<>()).getOrDefault(uuid, 0);
    }

    public void addKill(Player player, String itemId) {
        UUID uuid = player.getUniqueId();
        killCounts.computeIfAbsent(itemId, k -> new HashMap<>()).put(uuid, getKillCount(player, itemId) + 1);
    }

    public boolean isPassiveDisabled(Player player, String itemId) {
        UUID uuid = player.getUniqueId();
        return passiveDisabled.computeIfAbsent(itemId, k -> new HashMap<>()).getOrDefault(uuid, false);
    }

    public void disablePassive(Player player, String itemId) {
        UUID uuid = player.getUniqueId();
        passiveDisabled.computeIfAbsent(itemId, k -> new HashMap<>()).put(uuid, true);
    }

    public void setKillCount(Player player, String itemId, int count) {
        UUID uuid = player.getUniqueId();
        killCounts.computeIfAbsent(itemId, k -> new HashMap<>()).put(uuid, count);
    }
}