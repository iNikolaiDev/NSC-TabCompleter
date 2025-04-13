package com.nikolai.nsctabcompleter.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import com.nikolai.nsctabcompleter.Main;
import com.nikolai.nsctabcompleter.utilities.Colorization;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;

public class NSCTabCompleterCommand implements TabExecutor
{
    private final Main plugin;

    public NSCTabCompleterCommand(Main plugin)
    {
        this.plugin = plugin;
    }

    public List<String> GetOnlinePlayers()
    {
        final List<String> onlines = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) onlines.add(player.getName());
        return onlines;
    }
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args)
    {
        List<String> completions = new ArrayList<>();
        if (args.length == 1)
        {
            completions.addAll(Arrays.asList("reload", "help", "group", "version", "update", "changelog"));
            return FilterStartsWith(completions, args[0]);
        }
        if (args.length == 2 && args[0].equals("group"))
        {
            completions.addAll(Arrays.asList("information"));
            return FilterStartsWith(completions, args[1]);
        }
        if (args.length == 2 && args[0].equals("update"))
        {
            completions.addAll(Arrays.asList("all", "player"));
            return FilterStartsWith(completions, args[1]);
        }
        if ((args.length == 3) && args[0].equals("update") && args[1].equals("player"))
        {
            completions.addAll(GetOnlinePlayers());
            return FilterStartsWith(completions, args[args.length - 1]);
        }
        if ((args.length == 3) && args[0].equals("group") && args[1].equals("information"))
        {
            completions.addAll(plugin.getConfigManager().getCurrentGroups().keySet());
            return FilterStartsWith(completions, args[args.length - 1]);
        }
        return completions;
    }
    private List<String> FilterStartsWith(List<String> options, String prefix)
    {
        if (prefix == null || prefix.isEmpty()) return options;
        return options.stream()
                .filter(option -> option.toLowerCase().startsWith(prefix.toLowerCase()))
                .collect(Collectors.toList());
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        if (args.length == 0)
        {
            if (sender instanceof Player)
            {
                TextComponent Space = new TextComponent(" ");
                TextComponent helpOption = new TextComponent("§6[HELP]");
                TextComponent reloadOption = new TextComponent("§a[RELOAD]");
                TextComponent updateOption = new TextComponent("§d[UPDATE]");
                TextComponent changelogOption = new TextComponent("§e[CHANGELOG]");

                helpOption.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/nsctabcompleter help"));
                helpOption.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Click here for get help.")));

                reloadOption.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/nsctabcompleter reload"));
                reloadOption.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Click here for reload plugin.")));

                updateOption.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/nsctabcompleter update all"));
                updateOption.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Click here for update all players commands.")));

                changelogOption.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/nsctabcompleter changelog"));
                changelogOption.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Click here for see change log.")));

                sender.sendMessage("§8―――――――――――――――――――――――――――――――――――――――――――――――");
                sender.sendMessage(" ");
                sender.sendMessage(" §7[§d✦§7] " + Colorization.applyColorization("<#a800a8, #f51063, #ff8e44>NSC TabCompleter</#Gradient>") + " §dv" + Main.plugin.getDescription().getVersion() + " §7| §5Made by §dNikolai");
                sender.sendMessage(" ");
                sender.spigot().sendMessage(new BaseComponent[] { Space, helpOption, Space, reloadOption, Space, updateOption, Space, changelogOption });
                sender.sendMessage(" ");
                sender.sendMessage("§8―――――――――――――――――――――――――――――――――――――――――――――――");
            }
            else
            {
                sender.sendMessage("§8―――――――――――――――――――――――――――――――――――――――――――――――");
                sender.sendMessage(" ");
                sender.sendMessage(" §7[§d✦§7] §5NSC TabCompleter §dv" + Main.plugin.getDescription().getVersion() + " §7| §5Made by §dNikolai");
                sender.sendMessage(" ");
                sender.sendMessage("§8―――――――――――――――――――――――――――――――――――――――――――――――");
            }
            return true;
        }
        else if (args[0].equalsIgnoreCase("version"))
        {
            sender.sendMessage(" §5♦ §dNSC §5§l› §dThe current version is §5v" + Main.plugin.getDescription().getVersion());
        }
        else if (args[0].equalsIgnoreCase("update"))
        {
            if (args[1].equalsIgnoreCase("player"))
            {
                if (args.length < 3)
                {
                    sender.sendMessage("§5♦ §dError §5§l› §dYou must specify a player name.");
                    return true;
                }
    
                Player target = Bukkit.getPlayer(args[2]);
                if (target == null)
                {
                    sender.sendMessage("§5♦ §dError §5§l› §dPlayer not found or not online.");
                    return true;
                }
    
                target.updateCommands();
                sender.sendMessage(" §5♦ §dNSC §5§l› §dSuccessfully updated commands for §5" + target.getName() + "§d.");
            }
            else if (args[1].equalsIgnoreCase("all"))
            {
                for (Player player : Bukkit.getOnlinePlayers()) player.updateCommands();
                sender.sendMessage(" §5♦ §dNSC §5§l› §dSuccessfully updated commands for all players.");
            }
        }
        else if (args[0].equalsIgnoreCase("reload"))
        {
            if (!sender.hasPermission("nsctab.reload"))
            {
                sender.sendMessage("§5♦ §dError §5§l› §dYou don't have permission!");
                return true;
            }
            Main.configManager.loadConfigurationFile();
            for (Player player : Bukkit.getOnlinePlayers()) player.updateCommands();
            sender.sendMessage(" §5♦ §dNSC §5§l› §dConfiguration file successfully reloaded.");
        }
        else if (args[0].equalsIgnoreCase("help"))
        {
            sender.sendMessage("§8――― §5•§d♦§5• §8―――――――――― §5• §d« Help » §5• §8―――――――――― §5•§d♦§5• §8―――");
            sender.sendMessage(" ");
            sender.sendMessage("   §8•§r♦§8• §7Sub commands");
            sender.sendMessage(" ");
            sender.sendMessage(" §8♦ §rReload        §7‹ Reload cofinguration file. ›");
            sender.sendMessage(" §8♦ §rGroup         §7‹ Manage groups. ›");
            sender.sendMessage(" §8♦ §rVersion         §7‹ See current version of plugin. ›");
            sender.sendMessage(" §8♦ §rChangelog         §7‹ See update changelog. ›");
            sender.sendMessage(" §8♦ §rUpdate         §7‹ Update player's commands list. ›");
            sender.sendMessage(" ");
            sender.sendMessage("   §8•§r♦§8• §dGroup §7Sub command's args");
            sender.sendMessage(" ");
            sender.sendMessage(" §8♦ §rInformation        §7‹ Get group's information. ›");
            sender.sendMessage(" ");
            sender.sendMessage("§8――― §5•§d♦§5• §8―――――――――― §5• §d« §8―――― §d» §5• §8―――――――――― §5•§d♦§5• §8―――");
        }
        else if (args[0].equalsIgnoreCase("changelog"))
        {
            sender.sendMessage("§8――― §5•§d♦§5• §8――― §dv2.2.0 §8――― §5• §d« Change Log » §5• §8―――――――――― §5•§d♦§5• §8―――");
            sender.sendMessage(" ");
            sender.sendMessage("   §2•§a♦§2• §aWhat's New:");
            sender.sendMessage(" ");

            sender.sendMessage(" §a♦ §aPermission §2‹ nsctab.whitelist.command.<cmd> §2›");
            sender.sendMessage(" §a♦ §aPermission §2‹ nsctab.blacklist.command.<cmd> §2›");
            sender.sendMessage(" §a♦ §aPermission §2‹ nsctab.include.commands.execution §2›");
            sender.sendMessage(" §a♦ §aPermission §2‹ nsctab.include.commands.tabcomplation §2›");
            sender.sendMessage(" ");
            sender.sendMessage(" §a♦ §aEnable or disable tab-complation in config.");
            sender.sendMessage(" §a♦ §aBlacklist or whitelist commands with permission.");
            sender.sendMessage(" ");
            sender.sendMessage("   §e•§6♦§e• §6Fixes:");
            sender.sendMessage(" ");
            sender.sendMessage(" §e♦ §6Fixed known bugs.");
            sender.sendMessage(" ");
            sender.sendMessage(" §d•§5♦§d• Improved permissions and plugin features compatibility with op rank.");
            sender.sendMessage(" ");
            sender.sendMessage("§8――― §5•§d♦§5• §8―――――――――― §5• §d« §8―――― ―――― ―――― §d» §5• §8―――――――――― §5•§d♦§5• §8―――");
        }
        else if (args[0].equalsIgnoreCase("group") && args[1].equalsIgnoreCase("information"))
        {
            if (args[2].isEmpty() || !plugin.getConfigManager().isGroupExist(args[2]))
            {
                sender.sendMessage("§5♦ §dError §5§l› §dInvalid group!");
            } 
            else
            {
                sender.sendMessage("§8――― §5•§d♦§5• §8――― §5• §d« Group Information » §5• §8――― §5•§d♦§5• §8―――");
                sender.sendMessage(" ");
                sender.sendMessage("   §8•§r♦§8• §7Group: §d" + args[2]);
                sender.sendMessage(" ");

                sender.sendMessage(" §8♦ §rCommands    §7‹ " + plugin.getConfigManager().getGroupManager(args[2]).getCommands() + " ›");
                sender.sendMessage(" §8♦ §rPlayers in group    §7‹ " + plugin.getConfigManager().getGroupPlayers(args[2]) + " player ›");
                sender.sendMessage(" ");
                sender.sendMessage("§8――― §5•§d♦§5• §8――――――――― §5• §d« §8―――― §d» §5• §8――――――――― §5•§d♦§5• §8―――");
            }
        }
        return true;
    }
}
