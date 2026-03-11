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
        DataManager dm = PunishPlugin.getInstance().getDataManager();
        String lang = (sender instanceof Player) ? dm.getLanguage(((Player) sender).getUniqueId()) : dm.getConfigString("default-language");

        if (!sender.hasPermission("punish.use")) {
            sender.sendMessage(dm.getMessage("no-permission", lang));
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(dm.getMessage("usage-punish", lang));
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        if (!target.hasPlayedBefore() && !target.isOnline()) {
            sender.sendMessage(dm.getMessage("player-not-found", lang));
            return true;
        }

        if (args.length == 1) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(dm.getMessage("gui-only", lang));
                return true;
            }
            PunishGUI.openGUI((Player) sender, target);
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage(dm.getMessage("missing-reason", lang));
            return true;
        }

        String timeInput = args[1].toLowerCase();
        Instant expiry = parseDuration(timeInput);

        if (expiry == null) {
            sender.sendMessage(dm.getMessage("invalid-time", lang));
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
            String kickMsg = dm.getMessage("banned-kick", lang)
                    .replace("%reason%", reason)
                    .replace("%time%", timeInput)
                    .replace("%prefix%", ""); // Clean prefix for kick screen
            target.getPlayer().kickPlayer(kickMsg);
        }

        PunishPlugin.getInstance().getPunishLogger().logBan(sender.getName(), target.getName(), reason, timeInput);
        dm.addHistory(target.getUniqueId(), "§cBan: §7" + reason + " (" + timeInput + ") by " + sender.getName());

        String successMsg = dm.getMessage("punish-success", lang)
                .replace("%player%", target.getName())
                .replace("%time%", timeInput)
                .replace("%reason%", reason);
        sender.sendMessage(successMsg);

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