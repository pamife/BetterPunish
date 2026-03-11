package me.pamife.punishPlugin;

import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ChatListener implements Listener {

    @EventHandler
    public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        DataManager dataManager = PunishPlugin.getInstance().getDataManager();

        if (dataManager.isMuted(player.getUniqueId())) {
            event.setCancelled(true);
            player.sendMessage("§cYou are currently muted and cannot use the chat!");
        }
    }
}