package dev.ixpu.leaguemechanics.player;

import dev.ixpu.leaguemechanics.LeagueMechanics;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerClass {

    private static final Map<UUID, PlayerClassType> playerClassCache = new HashMap<>();

    private static final Map<PlayerClassType, Double> BASE_AD = new HashMap<>();
    private static final Map<PlayerClassType, Double> BASE_AP = new HashMap<>();
    private static final Map<PlayerClassType, Double> BASE_HP = new HashMap<>();
    private static final Map<PlayerClassType, Double> BASE_AR = new HashMap<>();
    private static final Map<PlayerClassType, Double> BASE_MR = new HashMap<>();
    private static final Map<PlayerClassType, Double> BASE_AS = new HashMap<>();
    private static final Map<PlayerClassType, Double> ATTACK_SPEED_RATIO = new HashMap<>();

    static {
        BASE_AD.put(PlayerClassType.FIGHTER, 10.0);
        BASE_AD.put(PlayerClassType.SUPPORT, 7.5);
        BASE_AD.put(PlayerClassType.ASSASSIN, 4.5);
        BASE_AD.put(PlayerClassType.MAGE, 0.0);
        BASE_AD.put(PlayerClassType.TANK, 3.5);
        BASE_AD.put(PlayerClassType.MARKSMAN, 6.0);

        BASE_AP.put(PlayerClassType.FIGHTER, 0.0);
        BASE_AP.put(PlayerClassType.SUPPORT, 0.0);
        BASE_AP.put(PlayerClassType.ASSASSIN, 2.5);
        BASE_AP.put(PlayerClassType.MAGE, 15.0);
        BASE_AP.put(PlayerClassType.TANK, 2.0);
        BASE_AP.put(PlayerClassType.MARKSMAN, 0.0);

        BASE_HP.put(PlayerClassType.FIGHTER, 0.0);
        BASE_HP.put(PlayerClassType.SUPPORT, 3.0);
        BASE_HP.put(PlayerClassType.ASSASSIN, 0.0);
        BASE_HP.put(PlayerClassType.MAGE, 0.0);
        BASE_HP.put(PlayerClassType.TANK, 6.0);
        BASE_HP.put(PlayerClassType.MARKSMAN, 0.0);

        BASE_AR.put(PlayerClassType.FIGHTER, 10.0);
        BASE_AR.put(PlayerClassType.SUPPORT, 0.0);
        BASE_AR.put(PlayerClassType.ASSASSIN, 0.0);
        BASE_AR.put(PlayerClassType.MAGE, 0.0);
        BASE_AR.put(PlayerClassType.TANK, 25.0);
        BASE_AR.put(PlayerClassType.MARKSMAN, 5.0);

        BASE_MR.put(PlayerClassType.FIGHTER, 0.0);
        BASE_MR.put(PlayerClassType.SUPPORT, 15.0);
        BASE_MR.put(PlayerClassType.ASSASSIN, 0.0);
        BASE_MR.put(PlayerClassType.MAGE, 0.0);
        BASE_MR.put(PlayerClassType.TANK, 15.0);
        BASE_MR.put(PlayerClassType.MARKSMAN, 0.0);

        BASE_AS.put(PlayerClassType.FIGHTER, 1.10);
        BASE_AS.put(PlayerClassType.SUPPORT, 1.00);
        BASE_AS.put(PlayerClassType.ASSASSIN, 1.10);
        BASE_AS.put(PlayerClassType.MAGE, 1.05);
        BASE_AS.put(PlayerClassType.TANK, 0.80);
        BASE_AS.put(PlayerClassType.MARKSMAN, 1.30);

        ATTACK_SPEED_RATIO.put(PlayerClassType.FIGHTER, 1.10);
        ATTACK_SPEED_RATIO.put(PlayerClassType.SUPPORT, 1.00);
        ATTACK_SPEED_RATIO.put(PlayerClassType.ASSASSIN, 1.10);
        ATTACK_SPEED_RATIO.put(PlayerClassType.MAGE, 1.05);
        ATTACK_SPEED_RATIO.put(PlayerClassType.TANK, 0.80);
        ATTACK_SPEED_RATIO.put(PlayerClassType.MARKSMAN, 1.30);
    }

    public static void setPlayerClass(Player player, PlayerClassType classType) {
        UUID uuid = player.getUniqueId();
        playerClassCache.put(uuid, classType);

        LeagueMechanics plugin = LeagueMechanics.getInstance();
        if (plugin != null && plugin.getRunePersistence() != null) {
            plugin.getRunePersistence().savePlayerClass(uuid, classType);
        }
    }

    public static PlayerClassType getPlayerClass(Player player) {
        UUID uuid = player.getUniqueId();

        if (playerClassCache.containsKey(uuid)) {
            return playerClassCache.get(uuid);
        }

        LeagueMechanics plugin = LeagueMechanics.getInstance();
        if (plugin != null && plugin.getRunePersistence() != null) {
            PlayerClassType loaded = plugin.getRunePersistence().loadPlayerClass(uuid);
            if (loaded != null) {
                playerClassCache.put(uuid, loaded);
                return loaded;
            }
        }

        return null;
    }

    public static void loadPlayerClass(Player player) {
        UUID uuid = player.getUniqueId();
        LeagueMechanics plugin = LeagueMechanics.getInstance();
        if (plugin != null && plugin.getRunePersistence() != null) {
            PlayerClassType loaded = plugin.getRunePersistence().loadPlayerClass(uuid);
            if (loaded != null) {
                playerClassCache.put(uuid, loaded);
            }
        }
    }

    public static void clearPlayerClass(Player player) {
        UUID uuid = player.getUniqueId();
        playerClassCache.remove(uuid);

        LeagueMechanics plugin = LeagueMechanics.getInstance();
        if (plugin != null && plugin.getRunePersistence() != null) {
            plugin.getRunePersistence().savePlayerClass(uuid, null);
        }
    }

    public static void unloadPlayer(UUID uuid) {
        playerClassCache.remove(uuid);
    }

    public static double getPlayerClassBaseAD(Player player) {
        PlayerClassType classType = getPlayerClass(player);
        return classType != null ? BASE_AD.getOrDefault(classType, 0.0) : 0.0;
    }

    public static double getPlayerClassBaseAP(Player player) {
        PlayerClassType classType = getPlayerClass(player);
        return classType != null ? BASE_AP.getOrDefault(classType, 0.0) : 0.0;
    }

    public static double getPlayerClassBaseHP(Player player) {
        PlayerClassType classType = getPlayerClass(player);
        return classType != null ? BASE_HP.getOrDefault(classType, 0.0) : 0.0;
    }

    public static double getPlayerClassBaseAR(Player player) {
        PlayerClassType classType = getPlayerClass(player);
        return classType != null ? BASE_AR.getOrDefault(classType, 0.0) : 0.0;
    }

    public static double getPlayerClassBaseMR(Player player) {
        PlayerClassType classType = getPlayerClass(player);
        return classType != null ? BASE_MR.getOrDefault(classType, 0.0) : 0.0;
    }

    public static double getPlayerClassBaseAS(Player player) {
        PlayerClassType classType = getPlayerClass(player);
        return classType != null ? BASE_AS.getOrDefault(classType, 0.0) : 0.0;
    }

    public static double getPlayerClassAttackSpeedRatio(Player player) {
        PlayerClassType classType = getPlayerClass(player);
        return classType != null ? ATTACK_SPEED_RATIO.getOrDefault(classType, 0.0) : 0.0;
    }
}
