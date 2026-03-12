# BetterPunish ⚖️

**BetterPunish** is a high-performance, all-in-one punishment and moderation suite for Minecraft servers (**Paper/Purpur 1.21.1+**). It combines intuitive GUIs, automated chat protection, and deep customization to make server management effortless.

---

## ✨ Key Features

* **🛡️ Smart Chat Filter:** Automatically blocks forbidden words and links. Optionally issues strikes to offenders.
* **⚠️ Advanced Warning System:** Warn players for minor offenses. Reaching **3 warnings** triggers an automatic 1-hour mute.
* **📋 Active Punishments GUI:** A dedicated menu (`/punishments`) to view and lift all current bans and mutes with a single click.
* **🖼️ Dynamic GUI:** Sleek, glass-framed interface featuring target player heads. Fully configurable via `config.yml`.
* **🌍 Multi-Language:** Individual language settings (EN/DE) for every moderator, saved via UUID.
* **🔔 Staff Notifications:** Keep your team informed with real-time alerts for punishments and filter triggers. Toggleable via `/punishnotify`.
* **⚙️ Live Reload:** Update your entire configuration and filter list without restarting the server using `/punishreload`.

---

## 🛠️ Commands & Permissions

| Command | Description | Permission |
| :--- | :--- | :--- |
| `/punish <player>` | Opens the main punishment GUI | `punish.use` |
| `/punishments` | Opens the active punishments manager | `punish.admin` |
| `/warn <player> <reason>` | Issues a warning (Auto-mute at 3) | `punish.use` |
| `/history <player>` | Displays a player's full offense log | `punish.history` |
| `/unpunish <player>` | Manually lifts a ban or mute | `punish.unpunish` |
| `/punishreload` | Reloads config and data files | `punish.admin` |
| `/punishnotify` | Toggles staff alerts for yourself | `punish.staff` |

---

## 💻 Compatibility
* **Platform:** Paper, Purpur (Recommended)
* **Java:** Version 21 or higher
* **Version:** 1.21.x

## 📥 Installation

1. Download the latest `.jar` file from the [Releases](https://github.com/pamife/BetterPunish/releases) page.
2. Place the file into your server's `plugins` folder.
3. Restart your server.
4. **Configure:** Edit the `config.yml` to customize your ban reasons, durations, and messages.
5. Use `/punishlang` in-game to set your preferred language.

---

## 💻 Compatibility

* **Version:** Minecraft 1.21.x
* **Platform:** Paper, Purpur (Recommended)
* **Java:** Version 21 or higher

> [!WARNING]
> This plugin uses Paper-specific events for chat handling. It is **not** compatible with vanilla Spigot or Bukkit.

---

## 📄 License

This project is licensed under the **MIT License**. See the [LICENSE](LICENSE) file for more details.

---

## 🤝 Contributing

Contributions, issues, and feature requests are welcome! Feel free to check the [issues page](https://github.com/pamife/PunishPlugin/issues) if you want to contribute to the development.

---
*Developed with ❤️ by [Paul (pamife)](https://github.com/pamife)*
