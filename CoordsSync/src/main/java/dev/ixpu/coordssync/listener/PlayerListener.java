package dev.ixpu.coordssync.listener;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import dev.ixpu.coordssync.CoordsSync;
import dev.ixpu.coordssync.database.DatabaseManager;
import dev.ixpu.coordssync.database.PlayerData;



public class PlayerListener implements Listener {

    private final CoordsSync plugin;
    private final DatabaseManager databaseManager;

    public PlayerListener(CoordsSync plugin, DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
    }


    // Handle player join 

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String uuid = player.getUniqueId().toString();

        // Check if player has permission 
        if (!player.hasPermission("coordssync.use")) {
            plugin.getLogger().info(() -> "[CoordsSync] " + player.getName() + " does not have coordssync.use permission");
            return;
        }

        // Run async to avoid blocking main thread
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            PlayerData data = databaseManager.getPlayerCoordinates(uuid);

            if (data == null) {
                plugin.getLogger().info(() -> "[CoordsSync] No saved location for " + player.getName());
                return;
            }

            // Teleport to saved location on main thread
            Bukkit.getScheduler().runTask(plugin, () -> {
                teleportPlayerToLocation(player, data);
            });
        });
    }


    // Handle player quit 

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        String uuid = player.getUniqueId().toString();

        // Save coordinates async
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                Location loc = player.getLocation();
                if (loc == null || loc.getWorld() == null) {
                    plugin.getLogger().warning(() -> "[CoordsSync] Invalid location for " + player.getName());
                    return;
                }
                PlayerData data = new PlayerData(
                    uuid,
                    player.getName(),
                    loc.getWorld().getName(),
                    loc.getX(),
                    loc.getY(),
                    loc.getZ(),
                    loc.getYaw(),
                    loc.getPitch(),
                    plugin.getConfigManager().getServerId()
                );

                if (databaseManager.savePlayerCoordinates(data)) {
                    plugin.getLogger().info(() -> "[CoordsSync] Saved location for " + player.getName());
                }
            } catch (Exception e) {
                plugin.getLogger().warning(() -> "[CoordsSync] Failed to save coordinates for " + player.getName() + ": " + e.getMessage());
            }
        });
    }

 
    // Teleports the player to their saved location

    private void teleportPlayerToLocation(Player player, PlayerData data) {
        World world = Bukkit.getWorld(data.getWorld());

        if (world == null) {
            plugin.getLogger().warning(() -> "[CoordsSync] World '" + data.getWorld() + "' not found for " + player.getName());

            // Try to use first available world
            List<World> worlds = Bukkit.getWorlds();
            if (worlds.isEmpty()) {
                plugin.getLogger().severe("[CoordsSync] CRITICAL: No worlds available!");
                player.sendMessage("§c✗ No worlds available on this server. Contact an administrator.");
                return;
            }

            world = worlds.get(0);
            player.sendMessage("§e⚠ Your world '" + data.getWorld() + "' doesn't exist on this server.");
            player.sendMessage("§e⚠ Teleported to " + world.getName() + " instead.");
        }

        try {
            Location location = new Location(
                world,
                data.getX(),
                data.getY(),
                data.getZ(),
                data.getYaw(),
                data.getPitch()
            );

            player.teleport(location);
            player.sendMessage("§a✓ Teleported to your last location!");
            plugin.getLogger().info("[CoordsSync] Teleported " + player.getName() + " to " + world.getName());
        } catch (Exception e) {
            plugin.getLogger().severe(() -> "[CoordsSync] Failed to teleport " + player.getName() + ": " + e.getMessage());
            player.sendMessage("§c✗ Failed to teleport you to your last location.");
        }
    }
}