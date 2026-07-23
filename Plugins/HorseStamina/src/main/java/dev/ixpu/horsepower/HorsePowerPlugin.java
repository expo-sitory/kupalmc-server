package dev.ixpu.horsepower;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class HorsePowerPlugin extends JavaPlugin {
  private HorseRaceManager raceManager;
  private StaminaMinigameManager minigameManager;
  private HorseCollisionTask collisionTask;

  @Override
  public void onEnable() {
    getLogger().info("HorsePower enabled!");
    saveDefaultConfig();
    
    raceManager = new HorseRaceManager(this);
    minigameManager = new StaminaMinigameManager(this, raceManager);
    getServer().getPluginManager().registerEvents(new HorseJumpListener(this, raceManager, minigameManager), this);
    getServer().getPluginManager().registerEvents(new HorseRaceListener(raceManager), this);
    
    getCommand("race").setExecutor(new HorseRaceCommand(raceManager));
    getCommand("horsestamina").setExecutor(new HorsePowerReloadCommand(this));
    
    // Start collision detection
    collisionTask = new HorseCollisionTask(this, raceManager);
    collisionTask.start();
    
    Bukkit.getScheduler().runTaskTimer(this, () -> {
      raceManager.updateAllActionbars();
    }, 0, 1);
  }

  @Override
  public void onDisable() {
    if (collisionTask != null) {
      collisionTask.stop();
    }
    getLogger().info("HorsePower disabled!");
  }

  public FileConfiguration getPluginConfig() {
    return getConfig();
  }

  public HorseCollisionTask getCollisionTask() {
    return collisionTask;
  }
}