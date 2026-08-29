package dev.ixpu.horsepower;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.entity.EntityDismountEvent;

public class HorseRaceListener implements Listener {
  private final HorseRaceManager raceManager;

  public HorseRaceListener(HorseRaceManager raceManager) {
    this.raceManager = raceManager;
  }

  @EventHandler
  public void onDismount(EntityDismountEvent event) {
    if (event.getEntity() instanceof Player) {
      raceManager.endRace((Player) event.getEntity());
    }
  }

  @EventHandler
  public void onQuit(PlayerQuitEvent event) {
    raceManager.endRace(event.getPlayer());
  }
}
