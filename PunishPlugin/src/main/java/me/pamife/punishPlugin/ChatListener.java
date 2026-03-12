package me.pamife.punishPlugin;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener {

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        PunishPlugin plugin = PunishPlugin.getInstance();
        DataManager dm = plugin.getDataManager();
        String lang = dm.getLanguage(player.getUniqueId());

        // 1. Mute Check
        if (dm.isMuted(player.getUniqueId())) {
            event.setCancelled(true);
            player.sendMessage(dm.getMessage("mute-message", lang));
            return;
        }

        // 2. Chat Filter Check (Überspringt Mods mit bypassfilter Permission)
        if (!player.hasPermission("punish.bypassfilter")) {
            String caughtWord = plugin.getFilterManager().getCaughtWord(event.getMessage());

            if (caughtWord != null) {
                event.setCancelled(true); // Blockiert die Nachricht komplett

                // Nachricht an den Spieler (aus der config.yml)
                player.sendMessage(dm.getMessage("filter-caught", lang));

                // Optionale Auto-Warnung ausführen, wenn in Config aktiviert
                if (plugin.getConfig().getBoolean("chat-filter.auto-warn", true)) {
                    // Wir führen den Warn-Befehl über die Konsole aus, um die Logik aus WarnCommand zu nutzen
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "warn " + player.getName() + " Chat-Filter: " + caughtWord);
                    });
                }
            }
        }
    }
}