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
            player.sendMessage(Component.text("§cUsage: /lm <shop|class|runes|reload>"));
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
            sendRunesUsage(player);
            return true;
        }

        String runesSubcommand = args[1].toLowerCase();
        return switch (runesSubcommand) {
            case "select"   -> handleRuneSelect(player, args);
            case "clear"    -> handleRunesClear(player, args);
            case "info"     -> handleRunesInfo(player);
            default -> {
                sendRunesUsage(player);
                yield true;
            }
        };
    }

    private void sendRunesUsage(Player player) {
        player.sendMessage(Component.text("§6§lRunes Commands:"));
        player.sendMessage(Component.text("§7  /lm runes select primary §e<path> [keystone] [slot1] [slot2] [slot3]"));
        player.sendMessage(Component.text("§7  /lm runes select secondary §e<path> [slot1] [slot2]"));
        player.sendMessage(Component.text("§7  /lm runes clear §8— §fclear all runes"));
        player.sendMessage(Component.text("§7  /lm runes info §8— §fshow currently equipped runes"));
    }

    private boolean handleRuneSelect(Player player, String[] args) {
        if (args.length < 3) {
            sendRunesUsage(player);
            return true;
        }

        String location = args[2].toLowerCase();
        if (location.equals("primary")) {
            return handleRuneSelectPrimary(player, args);
        } else if (location.equals("secondary")) {
            return handleRuneSelectSecondary(player, args);
        } else {
            sendRunesUsage(player);
            return true;
        }
    }

    private boolean handleRuneSelectPrimary(Player player, String[] args) {
        if (args.length < 4 || args.length > 8) {
            player.sendMessage(Component.text("§cUsage: /lm runes select primary <path> [keystone] [slot1] [slot2] [slot3]"));
            return true;
        }

        RunePath path = RunePath.fromId(args[3].toLowerCase());
        if (path == null) {
            player.sendMessage(Component.text("§cInvalid path. Use: domination, precision, inspiration, resolve, or sorcery"));
            return true;
        }

        runeManager.setPlayerPrimaryPath(player, path);
        runePersistence.savePrimaryPath(player.getUniqueId(), path);

        RuneSlot[] slotOrder = {
                RuneSlot.KEYSTONE,
                RuneSlot.PRIMARY_SLOT_1,
                RuneSlot.PRIMARY_SLOT_2,
                RuneSlot.PRIMARY_SLOT_3
        };
        StringBuilder summary = new StringBuilder("§a✓ Primary path set to §e" + path.getId());

        for (int i = 4; i < args.length; i++) {
            RuneSlot slot = slotOrder[i - 4];
            CooldownHandler rune = resolveAndValidateRune(player, args[i], path, slot);
            if (rune == null) return true;

            applyPrimaryRune(player, slot, rune);
            runePersistence.savePrimaryRuneSlot(player.getUniqueId(), slot, rune.getId());

            summary.append(" §7| ").append(slot.getId()).append(": §e").append(rune.getId());
        }
        player.sendMessage(Component.text(summary.toString()));
        return true;
    }

    private boolean handleRuneSelectSecondary(Player player, String[] args) {
        if (args.length < 4 || args.length > 6) {
            player.sendMessage(Component.text("§cUsage: /lm runes select secondary <path> [slot1] [slot2]"));
            return true;
        }

        RunePath path = RunePath.fromId(args[3].toLowerCase());
        if (path == null) {
            player.sendMessage(Component.text("§cInvalid path. Use: domination, precision, inspiration, resolve, or sorcery"));
            return true;
        }

        runeManager.setPlayerSecondaryPath(player, path);
        runePersistence.saveSecondaryPath(player.getUniqueId(), path);

        RuneSlot[] slotOrder = {RuneSlot.SECONDARY_SLOT_1, RuneSlot.SECONDARY_SLOT_2};
        StringBuilder summary = new StringBuilder("§a✓ Secondary path set to §e" + path.getId());

        for (int i = 4; i < args.length; i++) {
            RuneSlot slot = slotOrder[i - 4];
            CooldownHandler rune = resolveAndValidateRune(player, args[i], path, slot);
            if (rune == null) return true;

            applySecondaryRune(player, slot, rune);
            runePersistence.saveSecondaryRuneSlot(player.getUniqueId(), slot, rune.getId());

            summary.append(" §7| ").append(slot.getId()).append(": §e").append(rune.getId());
        }
        player.sendMessage(Component.text(summary.toString()));
        return true;
    }

    private void applyPrimaryRune(Player player, RuneSlot slot, CooldownHandler rune) {
        switch (slot) {
            case KEYSTONE -> runeManager.setPlayerKeystoneRune(player, rune);
            case PRIMARY_SLOT_1 -> runeManager.setPlayerPrimarySlot1Rune(player, rune);
            case PRIMARY_SLOT_2 -> runeManager.setPlayerPrimarySlot2Rune(player, rune);
            case PRIMARY_SLOT_3 -> runeManager.setPlayerPrimarySlot3Rune(player, rune);
            default -> throw new IllegalArgumentException("Not a primary slot: " + slot);
        }
    }

    private void applySecondaryRune(Player player, RuneSlot slot, CooldownHandler rune) {
        switch (slot) {
            case SECONDARY_SLOT_1 -> runeManager.setPlayerSecondarySlot1Rune(player, rune);
            case SECONDARY_SLOT_2 -> runeManager.setPlayerSecondarySlot2Rune(player, rune);
            default -> throw new IllegalArgumentException("Not a secondary slot: " + slot);
        }
    }

    private CooldownHandler resolveAndValidateRune(Player player, String runeId, RunePath path, RuneSlot slot) {
        String normalized = runeId.toLowerCase();
        CooldownHandler rune = RuneRegistry.getInstance().getRune(normalized);
        if (rune == null) {
            player.sendMessage(Component.text("§cRune not found: §e" + normalized));
            return null;
        }
        if (!rune.getPath().equals(path)) {
            player.sendMessage(Component.text("§c" + normalized + " is not in the " + path.getId() + " path."));
            return null;
        }
        if (!rune.getSlot().equals(slot)) {
            player.sendMessage(Component.text("§c" + normalized + " is not a " + slot.getId() + " slot rune"));
            return null;
        }
        String permissionKey = "rune." + normalized;
        if (!player.hasPermission(permissionKey)) {
            player.sendMessage(Component.text("§cYou don't have permission to select " + normalized + "."));
            return null;
        }
        return rune;
    }

    private boolean handleRunesClear(Player player, String[] args) {
        runePersistence.clearAllRunes(player.getUniqueId());
        runeManager.clearPlayerRunes(player);
        player.sendMessage(Component.text("§a✓ All runes cleared"));
        return true;
    }

    private boolean handleRunesInfo(Player player) {
        dev.ixpu.leaguemechanics.player.PlayerRuneData data = runeManager.getPlayerRuneData(player);
        if (data == null) {
            player.sendMessage(Component.text("§cNo runes loaded. Try rejoining."));
            return true;
        }

        String primary = data.getPrimaryPath() != null ? data.getPrimaryPath().getId() : "§7none";
        String secondary = data.getSecondaryPath() != null ? data.getSecondaryPath().getId() : "§7none";
        String keystone = data.getKeystoneRune() != null ? data.getKeystoneRune().getId() : "§7none";
        String p1 = data.getPrimarySlot1Rune() != null ? data.getPrimarySlot1Rune().getId() : "§7none";
        String p2 = data.getPrimarySlot2Rune() != null ? data.getPrimarySlot2Rune().getId() : "§7none";
        String p3 = data.getPrimarySlot3Rune() != null ? data.getPrimarySlot3Rune().getId() : "§7none";
        String s1 = data.getSecondarySlot1Rune() != null ? data.getSecondarySlot1Rune().getId() : "§7none";
        String s2 = data.getSecondarySlot2Rune() != null ? data.getSecondarySlot2Rune().getId() : "§7none";

        player.sendMessage(Component.text("§6ᴍʏ ᴀᴄᴛɪᴠᴇ ʀᴜɴᴇꜱ:"));
        player.sendMessage(Component.text("§7  Primary:   §e" + primary + " §7— keystone: §e" + keystone));
        player.sendMessage(Component.text("§7   slot 1: §e" + p1 + " §7| slot 2: §e" + p2 + " §7| slot 3: §e" + p3));
        player.sendMessage(Component.text("§7  Secondary: §e" + secondary));
        player.sendMessage(Component.text("§7   slot 1: §e" + s1 + " §7| slot 2: §e" + s2));
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
