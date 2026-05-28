package com.nikolai.nsctabcompleter.commands;

import com.nikolai.nsctabcompleter.Main;
import com.nikolai.nsctabcompleter.utilities.Colorization;
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
import java.util.stream.Collectors;

public class NSCTabCompleterCommand implements TabExecutor
{
    private final Main plugin;

    // ─── Message helpers (clean colour codes) ─────────────────────────────────
    private static final String TOP  = "§8§l╭§8§m                                                      §8§l╮";
    private static final String BOTTOM  = "§8§l╰§8§m                                                      §8§l╯";

    private static final String PREFIX  = " §5♦ §dNSC §5§l› §d";
    private static final String ERROR     = " §5♦ §dError §5§l› §d";

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
            if (sub.equals("group"))  completions.add("information");
            if (sub.equals("update")) completions.addAll(List.of("all", "player"));
            return filterStartsWith(completions, args[1]);
        }

        if (args.length == 3)
        {
            if (sub.equals("update") && args[1].equalsIgnoreCase("player"))
            {
                Bukkit.getOnlinePlayers().forEach(p -> completions.add(p.getName()));
            }
            if (sub.equals("group") && args[1].equalsIgnoreCase("information"))
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
            case "help"      -> handleHelp(sender);
            case "changelog" -> handleChangelog(sender);
            case "update"    -> handleUpdate(sender, args);
            case "group"     ->
            {
                if (args.length >= 2 && args[1].equalsIgnoreCase("information"))
                    handleGroupInfo(sender, args);
                else
                    sender.sendMessage(ERROR + "Usage: /nsctabcompleter group information <group>");
            }
            default -> sender.sendMessage(ERROR + "Unknown sub-command. Use §5/nsctabcompleter help§d.");
        }

        return true;
    }

    // ─── Sub-command Handlers ──────────────────────────────────────────────────

    @SuppressWarnings("deprecation")
    private void sendOverview(CommandSender sender)
    {
        if (sender instanceof Player player)
        {
            TextComponent first     = new TextComponent(" §7➜ ");
            TextComponent space     = new TextComponent(" ");
            TextComponent help      = clickable(Colorization.applyColorization("§6[HELP]"),      "/nsctabcompleter help",       "Click for help.");
            TextComponent reload    = clickable("§a[RELOAD]",    "/nsctabcompleter reload",     "Click to reload config.");
            TextComponent update    = clickable("§d[UPDATE]",    "/nsctabcompleter update all", "Click to update all players.");
            TextComponent changelog = clickable("§e[CHANGELOG]", "/nsctabcompleter changelog",  "Click for changelog.");

            TextComponent updateLink = new TextComponent("§4[DOWNLOAD LINK]");
            TextComponent outdatedMessage = new TextComponent("§7[§4✦§7] §cYou are using outdated version");

            updateLink.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.spigotmc.org/threads/nsc-tabcompleter.685108/"));
            updateLink.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Download our latest version")));

            sender.sendMessage(TOP);
            sender.sendMessage(" ");
            sender.sendMessage("                   " + Colorization.applyColorization("<#a800a8, #f51063, #ff8e44>NSC TabCompleter</#Gradient>"));
            sender.sendMessage("§7            V E R S I O N" + " §8♦ §7" + Main.plugin.getDescription().getVersion());
            sender.sendMessage(" ");
            player.spigot().sendMessage(new BaseComponent[]{ first, help, space, reload, space, update, space, changelog });

            sender.sendMessage(" ");

            if (plugin.isAvailableUpdate())
            {
                    sender.spigot().sendMessage(new BaseComponent[] { space, outdatedMessage, space, updateLink });
                    sender.sendMessage(" ");
            }
            sender.sendMessage(BOTTOM);
        }
        else
        {
            sender.sendMessage(TOP);
            sender.sendMessage(" §7[§d✦§7] §5NSC TabCompleter §dv" + Main.plugin.getDescription().getVersion() + " §7| §5Made by §dNikolai");
            sender.sendMessage(BOTTOM);
        }
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

    private void handleHelp(CommandSender sender)
    {
        if (!sender.hasPermission("nsctab.help"))
        {
            sender.sendMessage(ERROR + "You don't have permission!");
            return;
        }

        sender.sendMessage("§8─── §5•§d♦§5• §8── §5• §d« Help » §5• §8─────────────── §5•§d♦§5• §8───");
        sender.sendMessage(" ");
        sender.sendMessage("  §8•§r♦§8• §7Sub-commands");
        sender.sendMessage(" ");
        sender.sendMessage(" §8♦ §rReload      §7‹ Reload configuration file. ›");
        sender.sendMessage(" §8♦ §rGroup       §7‹ Manage groups. ›");
        sender.sendMessage(" §8♦ §rVersion     §7‹ Show current plugin version. ›");
        sender.sendMessage(" §8♦ §rChangelog   §7‹ Show update changelog. ›");
        sender.sendMessage(" §8♦ §rUpdate      §7‹ Update player command lists. ›");
        sender.sendMessage(" ");
        sender.sendMessage("  §8•§r♦§8• §dGroup §7sub-command args");
        sender.sendMessage(" ");
        sender.sendMessage(" §8♦ §rInformation §7‹ Get group info. ›");
        sender.sendMessage(" ");
        sender.sendMessage("§8─── §5•§d♦§5• ─────────────── §5• §d« » §5• ─────────────── §5•§d♦§5• §8───");
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

    private TextComponent clickable(String text, String command, String hoverText)
    {
        TextComponent comp = new TextComponent(text);
        comp.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));
        comp.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(hoverText)));
        return comp;
    }
}