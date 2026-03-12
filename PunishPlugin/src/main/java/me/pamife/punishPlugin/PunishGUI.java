package me.pamife.punishPlugin;

import org.bukkit.Bukkit;
import org.bukkit.BanList;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.ban.ProfileBanList;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class PunishGUI implements Listener {

    public static void openGUI(Player moderator, OfflinePlayer target) {
        DataManager data = PunishPlugin.getInstance().getDataManager();
        boolean de = data.getLanguage(moderator.getUniqueId()).equals("de");

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

        // --- DYNAMISCHE ITEMS AUS CONFIG LADEN ---
        ConfigurationSection items = data.getGuiItems();
        if (items != null) {
            for (String slotStr : items.getKeys(false)) {
                try {
                    int slot = Integer.parseInt(slotStr);
                    String matStr = items.getString(slotStr + ".material", "STONE");
                    Material mat = Material.matchMaterial(matStr);
                    if (mat == null) mat = Material.STONE;

                    String itemName = de ? items.getString(slotStr + ".name-de") : items.getString(slotStr + ".name-en");
                    itemName = ChatColor.translateAlternateColorCodes('&', itemName);

                    List<String> rawLore = de ? items.getStringList(slotStr + ".lores-de") : items.getStringList(slotStr + ".lores-en");
                    List<String> coloredLore = new ArrayList<>();
                    for (String l : rawLore) {
                        coloredLore.add(ChatColor.translateAlternateColorCodes('&', l));
                    }

                    inv.setItem(slot, createItem(mat, itemName, coloredLore.toArray(new String[0])));
                } catch (NumberFormatException ignored) {}
            }
        }

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

        int clickedSlot = event.getRawSlot();
        ConfigurationSection items = data.getGuiItems();

        // Prüfen ob der geklickte Slot in der Config existiert
        if (items == null || !items.contains(String.valueOf(clickedSlot))) {
            return; // Deko Items (Glas/Kopf) werden hier automatisch ignoriert
        }

        String slotKey = String.valueOf(clickedSlot);
        String reason = items.getString(slotKey + ".internal-reason", "Unknown");
        String type = items.getString(slotKey + ".type", "BAN");
        List<String> durations = items.getStringList(slotKey + ".durations");

        if (durations.isEmpty()) return;

        int offenses = data.getOffenseCount(target.getUniqueId(), reason);
        String durationLog = "";

        if (offenses >= durations.size()) {
            durationLog = durations.get(durations.size() - 1); // Letzte Strafe aus der Liste nehmen
        } else {
            durationLog = durations.get(offenses);
        }

        Instant expiry = data.parseDuration(durationLog);
        if (expiry == null) return;

        boolean isMute = type.equalsIgnoreCase("MUTE");

        // Strafe ausführen
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

            // STAFF BENACHRICHTIGEN
            data.broadcastStaffMessage("staff-notify-punish", target.getName(), moderator.getName(), reason);

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

            // STAFF BENACHRICHTIGEN
            data.broadcastStaffMessage("staff-notify-punish", target.getName(), moderator.getName(), reason);
        }

        PunishPlugin.getInstance().getPunishLogger().logBan(moderator.getName(), target.getName(), reason, durationLog);
        moderator.closeInventory();
    }
}