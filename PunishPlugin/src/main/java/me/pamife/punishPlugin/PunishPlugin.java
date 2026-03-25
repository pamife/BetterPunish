package me.pamife.punishPlugin;

import org.bukkit.plugin.java.JavaPlugin;

public class PunishPlugin extends JavaPlugin {

    private static PunishPlugin instance;
    private PunishLogger punishLogger;
    private DataManager dataManager;
    private FilterManager filterManager;
    private DiscordManager discordManager;
    private DatabaseManager databaseManager;
    private APIManager apiManager; // NEU

    @Override
    public void onEnable() {
        instance = this;

        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        saveDefaultConfig();

        databaseManager = new DatabaseManager(this);
        databaseManager.connect();

        discordManager = new DiscordManager(this);
        dataManager = new DataManager(this);
        punishLogger = new PunishLogger(this);
        filterManager = new FilterManager(this);

        // API Starten
        apiManager = new APIManager(this);
        apiManager.start();

        getServer().getPluginManager().registerEvents(new PunishGUI(), this);
        getServer().getPluginManager().registerEvents(new ChatListener(), this);
        getServer().getPluginManager().registerEvents(new ActivePunishmentsGUI(), this);
        getServer().getPluginManager().registerEvents(new HistoryGUI(), this);
        getServer().getPluginManager().registerEvents(new ReportsGUI(), this);

        getCommand("punish").setExecutor(new PunishCommand());
        getCommand("history").setExecutor(new HistoryCommand());
        getCommand("unpunish").setExecutor(new UnpunishCommand());
        getCommand("punishlang").setExecutor(new LanguageCommand());
        getCommand("warn").setExecutor(new WarnCommand());
        getCommand("punishreload").setExecutor(new ReloadCommand());
        getCommand("punishnotify").setExecutor(new NotifyCommand());
        getCommand("punishments").setExecutor(new PunishmentsCommand());
        getCommand("report").setExecutor(new ReportCommand());
        getCommand("reports").setExecutor(new ReportsCommand());
        getCommand("webpanel").setExecutor(new WebpanelCommand());

        getLogger().info("BetterPunish plugin has been enabled!");
    }

    @Override
    public void onDisable() {
        if (databaseManager != null) {
            databaseManager.disconnect();
        }
        if (apiManager != null) {
            apiManager.stop(); // API sicher herunterfahren
        }
    }

    public static PunishPlugin getInstance() {
        return instance;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public PunishLogger getPunishLogger() {
        return punishLogger;
    }

    public DataManager getDataManager() {
        return dataManager;
    }

    public FilterManager getFilterManager() {
        return filterManager;
    }

    public DiscordManager getDiscordManager() {
        return discordManager;
    }

    public APIManager getApiManager() {
        return apiManager;
    }
}