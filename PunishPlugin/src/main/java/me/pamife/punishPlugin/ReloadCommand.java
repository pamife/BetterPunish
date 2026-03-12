package me.pamife.punishPlugin;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ReloadCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        DataManager dm = PunishPlugin.getInstance().getDataManager();
        String lang = (sender instanceof Player) ? dm.getLanguage(((Player) sender).getUniqueId()) : dm.getConfigString("default-language");

        if (!sender.hasPermission("punish.admin")) {
            sender.sendMessage(dm.getMessage("no-permission", lang));
            return true;
        }

        // Lade config.yml und data.yml neu!
        dm.reloadConfigs();

        // Hole die Nachricht neu (damit sie garantiert aus der neuen Config kommt)
        sender.sendMessage(dm.getMessage("reload-success", lang));

        return true;
    }
}