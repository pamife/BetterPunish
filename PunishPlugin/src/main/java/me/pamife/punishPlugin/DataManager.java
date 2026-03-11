package me.pamife.punishPlugin;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class DataManager {

    private final PunishPlugin plugin;
    private File file;
    private FileConfiguration config;

    public DataManager(PunishPlugin plugin) {
        this.plugin = plugin;
        setup();
    }

    private void setup() {
        file = new File(plugin.getDataFolder(), "data.yml");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        config = YamlConfiguration.loadConfiguration(file);
    }

    private void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // --- SPRACH SYSTEM (NEU) ---
    public void setLanguage(UUID uuid, String lang) {
        config.set("Language." + uuid.toString(), lang);
        save();
    }

    public String getLanguage(UUID uuid) {
        // Standardmäßig Englisch ("en"), wenn noch nichts gesetzt wurde
        return config.getString("Language." + uuid.toString(), "en");
    }

    // --- MUTE SYSTEM ---
    public void setMute(UUID uuid, long expiryMillis) {
        config.set("Mutes." + uuid.toString(), expiryMillis);
        save();
    }

    public void removeMute(UUID uuid) {
        config.set("Mutes." + uuid.toString(), null);
        save();
    }

    public boolean isMuted(UUID uuid) {
        if (!config.contains("Mutes." + uuid.toString())) return false;
        long expiry = config.getLong("Mutes." + uuid.toString());
        if (System.currentTimeMillis() > expiry) {
            removeMute(uuid);
            return false;
        }
        return true;
    }

    // --- VERGEHEN (OFFENSES) ---
    public int getOffenseCount(UUID uuid, String reason) {
        return config.getInt("Offenses." + uuid.toString() + "." + reason, 0);
    }

    public void addOffense(UUID uuid, String reason) {
        int current = getOffenseCount(uuid, reason);
        config.set("Offenses." + uuid.toString() + "." + reason, current + 1);
        save();
    }

    // --- HISTORIE ---
    public void addHistory(UUID uuid, String logEntry) {
        List<String> history = config.getStringList("History." + uuid.toString());
        String date = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date());
        history.add("§8[" + date + "] §r" + logEntry);
        config.set("History." + uuid.toString(), history);
        save();
    }

    public List<String> getHistory(UUID uuid) {
        return config.getStringList("History." + uuid.toString());
    }
}