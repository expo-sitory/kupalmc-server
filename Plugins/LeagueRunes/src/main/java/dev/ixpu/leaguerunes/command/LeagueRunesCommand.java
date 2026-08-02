package dev.ixpu.leaguerunes.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import dev.ixpu.leaguerunes.LeagueRunes;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class LeagueRunesCommand implements CommandExecutor {
    private final LeagueRunes plugin;

    public LeagueRunesCommand(LeagueRunes plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(Component.text("Usage: /" + label + " reload", NamedTextColor.RED));
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("leaguerunes.admin")) {
                sender.sendMessage(Component.text("You do not have permission to use this command.", NamedTextColor.RED));
                return true;
            }

            try {
                plugin.reloadConfig();
                sender.sendMessage(Component.text("LeagueRunes config reloaded successfully!", NamedTextColor.GREEN));
                plugin.getLogger().info(() -> "Config reloaded by " + sender.getName());
                return true;
            } catch (Exception e) {
                sender.sendMessage(Component.text("Failed to reload config: " + e.getMessage(), NamedTextColor.RED));
                plugin.getLogger().severe(() -> "Failed to reload config: " + e.getMessage());
                return true;
            }
        }

        sender.sendMessage(Component.text("Unknown subcommand. Usage: /" + label + " reload", NamedTextColor.RED));
        return true;
    }
}