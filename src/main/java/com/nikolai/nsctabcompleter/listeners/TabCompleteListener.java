package com.nikolai.nsctabcompleter.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandSendEvent;

import com.nikolai.nsctabcompleter.Main;

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
    public void OnTabComplete(PlayerCommandSendEvent event)
    {
        Player player = event.getPlayer();

        boolean tabCompletionEnabled = plugin.getConfigManager().isTabComplationTrue();
        boolean hasBypassPerm = player.hasPermission("nsctab.bypass.commands.tabcomplation");
        boolean hasIncludePerm = player.hasPermission("nsctab.include.commands.tabcomplation");

        if (!tabCompletionEnabled && !hasIncludePerm) return;
        if (hasBypassPerm && !hasIncludePerm) return;

        Set<String> currentCommands = new HashSet<>(event.getCommands());
        List<String> playerGroups = plugin.getConfigManager().getPlayerGroups(player);
        Set<String> allowedCommands = plugin.getConfigManager().getGroupsCommands(playerGroups, currentCommands, player);

        event.getCommands().clear();
        event.getCommands().addAll(allowedCommands);
    }
}
