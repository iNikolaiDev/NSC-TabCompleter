# NSC TabCompleter

<img src="/pictures/NSC-TABCOMPLETER.png" />

<img src="/pictures/2025-04-02_18.52.58.png" />

This plugin is a tool for managing command tab completion.

My Discord: nikolai0803
My Telegram: @NikoIaiDev

<img src="/pictures/GROUPS.png" />

You can easily create an group and manage it by like below.

<img src="/pictures/ADDGROUP.png" />

* **newgroup**: You can choose any name you want for your group.
* **mode**: You can choose between "Blacklist" and "Whitelist" mode to decide what the group should do.
* **commands**: The list of commands that you want the group to blacklist or whitelist according to the mode.
* **priority**: Group action priority, if you want two groups to have the same priority, set their numbers the same.

<img src="/pictures/FEATURES.png" />

* Grouping for better management.
* Blacklist and Whitelist.
* Prioritization for groups.
* Group merging.
* Customize insfussient permission message.
* Support html colors and & formats.
* Bypass permissions for customize.
* Multiple Gradients support.
  
<img src="/pictures/HOWS-WORK.png" />

First of all, you need a permission management plugin, I suggest you luckperms. Using this unique plugin, you can easily apply groups to players like below.

<img src="/pictures/APPLY-FOR-PLAYER.png" />

If you have multiple groups applied to the player, they will be combined.

<img src="/pictures/PLAYERS-GROUPS.png" />

For example, you have two groups applied to the player, which are the following commands:

<img src="/pictures/GROUP-1-COMMANDS.png" /><img src="/pictures/GROUP-2-COMMANDS.png" />

And your output is something similar as below.

<img src="/pictures/MERGED-GROUP-COMMANDS.png" />

<img src="/pictures/GRADIENT.png" />

The plugin also supports gradients, which you can use for customization. Here's an example:

<img src="/pictures/GRADIENT-PREVIEW.png" />

It also supports the multiple color feature, allowing you to do this using your favorite colors. Here's how to do it:

`<#a800a8, #f51063, #ff8e44>♦ NSC TabCompleter ›</#Gradient>`

![](https://raw.githubusercontent.com/iNikolaiDev/pictures/refs/heads/main/PERMISSIONS.png)
Permissions allow you to make your own customizations.

| Permission | Description |
| ------------- | ------------- |
| nsctab.reload | Reloads the plugin config file. |
| nsctab.group.‹group› | Applies the group to the player. |
| nsctab.whitelist.command.‹command› | Applies command to the player as a whitelist. |
| nsctab.blacklist.command.‹command› | Applies command to the player as a blacklist. |
| nsctab.bypass.commands.execution | Apply bypass command execution for the player. |
| nsctab.bypass.commands.tabcomplation | Apply bypass tab completion for the player. |
| nsctab.include.commands.execution | Including player in command execution. |
| nsctab.include.commands.tabcomplation | Including player in tab complation. |
