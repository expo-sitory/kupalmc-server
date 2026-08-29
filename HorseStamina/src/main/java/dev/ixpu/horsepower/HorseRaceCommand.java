package dev.ixpu.horsepower;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;

public class HorseRaceCommand implements CommandExecutor {
  private final HorseRaceManager raceManager;

  public HorseRaceCommand(HorseRaceManager raceManager) {
    this.raceManager = raceManager;
  }

  @Override
  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    if (!(sender instanceof Player)) {
      sender.sendMessage("§d§lᴋᴜᴘᴀʟᴍᴄ §r§7- §fʜᴏʀꜱᴇ §7| §cPlayers only!");
      return true;
    }

    Player player = (Player) sender;
    if (!player.hasPermission("horsepower.race")) {
      player.sendMessage("§d§lᴋᴜᴘᴀʟᴍᴄ §r§7- §fʜᴏʀꜱᴇ §7| §cUnknown or incomplete command");
      return true;
    }

    if (!player.isInsideVehicle() || !(player.getVehicle() instanceof Horse)) {
      player.sendMessage("§d§lᴋᴜᴘᴀʟᴍᴄ §r§7- §fʜᴏʀꜱᴇ §7| §cYou must be riding a horse!");
      return true;
    }

    Horse horse = (Horse) player.getVehicle();
    if (!horse.isTamed() || !horse.getOwner().equals(player)) {
      player.sendMessage("§d§lᴋᴜᴘᴀʟᴍᴄ §r§7- §fʜᴏʀꜱᴇ §7| §cYou don't own this horse!");
      return true;
    }

    raceManager.startRace(player, horse);
    return true;
  }
}
