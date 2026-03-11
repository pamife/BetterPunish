package me.pamife.punishPlugin;

import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.ban.ProfileBanList;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

public class PunishCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("punish.use")) {
            sender.sendMessage("§cYou do not have permission to do this!");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage("§cUsage:");
            sender.sendMessage("§cGUI: /punish <Player>");
            sender.sendMessage("§cCustom: /punish <Player> <Time (e.g., 5d, 12h)> <Reason>");
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        if (!target.hasPlayedBefore() && !target.isOnline()) {
            sender.sendMessage("§cThis player has never been on the server.");
            return true;
        }

        if (args.length == 1) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("The GUI can only be opened by players.");
                return true;
            }
            PunishGUI.openGUI((Player) sender, target);
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage("§cPlease provide a reason. Example: /punish Player 5d Hacking");
            return true;
        }

        String timeInput = args[1].toLowerCase();
        Instant expiry = parseDuration(timeInput);

        if (expiry == null) {
            sender.sendMessage("§cInvalid time format! Use s(sec), m(min), h(hours), d(days), w(weeks). Ex: 5d");
            return true;
        }

        StringBuilder reasonBuilder = new StringBuilder();
        for (int i = 2; i < args.length; i++) {
            reasonBuilder.append(args[i]).append(" ");
        }
        String reason = reasonBuilder.toString().trim();

        ProfileBanList banList = Bukkit.getBanList(BanList.Type.PROFILE);
        banList.addBan(target.getPlayerProfile(), reason, Date.from(expiry), sender.getName());

        if (target.isOnline() && target.getPlayer() != null) {
            target.getPlayer().kickPlayer("§cYou have been banned from the server!\n§7Reason: " + reason + "\n§7Duration: " + timeInput);
        }

        PunishPlugin.getInstance().getPunishLogger().logBan(sender.getName(), target.getName(), reason, timeInput);
        PunishPlugin.getInstance().getDataManager().addHistory(target.getUniqueId(), "§cBan: §7" + reason + " (" + timeInput + ") by " + sender.getName());

        sender.sendMessage("§aYou have successfully punished " + target.getName() + " for " + timeInput + ". (Reason: " + reason + ")");
        return true;
    }

    private Instant parseDuration(String input) {
        if (input == null || input.isEmpty()) return null;
        char unit = input.charAt(input.length() - 1);
        int amount;
        try {
            amount = Integer.parseInt(input.substring(0, input.length() - 1));
        } catch (NumberFormatException e) {
            return null;
        }
        Instant now = Instant.now();
        switch (unit) {
            case 's': return now.plus(amount, ChronoUnit.SECONDS);
            case 'm': return now.plus(amount, ChronoUnit.MINUTES);
            case 'h': return now.plus(amount, ChronoUnit.HOURS);
            case 'd': return now.plus(amount, ChronoUnit.DAYS);
            case 'w': return now.plus(amount * 7, ChronoUnit.DAYS);
            default: return null;
        }
    }
}