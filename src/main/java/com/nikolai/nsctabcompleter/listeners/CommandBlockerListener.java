package com.nikolai.nsctabcompleter.listeners;

import com.nikolai.nsctabcompleter.ConfigurationFileManager;
import com.nikolai.nsctabcompleter.Main;
import com.nikolai.nsctabcompleter.utilities.ChatUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.List;

public class CommandBlockerListener implements Listener
{
    @SuppressWarnings("unused")
    private final Main plugin;
    private final ConfigurationFileManager configManager;

    public CommandBlockerListener(Main plugin)
    {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event)
    {
        Player player  = event.getPlayer();
        String message = event.getMessage().toLowerCase().trim();
        String command = message.startsWith("/")
                ? message.substring(1).split("\\s+")[0]
                : message.split("\\s+")[0];

        configManager.logDebug("CommandBlocker → player={0}, command={1}", player.getName(), command);

        // Global toggle
        if (!configManager.isBlockExecutionEnabled())
        {
            configManager.logDebug("Block-execution disabled, passing command ''{0}''.", command);
            return;
        }

        // OP bypass
        if (player.isOp() && configManager.isOpBypassEnabled())
        {
            configManager.logDebug("OP bypass active for {0}, passing command ''{1}''.", player.getName(), command);
            return;
        }

        // Per-command permission overrides (checked before groups)
        String whitelistPerm = "nsctab.whitelist.command." + command;
        String blacklistPerm = "nsctab.blacklist.command." + command;
        boolean isWhitelisted = player.isPermissionSet(whitelistPerm) && player.hasPermission(whitelistPerm);
        boolean isBlacklisted = player.isPermissionSet(blacklistPerm) && player.hasPermission(blacklistPerm);

        if (isBlacklisted)
        {
            block(event, player, command, "per-command blacklist permission");
            return;
        }

        if (isWhitelisted)
        {
            configManager.logDebug("Command ''{0}'' whitelisted via permission for {1}.", command, player.getName());
            return;
        }

        // Group-based check
        List<String> playerGroups = configManager.getPlayerGroups(player);

        if (playerGroups.isEmpty())
        {
            block(event, player, command, "no groups assigned and no whitelist permission");
            return;
        }

        if (!configManager.isCommandAllowed(playerGroups, command))
        {
            block(event, player, command, "group restrictions");
        }
        else
        {
            configManager.logDebug("Command ''{0}'' allowed for {1}.", command, player.getName());
        }
    }

    // ─────────────────────────────────────────────

    private void block(PlayerCommandPreprocessEvent event, Player player, String command, String reason)
    {
        event.setCancelled(true);
        player.sendMessage(ChatUtil.colour(configManager.insufficientPermissionMessage()));
        configManager.logDebug("Command ''{0}'' blocked for {1} — reason: {2}.", command, player.getName(), reason);
    }
}