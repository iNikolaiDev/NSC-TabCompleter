package com.nikolai.nsctabcompleter.listeners;

import com.nikolai.nsctabcompleter.Main;
import com.nikolai.nsctabcompleter.utilities.Colorization;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.*;

public class CommandBlockerListener implements Listener
{
    private final Main plugin;
    private final Map<String, String> aliasCommands;

    public CommandBlockerListener(Main plugin)
    {
        this.plugin = plugin;
        this.aliasCommands = new HashMap<>();

        for (Command cmd : plugin.getServer().getCommandMap().getKnownCommands().values())
        {
            String command = cmd.getName().toLowerCase();
            aliasCommands.put(command, command);

            for (String alias : cmd.getAliases())
            {
                aliasCommands.put(alias.toLowerCase(), command);
            }
        }
    }

    @EventHandler
    public void onCommandPreprocess(PlayerCommandPreprocessEvent event)
    {
        Player player = event.getPlayer();

        boolean blockExecutionEnabled = plugin.getConfigManager().isBlockExecutionTrue();
        boolean hasBypassPerm = player.hasPermission("nsctab.bypass.commands.execution");
        boolean hasIncludePerm = player.hasPermission("nsctab.include.commands.execution");

        if (!blockExecutionEnabled && !hasIncludePerm) return;
        if (hasBypassPerm && !hasIncludePerm) return;

        String message = event.getMessage().toLowerCase().trim();
        String inputCommand = message.split(" ")[0].substring(1);
        String normalizedCommand = aliasCommands.getOrDefault(inputCommand, inputCommand);

        List<String> playerGroups = plugin.getConfigManager().getPlayerGroups(player);
        Set<String> allowedCommands = plugin.getConfigManager().getGroupsCommands(playerGroups, plugin.getServer().getCommandMap().getKnownCommands().keySet(), player);

        if (!allowedCommands.contains(normalizedCommand) && !allowedCommands.contains(inputCommand))
        {
            String messageDenied = Colorization.applyColorization(plugin.getConfigManager().insufficientPermissionMessage());
            event.setCancelled(true);
            player.sendMessage(messageDenied);
        }
    }
}
