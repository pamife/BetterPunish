package me.pamife.punishPlugin;

import org.bukkit.configuration.ConfigurationSection;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

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

        // 2. Check Banned Words & Regex
        List<String> bannedWords = filterConfig.getStringList("banned-words");
        for (String banned : bannedWords) {

            // REGEX CHECK: Wenn der Config-Eintrag mit "regex:" beginnt
            if (banned.toLowerCase().startsWith("regex:")) {
                String regexPattern = banned.substring(6).trim(); // Schneidet "regex:" ab
                try {
                    // Compiliert den Regex (Case Insensitive)
                    Pattern pattern = Pattern.compile(regexPattern, Pattern.CASE_INSENSITIVE);
                    if (pattern.matcher(message).find()) {
                        return "Regex-Match (" + regexPattern + ")"; // Gibt Regex-Pattern als Grund zurück
                    }
                } catch (PatternSyntaxException e) {
                    plugin.getLogger().warning("Ungültiger Regex im Chat-Filter gefunden: " + regexPattern);
                }
            }
            // STANDARD CHECK: Normales Wort
            else {
                if (lowerMsg.contains(banned.toLowerCase())) {
                    return banned;
                }
            }
        }

        return null; // Nachricht ist sauber
    }
}