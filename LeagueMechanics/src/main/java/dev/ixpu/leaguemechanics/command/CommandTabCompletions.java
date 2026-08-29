package dev.ixpu.leaguemechanics.command;

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
            return filter(completions, args[0]);
        }

        if (args.length >= 2 && args[0].equalsIgnoreCase("runes")) {
            if (args.length == 2) {
                completions.add("select");
                return filter(completions, args[1]);
            }

            if (args[1].equalsIgnoreCase("select")) {
                if (args.length == 3) {
                    completions.add("primary");
                    return filter(completions, args[2]);
                }

                if (args.length == 4) {
                    completions.add("domination");
                    completions.add("precision");
                    completions.add("inspiration");
                    completions.add("resolve");
                    completions.add("sorcery");
                    return filter(completions, args[3]);
                }

                if (args.length == 5) {
                    String pathName = args[3].toLowerCase();
                    RunePath path = RunePath.fromId(pathName);
                    if (path != null) {
                        completions.addAll(getKeystonesByPath(path));
                    }
                    return filter(completions, args[4]);
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

    private List<String> filter(List<String> suggestions, String input) {
        return suggestions.stream()
                .filter(s -> s.toLowerCase().startsWith(input.toLowerCase()))
                .collect(Collectors.toList());
    }
}