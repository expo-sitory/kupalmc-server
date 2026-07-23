package dev.ixpu.horsepower;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class HorseRaceManager {
  private final HorsePowerPlugin plugin;
  public final Map<UUID, RaceData> activeRaces = new HashMap<>();
  private final Map<UUID, Long> paceChangeCooldown = new HashMap<>();

  public HorseRaceManager(HorsePowerPlugin plugin) {
    this.plugin = plugin;
  }

  public boolean startRace(Player player, Horse horse) {
    UUID playerId = player.getUniqueId();
    if (activeRaces.containsKey(playerId)) {
      player.sendMessage("§cAlready in race mode!");
      return false;
    }

    int slownessAmplifier = plugin.getConfig().getInt("race.initial-slowness", 5);
    RaceData data = new RaceData(player, horse, slownessAmplifier, plugin);
    activeRaces.put(playerId, data);

    player.sendMessage("§a✓ Race Mode Activated!");
    applySlowness(player, slownessAmplifier);
    horse.setGlowing(true);
    return true;
  }

  public void endRace(Player player) {
    UUID playerId = player.getUniqueId();
    if (activeRaces.containsKey(playerId)) {
      RaceData data = activeRaces.get(playerId);
      data.horse.removePotionEffect(PotionEffectType.SLOWNESS);
      data.horse.setGlowing(false);
   
      data.staminaBar.removeAll();
      // Clean up collision task tracking
      plugin.getCollisionTask().cleanupHorse(data.horse);
      paceChangeCooldown.remove(playerId);
      
      activeRaces.remove(playerId);
      player.sendMessage("§d§lᴋᴜᴘᴀʟᴍᴄ §r§7- §fʜᴏʀꜱᴇ §7| §7Race Mode Deactivated");
    }
  }

  // ==================== STRATEGY PACE LEVELS (SPEED UP) ====================

  public void recordPerfectJump(Player player) {
      UUID playerId = player.getUniqueId();
      
      if (isPaceChangeOnCooldown(playerId)) {
        return;
      }
      
      RaceData data = activeRaces.get(playerId);
      if (data == null) return;

      if (data.slownessAmplifier >= 0) {
        int oldAmplifier = data.slownessAmplifier;
        data.slownessAmplifier--;
        applySlowness(player, data.slownessAmplifier);
        
        switch(data.slownessAmplifier) {
          case 5 -> sendActionBar(player, "§eᴘᴀᴄᴇ ᴄʜᴀɴɢᴇ: ᴡᴀʟᴋɪɴɢ");
          case 4 -> sendActionBar(player, "§eᴘᴀᴄᴇ ᴄʜᴀɴɢᴇ: ᴇɴᴅ ᴄʟᴏꜱᴇʀ");
          case 3 -> sendActionBar(player, "§eᴘᴀᴄᴇ ᴄʜᴀɴɢᴇ: ʟᴀᴛᴇ ꜱᴜʀɢᴇʀ");
          case 2 -> sendActionBar(player, "§eᴘᴀᴄᴇ ᴄʜᴀɴɢᴇ: ᴘᴀᴄᴇ ᴄʜᴀꜱᴇʀ");
          case 1 -> sendActionBar(player, "§eᴘᴀᴄᴇ ᴄʜᴀɴɢᴇ: ꜰʀᴏɴᴛ ʀᴜɴɴᴇʀ");
          case 0 -> sendActionBar(player, "§aʟᴀꜱᴛ ꜱᴘᴜʀᴛ");
        }
      }
    }

  // ==================== STRATEGY PACE LEVELS (SLOW DOWN) ====================

public void recordImPerfectJump(Player player) {
    UUID playerId = player.getUniqueId();
    
    if (isPaceChangeOnCooldown(playerId)) {
      return;
    }
    
    RaceData data = activeRaces.get(playerId);
    if (data == null) return;

    if (data.slownessAmplifier >= 1 && data.slownessAmplifier < 5) {
      int oldAmplifier = data.slownessAmplifier;
      data.slownessAmplifier++; 
      applySlowness(player, data.slownessAmplifier);
        
      switch(oldAmplifier) {
        case 1 -> sendActionBar(player, "§eᴘᴀᴄᴇ ᴄʜᴀɴɢᴇ: ᴘᴀᴄᴇ ᴄʜᴀꜱᴇʀ");
        case 2 -> sendActionBar(player, "§eᴘᴀᴄᴇ ᴄʜᴀɴɢᴇ: ʟᴀᴛᴇ ꜱᴜʀɢᴇʀ");
        case 3 -> sendActionBar(player, "§eᴘᴀᴄᴇ ᴄʜᴀɴɢᴇ: ᴇɴᴅ ᴄʟᴏꜱᴇʀ");
        case 4 -> sendActionBar(player, "§eᴘᴀᴄᴇ ᴄʜᴀɴɢᴇ: ᴡᴀʟᴋɪɴɢ");
      }
    }
  }

  // ==================== PACE CHANGE COOLDOWN ====================

  public boolean isPaceChangeOnCooldown(UUID playerId) {
    Long cooldownTime = paceChangeCooldown.get(playerId);
    if (cooldownTime == null) return false;
    return System.currentTimeMillis() < cooldownTime;
  }

  public void setPaceChangeCooldown(UUID playerId) {
    paceChangeCooldown.put(playerId, System.currentTimeMillis() + 2000);
  }

  public void addStamina(Player player, int amount) {
    UUID playerId = player.getUniqueId();
    RaceData data = activeRaces.get(playerId);
    if (data != null) {
      data.stamina = Math.min(data.maxStamina, data.stamina + amount);
    }
  }

  public void executeDash(Player player, RaceData data, double distanceBlocks, double speedMultiplier) {
    if (data == null || data.horse == null) return;
    
    // Get horse's forward direction
    org.bukkit.Location loc = data.horse.getLocation();
    org.bukkit.util.Vector direction = loc.getDirection().normalize();
    
    // Calculate velocity: distance / duration, with speed multiplier
    double duration = plugin.getConfig().getDouble("minigame.minigame-dash.duration-ticks", 20.0);
    double baseVelocity = (distanceBlocks / (duration / 20.0)) * (speedMultiplier / 10.0);
    
    org.bukkit.util.Vector dashVelocity = direction.multiply(baseVelocity);
    
    // Apply dash velocity for multiple ticks to maintain momentum
    int dashTicks = (int) duration;
    for (int i = 0; i < dashTicks; i++) {
      final int tickIndex = i;
      Bukkit.getScheduler().runTaskLater(plugin, () -> {
        if (data.horse != null && data.horse.isValid()) {
          data.horse.setVelocity(dashVelocity);
          
          // Visual effect: spawn particles along the path
          org.bukkit.Location currentLoc = data.horse.getLocation();
          data.horse.getWorld().spawnParticle(
            org.bukkit.Particle.CLOUD, 
            currentLoc, 
            3, 
            0.5, 0.5, 0.5, 
            0.1
          );
        }
      }, i);
    }
    
    // Apply bonus stamina from dash success
    int bonusStamina = plugin.getConfig().getInt("minigame.minigame-dash.bonus-stamina", 20);
    addStamina(player, bonusStamina);
  }

  public void updateAllActionbars() {
    for (RaceData data : activeRaces.values()) {
      Player player = data.player;
      if (player != null && player.isOnline()) {
        
        String paceName = getPaceNameByLevel(data.slownessAmplifier);
        data.staminaBar.setTitle("§eᴘᴀᴄᴇ: §f" + paceName + " §7| §eꜱᴛᴀᴍɪɴᴀ: §f" + String.format("%.0f/%.0f", data.stamina, data.maxStamina));
        displayStaminaBar(player, data);
        
        if (data.isHPBasedMode) {
          double currentHealth = data.horse.getHealth();
          if (currentHealth < data.previousHorseHealth) {
            double damageAmount = data.previousHorseHealth - currentHealth;
            double multiplier = plugin.getConfig().getDouble("race.stamina-hp-multiplier", 5);
            double maxStaminaLoss = damageAmount * multiplier;
            data.maxStamina = Math.max(0, data.maxStamina - maxStaminaLoss);
            // Current stamina is untouched by damage, but can't exceed the new max
            data.stamina = Math.min(data.stamina, data.maxStamina);
          }
          data.previousHorseHealth = currentHealth;
        }
        
        double emergencyThreshold = plugin.getConfig().getDouble("race.emergency-stamina-threshold", 20);
        double recoveryThreshold = plugin.getConfig().getDouble("race.recovery-stamina-threshold", 50);
        
        // Apply slowness based on pace level
        int paceSlowness = getSlownessAmplifierByPace(data.slownessAmplifier);
        
        // Check if entering emergency mode
        if (data.stamina < emergencyThreshold && !data.inEmergencyMode) {
          data.inEmergencyMode = true;
          int emergencyAmplifier = plugin.getConfig().getInt("race.emergency-slowness-amplifier", 4);
          applySlowness(player, Math.max(paceSlowness, emergencyAmplifier));
        }
        
        // Check if exiting emergency mode
        if (data.stamina >= recoveryThreshold && data.inEmergencyMode) {
          data.inEmergencyMode = false;
          applySlowness(player, paceSlowness);
        }
        
        if (!data.inEmergencyMode) {
          applySlowness(player, paceSlowness);
        }
        
        // Apply stamina changes
        if (data.inEmergencyMode) {
          // Emergency mode: deplete stamina, then add regen based on current stamina range
          checkBlocksDistance(data);
          double regen = getRegenByStamina(data.stamina);
          data.stamina = Math.min(100, data.stamina + regen / 20.0);
        } else {
          // Normal mode: only deplete, no regen
          checkBlocksDistance(data);
        }
      }
    }
  }

  private void sendActionBar(Player player, String message) {
    player.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR, 
      net.md_5.bungee.chat.ComponentSerializer.parse("{\"text\":\"" + message + "\"}")[0]);
  }

  private void checkBlocksDistance(RaceData data) {
    Location current = data.horse.getLocation();
    if (data.lastLocation == null) {
      data.lastLocation = current;
      return;
    }
    double distance = current.distance(data.lastLocation);
    double baseDepletion = getDepletionByPaceLevel(data.slownessAmplifier);
    data.stamina = Math.max(0, data.stamina - (distance * baseDepletion));
    data.lastLocation = current;
  }

  private double getDepletionByPaceLevel(int paceLevel) {
      return switch (paceLevel) {
          case 5 -> plugin.getConfig().getDouble("race.stamina-depletion.pace-5", 0.0);
          case 4 -> plugin.getConfig().getDouble("race.stamina-depletion.pace-4", 0.0);
          case 3 -> plugin.getConfig().getDouble("race.stamina-depletion.pace-3", 0.0);
          case 2 -> plugin.getConfig().getDouble("race.stamina-depletion.pace-2", 0.0);
          case 1 -> plugin.getConfig().getDouble("race.stamina-depletion.pace-1", 0.0);
          case 0 -> plugin.getConfig().getDouble("race.stamina-depletion.pace-0", 0.0);
          default -> plugin.getConfig().getDouble("race.stamina-depletion.pace-0", 0.0);
      }; 
  }

  private double getRegenByStamina(double stamina) {
    if (stamina <= 10) return plugin.getConfig().getDouble("race.stamina-ranges.0-10.regen", 0.0);
    else if (stamina <= 20) return plugin.getConfig().getDouble("race.stamina-ranges.10-20.regen", 0.0);
    else if (stamina <= 30) return plugin.getConfig().getDouble("race.stamina-ranges.20-30.regen", 0.0);
    else if (stamina <= 40) return plugin.getConfig().getDouble("race.stamina-ranges.30-40.regen", 0.0);
    else if (stamina <= 50) return plugin.getConfig().getDouble("race.stamina-ranges.40-50.regen", 0.0);
    else return plugin.getConfig().getDouble("race.stamina-ranges.50-500.regen", 0.0);
  }

  private int getSlownessAmplifierByPace(int paceLevel) {
      return switch (paceLevel) {
          case 5 -> plugin.getConfig().getInt("race.pace-slowness.pace-5", 5);
          case 4 -> plugin.getConfig().getInt("race.pace-slowness.pace-4", 4);
          case 3 -> plugin.getConfig().getInt("race.pace-slowness.pace-3", 3);
          case 2 -> plugin.getConfig().getInt("race.pace-slowness.pace-2", 2);
          case 1 -> plugin.getConfig().getInt("race.pace-slowness.pace-1", 1);
          case 0 -> plugin.getConfig().getInt("race.pace-slowness.pace-0", 0);
          default -> plugin.getConfig().getInt("race.pace-slowness.pace-0", 0);
      };
  }

  private int getSlownessAmplifierByStamina(double stamina) {
    if (stamina <= 10) return plugin.getConfig().getInt("race.stamina-ranges.0-10.slowness-amplifier", 0);
    else if (stamina <= 20) return plugin.getConfig().getInt("race.stamina-ranges.10-20.slowness-amplifier", 0);
    else if (stamina <= 30) return plugin.getConfig().getInt("race.stamina-ranges.20-30.slowness-amplifier", 0);
    else if (stamina <= 40) return plugin.getConfig().getInt("race.stamina-ranges.30-40.slowness-amplifier", 0);
    else return plugin.getConfig().getInt("race.stamina-ranges.40-50.slowness-amplifier", 0);
  }

  // ==================== PACE LEVEL NAME HELPER ====================
  
  private String getPaceNameByLevel(int paceLevel) {
    return switch(paceLevel) {
        case 5 -> "ᴡᴀʟᴋɪɴɢ";
        case 4 -> "ᴇɴᴅ ᴄʟᴏꜱᴇʀ";
        case 3 -> "ʟᴀᴛᴇ ꜱᴜʀɢᴇʀ";
        case 2 -> "ᴘᴀᴄᴇ ᴄʜᴀꜱᴇʀ";
        case 1 -> "ꜰʀᴏɴᴛ ʀᴜɴɴᴇʀ";
        case 0 -> "ʟᴀꜱᴛ ꜱᴘᴜʀᴛ";
        default -> "ʟᴀꜱᴛ ꜱᴘᴜʀᴛ";
    };
  }

  private void displayStaminaBar(Player player, RaceData data) {
    // Calculate progress bar relative to (possibly reduced) max stamina
    double progress = data.maxStamina > 0 ? Math.max(0, Math.min(1, data.stamina / data.maxStamina)) : 0;
    data.staminaBar.setProgress(progress);
    
    // Change color based on stamina level
    if (data.stamina <= 20) {
      data.staminaBar.setColor(BarColor.RED);
    } else if (data.stamina <= 40) {
      data.staminaBar.setColor(BarColor.YELLOW);
    } else {
      data.staminaBar.setColor(BarColor.GREEN);
    }
  }

  private void applySlowness(Player player, int amplifier) {
    RaceData data = activeRaces.get(player.getUniqueId());
    if (data == null) return;
    
    data.horse.removePotionEffect(PotionEffectType.SLOWNESS);
    if (amplifier > 0) {
      data.horse.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, Integer.MAX_VALUE, amplifier - 1, false, false));
    }
  }

  // ==================== COLLISION MECHANICS ====================

  // Check if a horse is currently in race mode
  public boolean isHorseInRace(Horse horse) {
    for (RaceData data : activeRaces.values()) {
      if (data.horse.equals(horse)) {
        return true;
      }
    }
    return false;
  }

  // Get the player who owns a horse in race mode
  public Player getHorseOwner(Horse horse) {
    for (RaceData data : activeRaces.values()) {
      if (data.horse.equals(horse)) {
        return data.player;
      }
    }
    return null;
  }

  // Get collision damage from config
  public double getCollisionDamage() {
    return plugin.getConfig().getDouble("race.collision.damage", 2.0);
  }

  public static class RaceData {
    public Player player;
    public Horse horse;
    public int slownessAmplifier;
    public double stamina;
    public double maxStamina;
    public double previousHorseHealth;
    public Location lastLocation;
    public boolean inEmergencyMode = false;
    public BossBar staminaBar;
    public boolean isHPBasedMode;

    public RaceData(Player player, Horse horse, int slownessAmplifier, HorsePowerPlugin plugin) {
      this.player = player;
      this.horse = horse;
      this.slownessAmplifier = slownessAmplifier;
      this.previousHorseHealth = horse.getHealth();
      
      String mode = plugin.getConfig().getString("race.stamina-mode", "fixed");
      this.isHPBasedMode = mode.equalsIgnoreCase("hp-based");
      
      if (this.isHPBasedMode) {
        double multiplier = plugin.getConfig().getDouble("race.stamina-hp-multiplier", 5);
        this.maxStamina = horse.getMaxHealth() * multiplier;
        this.stamina = this.maxStamina;
      } else {
        this.maxStamina = 100;
        this.stamina = 100;
      }
      
      // Create bossbar for this race
      this.staminaBar = Bukkit.createBossBar(
        "§eStamina: §f" + String.format("%.0f/%.0f", this.stamina, this.maxStamina),
        BarColor.YELLOW,
        BarStyle.SEGMENTED_20
      );
      this.staminaBar.addPlayer(player);
    }
  }
}
