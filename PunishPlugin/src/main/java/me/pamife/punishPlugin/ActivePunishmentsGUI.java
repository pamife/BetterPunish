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
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.SimpleDateFormat;
import java.util.*;

public class ActivePunishmentsGUI implements Listener {

    // --- FILTER ZUSTÄNDE ---
    public enum FilterType { ALL, BAN, MUTE }
    public enum FilterStatus { ALL, ACTIVE, EXPIRED }

    private static final Map<UUID, FilterType> typeFilters = new HashMap<>();
    private static final Map<UUID, FilterStatus> statusFilters = new HashMap<>();
    private static final Map<UUID, String> searchQueries = new HashMap<>();
    private static final Set<UUID> awaitingSearch = new HashSet<>();

    public static void openGUI(Player admin) {
        DataManager dm = PunishPlugin.getInstance().getDataManager();
        boolean de = dm.getLanguage(admin.getUniqueId()).equals("de");

        String title = de ? "§8Strafen verwalten" : "§8Manage Punishments";
        Inventory inv = Bukkit.createInventory(null, 54, title);

        UUID uuid = admin.getUniqueId();
        FilterType currentType = typeFilters.getOrDefault(uuid, FilterType.ALL);
        FilterStatus currentStatus = statusFilters.getOrDefault(uuid, FilterStatus.ALL);
        String currentSearch = searchQueries.getOrDefault(uuid, "");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        int slot = 0;

        // 1. BANS LADEN
        if (currentType == FilterType.ALL || currentType == FilterType.BAN) {
            ProfileBanList banList = Bukkit.getBanList(BanList.Type.PROFILE);

            for (BanEntry<?> entry : banList.getEntries()) {
                if (slot >= 45) break; // Untere Reihe freihalten

                org.bukkit.profile.PlayerProfile profile = (org.bukkit.profile.PlayerProfile) entry.getBanTarget();
                String targetName = profile.getName();
                if (targetName == null) targetName = "Unknown";

                // Such-Filter
                if (!currentSearch.isEmpty() && !targetName.toLowerCase().contains(currentSearch.toLowerCase())) continue;

                Date exp = entry.getExpiration();
                boolean isActive = exp == null || exp.after(new Date());

                // Status-Filter
                if (currentStatus == FilterStatus.ACTIVE && !isActive) continue;
                if (currentStatus == FilterStatus.EXPIRED && isActive) continue;

                ItemStack item = new ItemStack(Material.BARRIER);
                ItemMeta meta = item.getItemMeta();
                meta.setDisplayName("§c" + targetName);

                List<String> lore = new ArrayList<>();
                lore.add("§8§m------------------------");
                lore.add(de ? "§7Typ: §cBAN" : "§7Type: §cBAN");
                lore.add(de ? "§7Grund: §f" + entry.getReason() : "§7Reason: §f" + entry.getReason());
                lore.add(de ? "§7Von: §f" + entry.getSource() : "§7By: §f" + entry.getSource());

                String expires = (exp == null) ? (de ? "Permanent" : "Permanent") : sdf.format(exp);
                lore.add(de ? "§7Ablauf: §f" + expires : "§7Expires: §f" + expires);
                lore.add("§8§m------------------------");
                lore.add(isActive ? (de ? "§eKlicke zum Entbannen" : "§eClick to Unban") : (de ? "§cBereits abgelaufen" : "§cAlready expired"));

                meta.setLore(lore);
                item.setItemMeta(meta);
                inv.setItem(slot++, item);
            }
        }

        // 2. MUTES LADEN (Nutzt jetzt getAllMutes, um auch abgelaufene zu finden)
        if (currentType == FilterType.ALL || currentType == FilterType.MUTE) {
            for (UUID targetUuid : dm.getAllMutes()) {
                if (slot >= 45) break;

                long expiryMillis = dm.getMuteExpiry(targetUuid);
                boolean isActive = expiryMillis > System.currentTimeMillis();

                // Status-Filter
                if (currentStatus == FilterStatus.ACTIVE && !isActive) continue;
                if (currentStatus == FilterStatus.EXPIRED && isActive) continue;

                OfflinePlayer target = Bukkit.getOfflinePlayer(targetUuid);
                String targetName = target.getName() != null ? target.getName() : targetUuid.toString();

                // Such-Filter
                if (!currentSearch.isEmpty() && !targetName.toLowerCase().contains(currentSearch.toLowerCase())) continue;

                ItemStack item = new ItemStack(Material.PAPER);
                ItemMeta meta = item.getItemMeta();
                meta.setDisplayName("§e" + targetName);

                List<String> lore = new ArrayList<>();
                lore.add("§8§m------------------------");
                lore.add(de ? "§7Typ: §eMUTE" : "§7Type: §eMUTE");

                String expires = sdf.format(new Date(expiryMillis));
                lore.add(de ? "§7Ablauf: §f" + expires : "§7Expires: §f" + expires);
                lore.add("§8§m------------------------");
                lore.add(isActive ? (de ? "§eKlicke zum Entmuten" : "§eClick to Unmute") : (de ? "§cBereits abgelaufen" : "§cAlready expired"));

                meta.setLore(lore);
                item.setItemMeta(meta);
                inv.setItem(slot++, item);
            }
        }

        // --- STEUERUNGS-MENÜ (Bodenleiste) ---
        ItemStack glass = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glass.getItemMeta();
        glassMeta.setDisplayName(" ");
        glass.setItemMeta(glassMeta);
        for (int i = 45; i < 54; i++) {
            inv.setItem(i, glass);
        }

        ItemStack typeItem = new ItemStack(Material.HOPPER);
        ItemMeta typeMeta = typeItem.getItemMeta();
        typeMeta.setDisplayName(de ? "§6Filter: Typ" : "§6Filter: Type");
        typeMeta.setLore(Arrays.asList(
                de ? "§7Aktuell: §a" + currentType.name() : "§7Current: §a" + currentType.name(),
                "",
                de ? "§eKlicke zum Ändern" : "§eClick to change"
        ));
        typeItem.setItemMeta(typeMeta);
        inv.setItem(45, typeItem);

        ItemStack statusItem = new ItemStack(Material.CLOCK);
        ItemMeta statusMeta = statusItem.getItemMeta();
        statusMeta.setDisplayName(de ? "§6Filter: Status" : "§6Filter: Status");
        statusMeta.setLore(Arrays.asList(
                de ? "§7Aktuell: §a" + currentStatus.name() : "§7Current: §a" + currentStatus.name(),
                "",
                de ? "§eKlicke zum Ändern" : "§eClick to change"
        ));
        statusItem.setItemMeta(statusMeta);
        inv.setItem(46, statusItem);

        ItemStack searchItem = new ItemStack(Material.NAME_TAG);
        ItemMeta searchMeta = searchItem.getItemMeta();
        searchMeta.setDisplayName(de ? "§aSpieler Suchen" : "§aSearch Player");
        searchMeta.setLore(Arrays.asList(
                de ? "§7Aktuelle Suche: §f" + (currentSearch.isEmpty() ? "Keine" : currentSearch) : "§7Current Search: §f" + (currentSearch.isEmpty() ? "None" : currentSearch),
                "",
                de ? "§eKlicke zum Suchen" : "§eClick to search"
        ));
        searchItem.setItemMeta(searchMeta);
        inv.setItem(49, searchItem);

        ItemStack resetItem = new ItemStack(Material.LAVA_BUCKET);
        ItemMeta resetMeta = resetItem.getItemMeta();
        resetMeta.setDisplayName(de ? "§cFilter zurücksetzen" : "§cReset Filters");
        resetItem.setItemMeta(resetMeta);
        inv.setItem(53, resetItem);

        admin.openInventory(inv);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        if (!title.equals("§8Strafen verwalten") && !title.equals("§8Manage Punishments")) return;

        event.setCancelled(true);
        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.BLACK_STAINED_GLASS_PANE) return;

