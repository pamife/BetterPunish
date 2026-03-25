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
        DataManager dm = PunishPlugin.getInstance().getDataManager();
        String lang = (sender instanceof Player) ? dm.getLanguage(((Player) sender).getUniqueId()) : dm.getConfigString("default-language");

        if (!sender.hasPermission("punish.history")) {
            sender.sendMessage(dm.getMessage("no-permission", lang));
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(dm.getMessage("usage-history", lang));
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);

        // WENN DER SENDER EIN SPIELER IST -> GUI ÖFFNEN
        if (sender instanceof Player) {
            Player admin = (Player) sender;
            HistoryGUI.openGUI(admin, target);
            return true;
        }

        // FALLBACK FÜR DIE KONSOLE (Textausgabe)
        List<String> history = dm.getHistory(target.getUniqueId());

        if (history.isEmpty()) {
            sender.sendMessage(dm.getMessage("history-clean", lang));
            return true;
        }

        sender.sendMessage("§8§m--------------------------------");
        sender.sendMessage(dm.getMessage("history-header", lang).replace("%player%", target.getName()));
        for (String entry : history) {
            sender.sendMessage("§8- " + entry);
        }
        sender.sendMessage("§8§m--------------------------------");
        return true;
    }
}