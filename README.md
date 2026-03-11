# BetterPunish ⚖️

**BetterPunish** is a professional, lightweight, and high-performance punishment system for Minecraft servers running on **Paper** or **Purpur** (1.21.1+). It features an intuitive GUI, multi-language support, and persistent history tracking.

---

## ✨ Features

* **Offline Support:** Punish players even if they are not currently online.
* **Intuitive GUI:** Manage punishments through a clean graphical interface with escalating severity levels.
* **Multi-Language System:** Moderators can choose their preferred language (`/punishlang en/de`). Settings are saved per moderator.
* **Detailed History:** View a complete log of a player's past offenses with `/history`.
* **Mute System:** Efficiently manage chat behavior with time-based mutes.
* **Modern API:** Built using the latest Paper/Spigot `ProfileBanList` for maximum compatibility and reliability.
* **Clean Logging:** All actions are logged in a human-readable `punish_log.txt` file.

---

## 🛠️ Commands & Permissions

| Command | Description | Permission |
| :--- | :--- | :--- |
| `/punish <player>` | Opens the punishment GUI or applies a manual ban. | `punish.use` |
| `/history <player>` | Displays the full punishment history of a player. | `punish.history` |
| `/unpunish <player>` | Lifts an active ban or mute. | `punish.unpunish` |
| `/punishlang <en/de>`| Sets your personal language for the plugin. | `punish.use` |

---

## 📥 Installation

1.  Download the latest `.jar` file from the [Releases](https://github.com/YourUsername/BetterPunish/releases) page.
2.  Place the file into your server's `plugins` folder.
3.  Restart your server.
4.  Configure the plugin via the generated `data.yml` or simply start using it in-game.

---

## 💻 Compatibility

* **Version:** Minecraft 1.21.x
* **Platform:** Paper, Purpur (Recommended)
* **Java:** Version 21 or higher

> [!WARNING]
> This plugin uses Paper-specific events for chat handling. It is **not** compatible with vanilla Spigot or Bukkit unless modified.

---

## 📄 License

This project is licensed under the **MIT License**. See the [LICENSE](LICENSE) file for more details.

---

## 🤝 Contributing

Contributions, issues, and feature requests are welcome! Feel free to check the issues page if you want to contribute to the development.

---
*Developed with ❤️ by Paul (pamife)*
