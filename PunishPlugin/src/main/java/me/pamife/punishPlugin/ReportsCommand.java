package me.pamife.punishPlugin;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ReportsCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can open the reports GUI.");
            return true;
        }

        Player admin = (Player) sender;
        DataManager dm = PunishPlugin.getInstance().getDataManager();
        String lang = dm.getLanguage(admin.getUniqueId());

        if (!admin.hasPermission("punish.staff")) {
            admin.sendMessage(dm.getMessage("no-permission", lang));
            return true;
        }

        ReportsGUI.openGUI(admin);
        return true;
    }
}