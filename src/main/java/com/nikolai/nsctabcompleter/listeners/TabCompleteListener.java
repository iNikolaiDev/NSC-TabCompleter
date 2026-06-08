package com.nikolai.nsctabcompleter.listeners;

import com.nikolai.nsctabcompleter.Main;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandSendEvent;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TabCompleteListener implements Listener
{
    private final Main plugin;

    public TabCompleteListener(Main plugin)
    {
        this.plugin = plugin;
    }

    @EventHandler
    public void onTabComplete(PlayerCommandSendEvent event)
    {
        Player player = event.getPlayer();

        boolean isOp                  = player.isOp();
        boolean tabCompletionEnabled  = plugin.getConfigManager().isTabCompletionEnabled();
        boolean opBypass              = plugin.getConfigManager().isOpBypassEnabled();
        boolean hasBypassPerm         = player.hasPermission("nsctab.bypass.commands.tabcomplation");
        boolean hasIncludePerm        = player.hasPermission("nsctab.include.commands.tabcomplation");

        // OP with bypass → show everything
        if (isOp && opBypass)
        {
            plugin.getConfigManager().logDebug("Tab-complete: OP bypass for {0}.", player.getName());
            return;
        }

        // Global tab-completion disabled (unless player has include permission)
        if (!tabCompletionEnabled && !hasIncludePerm)
        {
            plugin.getConfigManager().logDebug("Tab-complete: disabled globally, skipping filter for {0}.", player.getName());
            return;
        }

        // Player has bypass perm and no forced-include perm → skip filter
        if (hasBypassPerm && !hasIncludePerm)
        {
            plugin.getConfigManager().logDebug("Tab-complete: bypass permission for {0}.", player.getName());
            return;
        }

        // Resolve which commands the player should see
        Set<String> currentCommands  = new HashSet<>(event.getCommands());
        List<String> playerGroups    = plugin.getConfigManager().getPlayerGroups(player);
        Set<String> allowedCommands  = plugin.getConfigManager().getGroupsCommands(playerGroups, currentCommands, player);

        event.getCommands().clear();
        event.getCommands().addAll(allowedCommands);

        plugin.getConfigManager().logDebug(
            "Tab-complete: {0} sees {1}/{2} commands.",
            player.getName(), allowedCommands.size(), currentCommands.size()
        );
    }
}