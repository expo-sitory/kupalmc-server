package dev.ixpu.horsepower;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

public class HorseCollisionTask {
  private final HorsePowerPlugin plugin;
  private final HorseRaceManager raceManager;
  private BukkitTask task;
  private final Map<UUID, Long> lastStaminaDrainTime = new HashMap<>();
  private final Map<UUID, Long> lastCollisionTime = new HashMap<>();
  private final Map<UUID, Long> lastDamageTakenTime = new HashMap<>();
  private final Map<UUID, org.bukkit.Location> lastHorsePosition = new HashMap<>();
  private Set<String> damageBlockTypes;
  private static final boolean DEBUG = true;

  public HorseCollisionTask(HorsePowerPlugin plugin, HorseRaceManager raceManager) {
    this.plugin = plugin;
    this.raceManager = raceManager;
    loadDamageBlockTypes();
  }

  public void start() {
    task = Bukkit.getScheduler().runTaskTimer(plugin, this::checkCollisions, 0L, 1L);
    if (DEBUG) {
      plugin.getLogger().info(() -> "Block Collision Task started. Configured damage blocks: " + damageBlockTypes);
    }
  }

  public void stop() {
    if (task != null) {
      task.cancel();
    }
  }

  public void cleanupHorse(Horse horse) {
    UUID horseId = horse.getUniqueId();
    lastStaminaDrainTime.remove(horseId);
    lastCollisionTime.remove(horseId);
    lastDamageTakenTime.remove(horseId);
    lastHorsePosition.remove(horseId);
  }

  private void loadDamageBlockTypes() {
    damageBlockTypes = new HashSet<>();
    java.util.List<String> blockList = plugin.getConfig().getStringList("race.block-contact.damage-blocks");
    damageBlockTypes.addAll(blockList);
    if (DEBUG) {
      plugin.getLogger().info(() -> "Loaded " + damageBlockTypes.size() + " damage block types: " + damageBlockTypes);
    }
  }

  public void reloadDamageBlocks() {
    loadDamageBlockTypes();
  }

  private boolean canCollide(Horse horse) {
    UUID uuid = horse.getUniqueId();
    long lastCollision = lastCollisionTime.getOrDefault(uuid, 0L);
    long collisionCooldownMs = plugin.getConfig().getLong("race.collision.cooldown-ticks", 30) * 50L;
    return System.currentTimeMillis() - lastCollision >= collisionCooldownMs;
  }

  private void setCollisionCooldown(Horse horse) {
    lastCollisionTime.put(horse.getUniqueId(), System.currentTimeMillis());
  }

  private void checkCollisions() {
    // Get all horses in active races
    java.util.List<Horse> racingHorses = new java.util.ArrayList<>();
    for (HorseRaceManager.RaceData data : raceManager.activeRaces.values()) {
      if (data.horse != null && data.horse.isValid()) {
        racingHorses.add(data.horse);
      }
    }

    // Check each horse for block contact and collisions
    for (int i = 0; i < racingHorses.size(); i++) {
      Horse horse1 = racingHorses.get(i);
      
      // Store last position for this horse
      lastHorsePosition.put(horse1.getUniqueId(), horse1.getLocation().clone());
      
      // Check block contact (all sides + diagonals)
      checkHorseBlockContact(horse1);
      
      // Check horse-to-horse collisions
      for (int j = i + 1; j < racingHorses.size(); j++) {
        Horse horse2 = racingHorses.get(j);
        
        // Check if horses are close enough to collide
        double distance = horse1.getLocation().distance(horse2.getLocation());
        double collisionRange = plugin.getConfig().getDouble("race.collision.range", 2.5);
        
        if (distance <= collisionRange) {
          handleHorseCollision(horse1, horse2);
        }
      }
    }
  }

  // ==================== FENCE CONTACT HANDLER ====================
  
  private void checkHorseBlockContact(Horse horse) {
    UUID horseId = horse.getUniqueId();
    Player owner = raceManager.getHorseOwner(horse);
    
    if (owner == null) {
      return;
    }
    
    org.bukkit.Location horseLoc = horse.getLocation();
    
    // ===== CARDINAL DIRECTIONS (6) =====
    Block blockAbove = horseLoc.clone().add(0, 1, 0).getBlock();
    Block blockBelow = horseLoc.clone().add(0, -1, 0).getBlock();
    Block blockNorth = horseLoc.clone().add(0, 0, -1).getBlock();
    Block blockSouth = horseLoc.clone().add(0, 0, 1).getBlock();
    Block blockEast = horseLoc.clone().add(1, 0, 0).getBlock();
    Block blockWest = horseLoc.clone().add(-1, 0, 0).getBlock();
    
    // ===== HORIZONTAL DIAGONALS (4) =====
    // Corners at same level as horse
    Block blockNorthEast = horseLoc.clone().add(1, 0, -1).getBlock();
    Block blockNorthWest = horseLoc.clone().add(-1, 0, -1).getBlock();
    Block blockSouthEast = horseLoc.clone().add(1, 0, 1).getBlock();
    Block blockSouthWest = horseLoc.clone().add(-1, 0, 1).getBlock();
    
    // ===== VERTICAL EDGES (4) =====
    // Top edges (useful for hanging obstacles)
    Block blockTopNorth = horseLoc.clone().add(0, 1, -1).getBlock();
    Block blockTopSouth = horseLoc.clone().add(0, 1, 1).getBlock();
    Block blockTopEast = horseLoc.clone().add(1, 1, 0).getBlock();
    Block blockTopWest = horseLoc.clone().add(-1, 1, 0).getBlock();
    
    // Check all blocks
    if (isBlockDamageType(blockAbove) || 
        isBlockDamageType(blockBelow) || 
        isBlockDamageType(blockNorth) || 
        isBlockDamageType(blockSouth) || 
        isBlockDamageType(blockEast) || 
        isBlockDamageType(blockWest) ||
        // Diagonals
        isBlockDamageType(blockNorthEast) ||
        isBlockDamageType(blockNorthWest) ||
        isBlockDamageType(blockSouthEast) ||
        isBlockDamageType(blockSouthWest) ||
        // Vertical edges
        isBlockDamageType(blockTopNorth) ||
        isBlockDamageType(blockTopSouth) ||
        isBlockDamageType(blockTopEast) ||
        isBlockDamageType(blockTopWest)) {
      
      if (DEBUG) {
        String blockName = getContactBlockName(blockAbove, blockBelow, blockNorth, blockSouth,
                                                blockEast, blockWest, blockNorthEast, blockNorthWest,
                                                blockSouthEast, blockSouthWest, blockTopNorth,
                                                blockTopSouth, blockTopEast, blockTopWest);
        plugin.getLogger().info(() -> "Horse contacting damage block: " + blockName);
      }
      
      applyBlockContactDamage(owner, horse);
    }
  }

