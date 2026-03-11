package me.pamife.punishPlugin;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class DataManager {

    private final PunishPlugin plugin;
    private File dataFile;
    private FileConfiguration dataConfig;
    private FileConfiguration config;

    public DataManager(PunishPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
        setupData();
    }

    private void setupData() {
        dataFile = new File(plugin.getDataFolder(), "data.yml");
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
    }

    public void saveData() {
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // --- CONFIG SYSTEM ---
    public String getMessage(String path, String lang) {
        String prefix = config.getString("prefix", "&8[&6⚖ BetterPunish&8] &7");
        String message = config.getString("messages." + path + "." + lang);
        if (message == null) {
            message = config.getString("messages." + path + ".en", "Missing string: " + path);
        }
        return ChatColor.translateAlternateColorCodes('&', prefix + message);
    }

    public String getConfigString(String path) {
        return ChatColor.translateAlternateColorCodes('&', config.getString(path, ""));
    }

    public ConfigurationSection getGuiItems() {
        return config.getConfigurationSection("gui-items");
    }

    // --- TIME PARSER ---
    public Instant parseDuration(String input) {
        if (input == null || input.isEmpty()) return null;
        char unit = input.charAt(input.length() - 1);
        int amount;
        try {
            amount = Integer.parseInt(input.substring(0, input.length() - 1));
        } catch (NumberFormatException e) {
            return null;
        }

        Instant now = Instant.now();
        switch (unit) {
            case 's': return now.plus(amount, ChronoUnit.SECONDS);
            case 'm': return now.plus(amount, ChronoUnit.MINUTES);
            case 'h': return now.plus(amount, ChronoUnit.HOURS);
            case 'd': return now.plus(amount, ChronoUnit.DAYS);
            case 'w': return now.plus(amount * 7, ChronoUnit.DAYS);
            default: return null;
        }
    }

    // --- SPRACH SYSTEM ---
    public void setLanguage(UUID uuid, String lang) {
        dataConfig.set("Language." + uuid.toString(), lang);
        saveData();
    }

    public String getLanguage(UUID uuid) {
        return dataConfig.getString("Language." + uuid.toString(), config.getString("default-language", "en"));
    }

    // --- MUTE SYSTEM ---
    public void setMute(UUID uuid, long expiryMillis) {
        dataConfig.set("Mutes." + uuid.toString(), expiryMillis);
        saveData();
    }

    public void removeMute(UUID uuid) {
        dataConfig.set("Mutes." + uuid.toString(), null);
        saveData();
    }

    public boolean isMuted(UUID uuid) {
        if (!dataConfig.contains("Mutes." + uuid.toString())) return false;
        long expiry = dataConfig.getLong("Mutes." + uuid.toString());
        if (System.currentTimeMillis() > expiry) {
            removeMute(uuid);
            return false;
        }
        return true;
    }

    // --- VERGEHEN & HISTORY ---
    public int getOffenseCount(UUID uuid, String reason) {
        return dataConfig.getInt("Offenses." + uuid.toString() + "." + reason, 0);
    }

    public void addOffense(UUID uuid, String reason) {
        dataConfig.set("Offenses." + uuid.toString() + "." + reason, getOffenseCount(uuid, reason) + 1);
        saveData();
    }

    public void addHistory(UUID uuid, String logEntry) {
        List<String> history = dataConfig.getStringList("History." + uuid.toString());
        String date = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date());
        history.add("§8[" + date + "] §r" + logEntry);
        dataConfig.set("History." + uuid.toString(), history);
        saveData();
    }

    public List<String> getHistory(UUID uuid) {
        return dataConfig.getStringList("History." + uuid.toString());
    }
}