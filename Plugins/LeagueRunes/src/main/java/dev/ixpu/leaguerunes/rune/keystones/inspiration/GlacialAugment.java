package dev.ixpu.leaguerunes.rune.keystones.inspiration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import dev.ixpu.leaguerunes.LeagueRunes;
import dev.ixpu.leaguerunes.rune.BaseRune;
import dev.ixpu.leaguerunes.rune.RunePath;
import dev.ixpu.leaguerunes.rune.RuneSlot;
import net.kyori.adventure.text.Component;

public class GlacialAugment extends BaseRune {
    private int FREEZE_DURATION_TICKS = 120;
    private double SPEED_REDUCTION = -0.40;
    private double DAMAGE_REDUCTION = -0.20;

    private final Map<UUID, Map<UUID, Integer>> frozenTargets = new HashMap<>();
    private final Map<UUID, Map<UUID, List<AttributeModifier>>> targetModifiers = new HashMap<>();
    private LeagueRunes plugin;

    public GlacialAugment(ConfigurationSection config) {
        super("glacial-augment", RunePath.INSPIRATION, RuneSlot.KEYSTONE);
        ConfigurationSection section = config.getConfigurationSection("runes.keystones.inspiration.glacial-augment");
        int COOLDOWN_DURATION_SECONDS = 45;
        if (section != null) {
            this.FREEZE_DURATION_TICKS = section.getInt("freeze-duration", this.FREEZE_DURATION_TICKS);
            this.SPEED_REDUCTION = section.getDouble("speed-reduction", this.SPEED_REDUCTION);
            this.DAMAGE_REDUCTION = section.getDouble("damage-reduction", this.DAMAGE_REDUCTION);
            COOLDOWN_DURATION_SECONDS = section.getInt("cooldown", COOLDOWN_DURATION_SECONDS);
        }
        this.setCooldownSeconds(COOLDOWN_DURATION_SECONDS);
    }

