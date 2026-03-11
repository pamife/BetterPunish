package me.pamife.punishPlugin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PunishLogger {

    private final File logFile;

    public PunishLogger(PunishPlugin plugin) {
        logFile = new File(plugin.getDataFolder(), "punish_log.txt");
        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void logBan(String moderator, String target, String reason, String duration) {
        try (FileWriter fw = new FileWriter(logFile, true);
             PrintWriter pw = new PrintWriter(fw)) {

            String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            pw.println("[" + date + "] " + target + " was punished by " + moderator + ". Reason: " + reason + " | Duration: " + duration);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}