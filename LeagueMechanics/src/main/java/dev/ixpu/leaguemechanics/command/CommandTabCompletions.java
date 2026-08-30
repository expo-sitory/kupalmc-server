package dev.ixpu.leaguemechanics.command;

import dev.ixpu.leaguemechanics.player.PlayerClassType;
import dev.ixpu.leaguemechanics.rune.CooldownHandler;
import dev.ixpu.leaguemechanics.rune.RunePath;
import dev.ixpu.leaguemechanics.rune.RuneRegistry;
import dev.ixpu.leaguemechanics.rune.RuneSlot;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CommandTabCompletions implements org.bukkit.command.TabCompleter {
    private final RuneRegistry runeRegistry;

    public CommandTabCompletions(RuneRegistry runeRegistry) {
        this.runeRegistry = runeRegistry;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        if (!(sender instanceof Player player)) {
            return new ArrayList<>();
        }

        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            if (player.hasPermission("leaguemechanics.admin")) {
                completions.add("reload");
                completions.add("shop");
                completions.add("runes");
            }
            completions.add("class");
            return filter(completions, args[0]);
        }

        if (args.length >= 2 && args[0].equalsIgnoreCase("class")) {
            List<String> classCompletions = new ArrayList<>(List.of(PlayerClassType.getAllIds()));
            classCompletions.add("clear");
            return filter(classCompletions, args[1]);
        }

        if (args.length >= 2 && args[0].equalsIgnoreCase("runes")) {
            if (args.length == 2) {
                completions.add("select");
                return filter(completions, args[1]);
            }

            if (args[1].equalsIgnoreCase("select")) {
                if (args.length == 3) {
                    completions.add("primary");
                    completions.add("secondary");
                    return filter(completions, args[2]);
                }

                if (args.length == 4) {
                    String location = args[2].toLowerCase();
                    if (location.equals("primary")) {
                        completions.add("keystone");
                        completions.add("slot-1");
                        completions.add("slot-2");
                        completions.add("slot-3");
                    } else if (location.equals("secondary")) {
                        completions.add("slot-1s");
                        completions.add("slot-2s");
                    }
                    return filter(completions, args[3]);
                }

                if (args.length == 5) {
                    completions.add("domination");
                    completions.add("precision");
                    completions.add("inspiration");
                    completions.add("resolve");
                    completions.add("sorcery");
                    return filter(completions, args[4]);
                }

                if (args.length == 6) {
                    String pathName = args[4].toLowerCase();
                    RunePath path = RunePath.fromId(pathName);
                    RuneSlot slot = resolveSlot(args[2].toLowerCase(), args[3].toLowerCase());
                    if (path != null && slot != null) {
                        completions.addAll(getRunesByPathAndSlot(path, slot));
                    }
                    return filter(completions, args[5]);
                }
            }
        }

        return new ArrayList<>();
    }

    private List<String> getKeystonesByPath(RunePath path) {
        return runeRegistry.getAllRunes().values().stream()
                .filter(rune -> rune.getPath().equals(path) && rune.getSlot().equals(RuneSlot.KEYSTONE))
                .map(CooldownHandler::getId)
                .collect(Collectors.toList());
    }

    private List<String> getRunesByPathAndSlot(RunePath path, RuneSlot slot) {
        return runeRegistry.getAllRunes().values().stream()
                .filter(rune -> rune.getPath().equals(path) && rune.getSlot().equals(slot))
                .map(CooldownHandler::getId)
                .collect(Collectors.toList());
    }

    private RuneSlot resolveSlot(String location, String slotArg) {
        boolean isPrimary = location.equals("primary");
        return switch (slotArg) {
            case "keystone" -> isPrimary ? RuneSlot.KEYSTONE : null;
            case "slot-1", "primary-slot-1" -> isPrimary ? RuneSlot.PRIMARY_SLOT_1 : null;
            case "slot-2", "primary-slot-2" -> isPrimary ? RuneSlot.PRIMARY_SLOT_2 : null;
            case "slot-3", "primary-slot-3" -> isPrimary ? RuneSlot.PRIMARY_SLOT_3 : null;
            case "slot-1s", "secondary-slot-1", "secondary-slot-one" -> !isPrimary ? RuneSlot.SECONDARY_SLOT_1 : null;
            case "slot-2s", "secondary-slot-2", "secondary-slot-two" -> !isPrimary ? RuneSlot.SECONDARY_SLOT_2 : null;
            default -> null;
        };
    }

    private List<String> filter(List<String> suggestions, String input) {
        return suggestions.stream()
                .filter(s -> s.toLowerCase().startsWith(input.toLowerCase()))
                .collect(Collectors.toList());
    }
}