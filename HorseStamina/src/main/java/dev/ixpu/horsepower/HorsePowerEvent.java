package dev.ixpu.horsepower;

import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class HorsePowerEvent extends Event {
  private static final HandlerList handlers = new HandlerList();
  private final Player player;
  private final Horse horse;
  private final double power;

  public HorsePowerEvent(Player player, Horse horse, double power) {
    this.player = player;
    this.horse = horse;
    this.power = power;
  }

  public Player getPlayer() { return player; }
  public Horse getHorse() { return horse; }
  public double getPower() { return power; }
  public boolean isPerfect() { return power >= 1.0; }

  @Override
  public HandlerList getHandlers() { return handlers; }
  public static HandlerList getHandlerList() { return handlers; }
}
