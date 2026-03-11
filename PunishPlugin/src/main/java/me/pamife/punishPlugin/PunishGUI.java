package me.pamife.punishPlugin;

import org.bukkit.Bukkit;
import org.bukkit.BanList;
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
import org.bukkit.inventory.meta.SkullMeta;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Date;

public class PunishGUI implements Listener {

    public static void openGUI(Player moderator, OfflinePlayer target) {
        boolean de = PunishPlugin.getInstance().getDataManager().getLanguage(moderator.getUniqueId()).equals("de");

        String name = target.getName() != null ? target.getName() : (de ? "Unbekannt" : "Unknown");
        String title = (de ? "§cStrafe: " : "§cPunish: ") + name;

        Inventory inv = Bukkit.createInventory(null, 27, title);

        // --- DEKORATION: SCHWARZES GLAS ---
        ItemStack glass = createItem(Material.BLACK_STAINED_GLASS_PANE, " ");
        int[] glassSlots = {0, 1, 2, 3, 5, 6, 7, 8, 9, 13, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26};
        for (int slot : glassSlots) {
            inv.setItem(slot, glass);
        }

        // --- DEKORATION: SPIELERKOPF (Slot 4) ---
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta headMeta = (SkullMeta) head.getItemMeta();
        if (headMeta != null) {
            headMeta.setOwningPlayer(target);
            headMeta.setDisplayName(de ? "§6§lSpieler: §e" + name : "§6§lPlayer: §e" + name);
            head.setItemMeta(headMeta);
        }
        inv.setItem(4, head);

        // --- BANS ---
        inv.setItem(10, createItem(Material.DIAMOND_SWORD, "§cHacking",
                de ? "§71. Mal: 30 Tage" : "§71st Offense: 30 Days",
                de ? "§72. Mal: Permanent" : "§72nd Offense: Permanent"));

        inv.setItem(11, createItem(Material.TNT, "§4Griefing",
                de ? "§71. Mal: 7 Tage" : "§71st Offense: 7 Days",
                de ? "§72. Mal: 30 Tage" : "§72nd Offense: 30 Days"));

        inv.setItem(12, createItem(Material.SPIDER_EYE, de ? "§5Bugusing" : "§5Bug Abuse",
                de ? "§71. Mal: 3 Tage" : "§71st Offense: 3 Days",
                de ? "§72. Mal: 14 Tage" : "§72nd Offense: 14 Days"));

        // --- MUTES ---
        inv.setItem(14, createItem(Material.PAPER, de ? "§eBeleidigung" : "§eInsulting",
                de ? "§71. Mal: Mute 1 Tag" : "§71st Offense: 1 Day Mute",
                de ? "§72. Mal: Mute 7 Tage" : "§72nd Offense: 7 Day Mute"));

        inv.setItem(15, createItem(Material.FEATHER, "§eSpam",
                de ? "§71. Mal: Mute 2 Stunden" : "§71st Offense: 2 Hour Mute",
                de ? "§72. Mal: Mute 1 Tag" : "§72nd Offense: 1 Day Mute"));

        inv.setItem(16, createItem(Material.NAME_TAG, de ? "§eWerbung" : "§eAdvertising",
                de ? "§7Sofort: Permanent Mute" : "§7Immediate: Permanent Mute"));

        moderator.openInventory(inv);
    }

