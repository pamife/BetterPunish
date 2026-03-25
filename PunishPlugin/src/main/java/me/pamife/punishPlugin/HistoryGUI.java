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

import java.util.*;

public class HistoryGUI implements Listener {

    public enum HistoryCategory { ALL, BANS, MUTES, WARNS }

    private static final Map<UUID, Integer> pageMap = new HashMap<>();
    private static final Map<UUID, HistoryCategory> categoryMap = new HashMap<>();
    private static final Map<UUID, UUID> targetMap = new HashMap<>();

    public static void openGUI(Player admin, OfflinePlayer target) {
        UUID adminId = admin.getUniqueId();
        DataManager dm = PunishPlugin.getInstance().getDataManager();
        boolean de = dm.getLanguage(adminId).equals("de");

        // Zustand laden
        targetMap.put(adminId, target.getUniqueId());
        int page = pageMap.getOrDefault(adminId, 0);
        HistoryCategory category = categoryMap.getOrDefault(adminId, HistoryCategory.ALL);

        String title = (de ? "§8Verlauf: " : "§8History: ") + target.getName() + " §7(" + (page + 1) + ")";
        Inventory inv = Bukkit.createInventory(null, 54, title);

        // Historie laden und umdrehen (Neueste zuerst)
        List<String> rawHistory = dm.getHistory(target.getUniqueId());
        Collections.reverse(rawHistory);

        // Liste filtern
        List<String> filteredHistory = new ArrayList<>();
        for (String entry : rawHistory) {
            String lower = entry.toLowerCase();
            if (category == HistoryCategory.ALL) {
                filteredHistory.add(entry);
            } else if (category == HistoryCategory.BANS && (lower.contains("ban:") || lower.contains("unbanned"))) {
                filteredHistory.add(entry);
            } else if (category == HistoryCategory.MUTES && (lower.contains("mute:") || lower.contains("unmuted"))) {
                filteredHistory.add(entry);
            } else if (category == HistoryCategory.WARNS && lower.contains("warn")) {
                filteredHistory.add(entry);
            }
        }

        // Pagination berechnen (Maximal 45 Items pro Seite)
        int maxItemsPerPage = 45;
        int totalPages = (int) Math.ceil((double) filteredHistory.size() / maxItemsPerPage);
        if (totalPages == 0) totalPages = 1;
        if (page >= totalPages) page = totalPages - 1;

        int startIndex = page * maxItemsPerPage;
        int endIndex = Math.min(startIndex + maxItemsPerPage, filteredHistory.size());

        // Items ins GUI setzen
        int slot = 0;
        for (int i = startIndex; i < endIndex; i++) {
            String entry = filteredHistory.get(i);
            Material icon = Material.PAPER;
            String itemName = "§7Eintrag";

            // Typ-Erkennung anhand des Textes
            if (entry.contains("Ban:") || entry.contains("Auto-Ban")) {
                icon = Material.BARRIER;
                itemName = de ? "§cBan" : "§cBan";
            } else if (entry.contains("Mute:") || entry.contains("Auto-Mute")) {
                icon = Material.NAME_TAG;
                itemName = de ? "§eMute" : "§eMute";
            } else if (entry.contains("Warn")) {
                icon = Material.YELLOW_DYE;
                itemName = de ? "§6Warnung" : "§6Warning";
            } else if (entry.contains("Unbanned") || entry.contains("Unmuted") || entry.contains("Unbanned")) {
                icon = Material.EMERALD;
                itemName = de ? "§aStrafe aufgehoben" : "§aPunishment Pardoned";
            }

            ItemStack item = new ItemStack(icon);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(itemName);

            // Text am "]" aufspalten, um Datum von Grund zu trennen
            String[] parts = entry.split("] §r", 2);
            List<String> lore = new ArrayList<>();
            if (parts.length == 2) {
                lore.add("§8" + parts[0].replace("§8[", "").trim()); // Datum
                lore.add("");

                // Langen Text sauber umbrechen (simpler Ansatz)
                String info = parts[1];
                lore.add(info);
            } else {
                lore.add(entry); // Fallback
            }

            meta.setLore(lore);
            item.setItemMeta(meta);
            inv.setItem(slot++, item);
        }

        // --- STEUERUNGS-MENÜ (Bodenleiste) ---
        ItemStack glass = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glass.getItemMeta();
        glassMeta.setDisplayName(" ");
        glass.setItemMeta(glassMeta);
        for (int i = 45; i < 54; i++) {
            inv.setItem(i, glass);
        }

        // Vorherige Seite (Slot 45)
        if (page > 0) {
            ItemStack prevItem = new ItemStack(Material.ARROW);
            ItemMeta prevMeta = prevItem.getItemMeta();
            prevMeta.setDisplayName(de ? "§aVorherige Seite" : "§aPrevious Page");
            prevItem.setItemMeta(prevMeta);
            inv.setItem(45, prevItem);
        }

        // Kategorie-Filter (Slot 49)
        ItemStack filterItem = new ItemStack(Material.HOPPER);
        ItemMeta filterMeta = filterItem.getItemMeta();
        filterMeta.setDisplayName(de ? "§6Filter: Kategorie" : "§6Filter: Category");
        filterMeta.setLore(Arrays.asList(
                de ? "§7Aktuell: §a" + category.name() : "§7Current: §a" + category.name(),
                "",
                de ? "§eKlicke zum Ändern" : "§eClick to change filter"
        ));
        filterItem.setItemMeta(filterMeta);
        inv.setItem(49, filterItem);

        // Nächste Seite (Slot 53)
        if (page < totalPages - 1) {
            ItemStack nextItem = new ItemStack(Material.ARROW);
            ItemMeta nextMeta = nextItem.getItemMeta();
            nextMeta.setDisplayName(de ? "§aNächste Seite" : "§aNext Page");
            nextItem.setItemMeta(nextMeta);
            inv.setItem(53, nextItem);
        }

        admin.openInventory(inv);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        if (!title.startsWith("§8Verlauf: ") && !title.startsWith("§8History: ")) return;

        event.setCancelled(true);
        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.BLACK_STAINED_GLASS_PANE) return;

        Player admin = (Player) event.getWhoClicked();
        UUID adminId = admin.getUniqueId();
        int slot = event.getRawSlot();

        UUID targetUuid = targetMap.get(adminId);
        if (targetUuid == null) return;
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetUuid);

        int currentPage = pageMap.getOrDefault(adminId, 0);

        if (slot == 45 && event.getCurrentItem().getType() == Material.ARROW) { // Zurück
            pageMap.put(adminId, Math.max(0, currentPage - 1));
            openGUI(admin, target);
        } else if (slot == 53 && event.getCurrentItem().getType() == Material.ARROW) { // Vor
            pageMap.put(adminId, currentPage + 1);
            openGUI(admin, target);
        } else if (slot == 49) { // Filter ändern
            HistoryCategory current = categoryMap.getOrDefault(adminId, HistoryCategory.ALL);
            HistoryCategory next = HistoryCategory.ALL;
            switch (current) {
                case ALL: next = HistoryCategory.BANS; break;
                case BANS: next = HistoryCategory.MUTES; break;
                case MUTES: next = HistoryCategory.WARNS; break;
                case WARNS: next = HistoryCategory.ALL; break;
            }
            categoryMap.put(adminId, next);
            pageMap.put(adminId, 0); // Bei Filterwechsel zurück auf Seite 1
            openGUI(admin, target);
        }
    }
}