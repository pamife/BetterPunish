package me.pamife.punishPlugin;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    private Connection connection;
    private final PunishPlugin plugin;
    private final boolean isMySQL;

    public DatabaseManager(PunishPlugin plugin) {
        this.plugin = plugin;
        // Liest aus, welches System genutzt werden soll (Standard: sqlite)
        this.isMySQL = plugin.getConfig().getString("storage.type", "sqlite").equalsIgnoreCase("mysql");
    }

    public void connect() {
        try {
            if (connection != null && !connection.isClosed()) return;
            synchronized (this) {
                if (connection != null && !connection.isClosed()) return;

                if (isMySQL) {
                    // --- MYSQL VERBINDUNG ---
                    String host = plugin.getConfig().getString("storage.mysql.host", "localhost");
                    int port = plugin.getConfig().getInt("storage.mysql.port", 3306);
                    String database = plugin.getConfig().getString("storage.mysql.database", "betterpunish");
                    String username = plugin.getConfig().getString("storage.mysql.username", "root");
                    String password = plugin.getConfig().getString("storage.mysql.password", "");

                    connection = DriverManager.getConnection(
                            "jdbc:mysql://" + host + ":" + port + "/" + database + "?autoReconnect=true&useSSL=false",
                            username, password
                    );
                    plugin.getLogger().info("✅ MySQL erfolgreich verbunden!");
                } else {
                    // --- SQLITE VERBINDUNG (Lokale Datei) ---
                    try {
                        Class.forName("org.sqlite.JDBC"); // Lädt den SQLite Treiber explizit
                    } catch (ClassNotFoundException ignored) {}

                    File dbFile = new File(plugin.getDataFolder(), "database.db");
                    connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
                    plugin.getLogger().info("✅ SQLite (lokale Datenbank) erfolgreich geladen! Keine Zugangsdaten nötig.");
                }

                createTables();
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("❌ Datenbank-Verbindungsfehler!");
            e.printStackTrace();
        }
    }

    public void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                plugin.getLogger().info("Datenbank-Verbindung getrennt.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connect();
            }
        } catch (SQLException e) {
            connect();
        }
        return connection;
    }

    private void createTables() {
        try (Statement st = getConnection().createStatement()) {
            st.executeUpdate("CREATE TABLE IF NOT EXISTS punish_settings (uuid VARCHAR(36) PRIMARY KEY, language VARCHAR(10), notify BOOLEAN)");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS punish_mutes (uuid VARCHAR(36) PRIMARY KEY, expiry BIGINT)");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS punish_warns (uuid VARCHAR(36) PRIMARY KEY, warn_count INT)");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS punish_offenses (uuid VARCHAR(36), reason VARCHAR(100), offense_count INT, PRIMARY KEY(uuid, reason))");

            // SQLite und MySQL haben leicht unterschiedliche Schreibweisen für Auto-Increment
            String autoInc = isMySQL ? "INT AUTO_INCREMENT PRIMARY KEY" : "INTEGER PRIMARY KEY AUTOINCREMENT";
            st.executeUpdate("CREATE TABLE IF NOT EXISTS punish_history (id " + autoInc + ", uuid VARCHAR(36), entry TEXT)");

            st.executeUpdate("CREATE TABLE IF NOT EXISTS punish_reports (id VARCHAR(8) PRIMARY KEY, reporter VARCHAR(36), target VARCHAR(36), reason TEXT, time BIGINT)");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}