# NSC TabCompleter ✨

![](https://raw.githubusercontent.com/iNikolaiDev/pictures/refs/heads/main/NSC-TABCOMPLETER.png)

![](https://raw.githubusercontent.com/iNikolaiDev/pictures/refs/heads/main/PREVIEW.png)

This **plugin** is a powerful tool for **managing command tab completion** in your server.

💬 If you encounter any issues, feel free to **contact me**! I'm here to help.

📌 [Discord](https://discord.gg/Dak8Wy3qQt) | [Telegram](https://t.me./NikoIaiDev) | [Wiki](https://github.com/iNikolaiDev/NSC-TabCompleter/wiki)

![](https://raw.githubusercontent.com/iNikolaiDev/pictures/refs/heads/main/GROUPS.png)

You can easily **create** and **manage** groups to organize your tab completion settings. Here's how:

![](https://raw.githubusercontent.com/iNikolaiDev/pictures/refs/heads/main/HOW-ADD-GROUP.png)

*  **newgroup** 📋: Choose any **name** for your group.
*  **mode** ⚙️: Select between **Blacklist** or **Whitelist** to define the group's behavior.
*  **commands** 📜: Specify the **list of commands** to **blacklist** or **whitelist** based on the mode.
*  **priority** 🔢: Set the group's **action priority**. For groups with equal priority, assign the same number.

![](https://raw.githubusercontent.com/iNikolaiDev/pictures/refs/heads/main/FEATURES.png)

* **Grouping** for streamlined management.
* **Blacklist** and **Whitelist** modes.
* Prioritization for groups.
* **Group merging** for combined functionality.
* **Customizable insufficient permission messages** 💬.
* **Support for HTML colors** and **& formats**.
* **Bypass permissions** for tailored control.
* **Multiple Gradients support**.
* **Arguments customization** for precise command handling.
  
![](https://raw.githubusercontent.com/iNikolaiDev/pictures/refs/heads/main/HOWS-WORK.png)

To get started, you'll need a **permission management plugin**. We recommend **[LuckPerms](https://www.spigotmc.org/resources/luckperms.28140/)** for seamless group application to players.

![](https://raw.githubusercontent.com/iNikolaiDev/pictures/refs/heads/main/HOW-APPLY-GROUP.png)

When **multiple groups** are applied to a player, they will be **combined** for a unified effect.

![](https://raw.githubusercontent.com/iNikolaiDev/pictures/refs/heads/main/MERGED-GROUPS.png)

Suppose you have two groups applied to a player with the following **commands**:

![](https://raw.githubusercontent.com/iNikolaiDev/pictures/refs/heads/main/GROUP1-COMMANDS.png)

![](https://raw.githubusercontent.com/iNikolaiDev/pictures/refs/heads/main/GROUP2-COMMANDS.png)

The output will reflect the combined rules, ensuring smooth tab completion.

![](https://raw.githubusercontent.com/iNikolaiDev/pictures/refs/heads/main/MERGED-RESULT.png)
![](https://raw.githubusercontent.com/iNikolaiDev/pictures/refs/heads/main/ARGUMENTS.png)

Since **version 2.3.0**, the plugin supports **command arguments** for enhanced customization. 🛠️

![](https://raw.githubusercontent.com/iNikolaiDev/pictures/refs/heads/main/ARGUMENT-PREVIEW.png)

**Manage arguments** like this:

![](https://raw.githubusercontent.com/iNikolaiDev/pictures/refs/heads/main/HOW-ADD-ARGUMENT.png)

⚠️ **WARNING**: Be cautious when managing arguments in a **blacklist group**. If multiple **whitelisted arguments** exist in another group, they may conflict.

![](https://raw.githubusercontent.com/iNikolaiDev/pictures/refs/heads/main/GRADIENT.png)

🌈 The plugin supports **gradients** for vibrant customization. Example:

![](https://raw.githubusercontent.com/iNikolaiDev/pictures/refs/heads/main/GRADIENT-PREVIEW.png)

You can also use **multiple color** formatting to style with your favorite colors.

`<#a800a8, #f51063, #ff8e44>♦ NSC TabCompleter ›</#Gradient>`

![](https://raw.githubusercontent.com/iNikolaiDev/pictures/refs/heads/main/PERMISSIONS.png)
🔐 Permissions allow you to achieve your **exact desired configuration**. Here's the full list:

| Permission | Description |
| ------------- | ------------- |
| nsctab.reload | Reloads the plugin config file. 🔄 |
| nsctab.update.all | Updates all players commands. 🌍 |
| nsctab.update.player | Updates a specific player's commands. 👤 |
| nsctab.help | Displays the plugin help. ❓ |
| nsctab.changelog | Shows the plugin update changelog. 📝 |
| nsctab.groups.information | Access group information. 📊 |
| nsctab.group.‹group› | Applies the specified group to the player. 🛡️ |
| nsctab.whitelist.command.‹command› | Whitelists a command for the player. ✅ |
| nsctab.blacklist.command.‹command› | Blacklists a command for the player. 🚫 |
| nsctab.bypass.commands.execution | Bypasses command execution restrictions. 🔓 |
| nsctab.bypass.commands.tabcomplation | Bypasses tab completion restrictions. 📑 |
| nsctab.include.commands.execution | Includes the player in command execution. ➕ |
| nsctab.include.commands.tabcomplation | Includes the player in tab completion. ➕ |

## Need Help? ❓

Visit our **[Wiki](https://github.com/iNikolaiDev/NSC-TabCompleter/wiki)** for detailed documentation, or join our community on **[Discord](https://discord.gg/Dak8Wy3qQt)** for support and updates.

Enjoy a smoother, more organized server experience with **NSC TabCompleter**! 🌟

![](https://raw.githubusercontent.com/iNikolaiDev/pictures/refs/heads/main/HOW-COMPILE.png)
### Requirements:
- Java 23+ ([Download Link](https://www.oracle.com/fr/java/technologies/downloads/))
- Gradle 8.13 ([Download Link](https://gradle.org/releases/))

### Step 1: Configure Paths
Add your **Gradle** and **Java** paths to your system environment variables:

( Syntax: `<Your gradle or java folder path>\bin` )

* **Gradle Example**: `D:\Programs\Gradle-8.13\bin`

* **Java Example**: `C:\Program Files\Java\jdk-23\bin`

![](https://raw.githubusercontent.com/iNikolaiDev/pictures/refs/heads/main/HOW-ADD-PATH.png)

### Step 2: Build the Project
1. Type `cmd` in the address bar and press Enter.
2. Run the `Gradlew` command to prepare the project prerequisites.
3. Run the `Gradlew build` command to compile project.
4. The compiled result will appear in the **Output folder**. 🎉

### 1.
![](https://raw.githubusercontent.com/iNikolaiDev/pictures/refs/heads/main/STEP2-1.png)

![](https://raw.githubusercontent.com/iNikolaiDev/pictures/refs/heads/main/STEP2-2.png)

### 2.
![](https://raw.githubusercontent.com/iNikolaiDev/pictures/refs/heads/main/STEP2-3.png)

### 3.
![](https://raw.githubusercontent.com/iNikolaiDev/pictures/refs/heads/main/STEP2-4.png)
