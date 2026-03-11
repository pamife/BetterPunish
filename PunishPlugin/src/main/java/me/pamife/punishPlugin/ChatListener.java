package me.pamife.punishPlugin;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener {

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        DataManager dataManager = PunishPlugin.getInstance().getDataManager();

        if (dataManager.isMuted(player.getUniqueId())) {
            event.setCancelled(true);
            boolean de = dataManager.getLanguage(player.getUniqueId()).equals("de");
            player.sendMessage(de ? "§cDu bist momentan aus dem Chat ausgeschlossen (Gemutet)!"
                    : "§cYou are currently muted and cannot use the chat!");
        }
    }
}