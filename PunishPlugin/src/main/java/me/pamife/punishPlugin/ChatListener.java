package me.pamife.punishPlugin;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ChatListener implements Listener {

    // Speicher für Anti-Spam & Flood-Control
    private final Map<UUID, Long> lastMessageTime = new HashMap<>();
    private final Map<UUID, String> lastMessage = new HashMap<>();

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        PunishPlugin plugin = PunishPlugin.getInstance();
        DataManager dm = plugin.getDataManager();
        UUID uuid = player.getUniqueId();
        String lang = dm.getLanguage(uuid);

        if (dm.isMuted(uuid)) {
            event.setCancelled(true);
            player.sendMessage(dm.getMessage("mute-message", lang));
            return;
        }

        // Admins mit Permission umgehen den Filter komplett
        if (player.hasPermission("punish.bypassfilter")) return;

        String message = event.getMessage();

        // 1. FLOOD CONTROL (Verhindert zu schnelles Schreiben)
        long cooldownMs = plugin.getConfig().getLong("chat-filter.flood-cooldown", 1500); // Standard: 1.5 Sekunden
        long now = System.currentTimeMillis();

        if (lastMessageTime.containsKey(uuid)) {
            if (now - lastMessageTime.get(uuid) < cooldownMs) {
                event.setCancelled(true);
                player.sendMessage(dm.getMessage("filter-flood", lang)); // Neue Nachricht in Config nötig!
                return;
            }
        }
        lastMessageTime.put(uuid, now);

        // 2. ANTI-SPAM (Verhindert das Wiederholen exakt derselben Nachricht)
        boolean blockSpam = plugin.getConfig().getBoolean("chat-filter.anti-spam", true);
        if (blockSpam && lastMessage.containsKey(uuid)) {
            if (message.equalsIgnoreCase(lastMessage.get(uuid))) {
                event.setCancelled(true);
                player.sendMessage(dm.getMessage("filter-spam", lang)); // Neue Nachricht in Config nötig!
                return;
            }
        }
        lastMessage.put(uuid, message);

        // 3. CAPS DETECTION (Blockiert Nachrichten mit zu vielen Großbuchstaben)
        boolean blockCaps = plugin.getConfig().getBoolean("chat-filter.caps-detection", true);
        if (blockCaps && message.length() > 5) {
            long upperCaseCount = message.chars().filter(Character::isUpperCase).count();
            double upperPercentage = (double) upperCaseCount / message.length();

            if (upperPercentage > 0.6) { // Wenn mehr als 60% aus Caps bestehen
                event.setCancelled(true);
                player.sendMessage(dm.getMessage("filter-caps", lang)); // Neue Nachricht in Config nötig!

                // ALTERNATIVE (Falls du es lieber in Kleinbuchstaben umwandeln willst statt zu blocken):
                // event.setMessage(message.toLowerCase());
                return;
            }
        }

        // 4. WORT & REGEX FILTER
        String caughtWord = plugin.getFilterManager().getCaughtWord(message);

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

    // Löscht die Spieler aus dem Speicher, wenn sie den Server verlassen (verhindert Memory Leaks)
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        lastMessageTime.remove(uuid);
        lastMessage.remove(uuid);
    }
}