package me.pamife.punishPlugin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class DataManager {

    private final PunishPlugin plugin;
    private FileConfiguration config;
    private final DatabaseManager db;

    public DataManager(PunishPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
        this.db = plugin.getDatabaseManager();
    }

    public void reloadConfigs() {
        plugin.reloadConfig();
        this.config = plugin.getConfig();
    }

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

    // --- SETTINGS (Language & Notify) ---
    public void setLanguage(UUID uuid, String lang) {
        boolean currentNotify = isNotifyEnabled(uuid);
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (PreparedStatement ps = db.getConnection().prepareStatement("REPLACE INTO punish_settings (uuid, language, notify) VALUES (?, ?, ?)")) {
                ps.setString(1, uuid.toString());
                ps.setString(2, lang);
                ps.setBoolean(3, currentNotify);
                ps.executeUpdate();
            } catch (SQLException e) { e.printStackTrace(); }
        });
    }

    public String getLanguage(UUID uuid) {
        try (PreparedStatement ps = db.getConnection().prepareStatement("SELECT language FROM punish_settings WHERE uuid = ?")) {
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString("language");
        } catch (SQLException e) { e.printStackTrace(); }
        return config.getString("default-language", "en");
    }

    public boolean isNotifyEnabled(UUID uuid) {
        try (PreparedStatement ps = db.getConnection().prepareStatement("SELECT notify FROM punish_settings WHERE uuid = ?")) {
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getBoolean("notify");
        } catch (SQLException e) { e.printStackTrace(); }
        return true;
    }

    public void setNotifyEnabled(UUID uuid, boolean enabled) {
        String currentLang = getLanguage(uuid);
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (PreparedStatement ps = db.getConnection().prepareStatement("REPLACE INTO punish_settings (uuid, language, notify) VALUES (?, ?, ?)")) {
                ps.setString(1, uuid.toString());
                ps.setString(2, currentLang);
                ps.setBoolean(3, enabled);
                ps.executeUpdate();
            } catch (SQLException e) { e.printStackTrace(); }
        });
    }

    public void broadcastStaffMessage(String path, String target, String moderator, String reasonOrWord) {
        plugin.getDiscordManager().sendWebhook(path, target, moderator, reasonOrWord);
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.hasPermission("punish.staff") && isNotifyEnabled(p.getUniqueId())) {
                String lang = getLanguage(p.getUniqueId());
                String msg = getMessage(path, lang).replace("%player%", target).replace("%moderator%", moderator).replace("%reason%", reasonOrWord).replace("%word%", reasonOrWord);
                p.sendMessage(msg);
            }
        }
    }

    // --- MUTE SYSTEM ---
    public void setMute(UUID uuid, long expiryMillis) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (PreparedStatement ps = db.getConnection().prepareStatement("REPLACE INTO punish_mutes (uuid, expiry) VALUES (?, ?)")) {
                ps.setString(1, uuid.toString());
                ps.setLong(2, expiryMillis);
                ps.executeUpdate();
            } catch (SQLException e) { e.printStackTrace(); }
        });
    }

    public void removeMute(UUID uuid) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (PreparedStatement ps = db.getConnection().prepareStatement("DELETE FROM punish_mutes WHERE uuid = ?")) {
                ps.setString(1, uuid.toString());
                ps.executeUpdate();
            } catch (SQLException e) { e.printStackTrace(); }
        });
    }

    public boolean isMuted(UUID uuid) {
        long expiry = getMuteExpiry(uuid);
        return expiry > System.currentTimeMillis();
    }

    public long getMuteExpiry(UUID uuid) {
        try (PreparedStatement ps = db.getConnection().prepareStatement("SELECT expiry FROM punish_mutes WHERE uuid = ?")) {
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getLong("expiry");
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public List<UUID> getActiveMutes() {
        List<UUID> mutes = new ArrayList<>();
        long now = System.currentTimeMillis();
        try (PreparedStatement ps = db.getConnection().prepareStatement("SELECT uuid FROM punish_mutes WHERE expiry > ?")) {
            ps.setLong(1, now);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) mutes.add(UUID.fromString(rs.getString("uuid")));
        } catch (SQLException e) { e.printStackTrace(); }
        return mutes;
    }

    public List<UUID> getAllMutes() {
        List<UUID> mutes = new ArrayList<>();
        try (PreparedStatement ps = db.getConnection().prepareStatement("SELECT uuid FROM punish_mutes")) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) mutes.add(UUID.fromString(rs.getString("uuid")));
        } catch (SQLException e) { e.printStackTrace(); }
        return mutes;
    }

    // --- WARN SYSTEM ---
    public int getWarnCount(UUID uuid) {
        try (PreparedStatement ps = db.getConnection().prepareStatement("SELECT warn_count FROM punish_warns WHERE uuid = ?")) {
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("warn_count");
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public void addWarn(UUID uuid) {
        int newCount = getWarnCount(uuid) + 1;
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (PreparedStatement ps = db.getConnection().prepareStatement("REPLACE INTO punish_warns (uuid, warn_count) VALUES (?, ?)")) {
                ps.setString(1, uuid.toString());
                ps.setInt(2, newCount);
                ps.executeUpdate();
            } catch (SQLException e) { e.printStackTrace(); }
        });
    }

    public void resetWarns(UUID uuid) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (PreparedStatement ps = db.getConnection().prepareStatement("DELETE FROM punish_warns WHERE uuid = ?")) {
                ps.setString(1, uuid.toString());
                ps.executeUpdate();
            } catch (SQLException e) { e.printStackTrace(); }
        });
    }

    // --- VERGEHEN & HISTORY ---
    public int getOffenseCount(UUID uuid, String reason) {
        try (PreparedStatement ps = db.getConnection().prepareStatement("SELECT offense_count FROM punish_offenses WHERE uuid = ? AND reason = ?")) {
            ps.setString(1, uuid.toString());
            ps.setString(2, reason);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("offense_count");
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public void addOffense(UUID uuid, String reason) {
        int newCount = getOffenseCount(uuid, reason) + 1;
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (PreparedStatement ps = db.getConnection().prepareStatement("REPLACE INTO punish_offenses (uuid, reason, offense_count) VALUES (?, ?, ?)")) {
                ps.setString(1, uuid.toString());
                ps.setString(2, reason);
                ps.setInt(3, newCount);
                ps.executeUpdate();
            } catch (SQLException e) { e.printStackTrace(); }
        });
    }

    public void addHistory(UUID uuid, String logEntry) {
        String date = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date());
        String finalEntry = "§8[" + date + "] §r" + logEntry;
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (PreparedStatement ps = db.getConnection().prepareStatement("INSERT INTO punish_history (uuid, entry) VALUES (?, ?)")) {
                ps.setString(1, uuid.toString());
                ps.setString(2, finalEntry);
                ps.executeUpdate();
            } catch (SQLException e) { e.printStackTrace(); }
        });
    }

    public List<String> getHistory(UUID uuid) {
        List<String> history = new ArrayList<>();
        try (PreparedStatement ps = db.getConnection().prepareStatement("SELECT entry FROM punish_history WHERE uuid = ? ORDER BY id ASC")) {
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) history.add(rs.getString("entry"));
        } catch (SQLException e) { e.printStackTrace(); }
        return history;
    }

    // --- REPORT SYSTEM ---
    public static class ReportEntry {
        public String id, reporter, target, reason;
        public long time;
    }

    public void addReport(UUID reporter, UUID target, String reason) {
        String id = UUID.randomUUID().toString().substring(0, 8);
        long time = System.currentTimeMillis();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (PreparedStatement ps = db.getConnection().prepareStatement("INSERT INTO punish_reports (id, reporter, target, reason, time) VALUES (?, ?, ?, ?, ?)")) {
                ps.setString(1, id);
                ps.setString(2, reporter.toString());
                ps.setString(3, target.toString());
                ps.setString(4, reason);
                ps.setLong(5, time);
                ps.executeUpdate();
            } catch (SQLException e) { e.printStackTrace(); }
        });
    }

    public List<ReportEntry> getReports() {
        List<ReportEntry> reports = new ArrayList<>();
        try (PreparedStatement ps = db.getConnection().prepareStatement("SELECT * FROM punish_reports ORDER BY time DESC")) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ReportEntry re = new ReportEntry();
                re.id = rs.getString("id");
                re.reporter = rs.getString("reporter");
                re.target = rs.getString("target");
                re.reason = rs.getString("reason");
                re.time = rs.getLong("time");
                reports.add(re);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return reports;
    }

    public void deleteReport(String id) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (PreparedStatement ps = db.getConnection().prepareStatement("DELETE FROM punish_reports WHERE id = ?")) {
                ps.setString(1, id);
                ps.executeUpdate();
            } catch (SQLException e) { e.printStackTrace(); }
        });
    }
}