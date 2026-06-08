package com.nikolai.nsctabcompleter.commands;

import com.nikolai.nsctabcompleter.Main;
import com.nikolai.nsctabcompleter.utilities.ChatUtil;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class NSCTabCompleterCommand implements TabExecutor
{
    private final Main plugin;

    // Bars
    private static final String TOP  = " §8§m                                                         ";
    private static final String BOTTOM  = " §8§m                                                         ";
    // Prefix / feedback
    private static final String PREFIX = " §5§l◈ §dNSC §8§l» §d";
    private static final String ERROR  = " §5§l◈ §4§lError §8§l» §c";
    private static final String BULL   = " §5◆ §7";

    // Click actions
    private static final ClickEvent.Action RUN = ClickEvent.Action.RUN_COMMAND;
    private static final ClickEvent.Action SUGGEST = ClickEvent.Action.SUGGEST_COMMAND;
    private static final ClickEvent.Action OPEN = ClickEvent.Action.OPEN_URL;

    public NSCTabCompleterCommand(Main plugin)
    {
        this.plugin = plugin;
    }

    // ─── Tab-Completion ────────────────────────────────────────────────────────

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args)
    {
        List<String> completions = new ArrayList<>();

        if (args.length == 1)
        {
            completions.addAll(List.of("reload", "help", "group", "version", "update", "changelog"));
            return filterStartsWith(completions, args[0]);
        }

        String sub = args[0].toLowerCase();

        if (args.length == 2)
        {
            if (sub.equals("group"))  completions.add("info");
            if (sub.equals("update")) completions.addAll(List.of("all", "player"));
            if (sub.equals("help")) completions.addAll(List.of("group", "other"));
            return filterStartsWith(completions, args[1]);
        }

        if (args.length == 3)
        {
            if (sub.equals("update") && args[1].equalsIgnoreCase("player"))
            {
                Bukkit.getOnlinePlayers().forEach(p -> completions.add(p.getName()));
            }
            if (sub.equals("group") && args[1].equalsIgnoreCase("info"))
            {
                completions.addAll(plugin.getConfigManager().getCurrentGroups().keySet());
            }
            return filterStartsWith(completions, args[2]);
        }

        return completions;
    }

    private List<String> filterStartsWith(List<String> options, String prefix)
    {
        if (prefix == null || prefix.isBlank()) return options;
        String lower = prefix.toLowerCase();
        return options.stream()
                .filter(o -> o.toLowerCase().startsWith(lower))
                .collect(Collectors.toList());
    }

    // ─── Command Execution ─────────────────────────────────────────────────────

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        if (args.length == 0)
        {
            sendOverview(sender);
            return true;
        }

        switch (args[0].toLowerCase())
        {
            case "version"   -> sendVersion(sender);
            case "reload"    -> handleReload(sender);
            case "help"      -> handleHelp(sender, args);
            case "changelog" -> handleChangelog(sender);
            case "update"    -> handleUpdate(sender, args);
            case "group"     ->
            {
                if (args.length >= 2 && args[1].equalsIgnoreCase("info"))
                    handleGroupInfo(sender, args);
                else
                    sender.sendMessage(ERROR + "Usage: /nsctabcompleter group info <group>");
            }
            default -> sender.sendMessage(ERROR + "Unknown sub-command. Use §5/nsctabcompleter help§d.");
        }

        return true;
    }

    // ─── Sub-command Handlers ──────────────────────────────────────────────────

    private void sendOverview(CommandSender sender)
    {
        if (!(sender instanceof Player player))
        {
            sender.sendMessage(TOP);
            sender.sendMessage(" §7[§d✦§7] §5NSC TabCompleter §dv" + Main.plugin.getDescription().getVersion() + " §7| §5Made by §dNikolai");
            sender.sendMessage(BOTTOM);
            return;
        }
        sender.sendMessage(TOP);
        sender.sendMessage(" ");
        sender.sendMessage(ChatUtil.centerText(ChatUtil.colour("<#a800a8, #f51063, #ff8e44>NSC TabCompleter</#Gradient>")));
        sender.sendMessage(ChatUtil.centerText("§7V E R S I O N" + " §8♦ §7" + Main.plugin.getDescription().getVersion()));
        sender.sendMessage(" ");

        player.spigot().sendMessage(buildRow(
            plainText(" §7➜ "),
            clickButton("§6[HELP]", "§6Command reference", "/nsctabcompleter help", RUN),
            plainText(" "),
            clickButton("§3[MANAGE]", "§3Management tools", "/nsctabcompleter manage", RUN),
            plainText(" "),
            clickButton("§a[RELOAD]", "§aReload config from disk", "/nsctabcompleter reload", RUN),
            plainText(" "),
            clickButton("§d[UPDATE]", "§dRefresh all players", "/nsctabcompleter update all", RUN),
            plainText(" "),
            clickButton("§e[CHANGELOG]", "§eWhat's new", "/nsctabcompleter changelog", RUN),
            plainText(" ")
        ));

        if (plugin.isAvailableUpdate())
        {
            TextComponent updateLink = new TextComponent("§4[DOWNLOAD LINK]");
            TextComponent outdatedMessage = new TextComponent(" §7[§4✦§7] §cYou are using outdated version ");

            updateLink.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.spigotmc.org/threads/nsc-tabcompleter.685108/"));
            updateLink.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§4Download our latest version")));

            sender.sendMessage(" ");
            sender.spigot().sendMessage(new BaseComponent[] { outdatedMessage, updateLink });
        }
        
        var configManager = plugin.getConfigManager();

        if (configManager.isQuickStatisticsEnabled())
        {
            sender.sendMessage(" ");

            int totalGroups   = configManager.getCurrentGroups().size();
            int totalCommands = configManager.getCurrentGroups().values().stream().mapToInt(g -> g.getCommands().size()).sum();
            int onlineUsers = Bukkit.getOnlinePlayers().size();

            sender.sendMessage(" §7♦ Groups: §d" + totalGroups + " §7§l◈ §7Commands: §d" + totalCommands + " §7§l◈ §7Online: §d" + onlineUsers);    
        }

        sender.sendMessage(" ");
        sender.sendMessage(BOTTOM);
    }

    private void sendVersion(CommandSender sender)
    {
        sender.sendMessage(PREFIX + "Current version: §5v" + Main.plugin.getDescription().getVersion());
    }

    private void handleReload(CommandSender sender)
    {
        if (!sender.hasPermission("nsctab.reload"))
        {
            sender.sendMessage(ERROR + "You don't have permission!");
            return;
        }

        plugin.getConfigManager().loadConfigurationFile();
        Bukkit.getOnlinePlayers().forEach(Player::updateCommands);
        sender.sendMessage(PREFIX + "Configuration reloaded successfully.");
    }
    /*public static String centerText(String text)
    {
        if (text == null || text.isEmpty())
            return "";
    
        String plain = text.replaceAll("§[0-9a-fk-orx]", "");
    
        final int CHAT_WIDTH = 65;
    
        int padding = Math.max(0, (CHAT_WIDTH - plain.length()) / 2);
    
        return " ".repeat(padding) + text;
    }*/
    private void handleHelp(CommandSender sender, String[] args)
    {
        if (!sender.hasPermission("nsctab.help"))
        {
            sender.sendMessage(ERROR + "You don't have permission!");
            return;
        }

        sender.sendMessage(TOP);
        sender.sendMessage(" ");

        sender.sendMessage(ChatUtil.centerText(ChatUtil.colour("<#a800a8, #f51063, #ff8e44>NSC TabCompleter</#Gradient>")));
        sender.sendMessage("§r " + ChatUtil.centerText(ChatUtil.colour("<#a800a8, #f51063, #ff8e44>┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄</#Gradient>")));

        String category = args.length > 1 ? args[1].toLowerCase(Locale.ROOT) : "";

        switch (category)
        {
            case "group":
            {
                sender.sendMessage(ChatUtil.centerText("§7M A N A G E  G R O U P"));
                sender.sendMessage(" ");

                helpRow(sender, "§rInfo", "/nsctabcompleter manage group info", "           §7‹ Group details. ›");
                break;
            }
            case "other":
            {

                break;
            }
            default:
            {
                sender.sendMessage(ChatUtil.centerText("§7M E N U §8♦ §7H E L P"));
                sender.sendMessage(" ");

                helpRow(sender, "§rReload", "/nsctabcompleter reload", "          §7‹ Reload configuration file. ›");
                helpRow(sender, "§rManage", "/nsctabcompleter manage ", "         §7‹ Management tools. ›");
                helpRow(sender, "§rUpdate", "/nsctabcompleter update ", "          §7‹ Refresh player commands. ›");
                helpRow(sender, "§rVersion", "/nsctabcompleter version", "         §7‹ Show current version of plugin. ›");
                helpRow(sender, "§rChangelog", "/nsctabcompleter changelog", "      §7‹ Show latest changelog. ›");

                sender.sendMessage(" ");

                sender.spigot().sendMessage(buildRow(
                    plainText(" §7➜ "),
                    clickButton("§3[MANAGE GROUP]", "§3Manage groups help", "/nsctabcompleter help group", RUN),
                    plainText(" "),
                    clickButton("§6[MANAGE OTHER]", "§6Manage other things help", "/nsctabcompleter help other", RUN)
                ));
                break;
            }
        }

        sender.sendMessage(" ");
        sender.sendMessage(BOTTOM);
    }

    private void handleChangelog(CommandSender sender)
    {
        if (!sender.hasPermission("nsctab.changelog"))
        {
            sender.sendMessage(ERROR + "You don't have permission!");
            return;
        }

        sender.sendMessage("§8─── §5•§d♦§5• §8── §dv3.0.0 §8── §5• §d« Change Log » §5• §8── §5•§d♦§5• §8───");
        sender.sendMessage(" ");
        sender.sendMessage("  §2•§a♦§2• §aWhat's New:");
        sender.sendMessage(" ");
        sender.sendMessage(" §2♦ §aRefactored ConfigurationFileManager for 3.0.0.");
        sender.sendMessage(" ");
        sender.sendMessage(" §4♦ §cFixed blacklist permission check (was checking wrong perm node).");
        sender.sendMessage(" §4♦ §cFixed isCommandAllowed logic duplication across classes.");
        sender.sendMessage(" ");
        sender.sendMessage("§8─── §5•§d♦§5• ─────────────── §5• §d« » §5• ─────────────── §5•§d♦§5• §8───");
    }

    private void handleUpdate(CommandSender sender, String[] args)
    {
        if (!sender.hasPermission("nsctab.update.player") && !sender.hasPermission("nsctab.update.all"))
        {
            sender.sendMessage(ERROR + "You don't have permission!");
            return;
        }

        if (args.length < 2)
        {
            sender.sendMessage(ERROR + "Usage: /nsctabcompleter update <all|player> [name]");
            return;
        }

        if (args[1].equalsIgnoreCase("all"))
        {
            Bukkit.getOnlinePlayers().forEach(Player::updateCommands);
            sender.sendMessage(PREFIX + "Updated commands for all online players.");
        }
        else if (args[1].equalsIgnoreCase("player"))
        {
            if (args.length < 3)
            {
                sender.sendMessage(ERROR + "You must specify a player name.");
                return;
            }

            Player target = Bukkit.getPlayer(args[2]);
            if (target == null)
            {
                sender.sendMessage(ERROR + "Player not found or not online.");
                return;
            }

            target.updateCommands();
            sender.sendMessage(PREFIX + "Updated commands for §5" + target.getName() + "§d.");
        }
        else
        {
            sender.sendMessage(ERROR + "Unknown option. Use §5all§d or §5player§d.");
        }
    }

    private void handleGroupInfo(CommandSender sender, String[] args)
    {
        if (!sender.hasPermission("nsctab.groups.information"))
        {
            sender.sendMessage(ERROR + "You don't have permission!");
            return;
        }

        if (args.length < 3 || args[2].isBlank())
        {
            sender.sendMessage(ERROR + "Usage: /nsctabcompleter group information <group>");
            return;
        }

        String groupName = args[2].toLowerCase();

        if (!plugin.getConfigManager().isGroupExist(groupName))
        {
            sender.sendMessage(ERROR + "Group '§5" + groupName + "§d' does not exist.");
            return;
        }

        var group = plugin.getConfigManager().getGroupManager(groupName);
        int playerCount = plugin.getConfigManager().getGroupPlayerCount(groupName);

        sender.sendMessage("§8─── §5•§d♦§5• §8── §5• §d« Group Information » §5• §8── §5•§d♦§5• §8───");
        sender.sendMessage(" ");
        sender.sendMessage("  §8•§r♦§8• §7Group: §d" + groupName);
        sender.sendMessage(" ");
        sender.sendMessage(" §8♦ §rMode        §7‹ §d" + group.getMode() + " §7›");
        sender.sendMessage(" §8♦ §rPriority    §7‹ §d" + group.getPriority() + " §7›");
        sender.sendMessage(" §8♦ §rCommands    §7‹ §d" + group.getCommands() + " §7›");
        sender.sendMessage(" §8♦ §rPlayers     §7‹ §d" + playerCount + " online §7›");
        sender.sendMessage(" ");
        sender.sendMessage("§8─── §5•§d♦§5• ─────────────── §5• §d« » §5• ─────────────── §5•§d♦§5• §8───");
    }

    // ─── Utility ───────────────────────────────────────────────────────────────

    // Creates a TextComponent with no interactivity.
    private TextComponent plainText(String text)
    {
        return new TextComponent(text);
    }

    // Creates a clickable button with a hover tooltip.
    private TextComponent clickButton(String label, String hoverText, String command, ClickEvent.Action action)
    {
        TextComponent component = new TextComponent(label);
        component.setClickEvent(new ClickEvent(action, command));
        component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(hoverText)));
        return component;
    }

    // Assembles multiple BaseComponents into a single array for spigot().sendMessage().
    private BaseComponent[] buildRow(BaseComponent... parts)
    {
        return parts;
    }

    private void helpRow(CommandSender sender, String label, String command, String description)
    {
        if (!(sender instanceof Player player))
            {
                sender.sendMessage("- " + label);
                return;
        }

        player.spigot().sendMessage(buildRow(
            plainText(" §e♦ "),
            clickButton(label, "§7Click to auto-fill\n§f" + command, command, SUGGEST),
            plainText(description)
        ));
    }
}