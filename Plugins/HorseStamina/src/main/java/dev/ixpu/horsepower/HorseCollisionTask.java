package dev.ixpu.horsepower;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

 // ==================== HORSE COLLISSIONS ====================

public class HorseCollisionTask {
  private final HorsePowerPlugin plugin;
  private final HorseRaceManager raceManager;
  private BukkitTask task;

  public HorseCollisionTask(HorsePowerPlugin plugin, HorseRaceManager raceManager) {
    this.plugin = plugin;
    this.raceManager = raceManager;
  }

  public void start() {
    task = Bukkit.getScheduler().runTaskTimer(plugin, this::checkCollisions, 0L, 2L); // Check every 2 ticks
  }

  public void stop() {
    if (task != null) {
      task.cancel();
    }
  }

  private void checkCollisions() {
    // Get all horses in active races
    java.util.List<Horse> racingHorses = new java.util.ArrayList<>();
    for (HorseRaceManager.RaceData data : raceManager.activeRaces.values()) {
      if (data.horse != null && data.horse.isValid()) {
        racingHorses.add(data.horse);
      }
    }

    // Check each horse against all other horses
    for (int i = 0; i < racingHorses.size(); i++) {
      Horse horse1 = racingHorses.get(i);
      
      for (int j = i + 1; j < racingHorses.size(); j++) {
        Horse horse2 = racingHorses.get(j);
        
        // Check if horses are close enough to collide
        double distance = horse1.getLocation().distance(horse2.getLocation());
        double collisionRange = plugin.getConfig().getDouble("race.collision.range", 1.5);
        
        if (distance <= collisionRange) {
          handleCollision(horse1, horse2);
        }
      }
    }
  }

  private void handleCollision(Horse horse1, Horse horse2) {
    // Check cooldowns
    if (!raceManager.canCollide(horse1) || !raceManager.canCollide(horse2)) {
      return;
    }

    // Get owners
    Player player1 = raceManager.getHorseOwner(horse1);
    Player player2 = raceManager.getHorseOwner(horse2);
    
    if (player1 == null || player2 == null) {
      return;
    }

    // Apply damage
    double damage = raceManager.getCollisionDamage();
    applyDamageWithMinHealth(horse1, damage);
    applyDamageWithMinHealth(horse2, damage);

    // Apply knockback
    Location loc1 = horse1.getLocation();
    Location loc2 = horse2.getLocation();
    
    // Direction from horse1 to horse2
    Vector knockback1 = loc2.subtract(loc1).toVector().normalize().multiply(-0.5);
    // Direction from horse2 to horse1
    Vector knockback2 = loc1.subtract(loc2).toVector().normalize().multiply(-0.5);
    
    horse1.setVelocity(knockback1);
    horse2.setVelocity(knockback2);

    // Set cooldowns
    raceManager.setCollisionCooldown(horse1);
    raceManager.setCollisionCooldown(horse2);

    // Feedback
    player1.sendMessage("§c§lᴄᴏʟʟɪꜱɪᴏɴ!");
    player2.sendMessage("§c§lᴄᴏʟʟɪꜱɪᴏɴ!");
  }

  private void applyDamageWithMinHealth(Horse horse, double damage) {
    double newHealth = horse.getHealth() - damage;
    if (newHealth < 0.5) {
      horse.setHealth(0.5);
    } else {
      horse.setHealth(newHealth);
    }
  }
}