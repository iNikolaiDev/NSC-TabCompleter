# NSC TabCompleter

![](https://raw.githubusercontent.com/iNikolaiDev/pictures/refs/heads/main/NSC-TABCOMPLETER.png)

![](https://raw.githubusercontent.com/iNikolaiDev/pictures/refs/heads/main/PREVIEW.png)

This **plugin** is a tool for **managing command tab completion**.

If you have any issue contact me I will help you. (My **[Discord](https://discord.gg/Dak8Wy3qQt)** and **[Telegram](https://t.me./NikoIaiDev)**)

![](https://raw.githubusercontent.com/iNikolaiDev/pictures/refs/heads/main/GROUPS.png)

You can easily **create** an group and **manage** it by like below.

![](https://raw.githubusercontent.com/iNikolaiDev/pictures/refs/heads/main/HOW-ADD-GROUP.png)

* **newgroup**: You can choose any **name** you want for your group.
* **mode**: You can choose between "**Blacklist**" and "**Whitelist**" mode to decide what the **group** should do.
* **commands**: The **list of commands** that you want the group to **blacklist** or **whitelist** according to the mode.
* **priority**: Group **action priority**, if you want two groups to have the same priority, set their numbers the same.

![](https://raw.githubusercontent.com/iNikolaiDev/pictures/refs/heads/main/FEATURES.png)

* Grouping for better management.
* Blacklist and Whitelist.
* Prioritization for groups.
* Group merging.
* Customize insfussient permission message.
* Support html colors and & formats.
* Bypass permissions for customize.
* Multiple Gradients support.
* Agruments customization.
  
![](https://raw.githubusercontent.com/iNikolaiDev/pictures/refs/heads/main/HOWS-WORK.png)

First of all, you need a **permission management plugin**, I suggest you **[LuckPerms](https://www.spigotmc.org/resources/luckperms.28140/)**. Using this unique plugin, you can easily **apply groups** to players like below.

![](https://raw.githubusercontent.com/iNikolaiDev/pictures/refs/heads/main/HOW-APPLY-GROUP.png)

If you have **multiple** groups applied to the player, they will be **combined**.

![](https://raw.githubusercontent.com/iNikolaiDev/pictures/refs/heads/main/MERGED-GROUPS.png)

For example, you have two groups applied to the player, which are the following commands:

![](https://raw.githubusercontent.com/iNikolaiDev/pictures/refs/heads/main/GROUP1-COMMANDS.png)

![](https://raw.githubusercontent.com/iNikolaiDev/pictures/refs/heads/main/GROUP2-COMMANDS.png)

And your output is something similar as below.

![](https://raw.githubusercontent.com/iNikolaiDev/pictures/refs/heads/main/MERGED-RESULT.png)
![](https://raw.githubusercontent.com/iNikolaiDev/pictures/refs/heads/main/ARGUMENTS.png)

Support for command arguments has been available since **version 2.3.0**, allowing for better customization.

![](https://raw.githubusercontent.com/iNikolaiDev/pictures/refs/heads/main/ARGUMENT-PREVIEW.png)

You can **manage** the command arguments like below.

![](https://raw.githubusercontent.com/iNikolaiDev/pictures/refs/heads/main/HOW-ADD-ARGUMENT.png)

**WARNING! You should be careful that if you plan to manage arguments in a blacklist group, it will be applied if you have multiple whitelisted arguments in another group.**

![](https://raw.githubusercontent.com/iNikolaiDev/pictures/refs/heads/main/GRADIENT.png)

The plugin also **supports gradients**, which you can use for customization. Here's an example:

![](https://raw.githubusercontent.com/iNikolaiDev/pictures/refs/heads/main/GRADIENT-PREVIEW.png)

It also supports the **multiple** color feature, allowing you to do this using your favorite colors. Here's how to do it:

`<#a800a8, #f51063, #ff8e44>♦ NSC TabCompleter ›</#Gradient>`

![](https://raw.githubusercontent.com/iNikolaiDev/pictures/refs/heads/main/PERMISSIONS.png)
Permissions allow you to **achieve** your **exact desire**.

| Permission | Description |
| ------------- | ------------- |
| nsctab.reload | Reloads the plugin config file. |
| nsctab.update.all | Update the players commands. |
| nsctab.update.player | Update the player's commands. |
| nsctab.help | See the plugin help. |
| nsctab.changelog | See the plugin update changelog. |
| nsctab.groups.information | Access to group's information. |
| nsctab.group.‹group› | Applies the group to the player. |
| nsctab.whitelist.command.‹command› | Applies command to the player as a whitelist. |
| nsctab.blacklist.command.‹command› | Applies command to the player as a blacklist. |
| nsctab.bypass.commands.execution | Apply bypass command execution for the player. |
| nsctab.bypass.commands.tabcomplation | Apply bypass tab completion for the player. |
| nsctab.include.commands.execution | Including player in command execution. |
| nsctab.include.commands.tabcomplation | Including player in tab complation. |

![](https://raw.githubusercontent.com/iNikolaiDev/pictures/refs/heads/main/HOW-COMPILE.png)
### Requirements:
- Java 23+ ([Download Link](https://www.oracle.com/fr/java/technologies/downloads/))
- Gradle 8.13 ([Download Link](https://gradle.org/releases/))

### Step 1:
You must add your **Gradle** and **Java** path to system path ( Syntax: `<Your gradle or java folder path>\bin` )

Gradle Example: D:\Programs\Gradle-8.13\bin

Java Example: C:\Program Files\Java\jdk-23\bin

![](https://raw.githubusercontent.com/iNikolaiDev/pictures/refs/heads/main/HOW-ADD-PATH.png)

### Step 2:
You need to open a cmd terminal in the folder. Do the following:
![](https://raw.githubusercontent.com/iNikolaiDev/pictures/refs/heads/main/STEP2-1.png)

And type "Cmd" in the Bar:

![](https://raw.githubusercontent.com/iNikolaiDev/pictures/refs/heads/main/STEP2-2.png)

Then you will see a terminal where you need to enter the "**Gradlew**" command. This command will prepare the project required prerequisites.

![](https://raw.githubusercontent.com/iNikolaiDev/pictures/refs/heads/main/STEP2-3.png)

And finally compile with the "**Gradlew build**" command.

![](https://raw.githubusercontent.com/iNikolaiDev/pictures/refs/heads/main/STEP2-4.png)

You can see the compiled result in the Output folder.
