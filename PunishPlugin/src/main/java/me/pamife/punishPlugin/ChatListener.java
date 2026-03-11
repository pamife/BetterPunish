package me.pamife.punishPlugin;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener {

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        DataManager dm = PunishPlugin.getInstance().getDataManager();

        if (dm.isMuted(player.getUniqueId())) {
            event.setCancelled(true);
            String lang = dm.getLanguage(player.getUniqueId());
            player.sendMessage(dm.getMessage("mute-message", lang));
        }
    }
}