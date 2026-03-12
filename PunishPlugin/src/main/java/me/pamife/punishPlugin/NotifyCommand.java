package me.pamife.punishPlugin;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class NotifyCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;
        DataManager dm = PunishPlugin.getInstance().getDataManager();
        String lang = dm.getLanguage(player.getUniqueId());

        if (!player.hasPermission("punish.staff")) {
            player.sendMessage(dm.getMessage("no-permission", lang));
            return true;
        }

        // Status umkehren (Toggle)
        boolean currentState = dm.isNotifyEnabled(player.getUniqueId());
        boolean newState = !currentState;

        dm.setNotifyEnabled(player.getUniqueId(), newState);

        if (newState) {
            player.sendMessage(dm.getMessage("notify-enabled", lang));
        } else {
            player.sendMessage(dm.getMessage("notify-disabled", lang));
        }

        return true;
    }
}