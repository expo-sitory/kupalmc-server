package dev.ixpu.leaguemechanics.command;

import dev.ixpu.leaguemechanics.LeagueMechanics;
import dev.ixpu.leaguemechanics.manager.ItemStatsManager;
import dev.ixpu.leaguemechanics.manager.RuneManager;
import dev.ixpu.leaguemechanics.rune.BaseRune;
import dev.ixpu.leaguemechanics.rune.RunePath;
import dev.ixpu.leaguemechanics.rune.RuneRegistry;
import dev.ixpu.leaguemechanics.util.ItemStatHelper;
import dev.ixpu.leaguemechanics.util.RunePersistence;

import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class CommandHandler implements CommandExecutor {
    private final LeagueMechanics plugin;
    private final ItemStatsManager itemStatsManager;
    private final RuneManager runeManager;
    private final RunePersistence runePersistence;

    public CommandHandler(LeagueMechanics plugin, ItemStatsManager itemStatsManager, RuneManager runeManager, RunePersistence runePersistence) {
        this.plugin = plugin;
        this.itemStatsManager = itemStatsManager;
        this.runeManager = runeManager;
        this.runePersistence = runePersistence;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this command!");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(Component.text("§cUsage: /lm <subcommand>"));
            return true;
        }

        String subcommand = args[0].toLowerCase();

        return switch (subcommand) {
            case "addstat" -> handleAddStat(player, args);
            case "setbuild" -> handleSetBuild(player, args);
            case "stats" -> handleStats(player);
            case "clearstats" -> handleClearStats(player);
            case "reload" -> {
                if (!player.hasPermission("leaguemechanics.admin")) {
                    player.sendMessage(Component.text("§cYou don't have permission to use this command."));
                    yield true;
                }
                player.sendMessage(Component.text("§6⟳ Reloading LeagueMechanics..."));
                plugin.reloadPlugin();
                player.sendMessage(Component.text("§a✓ LeagueMechanics reloaded"));
                yield true;
            }
            case "runes" -> handleRunesCommand(player, args);
            default -> {
                player.sendMessage(Component.text("§cUnknown subcommand."));
                yield false;
            }
        };
    }

    private boolean handleRunesCommand(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(Component.text("§cUsage: /lm runes select primary <path> <keystone> | /lm runes clear"));
            return true;
        }

        String runesSubcommand = args[1].toLowerCase();
        return switch (runesSubcommand) {
            case "select" -> handleRuneSelect(player, args);
            case "clear" -> handleRunesClear(player);
            default -> {
                player.sendMessage(Component.text("§cUnknown runes subcommand."));
                yield false;
            }
        };
    }

    private boolean handleRuneSelect(Player player, String[] args) {
        if (args.length < 5) {
            player.sendMessage(Component.text("§cUsage: /lm runes select primary <path> <keystone>"));
            return true;
        }

        String slotType = args[2].toLowerCase();
        if (!slotType.equals("primary")) {
            player.sendMessage(Component.text("§cOnly primary slot is currently available."));
            return true;
        }

        String pathName = args[3].toLowerCase();
        RunePath path = RunePath.fromId(pathName);
        if (path == null) {
            player.sendMessage(Component.text("§cInvalid path. Use: domination, precision, inspiration, resolve, or sorcery"));
            return true;
        }

        String keystoneName = args[4].toLowerCase();
        BaseRune keystone = RuneRegistry.getInstance().getRune(keystoneName);

        if (keystone == null) {
            player.sendMessage(Component.text("§cKeystone not found."));
            return true;
        }

        if (!keystone.getPath().equals(path)) {
            player.sendMessage(Component.text("§c" + keystoneName + " is not in the " + path.getId() + " path."));
            return true;
        }

        String permissionKey = "rune-keystone." + keystoneName;
        if (!player.hasPermission(permissionKey)) {
            player.sendMessage(Component.text("§cYou don't have permission to select " + keystoneName + "."));
            return true;
        }

        String pathPermissionKey = "primary-rune-path." + pathName;
        if (!player.hasPermission(pathPermissionKey)) {
            player.sendMessage(Component.text("§cYou don't have permission to select the " + path.getId() + " path."));
            return true;
        }

        runeManager.setPlayerKeystoneRune(player, keystone);
        runePersistence.savePrimaryPath(player.getUniqueId(), path);
        runePersistence.saveKeystoneRune(player.getUniqueId(), keystoneName);
        player.sendMessage(Component.text("§a✓ Selected §e" + keystoneName + "§a from §e" + path.getId() + "§a path"));
        return true;
    }

    private boolean handleRunesClear(Player player) {
        runePersistence.clearAllRunes(player.getUniqueId());
        runeManager.clearPlayerRunes(player);
        player.sendMessage(Component.text("§a✓ All runes cleared"));
        return true;
    }

    private boolean handleAddStat(Player player, String[] args) {
        if (!player.hasPermission("leaguemechanics.admin")) {
            player.sendMessage(Component.text("§cYou don't have permission to use this command."));
            return true;
        }

        if (args.length < 3) {
            player.sendMessage(Component.text("§cUsage: /leaguemechanics addStat <AP|AD|AR|MR> <value>"));
            return true;
        }

        ItemStack heldItem = player.getInventory().getItemInMainHand();
        if (heldItem.getType().isAir()) {
            player.sendMessage(Component.text("§cYou must be holding an item"));
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
        if (!player.hasPermission("leaguemechanics.admin")) {
            player.sendMessage(Component.text("§cYou don't have permission to use this command."));
            return true;
        }

        ItemStack heldItem = player.getInventory().getItemInMainHand();
        if (heldItem.getType().isAir()) {
            player.sendMessage(Component.text("§cYou must be holding an item"));
            return true;
        }

        boolean currentMode = ItemStatHelper.isBuildMode(heldItem);
        boolean newMode = !currentMode;

        ItemStatHelper.setBuildMode(heldItem, newMode);
        player.sendMessage(Component.text(newMode ? "§a✓ Item set to build mode" : "§a✓ Item removed from build mode"));
        return true;
    }

    private boolean handleStats(Player player) {
        if (!player.hasPermission("leaguemechanics.admin")) {
            player.sendMessage(Component.text("§cYou don't have permission to use this command."));
            return true;
        }

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
        if (!player.hasPermission("leaguemechanics.admin")) {
            player.sendMessage(Component.text("§cYou don't have permission to use this command."));
            return true;
        }

        ItemStack heldItem = player.getInventory().getItemInMainHand();
        if (heldItem.getType().isAir()) {
            player.sendMessage(Component.text("§cYou must be holding an item"));
            return true;
        }

        ItemStatHelper.clearStats(heldItem);
        player.sendMessage(Component.text("§a✓ All item stats has been cleared"));
        return true;
    }

    private boolean isValidStat(String stat) {
        return stat.equals("AP") || stat.equals("AD") || stat.equals("AR") || stat.equals("MR");
    }

    private List<BaseRune> getKeystonesByPath(RunePath path) {
        List<BaseRune> keystones = new ArrayList<>();
        for (BaseRune rune : RuneRegistry.getInstance().getAllRunes().values()) {
            if (rune.getPath().equals(path) && rune.getSlot().name().equals("KEYSTONE")) {
                keystones.add(rune);
            }
        }
        return keystones;
    }
}