    private static ItemStack createItem(Material mat, String name, String... lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            if (lore.length > 0) {
                meta.setLore(Arrays.asList(lore));
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        if (!title.startsWith("§cPunish: ") && !title.startsWith("§cStrafe: ")) return;
        event.setCancelled(true);
        if (event.getCurrentItem() == null) return;

        Material clickedType = event.getCurrentItem().getType();

        // Klick auf Deko-Elemente (Glas oder Kopf) ignorieren
        if (clickedType == Material.BLACK_STAINED_GLASS_PANE || clickedType == Material.PLAYER_HEAD) {
            return;
        }

        Player moderator = (Player) event.getWhoClicked();
        DataManager data = PunishPlugin.getInstance().getDataManager();
        String lang = data.getLanguage(moderator.getUniqueId());
        boolean de = lang.equals("de");

        String targetName = title.replace("§cPunish: ", "").replace("§cStrafe: ", "");
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);

        if (!target.hasPlayedBefore() && !target.isOnline()) {
            moderator.sendMessage(data.getMessage("player-not-found", lang));
            moderator.closeInventory();
            return;
        }

        String reason = "";
        Instant expiry = null;
        boolean isMute = false;
        String durationLog = "";

        // --- BANS ---
        if (clickedType == Material.DIAMOND_SWORD) {
            reason = "Hacking";
            int offenses = data.getOffenseCount(target.getUniqueId(), reason);
            if (offenses == 0) { expiry = Instant.now().plus(30, ChronoUnit.DAYS); durationLog = de ? "30 Tage" : "30 Days"; }
            else { expiry = Instant.now().plus(3650, ChronoUnit.DAYS); durationLog = "Permanent"; }
        }
        else if (clickedType == Material.TNT) {
            reason = "Griefing";
            int offenses = data.getOffenseCount(target.getUniqueId(), reason);
            if (offenses == 0) { expiry = Instant.now().plus(7, ChronoUnit.DAYS); durationLog = de ? "7 Tage" : "7 Days"; }
            else { expiry = Instant.now().plus(30, ChronoUnit.DAYS); durationLog = de ? "30 Tage" : "30 Days"; }
        }
        else if (clickedType == Material.SPIDER_EYE) {
            reason = de ? "Bugusing" : "Bug Abuse";
            int offenses = data.getOffenseCount(target.getUniqueId(), reason);
            if (offenses == 0) { expiry = Instant.now().plus(3, ChronoUnit.DAYS); durationLog = de ? "3 Tage" : "3 Days"; }
            else { expiry = Instant.now().plus(14, ChronoUnit.DAYS); durationLog = de ? "14 Tage" : "14 Days"; }
        }
        // --- MUTES ---
        else if (clickedType == Material.PAPER) {
            reason = de ? "Beleidigung" : "Insulting";
            isMute = true;
            int offenses = data.getOffenseCount(target.getUniqueId(), reason);
            if (offenses == 0) { expiry = Instant.now().plus(1, ChronoUnit.DAYS); durationLog = de ? "1 Tag" : "1 Day"; }
            else { expiry = Instant.now().plus(7, ChronoUnit.DAYS); durationLog = de ? "7 Tage" : "7 Days"; }
        }
        else if (clickedType == Material.FEATHER) {
            reason = de ? "Spam" : "Spamming";
            isMute = true;
            int offenses = data.getOffenseCount(target.getUniqueId(), reason);
            if (offenses == 0) { expiry = Instant.now().plus(2, ChronoUnit.HOURS); durationLog = de ? "2 Stunden" : "2 Hours"; }
            else { expiry = Instant.now().plus(1, ChronoUnit.DAYS); durationLog = de ? "1 Tag" : "1 Day"; }
        }
        else if (clickedType == Material.NAME_TAG) {
            reason = de ? "Werbung" : "Advertising";
            isMute = true;
            expiry = Instant.now().plus(3650, ChronoUnit.DAYS);
            durationLog = "Permanent";
        }

        if (expiry != null) {
            data.addOffense(target.getUniqueId(), reason);

            if (isMute) {
                data.setMute(target.getUniqueId(), expiry.toEpochMilli());

                String successMsg = data.getMessage("mute-success-gui", lang).replace("%player%", target.getName()).replace("%reason%", reason);
                moderator.sendMessage(successMsg);

                if (target.isOnline() && target.getPlayer() != null) {
                    String notifyMsg = data.getMessage("muted-notify", lang).replace("%reason%", reason).replace("%time%", durationLog);
                    target.getPlayer().sendMessage(notifyMsg);
                }
                data.addHistory(target.getUniqueId(), "§eMute: §7" + reason + " (" + durationLog + ") " + (de ? "von " : "by ") + moderator.getName());
            } else {
                ProfileBanList banList = Bukkit.getBanList(BanList.Type.PROFILE);
                banList.addBan(target.getPlayerProfile(), reason, Date.from(expiry), moderator.getName());

                if (target.isOnline() && target.getPlayer() != null) {
                    String kickMsg = data.getMessage("banned-kick", lang).replace("%reason%", reason).replace("%time%", durationLog).replace("%prefix%", "");
                    target.getPlayer().kickPlayer(kickMsg);
                }

                String successMsg = data.getMessage("punish-success-gui", lang).replace("%player%", target.getName());
                moderator.sendMessage(successMsg);

                data.addHistory(target.getUniqueId(), "§cBan: §7" + reason + " (" + durationLog + ") " + (de ? "von " : "by ") + moderator.getName());
            }

            PunishPlugin.getInstance().getPunishLogger().logBan(moderator.getName(), target.getName(), reason, durationLog);
            moderator.closeInventory();
        }
    }
}