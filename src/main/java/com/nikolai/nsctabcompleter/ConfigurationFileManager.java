package com.nikolai.nsctabcompleter;

import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.nikolai.nsctabcompleter.listeners.CustomTabCompleterListener;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

/* 
 * test twatwatawmewqr
 */
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
            plugin.getLogger().log(Level.INFO, "[NSC TabCompleter] Config file saved successfully");
        }
        catch (IOException e)
        {
            plugin.getLogger().log(Level.SEVERE, "[NSC TabCompleter] Failed to save config.yml: {0}", e.getMessage());
        }
    }

    private void initializeGroups()
    {
        groups.clear();

        if (config.isConfigurationSection("groups"))
        {
            logDebug("[NSC TabCompleter] Found groups section in config, processing groups");
            Set<String> groupKeys = config.getConfigurationSection("groups").getKeys(false);
            logDebug("[NSC TabCompleter] Groups found: {0}", groupKeys);

            for (String group : groupKeys)
            {
                String path = "groups." + group;
                String mode = config.getString(path + ".mode", "blacklist");
                int priority = config.getInt(path + ".priority", 0);
                logDebug("[NSC TabCompleter] Processing group: {0}, mode: {1}, priority: {2}", group, mode, priority);
    
                List<String> groupCommands = config.getStringList(path + ".commands");
                logDebug("[NSC TabCompleter] Commands for group {0}: {1}", group, groupCommands);
    
                Set<String> commands = groupCommands.stream()
                        .filter(entry -> mode.equalsIgnoreCase("blacklist") ? !entry.contains(" ") : true)
                        .map(entry -> entry.trim().toLowerCase().split(" ")[0])
                        .collect(Collectors.toSet());

                logDebug("[NSC TabCompleter] Main commands extracted for group {0}: {1}", group, commands);
    
                initializeGroup getGroup = new initializeGroup(mode, new ArrayList<>(commands), priority);
                logDebug("[NSC TabCompleter] Created initializeGroup for {0} with commands: {1}", group, commands);

                for (String entry : groupCommands)
                {
                    String[] parts = entry.trim().toLowerCase().split(" ");
                    logDebug("[NSC TabCompleter] Processing command entry: {0}", entry);

                    if (parts.length == 0)
                    {
                        plugin.getLogger().log(Level.WARNING, "[NSC TabCompleter] Empty command entry skipped for group: {0}", group);
                        continue;
                    }

                    String command = parts[0];
                    String[] subArgs = Arrays.copyOfRange(parts, 1, parts.length);

                    if (subArgs.length > 0)
                    {
                        getGroup.addCommand(command, subArgs);
                    }
                    else
                    {
                        logDebug("[NSC TabCompleter] Skipping command {0} with no sub-arguments", command);
                    }
                }

                groups.put(group, getGroup);
                logDebug("[NSC TabCompleter] Group {0} added with {1} commands", group, commands.size());
            }

            logDebug("[NSC TabCompleter] Registering tab completers for groups");
            registerTabCompleters();
        }
        else
        {
            plugin.getLogger().log(Level.WARNING, "[NSC TabCompleter] No groups section found in config.yml");
        }
    }

    public void registerTabCompleters()
    {
        logDebug("[NSC TabCompleter] Registering tab completers");
        Set<String> commands = getCommands();
        logDebug("[NSC TabCompleter] Commands to register: {0}", commands);

        for (String command : commands)
        {
            PluginCommand pluginCommand = plugin.getServer().getPluginCommand(command);
            logDebug("[NSC TabCompleter] Processing command: {0}", command);

            if (pluginCommand != null)
            {
                pluginCommand.setTabCompleter(new CustomTabCompleterListener(plugin, command));
                logDebug("[NSC TabCompleter] Registered tab completer for command: {0}", command);
            }
            else
            {
                plugin.getLogger().log(Level.WARNING, "[NSC TabCompleter] Command not found: {0}", command);
            }
        }
        logDebug("[NSC TabCompleter] Finished registering tab completers, total: {0}", commands.size());
    }

    public List<String> getPlayerGroups(Player player)
    {
        logDebug("[NSC TabCompleter] Getting groups for player: {0}", player.getName());
        List<String> groupsList = new ArrayList<>();

        if (config.isConfigurationSection("groups"))
        {
            logDebug("[NSC TabCompleter] Checking permissions for groups");
            Set<String> groupKeys = config.getConfigurationSection("groups").getKeys(false);
            logDebug("[NSC TabCompleter] Groups available: {0}", groupKeys);

            for (String group : groupKeys)
            {
                String permission = "nsctab.group." + group;

                if (player.hasPermission(permission))
                {
                    groupsList.add(group);
                    logDebug("[NSC TabCompleter] Player {0} has permission {1}, added group: {2}", player.getName(), permission, group);
                }
                else
                {
                    logDebug("[NSC TabCompleter] Player {0} does not have permission {1}", player.getName(), permission);
                }
            }
        }
        else
        {
            plugin.getLogger().log(Level.WARNING, "[NSC TabCompleter] No groups section found for player: {0}", player.getName());
        }

        if (groupsList.isEmpty() && groups.containsKey("default"))
        {
            groupsList.add("default");
            logDebug("[NSC TabCompleter] No groups found for player {0}, assigned default group", player.getName());
        }

        logDebug("[NSC TabCompleter] Groups for player {0}: {1}", player.getName(), groupsList);
        return groupsList;
    }

    public Set<String> getGroupsCommands(List<String> groups, Set<String> originalCommands, Player player)
    {
        logDebug("[NSC TabCompleter] Getting commands for groups: {0}, player: {1}, originalCommands: {2}", groups, player.getName(), originalCommands);
        Set<String> commands = new LinkedHashSet<>();
        Set<String> blacklistCommands = new HashSet<>();
        boolean hasWhitelist = false;
        boolean hasBlacklist = false;

        int maxPriority = Integer.MIN_VALUE;
        List<initializeGroup> prioritizedGroups = new ArrayList<>();
        logDebug("[NSC TabCompleter] Finding groups with highest priority");

        for (String group : groups)
        {
            initializeGroup groupInfo = getGroupManager(group);
            int priority = groupInfo.getPriority();
            logDebug("[NSC TabCompleter] Group: {0}, priority: {1}", group, priority);

            if (priority > maxPriority)
            {
                maxPriority = priority;
                prioritizedGroups.clear();
                prioritizedGroups.add(groupInfo);
                logDebug("[NSC TabCompleter] New max priority: {0}, cleared previous groups", maxPriority);
            } 
            else if (priority == maxPriority)
            {
                prioritizedGroups.add(groupInfo);
                logDebug("[NSC TabCompleter] Added group {0} to prioritized groups", group);
            }
        }

        if (prioritizedGroups.isEmpty())
        {
            prioritizedGroups.add(getGroupManager("default"));
            logDebug("[NSC TabCompleter] No prioritized groups found, using default group");
        }
        logDebug("[NSC TabCompleter] Prioritized groups: {0}", prioritizedGroups.stream().map(g -> g.getMode()).collect(Collectors.toList()));

        Set<String> protectedCommands = new HashSet<>();
        for (initializeGroup groupInfo : prioritizedGroups)
        {
            List<String> groupCommands = groupInfo.getCommands();
            logDebug("[NSC TabCompleter] Processing commands for group mode: {0}, commands: {1}", groupInfo.getMode(), groupCommands);

            if (groupInfo.getMode().equalsIgnoreCase("blacklist"))
            {
                hasBlacklist = true;
                blacklistCommands.addAll(groupCommands);
                logDebug("[NSC TabCompleter] Added blacklist commands: {0}", blacklistCommands);
            } 
            else if (groupInfo.getMode().equalsIgnoreCase("whitelist"))
            {
                hasWhitelist = true;
                commands.addAll(groupCommands);
                protectedCommands.addAll(groupCommands);
                logDebug("[NSC TabCompleter] Added whitelist commands: {0}", commands);
            }
        }

        for (String cmd : originalCommands)
        {
            String lowerCase = cmd.toLowerCase();
            String whitelistPerm = "nsctab.whitelist.command." + lowerCase;
            String blacklistPerm = "nsctab.blacklist.command." + lowerCase;

            if (player.hasPermission(whitelistPerm) && player.isPermissionSet(whitelistPerm))
            {
                commands.add(cmd);
                protectedCommands.add(cmd);
                logDebug("[NSC TabCompleter] Player {0} has whitelist permission {1}, added command: {2}", player.getName(), whitelistPerm, cmd);
            }
            if (player.hasPermission(blacklistPerm) && player.isPermissionSet(whitelistPerm))
            {
                blacklistCommands.add(cmd);
                logDebug("[NSC TabCompleter] Player {0} has blacklist permission {1}, added command: {2}", player.getName(), blacklistPerm, cmd);
            }
        }

        logDebug("[NSC TabCompleter] Combining commands, hasWhitelist: {0}, hasBlacklist: {1}", hasWhitelist, hasBlacklist);
        if (hasWhitelist && hasBlacklist)
        {
            commands.removeAll(blacklistCommands);
            commands.addAll(protectedCommands);
            logDebug("[NSC TabCompleter] Restored protected whitelist commands, final commands: {0}", commands);
            return commands;
        } 
        else if (hasWhitelist)
        {
            logDebug("[NSC TabCompleter] Using whitelist commands: {0}", commands);
            return commands;
        }
        else
        {
            commands.addAll(originalCommands);
            commands.removeAll(blacklistCommands);
            logDebug("[NSC TabCompleter] Using original commands minus blacklist: {0}", commands);
            return commands;
        }
    }

    public Set<String> getSubArgsForCommand(List<String> playerGroups, String command, String[] args)
    {
        logDebug("[NSC TabCompleter] Getting sub-arguments for command: {0}, groups: {1}, args: {2}", command, playerGroups, Arrays.toString(args));

        Set<String> whitelist = new HashSet<>();
        Set<String> blacklist = new HashSet<>();
        int maxPriority = Integer.MIN_VALUE;
        List<initializeGroup> prioritizedGroups = new ArrayList<>();

        logDebug("[NSC TabCompleter] Finding groups with highest priority for sub-arguments");
        for (String groupName : playerGroups)
        {
            initializeGroup group = groups.getOrDefault(groupName, new initializeGroup("whitelist", List.of(), 0));
            int priority = group.getPriority();
            logDebug("[NSC TabCompleter] Group: {0}, priority: {1}", groupName, priority);

            if (priority > maxPriority)
            {
                maxPriority = priority;
                prioritizedGroups.clear();
                prioritizedGroups.add(group);
                logDebug("[NSC TabCompleter] New max priority: {0}, cleared previous groups", maxPriority);
            } 
            else if (priority == maxPriority)
            {
                prioritizedGroups.add(group);
                logDebug("[NSC TabCompleter] Added group {0} to prioritized groups", groupName);
            }
        }

        if (prioritizedGroups.isEmpty())
        {
            prioritizedGroups.add(groups.getOrDefault("default", new initializeGroup("whitelist", List.of(), 0)));
            logDebug("[NSC TabCompleter] No prioritized groups found, using default group");
        }

        logDebug("[NSC TabCompleter] Processing sub-arguments for {0} prioritized groups", prioritizedGroups.size());
        for (initializeGroup group : prioritizedGroups)
        {
            Set<String> subArgs = group.getSubArgs(command, args);
            logDebug("[NSC TabCompleter] Sub-arguments for group mode {0}: {1}", group.getMode(), subArgs);

            if (group.getMode().equals("whitelist"))
            {
                whitelist.addAll(subArgs);
                logDebug("[NSC TabCompleter] Added whitelist sub-arguments: {0}", subArgs);
            }
            else
            {
                blacklist.addAll(subArgs);
                logDebug("[NSC TabCompleter] Added blacklist sub-arguments: {0}", subArgs);
            }
        }

        logDebug("[NSC TabCompleter] Combining sub-arguments, whitelist: {0}, blacklist: {1}", whitelist, blacklist);

        if (!whitelist.isEmpty())
        {
            whitelist.removeAll(blacklist);
            logDebug("[NSC TabCompleter] Final sub-arguments after removing blacklist: {0}", whitelist);
            return whitelist;
        }

        logDebug("[NSC TabCompleter] No whitelist sub-arguments, returning empty set");
        return new HashSet<>();
    }

    public int getGroupPlayers(String group)
    {
        logDebug("[NSC TabCompleter] Counting players in group: {0}", group);
        int count = 0;

        for (Player player : Bukkit.getOnlinePlayers())
        {
            String permission = "nsctab.group." + group;

            if (player.hasPermission(permission))
            {
                count++;
                logDebug("[NSC TabCompleter] Player {0} has permission {1}, incrementing count", player.getName(), permission);
            } 
            else
            {
                logDebug("[NSC TabCompleter] Player {0} does not have permission {1}", player.getName(), permission);
            }
        }

        logDebug("[NSC TabCompleter] Total players in group {0}: {1}", group, count);
        return count;
    }

    public Boolean isOpByPassTrue()
    {
        Boolean result = config.getBoolean("settings.op-bypass");
        logDebug("[NSC TabCompleter] Checking op-bypass setting: {0}", result);
        return result;
    }

    public Boolean isTabComplationTrue()
    {
        Boolean result = config.getBoolean("settings.tab-complation");
        logDebug("[NSC TabCompleter] Checking tab-complation setting: {0}", result);
        return result;
    }

    public Boolean isBlockExecutionTrue()
    {
        Boolean result = config.getBoolean("settings.block-execution");
        logDebug("[NSC TabCompleter] Checking block-execution setting: {0}", result);
        return result;
    }

    public Boolean groupPrioritizationStatus()
    {
        Boolean result = config.getBoolean("settings.prioritization-enabled");
        logDebug("[NSC TabCompleter] Checking prioritization-enabled setting: {0}", result);
        return result;
    }

    public String insufficientPermissionMessage()
    {
        String result = config.getString("settings.insufficient-permission-message");
        logDebug("[NSC TabCompleter] Getting insufficient-permission-message: {0}", result);
        return result;
    }

    public Map<String, initializeGroup> getCurrentGroups()
    {
        logDebug("[NSC TabCompleter] Getting current groups: {0}", groups.keySet());
        return groups;
    }

    public boolean isGroupExist(String groupName)
    {
        boolean result = groups.containsKey(groupName);
        logDebug("[NSC TabCompleter] Checking if group {0} exists: {1}", groupName, result);
        return result;
    }

    public initializeGroup getGroupManager(String groupName)
    {
        initializeGroup result = groups.getOrDefault(groupName, new initializeGroup("blacklist", List.of(), 0));

        logDebug("[NSC TabCompleter] Getting group manager for {0}: mode={1}, commands={2}, priority={3}", 
            groupName, result.getMode(), result.getCommands(), result.getPriority());

        return result;
    }

    public Set<String> getCommands()
    {
        logDebug("[NSC TabCompleter] Getting all commands with sub-arguments from groups");

        Set<String> commands = groups.values().stream()
                .flatMap(group -> group.commandTree.keySet().stream()
                        .filter(command -> !group.commandTree.get(command).getChildren().isEmpty()))
                .collect(Collectors.toSet());

        logDebug("[NSC TabCompleter] Commands with sub-arguments retrieved: {0}", commands);
        return commands;
    }

    public Boolean isDebugEnabled()
    {
        Boolean result = config.getBoolean("settings.debug", false);
        return result;
    }

    public void logDebug(String message, Object... args)
    {
        if (isDebugEnabled())
        {
            plugin.getLogger().log(Level.INFO, "[NSC TabCompleter] " + message, args);
        }
    }

    public class initializeGroup
    {
        private String mode;
        private final List<String> commands;
        private int priority;
        private final Map<String, argumentNode> commandTree;

        public initializeGroup(String mode, List<String> commands, int priority)
        {
            this.mode = mode;
            this.commands = commands;
            this.priority = priority;
            this.commandTree = new HashMap<>();

            logDebug("[NSC TabCompleter] Creating initializeGroup: mode={0}, commands={1}, priority={2}", 
                mode, commands, priority);
            logDebug("[NSC TabCompleter] Initialized commandTree for group");
        }

        public String getMode()
        {
            logDebug("[NSC TabCompleter] Getting mode: {0}", mode);
            return mode;
        }

        public List<String> getCommands()
        {
            logDebug("[NSC TabCompleter] Getting commands: {0}", commands);
            return commands;
        }

        public int getPriority()
        {
            logDebug("[NSC TabCompleter] Getting priority: {0}", priority);
            return priority;
        }

        public void setMode(String x)
        {
            logDebug("[NSC TabCompleter] Setting mode from {0} to {1}", mode, x);
            this.mode = x;
        }

        public void setPriority(int x)
        {
            logDebug("[NSC TabCompleter] Setting priority from {0} to {1}", priority, x);
            this.priority = x;
        }

        public Set<String> getSubArgs(String command, String[] args)
        {
            logDebug("[NSC TabCompleter] Getting sub-arguments for command: {0}, args: {1}", command, Arrays.toString(args));
            argumentNode node = commandTree.get(command);

            if (node == null)
            {
                logDebug("[NSC TabCompleter] No node found for command: {0}, returning empty set", command);
                return Collections.emptySet();
            }

            logDebug("[NSC TabCompleter] Navigating tree for command: {0}, starting at root node", command);
            for (int i = 0; i < args.length - 1 && node != null; i++)
            {
                String arg = args[i].toLowerCase();
                logDebug("[NSC TabCompleter] Navigating to child for arg[{0}]: {1}", i, arg);
                node = node.getChild(arg);

                if (node == null)
                {
                    logDebug("[NSC TabCompleter] No child node found for arg[{0}]: {1}, stopping navigation", i, arg);
                }
            }

            Set<String> result = node != null ? node.getChildren() : Collections.emptySet();
            logDebug("[NSC TabCompleter] Sub-arguments retrieved: {0}", result);
            return result;
        }

        public void addCommand(String command, String[] subArgs)
        {
            logDebug("[NSC TabCompleter] Adding command: {0}, subArgs: {1}", command, Arrays.toString(subArgs));

            argumentNode node = commandTree.computeIfAbsent(command, k ->
            {
                logDebug("[NSC TabCompleter] Creating new node for command: {0}", command);
                return new argumentNode();
            });
            argumentNode current = node;

            for (int i = 0; i < subArgs.length; i++)
            {
                String subArg = subArgs[i];
                logDebug("[NSC TabCompleter] Adding subArg[{0}]: {1}", i, subArg);
                current = current.addChild(subArg);
            }
            logDebug("[NSC TabCompleter] Command {0} added to tree with {1} sub-arguments", command, subArgs.length);
        }

        private class argumentNode
        {
            private final Map<String, argumentNode> children;

            public argumentNode()
            {
                this.children = new HashMap<>();
                logDebug("[NSC TabCompleter] Creating new argumentNode");
            }

            public argumentNode addChild(String subArg)
            {
                logDebug("[NSC TabCompleter] Adding child node for subArg: {0}", subArg);
                argumentNode child = children.computeIfAbsent(subArg, k ->
                {
                    logDebug("[NSC TabCompleter] Creating new child node for subArg: {0}", subArg);
                    return new argumentNode();
                });
                logDebug("[NSC TabCompleter] Child node added for subArg: {0}", subArg);
                return child;
            }

            public argumentNode getChild(String subArg)
            {
                argumentNode child = children.get(subArg);
                logDebug("Getting child node for subArg: {0}, found: {1}", subArg, child != null);
                return child;
            }

            public Set<String> getChildren()
            {
                logDebug("Getting children: {0}", children.keySet());
                return children.keySet();
            }
        }
    }
}