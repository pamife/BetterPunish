package me.pamife.punishPlugin;

import org.bukkit.BanEntry;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.ban.ProfileBanList;
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

public class ActivePunishmentsGUI implements Listener {

    public static void openGUI(Player admin) {
        DataManager dm = PunishPlugin.getInstance().getDataManager();
        boolean de = dm.getLanguage(admin.getUniqueId()).equals("de");

        String title = de ? "§8Aktive Strafen" : "§8Active Punishments";
        // Erstellt ein Menü mit 54 Slots (Große Kiste)
        Inventory inv = Bukkit.createInventory(null, 54, title);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        int slot = 0;

        // 1. Aktive Bans laden
        ProfileBanList banList = Bukkit.getBanList(BanList.Type.PROFILE);

        // FIX: Wir nutzen ein Wildcard (BanEntry<?>), damit es sowohl mit Spigot als auch mit Paper funktioniert!
        for (BanEntry<?> entry : banList.getEntries()) {
            if (slot >= 54) break; // Inventar voll

            ItemStack item = new ItemStack(Material.BARRIER);
            ItemMeta meta = item.getItemMeta();

            // Profil sicher umwandeln und Namen abfragen
            org.bukkit.profile.PlayerProfile profile = (org.bukkit.profile.PlayerProfile) entry.getBanTarget();
            String targetName = profile.getName();
            if (targetName == null) targetName = "Unknown";

            meta.setDisplayName("§c" + targetName);
            List<String> lore = new ArrayList<>();
            lore.add("§8§m------------------------");
            lore.add(de ? "§7Typ: §cBAN" : "§7Type: §cBAN");
            lore.add(de ? "§7Grund: §f" + entry.getReason() : "§7Reason: §f" + entry.getReason());
            lore.add(de ? "§7Von: §f" + entry.getSource() : "§7By: §f" + entry.getSource());

            Date exp = entry.getExpiration();
            String expires = (exp == null) ? "Permanent" : sdf.format(exp);
            lore.add(de ? "§7Ablauf: §f" + expires : "§7Expires: §f" + expires);
            lore.add("§8§m------------------------");
            lore.add(de ? "§eKlicke zum Entbannen" : "§eClick to Unban");

            meta.setLore(lore);
            item.setItemMeta(meta);
            inv.setItem(slot++, item);
        }

        // 2. Aktive Mutes laden
        for (UUID uuid : dm.getActiveMutes()) {
            if (slot >= 54) break;

            OfflinePlayer target = Bukkit.getOfflinePlayer(uuid);
            String targetName = target.getName() != null ? target.getName() : uuid.toString();
            long expiryMillis = dm.getMuteExpiry(uuid);

            ItemStack item = new ItemStack(Material.PAPER);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName("§e" + targetName);

            List<String> lore = new ArrayList<>();
            lore.add("§8§m------------------------");
            lore.add(de ? "§7Typ: §eMUTE" : "§7Type: §eMUTE");

            String expires = sdf.format(new Date(expiryMillis));
            lore.add(de ? "§7Ablauf: §f" + expires : "§7Expires: §f" + expires);
            lore.add("§8§m------------------------");
            lore.add(de ? "§eKlicke zum Entmuten" : "§eClick to Unmute");

            meta.setLore(lore);
            item.setItemMeta(meta);
            inv.setItem(slot++, item);
        }

        admin.openInventory(inv);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        if (!title.equals("§8Aktive Strafen") && !title.equals("§8Active Punishments")) return;

        event.setCancelled(true);
        if (event.getCurrentItem() == null) return;

        Player admin = (Player) event.getWhoClicked();
        String targetName = ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName());

        if (targetName == null || targetName.isEmpty()) return;

        admin.closeInventory();

        // Führt den Unpunish-Befehl aus der Sicht des Admins aus
        admin.performCommand("unpunish " + targetName);

        // Öffnet das Menü nach einem winzigen Delay neu, damit die Liste aktualisiert ist
        Bukkit.getScheduler().runTaskLater(PunishPlugin.getInstance(), () -> openGUI(admin), 5L);
    }
}