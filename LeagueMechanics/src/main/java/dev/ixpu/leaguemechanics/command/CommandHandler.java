package dev.ixpu.leaguemechanics.command;

import dev.ixpu.leaguemechanics.LeagueMechanics;

import dev.ixpu.leaguemechanics.item.shop.ItemShopGUI;

import dev.ixpu.leaguemechanics.listener.PlayerEventListener;
import dev.ixpu.leaguemechanics.manager.ItemStatsManager;
import dev.ixpu.leaguemechanics.manager.RuneManager;

import dev.ixpu.leaguemechanics.rune.CooldownHandler;
import dev.ixpu.leaguemechanics.rune.RunePath;
import dev.ixpu.leaguemechanics.rune.RuneRegistry;
import dev.ixpu.leaguemechanics.rune.RuneSlot;

import dev.ixpu.leaguemechanics.player.PlayerClassType;
import dev.ixpu.leaguemechanics.util.RunePersistence;

import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CommandHandler implements CommandExecutor {
    private final LeagueMechanics plugin;
    private final ItemStatsManager itemStatsManager;
    private final RuneManager runeManager;
    private final RunePersistence runePersistence;
    private final PlayerEventListener playerEventListener;

    public CommandHandler(LeagueMechanics plugin, ItemStatsManager itemStatsManager, RuneManager runeManager, RunePersistence runePersistence, PlayerEventListener playerEventListener) {
        this.plugin = plugin;
        this.itemStatsManager = itemStatsManager;
        this.runeManager = runeManager;
        this.runePersistence = runePersistence;
        this.playerEventListener = playerEventListener;
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
            case "shop" -> handleShop(player);
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
            case "class" -> handleClassCommand(player, args);
            default -> {
                player.sendMessage(Component.text("§cUnknown subcommand."));
                yield false;
            }
        };
    }

    private boolean handleClassCommand(Player player, String[] args) {
        if (!player.hasPermission("leaguemechanics.user")) {
            player.sendMessage(Component.text("§cYou don't have permission to use this command."));
            return true;
        }

        if (args.length < 2) {
            PlayerClassType currentClass = dev.ixpu.leaguemechanics.player.PlayerClass.getPlayerClass(player);
            if (currentClass != null) {
                player.sendMessage(Component.text("§6Your current class: §e" + currentClass.getDisplayName()));
            } else {
                player.sendMessage(Component.text("§cYou don't have a class set. Use §e/lm class <class> §cto set one."));
            }
            player.sendMessage(Component.text("§7Available classes: §afighter §7| §asupport §7| §aassassin §7| §amage §7| §atank §7| §amarksman"));
            return true;
        }

        String classArg = args[1].toLowerCase();

        if (classArg.equals("clear")) {
            dev.ixpu.leaguemechanics.player.PlayerClass.clearPlayerClass(player);
            player.sendMessage(Component.text("§a✓ Class cleared!"));
            return true;
        }

        PlayerClassType classType = PlayerClassType.fromId(classArg);

        if (classType == null) {
            player.sendMessage(Component.text("§cInvalid class. Available: §afighter §7| §asupport §7| §aassassin §7| §amage §7| §atank §7| §amarksman"));
            return true;
        }

        dev.ixpu.leaguemechanics.player.PlayerClass.setPlayerClass(player, classType);
        playerEventListener.applyPlayerStats(player);
        player.sendMessage(Component.text("§a✓ Class set to §e" + classType.getDisplayName() + "§a!"));
        return true;
    }

    private boolean handleRunesCommand(Player player, String[] args) {
        if (!player.hasPermission("leaguemechanics.user")) {
            player.sendMessage(Component.text("§cYou don't have permission to use this command."));
            return true;
        }

        if (args.length < 2) {
            player.sendMessage(Component.text("§cUsage: /lm runes select <primary|secondary> <slot> <path> <rune> | /lm runes clear"));
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
        if (args.length < 6) {
            player.sendMessage(Component.text("§cUsage: /lm runes select <primary|secondary> <slot> <path> <rune>"));
            return true;
        }

        String slotLocation = args[2].toLowerCase();
        boolean isPrimary = slotLocation.equals("primary");
        boolean isSecondary = slotLocation.equals("secondary");

        if (!isPrimary && !isSecondary) {
            player.sendMessage(Component.text("§cInvalid slot location. Use: primary or secondary"));
            return true;
        }

        String slotName = args[3].toLowerCase();
        RuneSlot slot = null;

        switch (slotName) {
            case "keystone":
                slot = RuneSlot.KEYSTONE;
                break;
            case "primary-slot-1":
            case "slot-1":
                slot = RuneSlot.PRIMARY_SLOT_1;
                break;
            case "primary-slot-2":
            case "slot-2":
                slot = RuneSlot.PRIMARY_SLOT_2;
                break;
            case "primary-slot-3":
            case "slot-3":
                slot = RuneSlot.PRIMARY_SLOT_3;
                break;
            case "secondary-slot-1":
            case "secondary-slot-one":
            case "slot-1s":
                slot = RuneSlot.SECONDARY_SLOT_1;
                break;
            case "secondary-slot-2":
            case "secondary-slot-two":
            case "slot-2s":
                slot = RuneSlot.SECONDARY_SLOT_2;
                break;
            default:
                player.sendMessage(Component.text("§cInvalid slot. Use: keystone, primary-slot-1, primary-slot-2, primary-slot-3, secondary-slot-1, secondary-slot-2"));
                return true;
        }

        if (isSecondary && (slot == RuneSlot.KEYSTONE)) {
            player.sendMessage(Component.text("§cKeystone can only be placed in primary location"));
            return true;
        }

        String pathName = args[4].toLowerCase();
        RunePath path = RunePath.fromId(pathName);
        if (path == null) {
            player.sendMessage(Component.text("§cInvalid path. Use: domination, precision, inspiration, resolve, or sorcery"));
            return true;
        }

        String runeName = args[5].toLowerCase();
        CooldownHandler rune = RuneRegistry.getInstance().getRune(runeName);

        if (rune == null) {
            player.sendMessage(Component.text("§cRune not found."));
            return true;
        }

        if (!rune.getPath().equals(path)) {
            player.sendMessage(Component.text("§c" + runeName + " is not in the " + path.getId() + " path."));
            return true;
        }

        if (!rune.getSlot().equals(slot)) {
            player.sendMessage(Component.text("§c" + runeName + " is not a " + slot.getId() + " slot rune"));
            return true;
        }

        String permissionKey = "rune." + runeName;
        if (!player.hasPermission(permissionKey)) {
            player.sendMessage(Component.text("§cYou don't have permission to select " + runeName + "."));
            return true;
        }

        boolean success;
        switch (slot) {
            case KEYSTONE:
                runeManager.setPlayerKeystoneRune(player, rune);
                runePersistence.saveKeystoneRune(player.getUniqueId(), runeName);
                success = true;
                break;
            case PRIMARY_SLOT_1:
                runeManager.setPlayerPrimarySlot1Rune(player, rune);
                runePersistence.savePrimarySlot1Rune(player.getUniqueId(), runeName);
                success = true;
                break;
            case PRIMARY_SLOT_2:
                runeManager.setPlayerPrimarySlot2Rune(player, rune);
                runePersistence.savePrimarySlot2Rune(player.getUniqueId(), runeName);
                success = true;
                break;
            case PRIMARY_SLOT_3:
                runeManager.setPlayerPrimarySlot3Rune(player, rune);
                runePersistence.savePrimarySlot3Rune(player.getUniqueId(), runeName);
                success = true;
                break;
            case SECONDARY_SLOT_1:
                runeManager.setPlayerSecondarySlot1Rune(player, rune);
                runePersistence.saveSecondarySlot1Rune(player.getUniqueId(), runeName);
                success = true;
                break;
            case SECONDARY_SLOT_2:
                runeManager.setPlayerSecondarySlot2Rune(player, rune);
                runePersistence.saveSecondarySlot2Rune(player.getUniqueId(), runeName);
                success = true;
                break;
            default:
                success = false;
        }

        if (success) {
            String locationText = isPrimary ? "primary" : "secondary";
            player.sendMessage(Component.text("§a✓ Selected §e" + runeName + "§a in §e" + locationText + " §e" + slot.getId()));
        } else {
            player.sendMessage(Component.text("§cFailed to select rune"));
        }
        return success;
    }

    private boolean handleRunesClear(Player player) {
        runePersistence.clearAllRunes(player.getUniqueId());
        runeManager.clearPlayerRunes(player);
        player.sendMessage(Component.text("§a✓ All runes cleared"));
        return true;
    }

    private boolean handleShop(Player player) {
        if (!player.hasPermission("leaguemechanics.user")) {
            player.sendMessage(Component.text("§cYou don't have permission to use this command."));
            return true;
        }
        ItemShopGUI.openShop(player);
        return true;
    }
}