package dev.ixpu.horsepower;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class HorsePowerReloadCommand implements CommandExecutor {
  private final HorsePowerPlugin plugin;

  public HorsePowerReloadCommand(HorsePowerPlugin plugin) {
    this.plugin = plugin;
  }

  @Override
  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    if (!sender.isOp()) {
      sender.sendMessage("§d§lᴋᴜᴘᴀʟᴍᴄ §r§7- §fʜᴏʀꜱᴇ §7| §cYou must be an operator to use this command!");
      return true;
    }

    if (args.length == 0 || !args[0].equalsIgnoreCase("reload")) {
      sender.sendMessage("§d§lᴋᴜᴘᴀʟᴍᴄ §r§7- §fʜᴏʀꜱᴇ §7| §6Usage: /horsestamina reload");
      return true;
    }

    try {
      plugin.reloadConfig();
      sender.sendMessage("§d§lᴋᴜᴘᴀʟᴍᴄ §r§7- §fʜᴏʀꜱᴇ §7| §a✓ HorseStamina config reloaded successfully!");
    } catch (Exception e) {
      sender.sendMessage("§c✗ Failed to reload config: " + e.getMessage());
      plugin.getLogger().severe(() -> "Failed to reload config: " + e.getMessage());
    }

    return true;
  }
}
