package dev.ixpu.leaguemechanics.rune.keystones.resolve;

import dev.ixpu.leaguemechanics.manager.DamageManager;
import dev.ixpu.leaguemechanics.rune.BaseRune;
import dev.ixpu.leaguemechanics.rune.RunePath;
import dev.ixpu.leaguemechanics.rune.RuneSlot;
import dev.ixpu.leaguemechanics.player.PlayerStats;
import dev.ixpu.leaguemechanics.util.DebugLogger;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.bukkit.Sound;

import org.bukkit.entity.Player;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.configuration.ConfigurationSection;

import net.kyori.adventure.text.Component;

public class AfterShock extends BaseRune {
    private int EFFECT_DURATION_TICKS = 300;
    private int ABSORPTION_LEVEL = 2;
    private int RESISTANCE_LEVEL = 2;

    int COOLDOWN_SECONDS = 45;

    private final Map<UUID, Long> effectStartTime = new HashMap<>();

    public AfterShock(ConfigurationSection config) {
        super("aftershock", RunePath.RESOLVE, RuneSlot.KEYSTONE);
        ConfigurationSection section = config.getConfigurationSection("runes.keystones.resolve.aftershock");
        
        if (section != null) {
            this.EFFECT_DURATION_TICKS = section.getInt("effect-duration", this.EFFECT_DURATION_TICKS);
            this.ABSORPTION_LEVEL = section.getInt("absorption-level", this.ABSORPTION_LEVEL);
            this.RESISTANCE_LEVEL = section.getInt("resistance-level", this.RESISTANCE_LEVEL);
            this.COOLDOWN_SECONDS = section.getInt("cooldown", COOLDOWN_SECONDS);
        }
        this.setCooldownSeconds(COOLDOWN_SECONDS);
    }

    @Override
    public void onEnable(Player player) {
        UUID uuid = player.getUniqueId();
        effectStartTime.put(uuid, 0L);
    }

    @Override
    public void onDisable(Player player) {
        UUID uuid = player.getUniqueId();
        clearPlayerCooldown(player);
        effectStartTime.remove(uuid);
    }

    public void onProjectileHit(Player shooter, Entity target) {
        if (!(target instanceof LivingEntity livingTarget)) {
            return;
        }

        double statsDamage = playerDamage(shooter, target);
        double newHealth = Math.max(0, Math.min(livingTarget.getMaxHealth(), livingTarget.getHealth() - statsDamage));

        DebugLogger.debug(shooter, "§7[Debug] §f[§aAfter Shock§f] (Projectile) Stats Damage = §d" + Math.ceil(statsDamage * 100) / 100.0);
        DebugLogger.debug(shooter, "§7[Debug] §f[§aAfter Shock§f] (Projectile) Target New HP = §d" + Math.ceil(newHealth * 100) / 100.0);

        livingTarget.setHealth(newHealth);
    }

    public void onAttack(Player attacker, Entity target) {
        if (!(target instanceof LivingEntity livingTarget)) {
            return;
        }

        double statsDamage = playerDamage(attacker, target);
        double newHealth = Math.max(0, Math.min(livingTarget.getMaxHealth(), livingTarget.getHealth() - statsDamage));

        DebugLogger.debug(attacker, "§7[Debug] §f[§aAfter Shock§f] (Melee) Stats Damage = §d" + Math.ceil(statsDamage * 100) / 100.0);
        DebugLogger.debug(attacker, "§7[Debug] §f[§aAfter Shock§f] (Melee) Target New HP = §d" + Math.ceil(newHealth * 100) / 100.0);

        livingTarget.setHealth(newHealth);

        activateAfterShock(attacker, target);
    }

    private void activateAfterShock(Player player, Entity target) {
        UUID attackerUUID = player.getUniqueId();

        if (!isWindBurstMace(player.getInventory().getItemInMainHand())) {
            return;
        }
        if (!(target instanceof LivingEntity livingTarget)) {
            return;
        }
        if (livingTarget.getMaxHealth() < 20) {
            return;
        }
        if (isOnCooldown(player)) {
            return;
        }

        long effectStart = effectStartTime.getOrDefault(attackerUUID, 0L);
        long currentTime = System.currentTimeMillis();
        long effectDurationMs = EFFECT_DURATION_TICKS * 50L;

        if (effectStart > 0 && (currentTime - effectStart) < effectDurationMs) {
            return;
        }

        activateEffects(player);

        org.bukkit.Bukkit.getScheduler().scheduleSyncDelayedTask(
                dev.ixpu.leaguemechanics.LeagueMechanics.getInstance(),
                () -> resetCooldown(player),
                EFFECT_DURATION_TICKS
        );
    }

    private double playerDamage(Player player, Entity target) {
        DamageManager damageManager = new DamageManager();
        return damageManager.totalBonusDamage(player, target, 0);
    }

    private void activateEffects(Player player) {
        UUID playerUUID = player.getUniqueId();
        effectStartTime.put(playerUUID, System.currentTimeMillis());

        player.addPotionEffect(new PotionEffect(
                PotionEffectType.ABSORPTION,
                EFFECT_DURATION_TICKS,
                ABSORPTION_LEVEL - 1,
                false,
                false
        ));

        player.addPotionEffect(new PotionEffect(
                PotionEffectType.RESISTANCE,
                EFFECT_DURATION_TICKS,
                RESISTANCE_LEVEL - 1,
                false,
                false
        ));

        player.playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 1.0f);
    }

    private boolean isWindBurstMace(ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return false;
        }

        if (!item.getType().name().equals("MACE")) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }

        return meta.hasEnchant(Objects.requireNonNull(Enchantment.getByName("wind_burst")));
    }

    @Override
    public void tick(Player player) {
        UUID playerUUID = player.getUniqueId();
        long effectStart = effectStartTime.getOrDefault(playerUUID, 0L);
        long currentTime = System.currentTimeMillis();
        long effectDurationMs = EFFECT_DURATION_TICKS * 50L;

        RuneState state;
        long remainingMs = 0;

        if (effectStart > 0 && (currentTime - effectStart) < effectDurationMs) {
            state = RuneState.ACTIVE;
            remainingMs = effectDurationMs - (currentTime - effectStart);
        } else if (isOnCooldown(player)) {
            state = RuneState.COOLDOWN;
        } else {
            state = RuneState.IDLE;
        }

        String runeDisplay = getRuneDisplay(state, player, remainingMs);
        setPlayerDisplay(player, runeDisplay);
    }

    private void setPlayerDisplay(Player player, String runeDisplay) {
        PlayerStats playerStats = new PlayerStats();
        String statsDisplay = playerStats.getActionBarSections(player);
        
        String actionBarMessage = runeDisplay + " " + statsDisplay;
        player.sendActionBar(Component.text(actionBarMessage));
    }

    enum RuneState {
        ACTIVE, COOLDOWN, IDLE
    }

    private String getRuneDisplay(RuneState state, Player player, long remainingMs) {
        return switch (state) {
            case COOLDOWN -> "§7💢 " + getCooldownDisplay(player);
            case ACTIVE -> {
                double remainingSeconds = remainingMs / 1000.0;
                yield String.format("§a💢 (%.1fs)", remainingSeconds);
            }
            case IDLE -> "§2💢";
        };
    }

}