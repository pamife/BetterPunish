# BetterPunish ⚖️

**BetterPunish** is a professional, lightweight, and high-performance punishment system for Minecraft servers running on **Paper** or **Purpur** (1.21.1+). It features a fully customizable GUI, an automated warning system, and multi-language support.

---

## ✨ Features

* **⚠️ Advanced Warning System:** Warn players with `/warn`. Reaching **3 warnings** triggers an automatic 1-hour mute to reduce manual moderation.
* **🖼️ Dynamic GUI:** A clean, glass-framed interface featuring the target player's head. Everything from items to durations is now fully configurable via `config.yml`.
* **🌍 Multi-Language System:** Moderators can choose their preferred language (`/punishlang en/de`). Settings are saved per moderator UUID.
* **🕵️ Offline Support:** Punish players even if they are not currently online.
* **📜 Detailed History:** View a complete log of a player's past offenses, warns, and bans with `/history`.
* **⚙️ Fully Customizable:** Server owners can edit every message, ban reason, and punishment duration in the config.
* **📂 Clean Logging:** All actions are archived in a human-readable `punish_log.txt` file.

---

## 🛠️ Commands & Permissions

| Command | Description | Permission |
| :--- | :--- | :--- |
| `/punish <player>` | Opens the punishment GUI or applies a manual ban. | `punish.use` |
| `/warn <player> <reason>`| Warns a player. Auto-mutes at 3 warnings. | `punish.use` |
| `/history <player>` | Displays the full punishment history of a player. | `punish.history` |
| `/unpunish <player>` | Lifts an active ban or mute. | `punish.unpunish` |
| `/punishlang <en/de>` | Sets your personal language for the plugin. | `punish.use` |

---

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
