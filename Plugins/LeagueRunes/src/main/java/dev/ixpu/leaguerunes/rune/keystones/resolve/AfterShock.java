package dev.ixpu.leaguerunes.rune.keystones.resolve;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import dev.ixpu.leaguerunes.rune.BaseRune;
import dev.ixpu.leaguerunes.rune.RunePath;
import dev.ixpu.leaguerunes.rune.RuneSlot;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class AfterShock extends BaseRune {
    private int EFFECT_DURATION_TICKS = 300;
    private int ABSORPTION_LEVEL = 2;
    private int RESISTANCE_LEVEL = 2;

    private final Map<UUID, Long> effectStartTime = new HashMap<>();

    public AfterShock(ConfigurationSection config) {
        super("aftershock", RunePath.RESOLVE, RuneSlot.KEYSTONE);
        ConfigurationSection section = config.getConfigurationSection("runes.keystones.resolve.aftershock");
        int COOLDOWN_SECONDS = 40;
        if (section != null) {
            this.EFFECT_DURATION_TICKS = section.getInt("effect-duration", this.EFFECT_DURATION_TICKS);
            this.ABSORPTION_LEVEL = section.getInt("absorption-level", this.ABSORPTION_LEVEL);
            this.RESISTANCE_LEVEL = section.getInt("resistance-level", this.RESISTANCE_LEVEL);
            COOLDOWN_SECONDS = section.getInt("cooldown", COOLDOWN_SECONDS);
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

    @Override
    public void onAttack(Player attacker, Entity target, EntityDamageByEntityEvent event) {
        if (!isWindBurstMace(attacker.getInventory().getItemInMainHand())) {
            return;
        }

        if (!(target instanceof LivingEntity livingTarget)) {
            return;
        }

        double maxHealth = Objects.requireNonNull(livingTarget.getAttribute(Attribute.GENERIC_MAX_HEALTH)).getValue();

        if (maxHealth < 20) {
            return;
        }

        UUID playerUUID = attacker.getUniqueId();
        long effectStart = effectStartTime.getOrDefault(playerUUID, 0L);
        long currentTime = System.currentTimeMillis();
        long effectDurationMs = EFFECT_DURATION_TICKS * 50L;

        if (effectStart > 0 && (currentTime - effectStart) < effectDurationMs) {
            return;
        }

        if (isOnCooldown(attacker)) {
            return;
        }

        applyAfterShockEffect(attacker);

        org.bukkit.Bukkit.getScheduler().scheduleSyncDelayedTask(
                dev.ixpu.leaguerunes.LeagueRunes.getInstance(),
                () -> resetCooldown(attacker),
                EFFECT_DURATION_TICKS
        );
    }

    @Override
    public void tick(Player player) {
        UUID playerUUID = player.getUniqueId();
        long effectStart = effectStartTime.getOrDefault(playerUUID, 0L);
        long currentTime = System.currentTimeMillis();
        long effectDurationMs = EFFECT_DURATION_TICKS * 50L;

        if (effectStart > 0 && (currentTime - effectStart) < effectDurationMs) {
            displayActiveEffectInfo(player);
            return;
        }

        if (isOnCooldown(player)) {
            displayCooldownInfo(player);
            return;
        }

        displayIdleState(player);
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

        return meta.hasEnchant(org.bukkit.enchantments.Enchantment.getByName("wind_burst"));
    }

    private void applyAfterShockEffect(Player player) {
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

    private void displayActiveEffectInfo(Player player) {
        UUID playerUUID = player.getUniqueId();
        long effectStart = effectStartTime.getOrDefault(playerUUID, 0L);
        long currentTime = System.currentTimeMillis();
        long effectDurationMs = EFFECT_DURATION_TICKS * 50L;

        long remainingMs = effectDurationMs - (currentTime - effectStart);
        double remainingSeconds = remainingMs / 1000.0;

        player.sendActionBar(Component.text()
                .append(Component.text(String.format("§a💢 (%.1fs)", remainingSeconds), NamedTextColor.WHITE))
                .build());
    }

    private void displayCooldownInfo(Player player) {
        String cooldownDisplay = getCooldownDisplay(player);
        player.sendActionBar(Component.text()
                .append(Component.text("§7💢 " + cooldownDisplay, NamedTextColor.WHITE))
                .build());
    }

    private void displayIdleState(Player player) {
        player.sendActionBar(Component.text("§2💢", NamedTextColor.WHITE));
    }
}