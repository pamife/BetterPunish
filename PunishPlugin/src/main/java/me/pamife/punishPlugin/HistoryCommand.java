package me.pamife.punishPlugin;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class HistoryCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        boolean de = false;
        if (sender instanceof Player) {
            de = PunishPlugin.getInstance().getDataManager().getLanguage(((Player) sender).getUniqueId()).equals("de");
        }

        if (!sender.hasPermission("punish.history")) {
            sender.sendMessage(de ? "§cDu hast keine Rechte dafür!" : "§cYou do not have permission to do this!");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(de ? "§cVerwendung: /history <Spieler>" : "§cUsage: /history <Player>");
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        List<String> history = PunishPlugin.getInstance().getDataManager().getHistory(target.getUniqueId());

        if (history.isEmpty()) {
            sender.sendMessage(de ? "§aDieser Spieler hat eine saubere Weste!" : "§aThis player has a clean record!");
            return true;
        }

        sender.sendMessage("§8§m--------------------------------");
        sender.sendMessage(de ? "§eStrafhistorie von §6" + target.getName() : "§ePunishment History of §6" + target.getName());
        for (String entry : history) {
            sender.sendMessage("§8- " + entry);
        }
        sender.sendMessage("§8§m--------------------------------");
        return true;
    }
}