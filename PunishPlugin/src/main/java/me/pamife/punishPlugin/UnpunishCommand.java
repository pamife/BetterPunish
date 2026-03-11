package me.pamife.punishPlugin;

import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.ban.ProfileBanList;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class UnpunishCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        DataManager dm = PunishPlugin.getInstance().getDataManager();
        String lang = (sender instanceof Player) ? dm.getLanguage(((Player) sender).getUniqueId()) : dm.getConfigString("default-language");

        if (!sender.hasPermission("punish.unpunish")) {
            sender.sendMessage(dm.getMessage("no-permission", lang));
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(dm.getMessage("usage-unpunish", lang));
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        boolean changed = false;

        ProfileBanList banList = Bukkit.getBanList(BanList.Type.PROFILE);
        if (banList.isBanned(target.getPlayerProfile())) {
            banList.pardon(target.getPlayerProfile());
            sender.sendMessage(dm.getMessage("unpunish-ban", lang).replace("%player%", target.getName()));
            dm.addHistory(target.getUniqueId(), "§aUnbanned §7by " + sender.getName());
            changed = true;
        }

        if (dm.isMuted(target.getUniqueId())) {
            dm.removeMute(target.getUniqueId());
            sender.sendMessage(dm.getMessage("unpunish-mute", lang).replace("%player%", target.getName()));
            dm.addHistory(target.getUniqueId(), "§aUnmuted §7by " + sender.getName());
            changed = true;
        }

        if (!changed) {
            sender.sendMessage(dm.getMessage("unpunish-none", lang));
        }

        return true;
    }
}