package me.pamife.punishPlugin;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class WarnCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        DataManager dm = PunishPlugin.getInstance().getDataManager();
        String lang = (sender instanceof Player) ? dm.getLanguage(((Player) sender).getUniqueId()) : dm.getConfigString("default-language");

        if (!sender.hasPermission("punish.use")) {
            sender.sendMessage(dm.getMessage("no-permission", lang));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(dm.getMessage("usage-warn", lang));
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        if (!target.hasPlayedBefore() && !target.isOnline()) {
            sender.sendMessage(dm.getMessage("player-not-found", lang));
            return true;
        }

        // Grund zusammenbauen
        StringBuilder reasonBuilder = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            reasonBuilder.append(args[i]).append(" ");
        }
        String reason = reasonBuilder.toString().trim();

        // Warnung hinzufügen
        dm.addWarn(target.getUniqueId());
        int warnCount = dm.getWarnCount(target.getUniqueId());

        dm.addHistory(target.getUniqueId(), "§eWarn (" + warnCount + "/3): §7" + reason + " by " + sender.getName());

        // Nachricht an den Ausführenden
        String successMsg = dm.getMessage("warn-success", lang)
                .replace("%player%", target.getName())
                .replace("%reason%", reason)
                .replace("%count%", String.valueOf(warnCount));
        sender.sendMessage(successMsg);

        // Nachricht an den Ziel-Spieler (falls online)
        if (target.isOnline() && target.getPlayer() != null) {
            String targetLang = dm.getLanguage(target.getUniqueId());
            String notifyMsg = dm.getMessage("warn-notify", targetLang)
                    .replace("%reason%", reason)
                    .replace("%count%", String.valueOf(warnCount));
            target.getPlayer().sendMessage(notifyMsg);
        }

        // AUTO-MUTE CHECK
        if (warnCount >= 3) {
            // Mute für 1 Stunde
            Instant expiry = Instant.now().plus(1, ChronoUnit.HOURS);
            dm.setMute(target.getUniqueId(), expiry.toEpochMilli());

            // Warnungen zurücksetzen
            dm.resetWarns(target.getUniqueId());
            dm.addHistory(target.getUniqueId(), "§cAuto-Mute: §7Reached 3 warnings (1 Hour)");

            // Mod benachrichtigen
            String modMsg = dm.getMessage("auto-mute-mod", lang).replace("%player%", target.getName());
            sender.sendMessage(modMsg);

            // Spieler benachrichtigen
            if (target.isOnline() && target.getPlayer() != null) {
                String targetLang = dm.getLanguage(target.getUniqueId());
                target.getPlayer().sendMessage(dm.getMessage("auto-mute-notify", targetLang));
            }

            PunishPlugin.getInstance().getPunishLogger().logBan("SYSTEM", target.getName(), "3 Warnings Reached", "1h");
        }

        return true;
    }
}