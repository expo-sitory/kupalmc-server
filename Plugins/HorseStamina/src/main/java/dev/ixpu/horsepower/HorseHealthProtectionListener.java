package dev.ixpu.horsepower;

import org.bukkit.entity.Horse;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class HorseHealthProtectionListener implements Listener {
  private final HorseRaceManager raceManager;

  public HorseHealthProtectionListener(HorseRaceManager raceManager) {
    this.raceManager = raceManager;
  }

  @EventHandler
  public void onHorseDamage(EntityDamageEvent event) {
    if (!(event.getEntity() instanceof Horse)) {
      return;
    }

    Horse horse = (Horse) event.getEntity();

    // Check if horse is in race mode
    if (!raceManager.isHorseInRace(horse)) {
      return;
    }

    // Calculate what health would be after damage
    double damageAmount = event.getFinalDamage();
    double newHealth = horse.getHealth() - damageAmount;

   
    if (newHealth <= 0) {
      event.setCancelled(true);
      horse.setHealth(0.5);
    }
  }
}