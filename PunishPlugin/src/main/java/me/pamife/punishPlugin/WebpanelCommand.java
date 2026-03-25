package me.pamife.punishPlugin;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class WebpanelCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        Player player = (Player) sender;

        if (!player.hasPermission("punish.admin")) {
            player.sendMessage("§cKeine Rechte.");
            return true;
        }

        String url = PunishPlugin.getInstance().getApiManager().getPanelURL();
        player.sendMessage("§8§m--------------------------------");
        player.sendMessage("§6§lBetterPunish Webpanel");
        player.sendMessage("§7Klicke hier, um das Dashboard zu öffnen:");
        player.sendMessage("§b" + url);
        player.sendMessage("§8§m--------------------------------");

        return true;
    }
}