package dev.ixpu.leaguemechanics.command;

import dev.ixpu.leaguemechanics.LeagueMechanics;
import dev.ixpu.leaguemechanics.manager.ItemStatsManager;
import dev.ixpu.leaguemechanics.util.ItemStatHelper;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class CommandHandler implements CommandExecutor {
    private final LeagueMechanics plugin;
    private final ItemStatsManager itemStatsManager;

    public CommandHandler(LeagueMechanics plugin, ItemStatsManager itemStatsManager) {
        this.plugin = plugin;
        this.itemStatsManager = itemStatsManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this command!");
            return true;
        }

        if (!player.hasPermission("leaguemechanics.admin")) {
            player.sendMessage(Component.text("§cYou don't have permission to use this command."));
            return true;
        }

        String subcommand = args[0].toLowerCase();

        return switch (subcommand) {
            case "addstat" -> handleAddStat(player, args);
            case "setbuild" -> handleSetBuild(player, args);
            case "stats" -> handleStats(player);
            case "clearstats" -> handleClearStats(player);
            case "reload" -> handleReload(player);
            default -> {
                player.sendMessage(Component.text("§cUnknown subcommand."));
                yield false;
            }
        };
    }

    private boolean handleAddStat(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(Component.text("§cUsage: /leaguemechanics addStat <AP|AD|AR|MR> <value>"));
            return true;
        }

        ItemStack heldItem = player.getInventory().getItemInMainHand();
        if (heldItem.getType().isAir()) {
            player.sendMessage(Component.text("§cYou must be holding an item niga"));
            return true;
        }

        String statType = args[1].toUpperCase();
        if (!isValidStat(statType)) {
            player.sendMessage(Component.text("§cInvalid stat type. Use AP, AD, AR, or MR"));
            return true;
        }

        try {
            double value = Double.parseDouble(args[2]);
            ItemStatHelper.setStat(heldItem, statType, value);
            player.sendMessage(Component.text("§a✓ Added " + value + " " + statType + " to held item"));
            return true;
        } catch (NumberFormatException e) {
            player.sendMessage(Component.text("§cInvalid value"));
            return true;
        }
    }

    private boolean handleSetBuild(Player player, String[] args) {
        ItemStack heldItem = player.getInventory().getItemInMainHand();
        if (heldItem.getType().isAir()) {
            player.sendMessage(Component.text("§cYou must be holding an item niga"));
            return true;
        }

        boolean currentMode = ItemStatHelper.isBuildMode(heldItem);
        boolean newMode = !currentMode;

        ItemStatHelper.setBuildMode(heldItem, newMode);

        return true;
    }

    private boolean handleStats(Player player) {
        double ap = itemStatsManager.getItemAP(player);
        double ad = itemStatsManager.getItemAD(player);
        double ar = itemStatsManager.getItemAR(player);
        double mr = itemStatsManager.getItemMR(player);

        player.sendMessage(Component.text("§7─── §6 ɪᴛᴇᴍ ꜱᴛᴀᴛꜱ §7───"));
        player.sendMessage(Component.text("AP: " + String.format("%.1f", ap)));
        player.sendMessage(Component.text("AD: " + String.format("%.1f", ad)));
        player.sendMessage(Component.text("AR: " + String.format("%.1f", ar)));
        player.sendMessage(Component.text("MR: " + String.format("%.1f", mr)));
        return true;
    }

    private boolean handleClearStats(Player player) {
        ItemStack heldItem = player.getInventory().getItemInMainHand();
        if (heldItem.getType().isAir()) {
            player.sendMessage(Component.text("§cYou must be holding an item niga"));
            return true;
        }

        ItemStatHelper.clearStats(heldItem);
        player.sendMessage(Component.text("§a✓ All item stats has been cleared"));
        return true;
    }

    private boolean isValidStat(String stat) {
        return stat.equals("AP") || stat.equals("AD") || stat.equals("AR") || stat.equals("MR");
    }

    private boolean handleReload(Player player) {
        player.sendMessage(Component.text("§6⟳ Reloading LeagueMechanics..."));
        plugin.reloadPlugin();
        player.sendMessage(Component.text("§a✓ LeagueMechanics reloaded"));
        return true;
    }
}