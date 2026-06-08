package com.nikolai.nsctabcompleter.listeners;

import com.nikolai.nsctabcompleter.ConfigurationFileManager;
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
    private final ConfigurationFileManager configManager;

    public CustomTabCompleterListener(Main plugin, String command)
    {
        this.plugin = plugin;
        this.command = command.toLowerCase();
        this.configManager = plugin.getConfigManager();
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args)
    {
        plugin.getConfigManager().logDebug("[NSC TabCompleter] Processing tab completion for command: {0}, alias: {1}, args: {2}, sender: {3}",
                cmd.getName(), alias, Arrays.toString(args), sender.getName());

        if (!(sender instanceof Player))
        {
            plugin.getConfigManager().logDebug("[NSC TabCompleter] Sender is not a player, returning empty list");
            return Collections.emptyList();
        }

        Player player = (Player) sender;

        if (!configManager.isTabComplationTrue())
        {
            plugin.getConfigManager().logDebug("[NSC TabCompleter] Tab completion is disabled in config, returning empty list");
            return Collections.emptyList();
        }

        if (player.isOp() && configManager.isOpByPassTrue())
        {
            plugin.getConfigManager().logDebug("[NSC TabCompleter] Player {0} is OP and op-bypass is enabled, returning all suggestions", player.getName());
            return null;
        }

        List<String> playerGroups = configManager.getPlayerGroups(player);
        plugin.getConfigManager().logDebug("[NSC TabCompleter] Player groups for {0}: {1}", player.getName(), playerGroups);

        String whitelistPerm = "nsctab.whitelist.command." + command;
        String blacklistPerm = "nsctab.blacklist.command." + command;
        boolean isWhitelisted = player.hasPermission(whitelistPerm) && player.isPermissionSet(whitelistPerm);
        boolean isBlacklisted = player.hasPermission(blacklistPerm)  && player.isPermissionSet(whitelistPerm);

        plugin.getConfigManager().logDebug("[NSC TabCompleter] Permission check for {0}: whitelist={1}, blacklist={2}",
                command, isWhitelisted, isBlacklisted);

        if (isBlacklisted)
        {
            plugin.getConfigManager().logDebug("[NSC TabCompleter] Command {0} is blacklisted for player {1}, returning empty list",
                    command, player.getName());

            return Collections.emptyList();
        }

        if (!isWhitelisted && playerGroups.isEmpty())
        {
            plugin.getConfigManager().logDebug("[NSC TabCompleter] Player {0} has no whitelist permission and no groups, returning empty list",
                    player.getName());

            return Collections.emptyList();
        }

        Set<String> suggestions = configManager.getSubArgsForCommand(playerGroups, command, args);
        plugin.getConfigManager().logDebug("[NSC TabCompleter] Raw suggestions for command {0}: {1}", command, suggestions);

        String lastArg = args.length > 0 ? args[args.length - 1].toLowerCase() : "";
        List<String> filteredSuggestions = StringUtil.copyPartialMatches(lastArg, suggestions, new ArrayList<>());
        Collections.sort(filteredSuggestions);
        plugin.getConfigManager().logDebug("[NSC TabCompleter] Filtered and sorted suggestions: {0}", filteredSuggestions);

        if (filteredSuggestions.isEmpty())
        {
            plugin.getConfigManager().logDebug("[NSC TabCompleter] No suggestions found for command {0}, returning null", command);
            return null;
        }

        return filteredSuggestions;
    }
}