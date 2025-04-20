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

        boolean isOp = player.isOp();
        boolean tabCompletionEnabled = plugin.getConfigManager().isTabComplationTrue();
        boolean opBypass = plugin.getConfigManager().isOpByPassTrue();
        boolean hasBypassPerm = player.hasPermission("nsctab.bypass.commands.tabcomplation");
        boolean hasIncludePerm = player.hasPermission("nsctab.include.commands.tabcomplation");

        if (isOp && opBypass)
        {
            plugin.getConfigManager().logDebug("OP bypass for " + player.getName());
            return;
        }
        if (!tabCompletionEnabled && !hasIncludePerm)
        {
            plugin.getConfigManager().logDebug("Tab completion disabled for " + player.getName());
            return;
        }
        if (hasBypassPerm && !hasIncludePerm)
        {
            plugin.getConfigManager().logDebug("Bypass permission for " + player.getName());
            return;
        }

        Set<String> currentCommands = new HashSet<>(event.getCommands());
        List<String> playerGroups = plugin.getConfigManager().getPlayerGroups(player);
        Set<String> allowedCommands = plugin.getConfigManager().getGroupsCommands(playerGroups, currentCommands, player);

        event.getCommands().clear();
        event.getCommands().addAll(allowedCommands);
        plugin.getConfigManager().logDebug("Filtered tab completion for " + player.getName() + ": " + allowedCommands.size() + " commands");
    }
}