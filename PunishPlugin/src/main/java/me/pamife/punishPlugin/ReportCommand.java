package me.pamife.punishPlugin;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ReportCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can report.");
            return true;
        }

        Player player = (Player) sender;
        DataManager dm = PunishPlugin.getInstance().getDataManager();
        String lang = dm.getLanguage(player.getUniqueId());

        if (!player.hasPermission("punish.report")) {
            player.sendMessage(dm.getMessage("no-permission", lang));
            return true;
        }

        if (args.length < 2) {
            player.sendMessage(dm.getMessage("usage-report", lang));
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        if (!target.hasPlayedBefore() && !target.isOnline()) {
            player.sendMessage(dm.getMessage("player-not-found", lang));
            return true;
        }

        if (target.getUniqueId().equals(player.getUniqueId())) {
            player.sendMessage(dm.getMessage("report-self", lang));
            return true;
        }

        StringBuilder reasonBuilder = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            reasonBuilder.append(args[i]).append(" ");
        }
        String reason = reasonBuilder.toString().trim();

        // Report in die Daten eintragen
        dm.addReport(player.getUniqueId(), target.getUniqueId(), reason);

        player.sendMessage(dm.getMessage("report-success", lang).replace("%player%", target.getName()));

        // Team benachrichtigen
        dm.broadcastStaffMessage("staff-notify-report", target.getName(), player.getName(), reason);

        return true;
    }
}