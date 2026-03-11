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
        DataManager dm = PunishPlugin.getInstance().getDataManager();
        String currentLang = dm.getLanguage(player.getUniqueId());

        if (!player.hasPermission("punish.use")) {
            player.sendMessage(dm.getMessage("no-permission", currentLang));
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(dm.getMessage("usage-lang", currentLang));
            return true;
        }

        String lang = args[0].toLowerCase();

        if (lang.equals("de") || lang.equals("en")) {
            dm.setLanguage(player.getUniqueId(), lang);
            player.sendMessage(dm.getMessage("lang-success", lang));
        } else {
            player.sendMessage(dm.getMessage("lang-invalid", currentLang));
        }

        return true;
    }
}