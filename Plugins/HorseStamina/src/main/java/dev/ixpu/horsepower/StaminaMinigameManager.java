package dev.ixpu.horsepower;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class StaminaMinigameManager {
  private final HorsePowerPlugin plugin;
  private final HorseRaceManager raceManager;
  private final Random random = new Random();
  private final Map<UUID, MinigameState> activeMinigames = new HashMap<>();
  private final Map<UUID, Long> lastMinigameTime = new HashMap<>();
  private final Map<UUID, Location> lastMinigameCheckLocation = new HashMap<>();


  public StaminaMinigameManager(HorsePowerPlugin plugin, HorseRaceManager raceManager) {
    this.plugin = plugin;
    this.raceManager = raceManager;
    Bukkit.getScheduler().runTaskTimer(plugin, this::checkMinigameSpawn, 0, 20);
  }

  private void checkMinigameSpawn() {
    for (HorseRaceManager.RaceData data : raceManager.activeRaces.values()) {
      if (data.player == null || !data.player.isOnline()) continue;
      UUID playerId = data.player.getUniqueId();
      if (activeMinigames.containsKey(playerId)) continue;
      
      // CHECK IF HORSE IS MOVING
      Location currentLocation = data.horse.getLocation();
      Location lastLocation = lastMinigameCheckLocation.get(playerId);
      
      if (lastLocation != null) {
        double distanceMoved = currentLocation.distance(lastLocation);
        if (distanceMoved < 10) { 
          continue;
        }
      }
      lastMinigameCheckLocation.put(playerId, currentLocation.clone());
      
      // Check if minigames enabled for this pace level
      boolean game1Enabled = isMinigameEnabledForPace(data.slownessAmplifier, 1);
      boolean game2Enabled = isMinigameEnabledForPace(data.slownessAmplifier, 2);
      if (!game1Enabled && !game2Enabled) continue;
      
      long now = System.currentTimeMillis();
      if (lastMinigameTime.containsKey(playerId)) {
        long minCooldownSeconds = plugin.getConfig().getLong("minigame.cooldown-between", 10);
        long minCooldown = minCooldownSeconds * 1000;
        if (now - lastMinigameTime.get(playerId) < minCooldown) continue;
      }
      
      int gameType = random.nextBoolean() && game1Enabled ? 1 : 2;
      if (!game1Enabled) gameType = 2;
      if (!game2Enabled) gameType = 1;
      spawnMinigame(data.player, gameType, data.slownessAmplifier);
    }
  }

  private void spawnMinigame(Player player, int gameType, int paceLevel) {
    UUID playerId = player.getUniqueId();
    MinigameState state;
    
    if (gameType == 1) {
      int minRequired = plugin.getConfig().getInt("minigame.game1.perfect-jumps-required-min", 2);
      int maxRequired = plugin.getConfig().getInt("minigame.game1.perfect-jumps-required-max", 5);
      int required = minRequired + random.nextInt(maxRequired - minRequired + 1);
      
      int baseReward = plugin.getConfig().getInt("minigame.game1.stamina-reward", 10);
      double reward = getMinigameReward(paceLevel, 1, baseReward);  
      int durationSeconds = plugin.getConfig().getInt("minigame.game1.duration-seconds", 5);
      state = new MinigameState(1, required, (int)reward, durationSeconds);
      player.sendTitle("§e§l" + durationSeconds + "s - " + required + " ᴍᴀx ᴍᴇᴛᴇʀ", "§a+" + (int)reward + " ꜱᴛᴀᴍɪɴᴀ", 0, 60, 10);
      
    } else {
      int timerDuration = plugin.getConfig().getInt("minigame.game2.timer-duration-seconds", 5);
      
      int baseReward = plugin.getConfig().getInt("minigame.game2.stamina-reward", 10);
      double reward = getMinigameReward(paceLevel, 2, baseReward);
      
      state = new MinigameState(2, timerDuration, (int)reward, timerDuration);
    }
    
    activeMinigames.put(playerId, state);
    lastMinigameTime.put(playerId, System.currentTimeMillis());
    
    if (state.gameType == 2) {
      startTimerDisplay(player, state.jumpTargetOrTimer);
      player.sendTitle("§e§lʜɪᴛ ᴏɴ ᴢᴇʀᴏ", "§6" + state.jumpTargetOrTimer + " ꜱᴇᴄᴏɴᴅꜱ", 0, state.jumpTargetOrTimer * 20 + 20, 10);
    }
    
    long duration = gameType == 1 ? plugin.getConfig().getInt("minigame.game1.duration-seconds", 5) * 20 : ((long)plugin.getConfig().getInt("minigame.game2.timer-duration-seconds", 5) * 20);
    Bukkit.getScheduler().runTaskLater(plugin, () -> {
      if (activeMinigames.containsKey(playerId)) {
        MinigameState gameState = activeMinigames.get(playerId);
        
        // Cancel any remaining timer tasks if game 2
        if (gameState.gameType == 2) {
          for (int taskId : gameState.timerTaskIds) {
            Bukkit.getScheduler().cancelTask(taskId);
          }
        }
        
        activeMinigames.remove(playerId);                     
        player.sendTitle("§c§lᴛɪᴍᴇ'ꜱ ᴜᴘ!", "§c" + gameState.durationSeconds + "s - ᴇxᴘɪʀᴇᴅ", 0, 20, 10);
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_IMITATE_CREEPER, 5.0f, 1.0f);
        raceManager.setPaceChangeCooldown(playerId);
      }
    }, duration);
  }

  private void startTimerDisplay(Player player, int durationSeconds) {
    UUID playerId = player.getUniqueId();
    MinigameState state = activeMinigames.get(playerId);
    if (state == null) return;
    
    for (int i = durationSeconds; i > 0; i--) {
      final int countdown = i;
      int taskId = Bukkit.getScheduler().runTaskLater(plugin, () -> {
        player.sendTitle("§e§lʜɪᴛ ᴏɴ ᴢᴇʀᴏ", "§c" + countdown + " ꜱᴇᴄᴏɴᴅꜱ", 0, 22, 0);
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BANJO, 1.0f, 1.0f);
      }, (long)(durationSeconds - countdown) * 20).getTaskId();
      state.timerTaskIds.add(taskId);
    }
  }

  public void recordPerfectJump(Player player) {
    UUID playerId = player.getUniqueId();
    MinigameState state = activeMinigames.get(playerId);
    if (state == null) return;

    if (state.gameType == 1) {
      state.perfectJumpsHit++;
      if (state.perfectJumpsHit >= state.jumpTargetOrTimer) {
        player.sendTitle("§a§l✓ ꜱᴜᴄᴄᴇꜱꜱ!", "§a+" + state.reward + " ꜱᴛᴀᴍɪɴᴀ", 0, 20, 10);
        raceManager.addStamina(player, state.reward);
        activeMinigames.remove(playerId);

        triggerMinigameDash(player);
        raceManager.setPaceChangeCooldown(playerId);

      } else {
        player.sendTitle("§e§l" + state.durationSeconds + "s - " + (state.jumpTargetOrTimer - state.perfectJumpsHit) + " ᴍᴀx ᴍᴇᴛᴇʀ", "§a+" + state.reward + " ꜱᴛᴀᴍɪɴᴀ", 0, 40, 10);
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 5.0f, 1.0f);
      }
    } else if (state.gameType == 2) {
      long elapsed = System.currentTimeMillis() - state.startTime;
      double toleranceSeconds = plugin.getConfig().getDouble("minigame.game2.hit-tolerance-seconds", 0.5);
      long tolerance = (long)(toleranceSeconds * 1000);
      long targetTime = (long)plugin.getConfig().getInt("minigame.game2.timer-duration-seconds", 5) * 1000L;
      
      // Cancel timer tasks in ALL cases
      for (int taskId : state.timerTaskIds) {
        Bukkit.getScheduler().cancelTask(taskId);
      }
      
      if (Math.abs(elapsed - targetTime) <= tolerance) {
        player.sendTitle("§a§l✓ ᴘᴇʀꜰᴇᴄᴛ ᴛɪᴍɪɴɢ!", "§a+" + state.reward + " ꜱᴛᴀᴍɪɴᴀ", 0, 20, 10);
        raceManager.addStamina(player, state.reward);
        activeMinigames.remove(playerId);
        
        triggerMinigameDash(player);
        raceManager.setPaceChangeCooldown(playerId);

      } else {
        int penalty = plugin.getConfig().getInt("minigame.game2.stamina-penalty", 10);
        player.sendTitle("§c§lᴡʀᴏɴɢ ᴛɪᴍɪɴɢ! - ꜱᴋɪʟʟ ɪꜱꜱᴜᴇ", "§c-" + penalty + " ꜱᴛᴀᴍɪɴᴀ", 0, 20, 10);
        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
        raceManager.addStamina(player, -penalty);
        activeMinigames.remove(playerId);
        raceManager.setPaceChangeCooldown(playerId); 
      }
    }
  }

  // ==================== MINIGAME SUCCESS DASH ====================

  private void triggerMinigameDash(Player player) {
    UUID playerId = player.getUniqueId();
    HorseRaceManager.RaceData raceData = raceManager.activeRaces.get(playerId);
    if (raceData == null) return;
    
    // Get dash configuration
    double dashDistance = plugin.getConfig().getDouble("minigame.minigame-dash.distance-blocks", 20.0);
    double dashSpeedMultiplier = plugin.getConfig().getDouble("minigame.minigame-dash.speed-multiplier", 2.0);
    
    // Execute dash
    raceManager.executeDash(player, raceData, dashDistance, dashSpeedMultiplier);
    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
  }

  public void recordImperfectJump(Player player) {
    UUID playerId = player.getUniqueId();
    MinigameState state = activeMinigames.get(playerId);
    if (state == null) return;

    if (state.gameType == 1) {
      int penalty = plugin.getConfig().getInt("minigame.game1.stamina-penalty", 5);
      player.sendTitle("", "", 0, 1, 0);
      player.sendTitle("§c§lᴍᴇᴛᴇʀ ᴍɪꜱᴛᴀᴋᴇ! - ꜱᴋɪʟʟ ɪꜱꜱᴜᴇ", "§c-" + penalty + " ꜱᴛᴀᴍɪɴᴀ", 0, 20, 10);
      player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
      raceManager.addStamina(player, -penalty);
      activeMinigames.remove(playerId);
      raceManager.setPaceChangeCooldown(playerId); 
    } else if (state.gameType == 2) {
      int penalty = plugin.getConfig().getInt("minigame.game2.stamina-penalty", 10);
      for (int taskId : state.timerTaskIds) {
        Bukkit.getScheduler().cancelTask(taskId);
      }
      player.sendTitle("", "", 0, 1, 0);
      player.sendTitle("§c§lᴡʀᴏɴɢ ᴛɪᴍɪɴɢ - ᴍɪꜱᴛᴀᴋᴇ!", "§c-" + penalty + " ꜱᴛᴀᴍɪɴᴀ", 0, 20, 10);
      player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);      raceManager.addStamina(player, -penalty);
      activeMinigames.remove(playerId);
      raceManager.setPaceChangeCooldown(playerId);
    }
  }

  public boolean isInMinigame(Player player) {
    return activeMinigames.containsKey(player.getUniqueId());
  }

  public boolean isGame2(Player player) {
    MinigameState state = activeMinigames.get(player.getUniqueId());
    return state != null && state.gameType == 2;
  }

  // Check if a minigame is enabled for a specific pace level
  private boolean isMinigameEnabledForPace(int paceLevel, int minigameNumber) {
    String path = "race.pace-minigame.pace-" + paceLevel + ".minigame-" + minigameNumber + ".enabled";
    return plugin.getConfig().getBoolean(path, false);
  }

  private double getMinigameReward(int paceLevel, int minigameNumber, double defaultReward) {
    String path = "race.pace-minigame.pace-" + paceLevel + ".minigame-" + minigameNumber + ".stamina-reward";
    
    try {
      String value = plugin.getConfig().getString(path, "default");
      
      if (value.equalsIgnoreCase("default")) {
        return defaultReward;
      }
      
      
      if (value.contains("-")) {
        String[] parts = value.split("-");
        int min = Integer.parseInt(parts[0].trim());
        int max = Integer.parseInt(parts[1].trim());
        return min + random.nextInt(max - min + 1);
      }
      
      // Single value
      return Double.parseDouble(value);
    } catch (Exception e) {
      return defaultReward;
    }
  }

  public static class MinigameState {
    public int gameType;
    public int jumpTargetOrTimer;
    public int reward;
    public int durationSeconds;
    public int perfectJumpsHit = 0;
    public long startTime = System.currentTimeMillis();
    public java.util.List<Integer> timerTaskIds = new java.util.ArrayList<>();

    public MinigameState(int gameType, int targetOrTimer, int reward, int durationSeconds) {
      this.gameType = gameType;
      this.jumpTargetOrTimer = targetOrTimer;
      this.reward = reward;
      this.durationSeconds = durationSeconds;
    }
  }
}