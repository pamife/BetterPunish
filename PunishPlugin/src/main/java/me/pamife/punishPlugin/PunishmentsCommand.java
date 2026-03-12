package me.pamife.punishPlugin;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PunishmentsCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;
        DataManager dm = PunishPlugin.getInstance().getDataManager();
        String lang = dm.getLanguage(player.getUniqueId());

        if (!player.hasPermission("punish.admin")) {
            player.sendMessage(dm.getMessage("no-permission", lang));
            return true;
        }

        // Öffne das neue GUI
        ActivePunishmentsGUI.openGUI(player);
        return true;
    }
}