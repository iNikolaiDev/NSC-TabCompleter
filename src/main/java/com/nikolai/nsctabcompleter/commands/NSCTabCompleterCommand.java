package com.nikolai.nsctabcompleter.commands;

import com.nikolai.nsctabcompleter.ConfigurationFileManager.GroupData;
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
import java.util.Map;
import java.util.stream.Collectors;

public class NSCTabCompleterCommand implements TabExecutor
{
    private final Main plugin;

    // Bars
    private static final String THIN_BAR  = " §8§m                                                         ";

    // Prefix / feedback
    private static final String PREFIX = " §5§l◈ §dNSC §8§l» §d";
    private static final String ERROR  = " §5§l◈ §4§lError §8§l» §c";

    // Badge styles
    private static final String BADGE_WL  = "§2§l[✔ WHITELIST]§r";
    private static final String BADGE_BL  = "§4§l[✖ BLACKLIST]§r";
    private static final String BADGE_OP  = "§e§l[OP]§r";

    // Click actions
    private static final ClickEvent.Action RUN = ClickEvent.Action.RUN_COMMAND;
    private static final ClickEvent.Action SUGGEST = ClickEvent.Action.SUGGEST_COMMAND;

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
            completions.addAll(List.of("reload", "version", "help", "manage", "update", "changelog"));
            return filterStartsWith(completions, args[0]);
        }

        switch (args[0].toLowerCase())
        {
            case "help" -> {
                if (args.length == 2)
                    completions.addAll(List.of("group", "other"));
            }
            case "manage" -> {
                if (args.length == 2)
                    completions.addAll(List.of("group", "settings", "commands", "permissions", "players"));

                if (args.length == 3)
                {
                    switch (args[1].toLowerCase()) 
                    {
                        case "group"       -> completions.addAll(List.of("list", "info"));
                    }
                }
            }
            case "update" -> {
                if (args.length == 2) completions.addAll(List.of("all", "player"));
                if (args.length == 3 && args[1].equalsIgnoreCase("player"))
                    Bukkit.getOnlinePlayers().forEach(p -> completions.add(p.getName()));
            }
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
            case "manage"    -> handleManage(sender, args);
            default -> sender.sendMessage(ERROR + "Unknown sub-command. Use §5/nsctabcompleter help§d.");
        }

        return true;
    }

    // ─── Sub-command Handlers ──────────────────────────────────────────────────

    private void sendOverview(CommandSender sender)
    {
        if (!(sender instanceof Player player))
        {
            sender.sendMessage(THIN_BAR);
            sender.sendMessage(" ");
            sender.sendMessage("                     §8NSC TabCompleter");
            sender.sendMessage("              §7V E R S I O N" + " §8♦ §7" + Main.plugin.getDescription().getVersion());
            sender.sendMessage(" ");
            sender.sendMessage(THIN_BAR);
            return;
        }

        sender.sendMessage(THIN_BAR);
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
        sender.sendMessage(THIN_BAR);
    }

    private void handleManage(CommandSender sender, String[] args)
    {
        if (!(sender instanceof Player p))
        {
            return;
        }

        if (!p.hasPermission("nsctab.reload"))
        {
            p.sendMessage(ERROR + "You don't have permission!");
            return;
        }

        if (args.length < 2)
        {
            sender.sendMessage(THIN_BAR);
            sender.sendMessage(" ");

            sender.sendMessage(ChatUtil.centerText(ChatUtil.colour("<#a800a8, #f51063, #ff8e44>NSC TabCompleter</#Gradient>")));
            sender.sendMessage("§r " + ChatUtil.centerText(ChatUtil.colour("<#a800a8, #f51063, #ff8e44>┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄</#Gradient>")));
            sender.sendMessage(ChatUtil.centerText("§7M E N U" + " §8♦ §7M A N A G E"));

            sender.sendMessage(" ");
            sender.sendMessage(THIN_BAR);
            return;
        }

        switch (args[1].toLowerCase())
        {
            case "group"       -> handleManageGroup(p, args);
        }
    }

    private void handleManageGroup(Player player, String[] args)
    {
        if (args.length < 3)
        {
            showGroupList(player, 1);
            return;
        }

        switch (args[2].toLowerCase())
        {
            case "list"                 -> showGroupList(player, args.length >= 4 ? ChatUtil.parseInt(args[3], 1) : 1);
        }
    }

    private void showGroupList(Player player, int page)
    {
        List<Map.Entry<String, GroupData>> entries = new ArrayList<>(plugin.getConfigManager().getCurrentGroups().entrySet());

        ChatUtil.PaginatedResult<Map.Entry<String, GroupData>> paged = ChatUtil.paginate(entries, page, ChatUtil.GRP_PAGE);

        player.sendMessage(THIN_BAR);
        player.sendMessage(" ");

        player.sendMessage(ChatUtil.centerText(ChatUtil.colour("<#a800a8, #f51063, #ff8e44>NSC TabCompleter</#Gradient>")));
        player.sendMessage("§r " + ChatUtil.centerText(ChatUtil.colour("<#a800a8, #f51063, #ff8e44>┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄</#Gradient>")));
        player.sendMessage(ChatUtil.centerText("§7G R O U P" + " §8♦ §7L I S T"));
        player.sendMessage(" ");

        if (entries.isEmpty())
        {

        }
        else for (Map.Entry<String, GroupData> entry : paged.items)
        {
            String name = entry.getKey();
            //GroupData group = entry.getValue();

            player.spigot().sendMessage(buildRow(
                plainText(" §b♦ "),
                plainText("§9" + name),
                plainText(" "),
                clickButton("§3[INFO]", "§3View commands & details", "nsctabcompleter manage group info " + name + " 1", RUN),
                plainText(" "),
                clickButton("§a[EDIT]", "§aEdit group", "/nsctabcompleter manage group edit" + name, RUN),
                plainText(" "),
                clickButton("§4[DELETE]", "§4Delete group " + name + "\nConfirm by running the command", "/nsctabcompleter manage group remove" + name, RUN),
                plainText(" ")
            ));
        }

        player.sendMessage(" ");

        player.spigot().sendMessage(buildRow(
            plainText(" §7➜ "),
            clickButton("§a[+ NEW GROUP]", "§aCreate a new group", "/nsctabcompleter manage group create ", SUGGEST)

        ));

        player.sendMessage(" ");
        player.sendMessage(THIN_BAR);
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

    private void handleHelp(CommandSender sender, String[] args)
    {
        if (!sender.hasPermission("nsctab.help"))
        {
            sender.sendMessage(ERROR + "You don't have permission!");
            return;
        }

        sender.sendMessage(THIN_BAR);
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

                helpRow(sender, "§rInfo", "/nsctabcompleter manage group info", "              §7‹ Group details. ›");

                sender.sendMessage(" ");

                sender.spigot().sendMessage(buildRow(
                    plainText(" §7➜ "),
                    clickButton("§e[MAIN MENU]", "§eReturn to main menu", "/nsctabcompleter help", RUN),
                    plainText(" "),
                    clickButton("§6[MANAGE OTHER]", "§6Manage other things help", "/nsctabcompleter help other", RUN)
                ));
                break;
            }
            case "other":
            {
                sender.sendMessage(" ");

                sender.spigot().sendMessage(buildRow(
                    plainText(" §7➜ "),
                    clickButton("§e[MAIN MENU]", "§eReturn to main menu", "/nsctabcompleter help", RUN),
                    plainText(" "),
                    clickButton("§6[MANAGE GROUP]", "§6Manage groups help", "/nsctabcompleter help group", RUN)
                ));
                break;
            }
            default:
            {
                sender.sendMessage(ChatUtil.centerText("§7M E N U §8♦ §7H E L P"));
                sender.sendMessage(" ");

                helpRow(sender, "§rReload", "/nsctabcompleter reload", "              §7‹ Reload configuration file. ›");
                helpRow(sender, "§rManage", "/nsctabcompleter manage ", "             §7‹ Management tools. ›");
                helpRow(sender, "§rUpdate", "/nsctabcompleter update ", "              §7‹ Refresh player commands. ›");
                helpRow(sender, "§rVersion", "/nsctabcompleter version", "             §7‹ Show current version of plugin. ›");
                helpRow(sender, "§rChangelog", "/nsctabcompleter changelog", "          §7‹ Show latest changelog. ›");

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
        sender.sendMessage(THIN_BAR);
    }

    private void handleChangelog(CommandSender sender)
    {
        if (!sender.hasPermission("nsctab.changelog"))
        {
            sender.sendMessage(ERROR + "You don't have permission!");
            return;
        }

        sender.sendMessage(THIN_BAR);
        sender.sendMessage(" ");

        sender.sendMessage(ChatUtil.centerText(ChatUtil.colour("<#a800a8, #f51063, #ff8e44>NSC TabCompleter</#Gradient>")));
        sender.sendMessage("§r " + ChatUtil.centerText(ChatUtil.colour("<#a800a8, #f51063, #ff8e44>┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄</#Gradient>")));
        sender.sendMessage(ChatUtil.centerText("§7M E N U §8♦ §7C H A N G E L O G"));
        
        sender.sendMessage(" ");

        sender.sendMessage("  §2✦ §aWhat's new §2➜");
        sender.sendMessage(" ");
        sender.sendMessage("  §6✦ §eWhat's changed §6➜");
        sender.sendMessage(" ");
        sender.sendMessage("  §4✦ §cFixes §4➜");

        sender.sendMessage(" ");
        sender.sendMessage(THIN_BAR);
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