        Player admin = (Player) event.getWhoClicked();
        UUID uuid = admin.getUniqueId();
        int slot = event.getRawSlot();

        if (slot >= 45) {
            if (slot == 45) {
                FilterType current = typeFilters.getOrDefault(uuid, FilterType.ALL);
                typeFilters.put(uuid, current == FilterType.ALL ? FilterType.BAN : (current == FilterType.BAN ? FilterType.MUTE : FilterType.ALL));
                openGUI(admin);
            } else if (slot == 46) {
                FilterStatus current = statusFilters.getOrDefault(uuid, FilterStatus.ALL);
                statusFilters.put(uuid, current == FilterStatus.ALL ? FilterStatus.ACTIVE : (current == FilterStatus.ACTIVE ? FilterStatus.EXPIRED : FilterStatus.ALL));
                openGUI(admin);
            } else if (slot == 49) {
                admin.closeInventory();
                awaitingSearch.add(uuid);
                boolean de = PunishPlugin.getInstance().getDataManager().getLanguage(uuid).equals("de");
                admin.sendMessage(de ? "§aBitte schreibe den Spielernamen in den Chat (oder 'cancel' zum Abbrechen):" : "§aPlease type the player name in chat (or 'cancel' to abort):");
            } else if (slot == 53) {
                typeFilters.remove(uuid);
                statusFilters.remove(uuid);
                searchQueries.remove(uuid);
                openGUI(admin);
            }
            return;
        }

        String targetName = ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName());
        if (targetName == null || targetName.isEmpty() || targetName.trim().isEmpty()) return;

        // Verhindert Unpunish bei bereits abgelaufenen Strafen
        List<String> lore = event.getCurrentItem().getItemMeta().getLore();
        if (lore != null && (lore.contains("§cBereits abgelaufen") || lore.contains("§cAlready expired"))) {
            return;
        }

        admin.closeInventory();
        admin.performCommand("unpunish " + targetName);
        Bukkit.getScheduler().runTaskLater(PunishPlugin.getInstance(), () -> openGUI(admin), 5L);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncPlayerChatEvent event) {
        Player admin = event.getPlayer();
        UUID uuid = admin.getUniqueId();

        if (awaitingSearch.contains(uuid)) {
            event.setCancelled(true);
            awaitingSearch.remove(uuid);

            String input = event.getMessage().trim();
            if (input.equalsIgnoreCase("cancel")) {
                searchQueries.remove(uuid);
            } else {
                searchQueries.put(uuid, input);
            }

            Bukkit.getScheduler().runTask(PunishPlugin.getInstance(), () -> openGUI(admin));
        }
    }
}