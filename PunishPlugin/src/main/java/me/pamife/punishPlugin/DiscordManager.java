package me.pamife.punishPlugin;

import org.bukkit.Bukkit;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class DiscordManager {

    private final PunishPlugin plugin;

    public DiscordManager(PunishPlugin plugin) {
        this.plugin = plugin;
    }

    public void sendWebhook(String action, String player, String moderator, String reason) {
        String webhookUrl = plugin.getConfig().getString("discord.webhook-url");

        // Abbruch, wenn kein Webhook konfiguriert ist
        if (webhookUrl == null || webhookUrl.isEmpty() || webhookUrl.contains("DEIN_WEBHOOK_LINK_HIER")) {
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                URL url = new URL(webhookUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("User-Agent", "BetterPunish");
                connection.setDoOutput(true);

                // Farbe basierend auf der Aktion (Hex zu Dezimal)
                int color = 16753920; // Orange als Standard
                if (action.toLowerCase().contains("punish") || action.toLowerCase().contains("ban")) color = 16711680; // Rot
                if (action.toLowerCase().contains("report")) color = 65280; // Grün
                if (action.toLowerCase().contains("filter")) color = 16776960; // Gelb

                // json payload zusammenbauen (sehr simpel gehalten)
                String json = "{"
                        + "\"username\": \"BetterPunish System\","
                        + "\"embeds\": [{"
                        + "\"title\": \"🚨 System Notification\","
                        + "\"description\": \"**Action:** " + action + "\\n**Player:** " + player + "\\n**Moderator:** " + moderator + "\\n**Reason/Info:** " + reason.replace("\"", "\\\"") + "\","
                        + "\"color\": " + color
                        + "}]"
                        + "}";

                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = json.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                connection.getResponseCode(); // Request absenden
            } catch (Exception e) {
                plugin.getLogger().warning("Fehler beim Senden des Discord Webhooks: " + e.getMessage());
            }
        });
    }
}