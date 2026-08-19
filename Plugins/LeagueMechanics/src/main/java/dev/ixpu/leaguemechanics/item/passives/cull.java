package dev.ixpu.leaguemechanics.item.passives;

import dev.ixpu.leaguemechanics.item.ItemPassive;
import dev.ixpu.leaguemechanics.manager.ItemPassivesManager;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class cull implements ItemPassive {
    private static final int XP_PER_KILL = 10;
    private static final int MAX_XP = 500;
    private static final int UPGRADE_THRESHOLD = 100;
    private static final int UPGRADED_XP = 200;

    @Override
    public String getId() {
        return "cull";
    }

    @Override
    public String getDescription() {
        return "§7ᴜɴɪQᴜᴇ – ʀᴇᴀᴘ: §fKilling an §eentity §fgrants an additional §a10 xp§f, up to a maximum of §a500§f.\n§fAfter having killed §e100 entities§f, grants an additional §a200 xp§f and permanently disables this passive.";
    }

    @Override
    public void onEntityKill(Player player, ItemStack item) {
        ItemPassivesManager manager = ItemPassivesManager.getInstance();

        if (manager.isPassiveDisabled(player, getId())) {
            return;
        }

        int killCount = manager.getKillCount(player, getId());
        manager.addKill(player, getId());

        if (killCount >= UPGRADE_THRESHOLD) {
            player.giveExp(UPGRADED_XP);
            manager.disablePassive(player, getId());
        } else {
            int totalXpGained = killCount * XP_PER_KILL;
            if (totalXpGained < MAX_XP) {
                player.giveExp(XP_PER_KILL);
            }
        }
    }

    @Override
    public void tick(Player player, ItemStack item) {
    }
}