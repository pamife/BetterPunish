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
        boolean de = false;
        if (sender instanceof Player) {
            de = PunishPlugin.getInstance().getDataManager().getLanguage(((Player) sender).getUniqueId()).equals("de");
        }

        if (!sender.hasPermission("punish.unpunish")) {
            sender.sendMessage(de ? "§cDu hast keine Rechte dafür!" : "§cYou do not have permission to do this!");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(de ? "§cVerwendung: /unpunish <Spieler>" : "§cUsage: /unpunish <Player>");
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        DataManager data = PunishPlugin.getInstance().getDataManager();
        boolean changed = false;

        ProfileBanList banList = Bukkit.getBanList(BanList.Type.PROFILE);
        if (banList.isBanned(target.getPlayerProfile())) {
            banList.pardon(target.getPlayerProfile());
            sender.sendMessage(de ? "§aDer Ban von " + target.getName() + " wurde aufgehoben." : "§aThe ban for " + target.getName() + " has been lifted.");
            data.addHistory(target.getUniqueId(), (de ? "§aEntbannt §7von " : "§aUnbanned §7by ") + sender.getName());
            changed = true;
        }

        if (data.isMuted(target.getUniqueId())) {
            data.removeMute(target.getUniqueId());
            sender.sendMessage(de ? "§aDer Mute von " + target.getName() + " wurde aufgehoben." : "§aThe mute for " + target.getName() + " has been lifted.");
            data.addHistory(target.getUniqueId(), (de ? "§aEntmutet §7von " : "§aUnmuted §7by ") + sender.getName());
            changed = true;
        }

        if (!changed) {
            sender.sendMessage(de ? "§cDieser Spieler hat aktuell keine aktive Strafe." : "§cThis player currently has no active punishments.");
        }

        return true;
    }
}