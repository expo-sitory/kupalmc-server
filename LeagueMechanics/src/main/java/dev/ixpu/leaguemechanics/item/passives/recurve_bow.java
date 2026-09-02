package dev.ixpu.leaguemechanics.item.passives;

import dev.ixpu.leaguemechanics.LeagueMechanics;
import dev.ixpu.leaguemechanics.player.PlayerStats;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class recurve_bow implements ItemPassive {
    private static final double STING_BONUS_AD = 15.0;
    private static final long STING_DURATION_MS = 1500L;

    @Override
    public String getId() {
        return "recurve-bow";
    }

    @Override
    public String getDescription() {
        return "§7ᴜɴɪQᴜᴜᴇ – sᴛɪɴɢ: §fAttacks grant §6+15 bonus attack\n§6damage §ffor §e1.5 seconds§f, refreshed on each hit.";
    }

    @Override
    public void onEntityKill(Player player, ItemStack item) {}

    @Override
    public void onDealDamage(Player attacker, LivingEntity target, double damage,
                             boolean isPhysical, boolean isMagic) {
        if (attacker == null) return;
        PlayerStats stats = PlayerStats.getOrCreate(attacker);
        stats.modifyAD(STING_BONUS_AD);

        LeagueMechanics plugin = LeagueMechanics.getInstance();
        if (plugin == null) return;
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!attacker.isOnline()) {
                    cancel();
                    return;
                }
                PlayerStats s = PlayerStats.getOrCreate(attacker);
                s.modifyAD(-STING_BONUS_AD);
                plugin.getPlayerEventListener().applyPlayerStats(attacker);
            }
        }.runTaskLater(plugin, STING_DURATION_MS / 50L);
    }
}
