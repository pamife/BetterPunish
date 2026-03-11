package me.pamife.punishPlugin;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.List;

public class HistoryCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("punish.history")) {
            sender.sendMessage("§cYou do not have permission to do this!");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage("§cUsage: /history <Player>");
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        List<String> history = PunishPlugin.getInstance().getDataManager().getHistory(target.getUniqueId());

        if (history.isEmpty()) {
            sender.sendMessage("§aThis player has a clean record!");
            return true;
        }

        sender.sendMessage("§8§m--------------------------------");
        sender.sendMessage("§ePunishment History of §6" + target.getName());
        for (String entry : history) {
            sender.sendMessage("§8- " + entry);
        }
        sender.sendMessage("§8§m--------------------------------");
        return true;
    }
}