package dev.ixpu.leaguerunes.listener;

import dev.ixpu.leaguerunes.LeagueRunes;
import dev.ixpu.leaguerunes.RuneManager;
import dev.ixpu.leaguerunes.rune.RuneRegistry;
import dev.ixpu.leaguerunes.rune.keystones.resolve.GraspOfTheUndying;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerEventListener implements Listener {
    private final LeagueRunes plugin;
    private final RuneManager runeManager;
    private final RuneRegistry runeRegistry;

    public PlayerEventListener(LeagueRunes plugin) {
        this.plugin = plugin;
        this.runeManager = plugin.getRuneManager();
        this.runeRegistry = plugin.getRuneRegistry();
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        runeManager.loadPlayerRunes(player);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        runeManager.unloadPlayerRunes(player);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        GraspOfTheUndying grasp = (GraspOfTheUndying) runeRegistry.getRune("grasp-of-the-undying");
        
        if (grasp != null) {
            grasp.resetBonusHearts(player);
        }
    }
}