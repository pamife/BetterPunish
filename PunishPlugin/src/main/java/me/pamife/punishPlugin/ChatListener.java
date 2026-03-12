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

        if (dm.isMuted(player.getUniqueId())) {
            event.setCancelled(true);
            player.sendMessage(dm.getMessage("mute-message", lang));
            return;
        }

        if (!player.hasPermission("punish.bypassfilter")) {
            String caughtWord = plugin.getFilterManager().getCaughtWord(event.getMessage());

            if (caughtWord != null) {
                event.setCancelled(true);

                player.sendMessage(dm.getMessage("filter-caught", lang));

                // NOTIFICATION AN DAS TEAM:
                dm.broadcastStaffMessage("staff-notify-filter", player.getName(), "System", caughtWord);

                if (plugin.getConfig().getBoolean("chat-filter.auto-warn", true)) {
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "warn " + player.getName() + " Chat-Filter: " + caughtWord);
                    });
                }
            }
        }
    }
}