package me.pamife.punishPlugin;

import org.bukkit.configuration.ConfigurationSection;
import java.util.List;

public class FilterManager {

    private final PunishPlugin plugin;

    public FilterManager(PunishPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Prüft die Nachricht. Gibt das gefundene verbotene Wort zurück, oder null, wenn sie sauber ist.
     */
    public String getCaughtWord(String message) {
        ConfigurationSection filterConfig = plugin.getConfig().getConfigurationSection("chat-filter");
        if (filterConfig == null || !filterConfig.getBoolean("enabled", false)) {
            return null; // Filter ist komplett aus
        }

        String lowerMsg = message.toLowerCase();

        // 1. Check Allow-List (Wenn ein Wort hier drin ist, wird es ignoriert)
        List<String> allowedWords = filterConfig.getStringList("allowed-words");
        for (String allowed : allowedWords) {
            if (lowerMsg.contains(allowed.toLowerCase())) {
                // Entferne das erlaubte Wort temporär aus der Prüfung
                lowerMsg = lowerMsg.replace(allowed.toLowerCase(), "");
            }
        }

        // 2. Check Banned Words aus der Config
        List<String> bannedWords = filterConfig.getStringList("banned-words");
        for (String banned : bannedWords) {
            if (lowerMsg.contains(banned.toLowerCase())) {
                return banned;
            }
        }

        return null; // Nachricht ist sauber
    }
}