    public void setPlugin(LeagueRunes plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onEnable(Player player) {
        UUID uuid = player.getUniqueId();
        frozenTargets.put(uuid, new HashMap<>());
        targetModifiers.put(uuid, new HashMap<>());
    }

    @Override
    public void onDisable(Player player) {
        UUID uuid = player.getUniqueId();
        clearPlayerCooldown(player);
        frozenTargets.remove(uuid);
        targetModifiers.remove(uuid);
    }

    public void onProjectileHit(Player shooter, Entity target) {

        if (isOnCooldown(shooter)) {
            return;
        }

        if (!(target instanceof LivingEntity livingTarget)) {
            return;
        }

        if (livingTarget.getMaxHealth() < 20) {
            return;
        }

        applyFreeze(shooter, livingTarget);
        resetCooldown(shooter);
    }

    @Override
    public void tick(Player player) {
        UUID playerUUID = player.getUniqueId();

        if (isOnCooldown(player)) {
            displayCooldown(player);
            return;
        }

        Map<UUID, Integer> frozen = frozenTargets.get(playerUUID);
        if (frozen == null) {
            displayIdleState(player);
            return;
        }

        java.util.ArrayList<UUID> toRemove = new java.util.ArrayList<>();
        for (UUID targetUUID : new java.util.ArrayList<>(frozen.keySet())) {
            int duration = frozen.getOrDefault(targetUUID, 0);

            if (duration > 0) {
                duration--;
                frozen.put(targetUUID, duration);

                if (duration == 0) {
                    toRemove.add(targetUUID);
                }
            } else {
                toRemove.add(targetUUID);
            }
        }

        for (UUID targetUUID : toRemove) {
            frozen.remove(targetUUID);
            removeTargetModifiers(targetUUID);
            targetModifiers.get(playerUUID).remove(targetUUID);
        }

        displayIdleState(player);
    }

    private void applyFreeze(Player attacker, LivingEntity target) {
        UUID attackerUUID = attacker.getUniqueId();
        UUID targetUUID = target.getUniqueId();

        Map<UUID, Integer> frozen = frozenTargets.get(attackerUUID);
        if (frozen == null) {
            return;
        }

        frozen.put(targetUUID, FREEZE_DURATION_TICKS);

        org.bukkit.Location loc = target.getLocation();
        org.bukkit.block.Block[] snowBlocks = new org.bukkit.block.Block[9];
        snowBlocks[0] = loc.getBlock();
        snowBlocks[1] = loc.clone().add(1, 0, 0).getBlock();
        snowBlocks[2] = loc.clone().add(2, 0, 0).getBlock();
        snowBlocks[3] = loc.clone().add(-1, 0, 0).getBlock();
        snowBlocks[4] = loc.clone().add(-2, 0, 0).getBlock();
        snowBlocks[5] = loc.clone().add(0, 0, 1).getBlock();
        snowBlocks[6] = loc.clone().add(0, 0, 2).getBlock();
        snowBlocks[7] = loc.clone().add(0, 0, -1).getBlock();
        snowBlocks[8] = loc.clone().add(0, 0, -2).getBlock();

        for (int i = 0; i < 9; i++) {
            if (snowBlocks[i].getType() == org.bukkit.Material.AIR) {
                snowBlocks[i].setType(org.bukkit.Material.POWDER_SNOW);
            }
        }

        target.addPotionEffect(new PotionEffect(
                PotionEffectType.SLOWNESS,
                FREEZE_DURATION_TICKS,
                2,
                false,
                false
        ));

        attacker.playSound(target.getLocation(), org.bukkit.Sound.BLOCK_POWDER_SNOW_BREAK, 1.0f, 1.0f);

        if (plugin != null) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                target.removePotionEffect(PotionEffectType.SLOWNESS);

                for (int i = 0; i < 9; i++) {
                    if (snowBlocks[i].getType() == org.bukkit.Material.POWDER_SNOW) {
                        snowBlocks[i].setType(org.bukkit.Material.AIR);
                    }
                }
            }, FREEZE_DURATION_TICKS);
        }
    }

    @SuppressWarnings("removal")
    private void applyModifiers(LivingEntity target) {
        UUID targetUUID = target.getUniqueId();
        List<AttributeModifier> modifiers = new ArrayList<>();

        UUID speedModId = new UUID(0, 1);
        var speedModifier = new AttributeModifier(
                speedModId,
                "glacial-speed-reduction",
                SPEED_REDUCTION,
                AttributeModifier.Operation.MULTIPLY_SCALAR_1
        );
        target.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).addModifier(speedModifier);
        modifiers.add(speedModifier);

        UUID damageModId = new UUID(0, 2);
        var damageModifier = new AttributeModifier(
                damageModId,
                "glacial-damage-reduction",
                DAMAGE_REDUCTION,
                AttributeModifier.Operation.MULTIPLY_SCALAR_1
        );
        target.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).addModifier(damageModifier);
        modifiers.add(damageModifier);

        for (UUID playerUUID : frozenTargets.keySet()) {
            Map<UUID, List<AttributeModifier>> playerModifiers = targetModifiers.get(playerUUID);
            if (playerModifiers != null) {
                playerModifiers.put(targetUUID, modifiers);
                return;
            }
        }
    }

    private void removeTargetModifiers(UUID targetUUID) {
        for (UUID playerUUID : frozenTargets.keySet()) {
            Map<UUID, List<AttributeModifier>> playerModifiers = targetModifiers.get(playerUUID);
            if (playerModifiers != null) {
                List<AttributeModifier> mods = playerModifiers.get(targetUUID);
                if (mods != null) {
                    for (LivingEntity entity : getAllLivingEntities()) {
                        if (entity.getUniqueId().equals(targetUUID)) {
                            for (AttributeModifier mod : mods) {
                                try {
                                    entity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).removeModifier(mod);
                                    entity.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).removeModifier(mod);
                                } catch (Exception e) {}
                            }
                            break;
                        }
                    }
                }
            }
        }
    }

    private java.util.List<LivingEntity> getAllLivingEntities() {
        java.util.List<LivingEntity> entities = new ArrayList<>();
        for (org.bukkit.World world : Bukkit.getWorlds()) {
            for (LivingEntity entity : world.getLivingEntities()) {
                entities.add(entity);
            }
        }
        return entities;
    }

    private void displayCooldown(Player player) {
        String cooldownDisplay = getCooldownDisplay(player);
        player.sendActionBar(Component.text("§7❄ " + cooldownDisplay));
    }

    private void displayIdleState(Player player) {
        player.sendActionBar(Component.text("§3❄"));
    }
}