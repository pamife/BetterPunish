package me.pamife.punishPlugin;

import org.bukkit.plugin.java.JavaPlugin;

public class PunishPlugin extends JavaPlugin {

    private static PunishPlugin instance;
    private PunishLogger punishLogger;
    private DataManager dataManager;
    private FilterManager filterManager;

    @Override
    public void onEnable() {
        instance = this;

        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        saveDefaultConfig();
        dataManager = new DataManager(this);
        punishLogger = new PunishLogger(this);
        filterManager = new FilterManager(this);

        getServer().getPluginManager().registerEvents(new PunishGUI(), this);
        getServer().getPluginManager().registerEvents(new ChatListener(), this);

        getCommand("punish").setExecutor(new PunishCommand());
        getCommand("history").setExecutor(new HistoryCommand());
        getCommand("unpunish").setExecutor(new UnpunishCommand());
        getCommand("punishlang").setExecutor(new LanguageCommand());
        getCommand("warn").setExecutor(new WarnCommand());

        // NEUE BEFEHLE:
        getCommand("punishreload").setExecutor(new ReloadCommand());
        getCommand("punishnotify").setExecutor(new NotifyCommand());

        getLogger().info("BetterPunish plugin has been enabled!");
    }

    public static PunishPlugin getInstance() {
        return instance;
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
}