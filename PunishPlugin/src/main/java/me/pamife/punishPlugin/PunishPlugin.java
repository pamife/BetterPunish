package me.pamife.punishPlugin;

import org.bukkit.plugin.java.JavaPlugin;

public class PunishPlugin extends JavaPlugin {

    private static PunishPlugin instance;
    private PunishLogger punishLogger;
    private DataManager dataManager;

    @Override
    public void onEnable() {
        instance = this;

        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        // Lade config.yml (Nachrichten) und data.yml (Spielerdaten)
        saveDefaultConfig();
        dataManager = new DataManager(this);
        punishLogger = new PunishLogger(this);

        getServer().getPluginManager().registerEvents(new PunishGUI(), this);
        getServer().getPluginManager().registerEvents(new ChatListener(), this);

        getCommand("punish").setExecutor(new PunishCommand());
        getCommand("history").setExecutor(new HistoryCommand());
        getCommand("unpunish").setExecutor(new UnpunishCommand());
        getCommand("punishlang").setExecutor(new LanguageCommand());

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
}