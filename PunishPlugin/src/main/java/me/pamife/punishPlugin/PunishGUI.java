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

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Date;

public class PunishGUI implements Listener {

    public static void openGUI(Player moderator, OfflinePlayer target) {
        String name = target.getName() != null ? target.getName() : "Unknown";
        Inventory inv = Bukkit.createInventory(null, 27, "§cPunish: " + name);

        // BANS
        inv.setItem(10, createItem(Material.DIAMOND_SWORD, "§cHacking", "§71st Offense: 30 Days", "§72nd Offense: Permanent"));
        inv.setItem(11, createItem(Material.TNT, "§4Griefing", "§71st Offense: 7 Days", "§72nd Offense: 30 Days"));
        inv.setItem(12, createItem(Material.SPIDER_EYE, "§5Bug Abuse", "§71st Offense: 3 Days", "§72nd Offense: 14 Days"));

        // MUTES
        inv.setItem(14, createItem(Material.PAPER, "§eInsulting", "§71st Offense: 1 Day Mute", "§72nd Offense: 7 Day Mute"));
        inv.setItem(15, createItem(Material.FEATHER, "§eSpamming", "§71st Offense: 2 Hour Mute", "§72nd Offense: 1 Day Mute"));
        inv.setItem(16, createItem(Material.NAME_TAG, "§eAdvertising", "§7Immediate: Permanent Mute"));

        moderator.openInventory(inv);
    }

    private static ItemStack createItem(Material mat, String name, String... lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(Arrays.asList(lore));
            item.setItemMeta(meta);
        }
        return item;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        if (!title.startsWith("§cPunish: ")) return;
        event.setCancelled(true);
        if (event.getCurrentItem() == null) return;

        Player moderator = (Player) event.getWhoClicked();
        String targetName = title.replace("§cPunish: ", "");
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);

        if (!target.hasPlayedBefore() && !target.isOnline()) {
            moderator.sendMessage("§cPlayer not found.");
            moderator.closeInventory();
            return;
        }

        Material clickedType = event.getCurrentItem().getType();
        DataManager data = PunishPlugin.getInstance().getDataManager();

        String reason = "";
        Instant expiry = null;
        boolean isMute = false;
        String durationLog = "";

        // --- BANS ---
        if (clickedType == Material.DIAMOND_SWORD) {
            reason = "Hacking";
            int offenses = data.getOffenseCount(target.getUniqueId(), reason);
            if (offenses == 0) { expiry = Instant.now().plus(30, ChronoUnit.DAYS); durationLog = "30 Days"; }
            else { expiry = Instant.now().plus(3650, ChronoUnit.DAYS); durationLog = "Permanent"; }
        }
        else if (clickedType == Material.TNT) {
            reason = "Griefing";
            int offenses = data.getOffenseCount(target.getUniqueId(), reason);
            if (offenses == 0) { expiry = Instant.now().plus(7, ChronoUnit.DAYS); durationLog = "7 Days"; }
            else { expiry = Instant.now().plus(30, ChronoUnit.DAYS); durationLog = "30 Days"; }
        }
        else if (clickedType == Material.SPIDER_EYE) {
            reason = "Bug Abuse";
            int offenses = data.getOffenseCount(target.getUniqueId(), reason);
            if (offenses == 0) { expiry = Instant.now().plus(3, ChronoUnit.DAYS); durationLog = "3 Days"; }
            else { expiry = Instant.now().plus(14, ChronoUnit.DAYS); durationLog = "14 Days"; }
        }
        // --- MUTES ---
        else if (clickedType == Material.PAPER) {
            reason = "Insulting";
            isMute = true;
            int offenses = data.getOffenseCount(target.getUniqueId(), reason);
            if (offenses == 0) { expiry = Instant.now().plus(1, ChronoUnit.DAYS); durationLog = "1 Day"; }
            else { expiry = Instant.now().plus(7, ChronoUnit.DAYS); durationLog = "7 Days"; }
        }
        else if (clickedType == Material.FEATHER) {
            reason = "Spamming";
            isMute = true;
            int offenses = data.getOffenseCount(target.getUniqueId(), reason);
            if (offenses == 0) { expiry = Instant.now().plus(2, ChronoUnit.HOURS); durationLog = "2 Hours"; }
            else { expiry = Instant.now().plus(1, ChronoUnit.DAYS); durationLog = "1 Day"; }
        }
        else if (clickedType == Material.NAME_TAG) {
            reason = "Advertising";
            isMute = true;
            expiry = Instant.now().plus(3650, ChronoUnit.DAYS);
            durationLog = "Permanent";
        }

        if (expiry != null) {
            data.addOffense(target.getUniqueId(), reason);

            if (isMute) {
                data.setMute(target.getUniqueId(), expiry.toEpochMilli());
                moderator.sendMessage("§aYou have muted " + target.getName() + " for " + reason + ".");
                if (target.isOnline() && target.getPlayer() != null) {
                    target.getPlayer().sendMessage("§cYou have been muted!\n§7Reason: " + reason + "\n§7Duration: " + durationLog);
                }
                data.addHistory(target.getUniqueId(), "§eMute: §7" + reason + " (" + durationLog + ") by " + moderator.getName());
            } else {
                ProfileBanList banList = Bukkit.getBanList(BanList.Type.PROFILE);
                banList.addBan(target.getPlayerProfile(), reason, Date.from(expiry), moderator.getName());

                if (target.isOnline() && target.getPlayer() != null) {
                    target.getPlayer().kickPlayer("§cYou have been banned from the server!\n§7Reason: " + reason + "\n§7Duration: " + durationLog);
                }
                moderator.sendMessage("§aYou have successfully banned " + target.getName() + ".");
                data.addHistory(target.getUniqueId(), "§cBan: §7" + reason + " (" + durationLog + ") by " + moderator.getName());
            }

            PunishPlugin.getInstance().getPunishLogger().logBan(moderator.getName(), target.getName(), reason, durationLog);
            moderator.closeInventory();
        }
    }
}