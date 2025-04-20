package com.nikolai.nsctabcompleter.listeners;

import com.nikolai.nsctabcompleter.ConfigurationFileManager;
import com.nikolai.nsctabcompleter.Main;
import com.nikolai.nsctabcompleter.utilities.Colorization;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class CommandBlockerListener implements Listener
{
    private final Main plugin;
    private final ConfigurationFileManager configManager;

    public CommandBlockerListener(Main plugin)
    {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
        plugin.getConfigManager().logDebug("CommandBlockerListener initialized for plugin: {0}", plugin.getName());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event)
    {
        plugin.getConfigManager().logDebug("Processing command preprocess event for player: {0}, command: {1}",
                event.getPlayer().getName(), event.getMessage());

        Player player = event.getPlayer();
        String message = event.getMessage().toLowerCase().trim();
        String command = message.startsWith("/") ? message.substring(1).split(" ")[0] : message.split(" ")[0];

        if (!configManager.isBlockExecutionTrue())
        {
            plugin.getConfigManager().logDebug("Block execution is disabled in config, allowing command: {0}", command);
            return;
        }

        if (player.isOp() && configManager.isOpByPassTrue())
        {
            plugin.getConfigManager().logDebug("Player {0} is OP and op-bypass is enabled, allowing command: {1}",
                    player.getName(), command);
            return;
        }

        List<String> playerGroups = configManager.getPlayerGroups(player);
        plugin.getConfigManager().logDebug("Player groups for {0}: {1}", player.getName(), playerGroups);

        String whitelistPerm = "nsctab.whitelist.command." + command;
        String blacklistPerm = "nsctab.blacklist.command." + command;
        boolean isWhitelisted = player.hasPermission(whitelistPerm) && player.isPermissionSet(whitelistPerm);
        boolean isBlacklisted = player.hasPermission(blacklistPerm);

        plugin.getConfigManager().logDebug("Permission check for command {0}: whitelist={1}, blacklist={2}",
                command, isWhitelisted, isBlacklisted);

        if (isBlacklisted)
        {
            plugin.getLogger().log(Level.WARNING, "[NSC TabCompleter] Command {0} blocked for player {1} due to blacklist permission",
                    new Object[]{command, player.getName()});
            event.setCancelled(true);
            player.sendMessage(configManager.insufficientPermissionMessage());
            plugin.getConfigManager().logDebug("Command {0} cancelled for player {1}", command, player.getName());
            return;
        }

        if (!isWhitelisted && playerGroups.isEmpty())
        {
            plugin.getLogger().log(Level.WARNING, "[NSC TabCompleter] Command {0} blocked for player {1} due to no whitelist permission or groups",
                    new Object[]{command, player.getName()});
            event.setCancelled(true);
            player.sendMessage(configManager.insufficientPermissionMessage());
            plugin.getConfigManager().logDebug("Command {0} cancelled for player {1} due to no permissions", command, player.getName());
            return;
        }

        boolean isCommandAllowed = checkGroupPermissions(playerGroups, command);
        plugin.getConfigManager().logDebug("Group permission check for command {0}: allowed={1}", command, isCommandAllowed);

        if (!isCommandAllowed)
        {
            plugin.getLogger().log(Level.WARNING, "[NSC TabCompleter] Command {0} blocked for player {1} due to group restrictions",
                    new Object[]{command, player.getName()});
            event.setCancelled(true);
            player.sendMessage(Colorization.applyColorization(configManager.insufficientPermissionMessage()));
            plugin.getConfigManager().logDebug("Command {0} cancelled for player {1} due to group restrictions", command, player.getName());
        } 
        else
        {
            plugin.getConfigManager().logDebug("Command {0} allowed for player {1}", command, player.getName());
        }
    }

    private boolean checkGroupPermissions(List<String> playerGroups, String command)
    {
        plugin.getConfigManager().logDebug("Checking group permissions for command: {0}, groups: {1}", command, playerGroups);
        boolean hasWhitelist = false;
        boolean hasBlacklist = false;
        boolean commandInWhitelist = false;
        boolean commandInBlacklist = false;

        List<ConfigurationFileManager.initializeGroup> groups = playerGroups.stream()
                .map(configManager::getGroupManager)
                .collect(Collectors.toList());

        if (groups.isEmpty())
        {
            groups.add(configManager.getGroupManager("default"));
            plugin.getConfigManager().logDebug("No groups found, using default group");
        }

        if (configManager.groupPrioritizationStatus())
        {
            final int maxPriority = groups.stream()
                    .mapToInt(ConfigurationFileManager.initializeGroup::getPriority)
                    .max()
                    .orElse(Integer.MIN_VALUE);
            groups = groups.stream()
                    .filter(group -> group.getPriority() == maxPriority)
                    .collect(Collectors.toList());
            plugin.getConfigManager().logDebug("Filtered groups by max priority {0}: {1}", maxPriority,
                    groups.stream().map(g -> g.getMode()).collect(Collectors.toList()));
        } 
        else
        {
            plugin.getConfigManager().logDebug("Prioritization disabled, using all groups: {0}",
                    groups.stream().map(g -> g.getMode()).collect(Collectors.toList()));
        }

        for (ConfigurationFileManager.initializeGroup group : groups)
        {
            List<String> groupCommands = group.getCommands();
            plugin.getConfigManager().logDebug("Checking group mode: {0}, commands: {1}", group.getMode(), groupCommands);

            if (group.getMode().equalsIgnoreCase("whitelist"))
            {
                hasWhitelist = true;
                if (groupCommands.contains(command))
                {
                    commandInWhitelist = true;
                    plugin.getConfigManager().logDebug("Command {0} found in whitelist group", command);
                }
            } 
            else if (group.getMode().equalsIgnoreCase("blacklist"))
            {
                hasBlacklist = true;
                if (groupCommands.contains(command))
                {
                    commandInBlacklist = true;
                    plugin.getConfigManager().logDebug("Command {0} found in blacklist group", command);
                }
            }
        }

        plugin.getConfigManager().logDebug("Combining group permissions: hasWhitelist={0}, hasBlacklist={1}, commandInWhitelist={2}, commandInBlacklist={3}",
                hasWhitelist, hasBlacklist, commandInWhitelist, commandInBlacklist);

        if (hasWhitelist && hasBlacklist)
        {
            return commandInWhitelist && !commandInBlacklist;
        } 
        else if (hasWhitelist)
        {
            return commandInWhitelist;
        } 
        else if (hasBlacklist)
        {
            return !commandInBlacklist;
        } 
        else
        {
            return true;
        }
    }
}