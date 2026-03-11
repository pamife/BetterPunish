package me.pamife.punishPlugin;

import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.ban.ProfileBanList;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class UnpunishCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("punish.unpunish")) {
            sender.sendMessage("§cYou do not have permission to do this!");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage("§cUsage: /unpunish <Player>");
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        DataManager data = PunishPlugin.getInstance().getDataManager();
        boolean changed = false;

        ProfileBanList banList = Bukkit.getBanList(BanList.Type.PROFILE);
        if (banList.isBanned(target.getPlayerProfile())) {
            banList.pardon(target.getPlayerProfile());
            sender.sendMessage("§aThe ban for " + target.getName() + " has been lifted.");
            data.addHistory(target.getUniqueId(), "§aUnbanned §7by " + sender.getName());
            changed = true;
        }

        if (data.isMuted(target.getUniqueId())) {
            data.removeMute(target.getUniqueId());
            sender.sendMessage("§aThe mute for " + target.getName() + " has been lifted.");
            data.addHistory(target.getUniqueId(), "§aUnmuted §7by " + sender.getName());
            changed = true;
        }

        if (!changed) {
            sender.sendMessage("§cThis player currently has no active punishments.");
        }

        return true;
    }
}