package dev.ixpu.leaguemechanics.util;

import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;

public class ItemStatHelper {
    private static NamespacedKey AP_KEY;
    private static NamespacedKey AD_KEY;
    private static NamespacedKey AR_KEY;
    private static NamespacedKey MR_KEY;
    private static NamespacedKey BUILD_MODE_KEY;

    public static void initialize(Plugin plugin) {
        AP_KEY = new NamespacedKey(plugin, "league_ap");
        AD_KEY = new NamespacedKey(plugin, "league_ad");
        AR_KEY = new NamespacedKey(plugin, "league_ar");
        MR_KEY = new NamespacedKey(plugin, "league_mr");
        BUILD_MODE_KEY = new NamespacedKey(plugin, "league_build_mode");
    }

    public static void setStat(ItemStack item, String statType, double value) {
        if (item == null || item.getType().isAir() || item.getItemMeta() == null) return;

        var meta = item.getItemMeta();
        PersistentDataContainer data = meta.getPersistentDataContainer();
        NamespacedKey key = getKeyForStat(statType);

        if (key != null) {
            data.set(key, PersistentDataType.DOUBLE, value);
            item.setItemMeta(meta);
        }
    }

    public static double getStat(ItemStack item, String statType) {
        if (item == null || item.getType().isAir() || item.getItemMeta() == null) return 0;

        var meta = item.getItemMeta();
        PersistentDataContainer data = meta.getPersistentDataContainer();
        NamespacedKey key = getKeyForStat(statType);

        if (key != null && data.has(key, PersistentDataType.DOUBLE)) {
            return data.get(key, PersistentDataType.DOUBLE);
        }
        return 0;
    }

    public static boolean hasStat(ItemStack item, String statType) {
        if (item == null || item.getType().isAir() || item.getItemMeta() == null) return false;
        var meta = item.getItemMeta();
        NamespacedKey key = getKeyForStat(statType);
        return key != null && meta.getPersistentDataContainer().has(key, PersistentDataType.DOUBLE);
    }

    public static void setBuildMode(ItemStack item, boolean enabled) {
        if (item == null || item.getType().isAir() || item.getItemMeta() == null) return;

        var meta = item.getItemMeta();
        PersistentDataContainer data = meta.getPersistentDataContainer();

        if (enabled) {
            data.set(BUILD_MODE_KEY, PersistentDataType.BYTE, (byte) 1);
        } else {
            data.remove(BUILD_MODE_KEY);
        }

        item.setItemMeta(meta);
    }

    public static boolean isBuildMode(ItemStack item) {
        if (item == null || item.getType().isAir() || item.getItemMeta() == null) return false;
        var meta = item.getItemMeta();
        return meta.getPersistentDataContainer().has(BUILD_MODE_KEY, PersistentDataType.BYTE);
    }

    public static void clearStats(ItemStack item) {
        if (item == null || item.getType().isAir() || item.getItemMeta() == null) return;

        var meta = item.getItemMeta();
        PersistentDataContainer data = meta.getPersistentDataContainer();
        data.remove(AP_KEY);
        data.remove(AD_KEY);
        data.remove(AR_KEY);
        data.remove(MR_KEY);
        data.remove(BUILD_MODE_KEY);

        item.setItemMeta(meta);
    }

    private static NamespacedKey getKeyForStat(String statType) {
        return switch (statType.toUpperCase()) {
            case "AP" -> AP_KEY;
            case "AD" -> AD_KEY;
            case "AR" -> AR_KEY;
            case "MR" -> MR_KEY;
            default -> null;
        };
    }
}