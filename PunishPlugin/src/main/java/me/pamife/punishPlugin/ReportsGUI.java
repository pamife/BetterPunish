package me.pamife.punishPlugin;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class ReportsGUI implements Listener {

    public static void openGUI(Player admin) {
        DataManager dm = PunishPlugin.getInstance().getDataManager();
        boolean de = dm.getLanguage(admin.getUniqueId()).equals("de");

        String title = de ? "§cOffene Reports" : "§cActive Reports";
        Inventory inv = Bukkit.createInventory(null, 54, title);

        List<DataManager.ReportEntry> reports = dm.getReports();
        int slot = 0;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        for (DataManager.ReportEntry report : reports) {
            if (slot >= 54) break;

            OfflinePlayer reporter = Bukkit.getOfflinePlayer(UUID.fromString(report.reporter));
            OfflinePlayer target = Bukkit.getOfflinePlayer(UUID.fromString(report.target));

            String targetName = target.getName() != null ? target.getName() : "Unknown";
            String reporterName = reporter.getName() != null ? reporter.getName() : "Unknown";

            ItemStack item = new ItemStack(Material.BOOK);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName("§cReport: §e" + targetName);

            List<String> lore = new ArrayList<>();
            lore.add("§8§m------------------------");
            lore.add(de ? "§7Gemeldet von: §a" + reporterName : "§7Reported by: §a" + reporterName);
            lore.add(de ? "§7Grund: §f" + report.reason : "§7Reason: §f" + report.reason);
            lore.add(de ? "§7Datum: §8" + sdf.format(new Date(report.time)) : "§7Date: §8" + sdf.format(new Date(report.time)));
            lore.add("§8§m------------------------");
            lore.add(de ? "§eLinksklick §8» §7Zum Spieler TP" : "§eLeft-Click §8» §7TP to Player");
            lore.add(de ? "§eShift-Klick §8» §7Punish-Menü öffnen" : "§eShift-Click §8» §7Open Punish Menu");
            lore.add(de ? "§cRechtsklick §8» §7Report löschen" : "§cRight-Click §8» §7Delete Report");

            lore.add("§0" + report.id);

            meta.setLore(lore);
            item.setItemMeta(meta);
            inv.setItem(slot++, item);
        }

        admin.openInventory(inv);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        if (!title.equals("§cOffene Reports") && !title.equals("§cActive Reports")) return;

        event.setCancelled(true);
        if (event.getCurrentItem() == null || event.getCurrentItem().getType() != Material.BOOK) return;

        Player admin = (Player) event.getWhoClicked();
        ItemMeta meta = event.getCurrentItem().getItemMeta();
        if (meta == null || meta.getLore() == null) return;

        String reportIdZeile = meta.getLore().get(meta.getLore().size() - 1);
        String reportId = reportIdZeile.replace("§0", "");
        String targetName = meta.getDisplayName().replace("§cReport: §e", "");
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);

        DataManager dm = PunishPlugin.getInstance().getDataManager();
        boolean de = dm.getLanguage(admin.getUniqueId()).equals("de");

        if (event.isRightClick()) {
            dm.deleteReport(reportId);
            admin.sendMessage(de ? "§aReport gelöscht." : "§aReport deleted.");
            openGUI(admin);
        } else if (event.isShiftClick()) {
            admin.closeInventory();
            admin.performCommand("punish " + targetName);
        } else if (event.isLeftClick()) {
            if (target.isOnline() && target.getPlayer() != null) {
                admin.teleport(target.getPlayer().getLocation());
                admin.sendMessage(de ? "§aDu wurdest zu " + targetName + " teleportiert." : "§aYou were teleported to " + targetName + ".");
            } else {
                admin.sendMessage(de ? "§cDer Spieler ist offline!" : "§cThe player is offline!");
            }
        }
    }
}