package dev.ixpu.leaguemechanics.rune.slots.primary.slot_1.resolve;

import dev.ixpu.leaguemechanics.LeagueMechanics;
import dev.ixpu.leaguemechanics.player.PlayerRuneData;
import dev.ixpu.leaguemechanics.player.PlayerStats;
import dev.ixpu.leaguemechanics.rune.CooldownHandler;
import dev.ixpu.leaguemechanics.rune.RunePath;
import dev.ixpu.leaguemechanics.rune.RuneSlot;

import net.kyori.adventure.text.Component;

import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Demolish extends CooldownHandler {

    private int blocksPerStack = 5;
    private int stacksForActivation = 20;
    private int activeDurationTicks = 15 * 20;
    private int hasteAmplifier = 0;

    private final Map<UUID, Integer> progress = new HashMap<>();
    private final Map<UUID, Integer> stacks = new HashMap<>();
    private final Map<UUID, Integer> activeRemainingTicks = new HashMap<>();
    private final Map<UUID, Integer> activeTaskIds = new HashMap<>();

    public Demolish(ConfigurationSection config) {
        super("demolish", RunePath.RESOLVE, RuneSlot.PRIMARY_SLOT_1);

        ConfigurationSection section = config.getConfigurationSection("runes.slots.primary.slot-1.resolve.demolish");
        if (section != null) {
            this.blocksPerStack = section.getInt("blocks-per-stack", this.blocksPerStack);
            this.stacksForActivation = section.getInt("stacks-for-activation", this.stacksForActivation);
            this.activeDurationTicks = section.getInt("active-duration-ticks", this.activeDurationTicks);
            this.hasteAmplifier = section.getInt("haste-amplifier", this.hasteAmplifier);
        }
    }

    @Override
    public void onEnable(Player player) {
        UUID uuid = player.getUniqueId();
        progress.put(uuid, 0);
        stacks.put(uuid, 0);
        activeRemainingTicks.put(uuid, 0);
    }

    @Override
    public void onDisable(Player player) {
        UUID uuid = player.getUniqueId();
        cancelActiveTask(player);
        progress.remove(uuid);
        stacks.remove(uuid);
        activeRemainingTicks.remove(uuid);
    }

    @Override
    public void onBlockBreak(Player player, int blocksBroken) {
        if (blocksBroken <= 0) {
            return;
        }
        UUID uuid = player.getUniqueId();

        if (activeRemainingTicks.getOrDefault(uuid, 0) > 0) {
            return;
        }

        int currentProgress = progress.getOrDefault(uuid, 0) + blocksBroken;
        int currentStacks = stacks.getOrDefault(uuid, 0);

        while (currentProgress >= blocksPerStack && currentStacks < stacksForActivation) {
            currentProgress -= blocksPerStack;
            currentStacks++;
        }
        if (currentStacks >= stacksForActivation) {
            currentProgress = 0;
        }

        progress.put(uuid, currentProgress);
        stacks.put(uuid, currentStacks);

        if (currentStacks >= stacksForActivation) {
            triggerActive(player);
        }
    }

    private void triggerActive(Player player) {
        UUID uuid = player.getUniqueId();

        stacks.put(uuid, 0);
        progress.put(uuid, 0);
        activeRemainingTicks.put(uuid, activeDurationTicks);

        player.addPotionEffect(new PotionEffect(
                PotionEffectType.HASTE,
                activeDurationTicks,
                hasteAmplifier,
                true,
                true,
                true
        ));
        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.6f, 1.4f);

        cancelActiveTask(player);
        int[] taskId = { -1 };
        taskId[0] = LeagueMechanics.getInstance().getServer().getScheduler().scheduleSyncRepeatingTask(
                LeagueMechanics.getInstance(),
                new Runnable() {
                    int remaining = activeDurationTicks;

                    @Override
                    public void run() {
                        remaining--;
                        activeRemainingTicks.put(uuid, remaining);
                        if (remaining <= 0) {
                            LeagueMechanics.getInstance().getServer().getScheduler().cancelTask(taskId[0]);
                            activeTaskIds.remove(uuid);
                            activeRemainingTicks.remove(uuid);
                        }
                    }
                },
                0L, 1L
        );
        activeTaskIds.put(uuid, taskId[0]);
    }

    private void cancelActiveTask(Player player) {
        Integer taskId = activeTaskIds.remove(player.getUniqueId());
        if (taskId != null && taskId != -1) {
            LeagueMechanics.getInstance().getServer().getScheduler().cancelTask(taskId);
        }
    }

    @Override
    public void tick(Player player) {
        UUID uuid = player.getUniqueId();
        CooldownHandler keystone = getKeystone(player);
        String keystoneSection = keystone != null ? keystone.getDisplaySection(player) : "";

        int activeRemaining = activeRemainingTicks.getOrDefault(uuid, 0);
        if (activeRemaining > 0) {
            double remainingSeconds = activeRemaining / 20.0;
            String slotSection = String.format("§a⛏ §f(%.1fs)", remainingSeconds);
            String combined = keystoneSection + " " + slotSection + " " + PlayerStats.getOrCreate(player).getActionBarSections(player);
            player.sendActionBar(Component.text(combined));
            return;
        }

        int currentStacks = stacks.getOrDefault(uuid, 0);
        int currentProgress = progress.getOrDefault(uuid, 0);
        String slotSection = String.format("§2⛏ §f%d§7/§f%d", currentStacks, stacksForActivation)
                + (currentProgress > 0 ? String.format(" §7(+%d)", currentProgress) : "");
        String combined = keystoneSection + " " + slotSection + " " + PlayerStats.getOrCreate(player).getActionBarSections(player);
        player.sendActionBar(Component.text(combined));
    }

    private CooldownHandler getKeystone(Player player) {
        LeagueMechanics plugin = LeagueMechanics.getInstance();
        if (plugin == null || plugin.getRuneManager() == null) {
            return null;
        }
        PlayerRuneData runeData = plugin.getRuneManager().getPlayerRuneData(player);
        return runeData != null ? runeData.getKeystoneRune() : null;
    }
}
