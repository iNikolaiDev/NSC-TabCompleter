package com.nikolai.nsctabcompleter.listeners;

import com.nikolai.nsctabcompleter.Main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.*;

public class CustomTabCompleterListener implements TabCompleter
{
    private final Main plugin;
    private final String command;

    public CustomTabCompleterListener(Main plugin, String command)
    {
        this.plugin  = plugin;
        this.command = command.toLowerCase();
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args)
    {
        // Only handle player senders
        if (!(sender instanceof Player player))
        {
            return Collections.emptyList();
        }

        var configManager = plugin.getConfigManager();

        configManager.logDebug(
            "CustomTabComplete → command={0}, alias={1}, args={2}, player={3}",
            command, alias, Arrays.toString(args), player.getName()
        );

        // Global toggle
        if (!configManager.isTabCompletionEnabled())
        {
            return Collections.emptyList();
        }

        // OP bypass → let Bukkit handle suggestions natively
        if (player.isOp() && configManager.isOpBypassEnabled())
        {
            configManager.logDebug("OP bypass for {0} on ''{1}'' — returning null.", player.getName(), command);
            return null;
        }

        // Per-command permission overrides
        String blacklistPerm = "nsctab.blacklist.command." + command;
        String whitelistPerm = "nsctab.whitelist.command." + command;

        boolean isBlacklisted = player.isPermissionSet(blacklistPerm) && player.hasPermission(blacklistPerm);
        boolean isWhitelisted = player.isPermissionSet(whitelistPerm) && player.hasPermission(whitelistPerm);

        if (isBlacklisted)
        {
            configManager.logDebug("Command ''{0}'' blacklisted for {1} via permission.", command, player.getName());
            return Collections.emptyList();
        }

        List<String> playerGroups = configManager.getPlayerGroups(player);

        if (!isWhitelisted && playerGroups.isEmpty())
        {
            configManager.logDebug("Player {0} has no groups and no whitelist perm for ''{1}''.", player.getName(), command);
            return Collections.emptyList();
        }

        // Build suggestion list from sub-argument tree
        Set<String> suggestions = configManager.getSubArgsForCommand(playerGroups, command, args);

        String lastArg = args.length > 0 ? args[args.length - 1].toLowerCase() : "";
        List<String> filtered = StringUtil.copyPartialMatches(lastArg, suggestions, new ArrayList<>());
        Collections.sort(filtered);

        configManager.logDebug("Suggestions for ''{0}'' args={1}: {2}", command, Arrays.toString(args), filtered);

        // Return null (show all) if our list is empty, so Bukkit can still suggest players etc.
        return filtered.isEmpty() ? null : filtered;
    }
}