  private String getContactBlockName(Block... blocks) {
    String[] directions = {
      "top", "bottom", "north", "south", "east", "west",
      "northeast", "northwest", "southeast", "southwest",
      "top-north", "top-south", "top-east", "top-west"
    };
    
    for (int i = 0; i < blocks.length; i++) {
      if (isBlockDamageType(blocks[i])) {
        return blocks[i].getType().name() + " (" + directions[i] + ")";
      }
    }
    return "unknown";
  }

  private boolean isBlockDamageType(Block block) {
    if (block == null) return false;
    String blockType = block.getType().name();
    return damageBlockTypes.contains(blockType);
  }

  private void sendActionBar(Player player, String message) {
    player.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR, 
      net.md_5.bungee.chat.ComponentSerializer.parse("{\"text\":\"" + message + "\"}")[0]);
  }

  private void applyBlockContactDamage(Player player, Horse horse) {
    UUID horseId = horse.getUniqueId();
    long currentTime = System.currentTimeMillis();
    long lastDamage = lastDamageTakenTime.getOrDefault(horseId, 0L);
    
    // Cooldown to prevent spamming damage (in milliseconds)
    long cooldownMs = plugin.getConfig().getLong("race.block-contact.cooldown-ms", 500);
    if (currentTime - lastDamage < cooldownMs) {
      return; // Still in cooldown, skip damage
    }
    
    // Get damage amount
    double damage = plugin.getConfig().getDouble("race.block-contact.damage", 2.0);
    
    // Apply damage to horse
    double newHealth = horse.getHealth() - damage;
    if (newHealth < 0.5) {
      horse.setHealth(0.5);
    } else {
      horse.setHealth(newHealth);
    }
    
    // Apply stamina drain
    HorseRaceManager.RaceData data = raceManager.activeRaces.get(player.getUniqueId());
    if (data != null) {
      int staminaDrain = plugin.getConfig().getInt("race.block-contact.stamina-drain", 10);
      data.stamina = Math.max(0, data.stamina - staminaDrain);
    }
    
    // Update cooldown AFTER damage is applied
    lastDamageTakenTime.put(horseId, currentTime);
    
    // Feedback
    sendActionBar(player, "§c§l⚠ ꜰᴇɴᴄᴇ ᴄᴏɴᴛᴀᴄᴛ ⚠ §r-" + String.format("%.1f", damage) + " ʜᴘ");
    
  }

  // ==================== HORSE TO HORSE COLLISION HANDLER ====================

  private void handleHorseCollision(Horse horse1, Horse horse2) {
    // Check cooldowns
    if (!canCollide(horse1) || !canCollide(horse2)) {
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

    // Get locations
    org.bukkit.Location loc1 = horse1.getLocation();
    org.bukkit.Location loc2 = horse2.getLocation();
    
    // Direction vectors for separation
    Vector direction1to2 = loc2.toVector().subtract(loc1.toVector()).normalize();
    Vector direction2to1 = loc1.toVector().subtract(loc2.toVector()).normalize();
    
    // Collision parameters
    double separationDistance = plugin.getConfig().getDouble("race.collision.separation-distance", 2.0);
    double knockbackForce = plugin.getConfig().getDouble("race.collision.knockback-force", 1.5);
    
    // Stop both horses completely
    horse1.setVelocity(new Vector(0, 0, 0));
    horse2.setVelocity(new Vector(0, 0, 0));
    
    // Apply knockback force
    Vector knockback1 = direction1to2.multiply(-knockbackForce);
    Vector knockback2 = direction2to1.multiply(-knockbackForce);
    
    horse1.setVelocity(knockback1);
    horse2.setVelocity(knockback2);

    // Force separation if too close
    double currentDistance = loc1.distance(loc2);
    if (currentDistance < 1.5) {
      org.bukkit.Location newLoc1 = loc1.clone().add(direction1to2.multiply(-separationDistance));
      org.bukkit.Location newLoc2 = loc2.clone().add(direction2to1.multiply(-separationDistance));
      
      
      horse1.teleport(newLoc1);
      horse2.teleport(newLoc2);
    }

    // Set cooldowns
    setCollisionCooldown(horse1);
    setCollisionCooldown(horse2);

    // Feedback
    sendActionBar(player1, "§c§l⚠ ᴄᴏʟʟɪꜱɪᴏɴ ⚠");
    sendActionBar(player2, "§c§l⚠ ᴄᴏʟʟɪꜱɪᴏɴ ⚠");
    
    if (DEBUG) {
      plugin.getLogger().info(() -> "Collision between " + player1.getName() + " and " + player2.getName() 
              + " (distance: " + String.format("%.2f", currentDistance) + ")");
    }
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
