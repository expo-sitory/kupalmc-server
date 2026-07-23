package dev.ixpu.horsepower;

import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.HorseJumpEvent;

 // ==================== JUMP METER HANDLER ====================

public class HorseJumpListener implements Listener {
  private final HorsePowerPlugin plugin;
  private final HorseRaceManager raceManager;
  private final StaminaMinigameManager minigameManager;

  public HorseJumpListener(HorsePowerPlugin plugin, HorseRaceManager raceManager, StaminaMinigameManager minigameManager) {
    this.plugin = plugin;
    this.raceManager = raceManager;
    this.minigameManager = minigameManager;
  }
  @EventHandler
  public void onHorseJump(HorseJumpEvent event) {
    AbstractHorse horse = event.getEntity();
    if (horse.getPassengers().isEmpty()) return;
    
    Object passenger = horse.getPassengers().get(0);
    if (!(passenger instanceof Player)) return;

    Player player = (Player) passenger;
    double power = event.getPower();

    if (horse instanceof org.bukkit.entity.Horse) {
      org.bukkit.entity.Horse regularHorse = (org.bukkit.entity.Horse) horse;
      
      HorsePowerEvent customEvent = new HorsePowerEvent(player, regularHorse, power);
      plugin.getServer().getPluginManager().callEvent(customEvent);

      if (power >= 1.0) {
        if (minigameManager.isInMinigame(player)) {
          if (!minigameManager.isGame2(player)) {
            player.sendMessage("§a✓ PERFECT METER!");
          }
          minigameManager.recordPerfectJump(player);
        } else {
          raceManager.recordPerfectJump(player);
        }
      } else if (power <= 0.5) { 
        raceManager.recordImPerfectJump(player);
      } else if (power <= 0.9) {
        if (minigameManager.isInMinigame(player)) {
          player.sendMessage("§7METER: §e" + String.format("%.2f", power));  
          minigameManager.recordImperfectJump(player);
        } 
      }
      
      if (raceManager.isHorseInRace(regularHorse)) {
        event.setCancelled(true);
        org.bukkit.util.Vector vel = regularHorse.getVelocity();
        vel.setY(-10);
        regularHorse.setVelocity(vel);
      }
    }
  }
}