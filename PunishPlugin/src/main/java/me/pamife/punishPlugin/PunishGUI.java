package me.pamife.punishPlugin;
import org.bukkit.Bukkit;
import org.bukkit.BanList;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Date;

public class PunishGUI implements Listener {

    // Methode zum Erstellen und Öffnen des Inventars
    public static void openGUI(Player moderator, Player target) {
        // Der Name des Spielers wird im Titel gespeichert, um ihn später auszulesen
        Inventory inv = Bukkit.createInventory(null, 9, "§cStrafe: " + target.getName());

        inv.setItem(2, createItem(Material.DIAMOND_SWORD, "§cHacking", "§7Ban für 30 Tage"));
        inv.setItem(4, createItem(Material.PAPER, "§eChat-Vergehen", "§7Ban für 1 Tag"));
        inv.setItem(6, createItem(Material.TNT, "§4Griefing", "§7Ban für 7 Tage"));

        moderator.openInventory(inv);
    }

    // Hilfsmethode, um Items einfacher zu erstellen
    private static ItemStack createItem(Material mat, String name, String lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(Arrays.asList(lore));
            item.setItemMeta(meta);
        }
        return item;
    }

    // Wartet auf Klicks in Inventaren
    @EventHandler
    public void onClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();

        // Prüfen, ob es unser Straf-Menü ist
        if (!title.startsWith("§cStrafe: ")) return;

        // Verhindert, dass der Moderator die Items ins eigene Inventar zieht
        event.setCancelled(true);

        if (event.getCurrentItem() == null) return;

        Player moderator = (Player) event.getWhoClicked();
        // Zielspieler-Namen aus dem Titel extrahieren
        String targetName = title.replace("§cStrafe: ", "");
        Player target = Bukkit.getPlayer(targetName);

        if (target == null) {
            moderator.sendMessage("§cDer Spieler ist in der Zwischenzeit offline gegangen.");
            moderator.closeInventory();
            return;
        }

        Material clickedType = event.getCurrentItem().getType();
        Instant expiry = null;
        String reason = "";

        // Logik für die einzelnen Strafen
        if (clickedType == Material.DIAMOND_SWORD) {
            reason = "Hacking";
            expiry = Instant.now().plus(30, ChronoUnit.DAYS);
        } else if (clickedType == Material.PAPER) {
            reason = "Beleidigung / Chat-Vergehen";
            expiry = Instant.now().plus(1, ChronoUnit.DAYS);
        } else if (clickedType == Material.TNT) {
            reason = "Griefing";
            expiry = Instant.now().plus(7, ChronoUnit.DAYS);
        }

        // Wenn eine gültige Strafe ausgewählt wurde
        if (expiry != null) {
            // Trägt den Ban in die Server-Banliste ein
            Bukkit.getBanList(BanList.Type.NAME).addBan(target.getName(), reason, Date.from(expiry), moderator.getName());

            // Wirft den Spieler sofort vom Server
            target.kickPlayer("§cDu wurdest vom Server gebannt!\n§7Grund: " + reason);

            moderator.sendMessage("§aDu hast " + target.getName() + " erfolgreich für " + reason + " bestraft.");
            moderator.closeInventory();
        }
    }
}