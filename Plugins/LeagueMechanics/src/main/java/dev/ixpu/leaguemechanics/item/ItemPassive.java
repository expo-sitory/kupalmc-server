package dev.ixpu.leaguemechanics.item;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface ItemPassive {
    String getId();
    String getDescription();
    void onEntityKill(Player player, ItemStack item);
    void tick(Player player, ItemStack item);
}