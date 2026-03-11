package me.pamife.punishPlugin;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LanguageCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("punish.use")) {
            player.sendMessage("§cYou do not have permission to do this!");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage("§cUsage: /punishlang <en|de>");
            return true;
        }

        String lang = args[0].toLowerCase();

        if (lang.equals("de") || lang.equals("en")) {
            PunishPlugin.getInstance().getDataManager().setLanguage(player.getUniqueId(), lang);
            if (lang.equals("de")) {
                player.sendMessage("§aSprache erfolgreich auf Deutsch gesetzt!");
            } else {
                player.sendMessage("§aLanguage successfully set to English!");
            }
        } else {
            player.sendMessage("§cAvailable languages: en, de");
        }

        return true;
    }
}