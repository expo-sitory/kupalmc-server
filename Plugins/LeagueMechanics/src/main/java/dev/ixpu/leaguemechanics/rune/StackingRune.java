package dev.ixpu.leaguemechanics.rune;

import org.bukkit.entity.Player;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public abstract class StackingRune extends BaseRune {
    protected final Map<UUID, Integer> playerStacks = new HashMap<>();
    protected int stackResetTicks;

    public StackingRune(String id, RunePath path, RuneSlot slot, int maxStacks) {
        super(id, path, slot);
        setStacking(maxStacks);
        this.stackResetTicks = 0;
    }

    public StackingRune(String id, RunePath path, RuneSlot slot, int maxStacks, int stackResetTicks) {
        super(id, path, slot);
        setStacking(maxStacks);
        this.stackResetTicks = stackResetTicks;
    }

    public int getStacks(Player player) {
        return playerStacks.getOrDefault(player.getUniqueId(), 0);
    }

    public void addStack(Player player) {
        UUID uuid = player.getUniqueId();
        int currentStacks = playerStacks.getOrDefault(uuid, 0);
        if (currentStacks < maxStacks) {
            playerStacks.put(uuid, currentStacks + 1);
            onStackAdded(player, currentStacks + 1);
        }
    }

    public void setStacks(Player player, int stacks) {
        int clamped = Math.max(0, Math.min(stacks, maxStacks));
        playerStacks.put(player.getUniqueId(), clamped);
    }

    public void resetStacks(Player player) {
        playerStacks.remove(player.getUniqueId());
        onStacksReset(player);
    }

    public void clearAllStacks() {
        playerStacks.clear();
    }

    protected void onStackAdded(Player player, int newStackCount) {}

    protected void onStacksReset(Player player) {}

    @Override
    public void onDisable(Player player) {
        resetStacks(player);
    }
}
