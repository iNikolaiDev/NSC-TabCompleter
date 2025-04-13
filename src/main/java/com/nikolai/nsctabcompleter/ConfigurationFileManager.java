package com.nikolai.nsctabcompleter;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ConfigurationFileManager
{
    private final Main plugin;
    private File configFile;
    private FileConfiguration config;
    private Map<String, initializeGroup> groups;

    public ConfigurationFileManager(Main plugin)
    {
        this.plugin = plugin;
        this.groups = new HashMap<>();
        loadConfigurationFile();
    }
    public void loadConfigurationFile()
    {
        if (configFile == null)
        {
            configFile = new File(plugin.getDataFolder(), "config.yml");
        }
        if (!configFile.exists())
        {
            plugin.saveDefaultConfig();
            Bukkit.getConsoleSender().sendMessage("§8[NSC TabCompleter]: §dconfig.yml does not exist, Default config loaded successfully.");
        }
        else
        {
            config = YamlConfiguration.loadConfiguration(configFile);
            Bukkit.getConsoleSender().sendMessage("§8[NSC TabCompleter]: §dConfig loaded successfully, Groups are being processed.");
            initializeGroups();
        }
    }
    public void saveConfigurationFile()
    {
        try
        {
            config.save(configFile);
        }
        catch (IOException e)
        {
            plugin.getLogger().severe("Could not save config.yml: " + e.getMessage());
        }
    }
    private void initializeGroups()
    {
        groups.clear();
        if (config.isConfigurationSection("groups"))
        {
            for (String group : config.getConfigurationSection("groups").getKeys(false))
            {
                String path = "groups." + group;
                String mode = config.getString(path + ".mode", "blacklist");

                Set<String> groupCommands = new HashSet<>(config.getStringList(path + ".commands"));
                List<String> commands = new ArrayList<>(groupCommands);

                int priority = config.getInt(path + ".priority", 0);
                groups.put(group, new initializeGroup(mode, commands, priority));
            }
        }
    }
    public List<String> getPlayerGroups(Player player)
    {
        List<String> groupsList = new ArrayList<>();

        for (String group : config.getConfigurationSection("groups").getKeys(false))
        {
            if (player.hasPermission("nsctab.group." + group))
            {
                groupsList.add(group);
            }
        }
        return groupsList;
    }
    public Set<String> getGroupsCommands(List<String> groups, Set<String> originalCommands, Player player)
    {
        Set<String> commands = new LinkedHashSet<>(originalCommands);
        Set<String> whitelistCommands = new HashSet<>();
        Set<String> blacklistCommands = new HashSet<>();
        boolean hasWhitelist = false;
        boolean hasBlacklist = false;
        int maxPriority = Integer.MIN_VALUE;
        List<initializeGroup> prioritizedGroups = new ArrayList<>();

        for (String group : groups)
        {
            initializeGroup groupInfo = getGroupManager(group);
            int priority = groupInfo.getPriority();

            if (priority > maxPriority)
            {
                maxPriority = priority;
                prioritizedGroups.clear();
                prioritizedGroups.add(groupInfo);
            }
            else if (priority == maxPriority)
            {
                prioritizedGroups.add(groupInfo);
            }
        }

        for (initializeGroup groupInfo : prioritizedGroups)
        {
            if (groupInfo.getMode().equalsIgnoreCase("blacklist"))
            {
                for (String cmd : originalCommands)
                {
                    String lowerCase = cmd.toLowerCase();
                    if (player.hasPermission("nsctab.blacklist.command." + lowerCase))
                    {
                        if (!player.isOp() || player.isOp() && isOpByPassTrue())
                        {
                            blacklistCommands.add(cmd);
                        }
                    }
                }
                blacklistCommands.addAll(groupInfo.getCommands());
                hasBlacklist = true;
            }
            else if (groupInfo.getMode().equalsIgnoreCase("whitelist"))
            {
                whitelistCommands.addAll(groupInfo.getCommands());

                for (String cmd : originalCommands)
                {
                    String lowerCase = cmd.toLowerCase();
                    if (player.hasPermission("nsctab.whitelist.command." + lowerCase))
                    {
                        if (!player.isOp() || player.isOp() && isOpByPassTrue())
                        {
                            whitelistCommands.add(cmd);
                        }
                    }
                }
                hasWhitelist = true;
            }
        }

        if (hasWhitelist && hasBlacklist)
        {
            whitelistCommands.removeAll(blacklistCommands);
            return whitelistCommands;
        }
        else if (hasWhitelist)
        {
            return whitelistCommands;
        }
        else
        {
            commands.removeAll(blacklistCommands);
            return commands;
        }
    }
    public int getGroupPlayers(String group)
    {
        int count = 0;
        for (Player player : Bukkit.getOnlinePlayers())
        {
            if (player.hasPermission("nsctab.group." + group)) 
            {
                count++;
            }
        }
        return count;
    }
    public Boolean isOpByPassTrue()
    {
        return config.getBoolean("settings.op-bypass");
    }
    public Boolean isTabComplationTrue()
    {
        return config.getBoolean("settings.tab-complation");
    }
    public Boolean isBlockExecutionTrue()
    {
        return config.getBoolean("settings.block-execution");
    }
    public Boolean groupPrioritizationStatus()
    {
        return config.getBoolean("settings.prioritization-enabled");
    }
    public String insufficientPermissionMessage()
    {
        return config.getString("settings.insufficient-permission-message");
    }
    public Map<String, initializeGroup> getCurrentGroups()
    {
        return groups;
    }
    public boolean isGroupExist(String groupName)
    {
        return groups.containsKey(groupName);
    }
    public initializeGroup getGroupManager(String groupName)
    {
        return groups.getOrDefault(groupName, new initializeGroup("blacklist", List.of(), 0));
    }
    public static class initializeGroup
    {
        private String mode;
        private final List<String> commands;
        private int priority;

        public initializeGroup(String mode, List<String> commands, int priority)
        {
            this.mode = mode;
            this.commands = commands;
            this.priority = priority;
        }
        public String getMode()
        {
            return mode;
        }
        public List<String> getCommands()
        {
            return commands;
        }
        public int getPriority()
        {
            return priority;
        }

        public void setMode(String x)
        {
            this.mode = x;
        }
        public void setPriority(int x)
        {
            this.priority = x;
        }
    }
}
