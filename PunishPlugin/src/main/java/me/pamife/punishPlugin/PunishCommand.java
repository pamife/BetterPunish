package me.pamife.punishPlugin;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PunishCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Nur echte Spieler können ein Menü öffnen, nicht die Konsole
        if (!(sender instanceof Player)) {
            sender.sendMessage("Dieser Befehl ist nur für Spieler.");
            return true;
        }

        Player moderator = (Player) sender;

        if (!moderator.hasPermission("punish.use")) {
            moderator.sendMessage("§cDu hast keine Rechte dafür!");
            return true;
        }

        if (args.length != 1) {
            moderator.sendMessage("§cVerwendung: /punish <Spieler>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            moderator.sendMessage("§cDieser Spieler ist nicht online.");
            return true;
        }

        // Öffnet das Menü für den Moderator
        PunishGUI.openGUI(moderator, target);
        return true;
    }
}