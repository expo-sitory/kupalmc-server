package dev.ixpu.leaguemechanics.util;

import dev.ixpu.leaguemechanics.rune.RunePath;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.UUID;

public class RunePersistence {
    private final File dataFile;

    public RunePersistence(Plugin plugin) {
        File dataFolder = plugin.getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        this.dataFile = new File(dataFolder, "player-runes.yml");
        if (!this.dataFile.exists()) {
            try {
                this.dataFile.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void savePrimaryPath(UUID playerUUID, RunePath path) {
        FileConfiguration config = YamlConfiguration.loadConfiguration(dataFile);
        String playerKey = playerUUID.toString();
        config.set(playerKey + ".primary-path", path.getId());
        try {
            config.save(dataFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public RunePath loadPrimaryPath(UUID playerUUID) {
        FileConfiguration config = YamlConfiguration.loadConfiguration(dataFile);
        String playerKey = playerUUID.toString();
        String pathId = config.getString(playerKey + ".primary-path");
        if (pathId == null) {
            return null;
        }
        return RunePath.fromId(pathId);
    }

    public void saveKeystoneRune(UUID playerUUID, String runeId) {
        FileConfiguration config = YamlConfiguration.loadConfiguration(dataFile);
        String playerKey = playerUUID.toString();
        config.set(playerKey + ".keystone-rune", runeId);
        try {
            config.save(dataFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String loadKeystoneRune(UUID playerUUID) {
        FileConfiguration config = YamlConfiguration.loadConfiguration(dataFile);
        String playerKey = playerUUID.toString();
        return config.getString(playerKey + ".keystone-rune");
    }

    public void clearAllRunes(UUID playerUUID) {
        FileConfiguration config = YamlConfiguration.loadConfiguration(dataFile);
        String playerKey = playerUUID.toString();
        config.set(playerKey + ".primary-path", null);
        config.set(playerKey + ".keystone-rune", null);
        config.set(playerKey + ".secondary-path", null);
        config.set(playerKey + ".primary-slot-1-rune", null);
        config.set(playerKey + ".primary-slot-2-rune", null);
        config.set(playerKey + ".primary-slot-3-rune", null);
        config.set(playerKey + ".secondary-slot-1-rune", null);
        config.set(playerKey + ".secondary-slot-2-rune", null);

        if (config.getConfigurationSection(playerKey) != null && config.getConfigurationSection(playerKey).getKeys(false).isEmpty()) {
            config.set(playerKey, null);
        }

        try {
            config.save(dataFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}