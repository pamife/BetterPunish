package me.pamife.punishPlugin;

import org.bukkit.plugin.java.JavaPlugin;

public class PunishPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        // Registriert den Listener für Klicks im Inventar
        getServer().getPluginManager().registerEvents(new PunishGUI(), this);

        // Registriert den /punish Befehl
        getCommand("punish").setExecutor(new PunishCommand());

        getLogger().info("Punish-Plugin wurde aktiviert!");
    }
}
