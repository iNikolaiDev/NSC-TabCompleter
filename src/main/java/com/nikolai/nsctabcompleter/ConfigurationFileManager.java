package com.nikolai.nsctabcompleter;

import com.nikolai.nsctabcompleter.listeners.CustomTabCompleterListener;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * NSC TabCompleter - ConfigurationFileManager
 * Handles all configuration loading, group management, and permission resolution.
 *
 * @author Nikolai
 * @version 3.0.0
 */
public class ConfigurationFileManager
{
    // ─────────────────────────────────────────────
    //  Constants
    // ─────────────────────────────────────────────

    private static final String PREFIX          = "§8[NSC TabCompleter]: §d";
    private static final String SECTION_GROUPS  = "groups";
    private static final String SECTION_SETTINGS = "settings";
    private static final String KEY_MODE        = ".mode";
    private static final String KEY_PRIORITY    = ".priority";
    private static final String KEY_COMMANDS    = ".commands";
    private static final String DEFAULT_GROUP   = "default";
    private static final String PERM_GROUP      = "nsctab.group.";
    private static final String PERM_WHITELIST  = "nsctab.whitelist.command.";
    private static final String PERM_BLACKLIST  = "nsctab.blacklist.command.";

    // ─────────────────────────────────────────────
    //  Fields
    // ─────────────────────────────────────────────

    private final Main plugin;
    private File configFile;
    private FileConfiguration config;
    private final Map<String, GroupData> groups = new LinkedHashMap<>();

    // ─────────────────────────────────────────────
    //  Constructor
    // ─────────────────────────────────────────────

    public ConfigurationFileManager(Main plugin)
    {
        this.plugin = plugin;
        loadConfigurationFile();
    }

    public FileConfiguration getConfig()
    {
        return config;
    }

    // ─────────────────────────────────────────────
    //  Config I/O
    // ─────────────────────────────────────────────

    public void loadConfigurationFile()
    {
        if (configFile == null)
        {
            configFile = new File(plugin.getDataFolder(), "config.yml");
        }

        if (!configFile.exists())
        {
            plugin.saveDefaultConfig();
            sendConsole(PREFIX + "config.yml not found — default config generated.");
            config = YamlConfiguration.loadConfiguration(configFile);
        }
        else
        {
            config = YamlConfiguration.loadConfiguration(configFile);
            sendConsole(PREFIX + "Config loaded — processing groups...");
        }

        initializeGroups();
    }

    public void saveConfigurationFile()
    {
        if (config == null || configFile == null)
        {
            plugin.getLogger().warning("[NSC TabCompleter] Cannot save — config not loaded.");
            return;
        }
        try
        {
            config.save(configFile);
            plugin.getLogger().info("[NSC TabCompleter] Config saved successfully.");
        }
        catch (IOException e)
        {
            plugin.getLogger().log(Level.SEVERE, "[NSC TabCompleter] Failed to save config.yml: {0}", e.getMessage());
        }
    }

    // ─────────────────────────────────────────────
    //  Group Initialization
    // ─────────────────────────────────────────────

    private void initializeGroups()
    {
        groups.clear();

        ConfigurationSection section = config.getConfigurationSection(SECTION_GROUPS);
        if (section == null)
        {
            plugin.getLogger().warning("[NSC TabCompleter] No 'groups' section found in config.yml.");
            return;
        }

        logDebug("Found groups section — processing...");

        for (String groupName : section.getKeys(false))
        {
            parseGroup(groupName, section);
        }

        logDebug("Loaded {0} group(s): {1}", groups.size(), groups.keySet());
        registerTabCompleters();
    }

    private void parseGroup(String groupName, ConfigurationSection section)
    {
        String path       = groupName;
        String mode       = section.getString(path + KEY_MODE, "blacklist").toLowerCase();
        int    priority   = section.getInt(path + KEY_PRIORITY, 0);
        List<String> rawCommands = section.getStringList(path + KEY_COMMANDS);

        logDebug("Parsing group ''{0}'' | mode={1} | priority={2} | entries={3}",
                groupName, mode, priority, rawCommands.size());

        // Extract root command names
        List<String> rootCommands = rawCommands.stream()
                .filter(e -> e != null && !e.isBlank())
                .filter(e -> mode.equals("blacklist") ? !e.contains(" ") : true)
                .map(e -> e.trim().toLowerCase().split("\\s+")[0])
                .distinct()
                .collect(Collectors.toList());

        GroupData group = new GroupData(mode, rootCommands, priority);

        // Build sub-argument trees
        for (String entry : rawCommands)
        {
            if (entry == null || entry.isBlank()) continue;

            String[] parts  = entry.trim().toLowerCase().split("\\s+");
            String   root   = parts[0];
            String[] subArgs = Arrays.copyOfRange(parts, 1, parts.length);

            if (subArgs.length > 0)
            {
                group.addSubArgs(root, subArgs);
                logDebug("  Sub-args for ''{0}'': {1}", root, Arrays.toString(subArgs));
            }
        }

        groups.put(groupName, group);
        logDebug("Group ''{0}'' registered with {1} root command(s).", groupName, rootCommands.size());
    }

    // ─────────────────────────────────────────────
    //  Tab Completer Registration
    // ─────────────────────────────────────────────

    public void registerTabCompleters()
    {
        Set<String> commandsWithSubArgs = getCommandsWithSubArgs();
        logDebug("Registering tab completers for {0} command(s): {1}", commandsWithSubArgs.size(), commandsWithSubArgs);

        for (String cmd : commandsWithSubArgs)
        {
            PluginCommand pluginCmd = plugin.getServer().getPluginCommand(cmd);
            if (pluginCmd != null)
            {
                pluginCmd.setTabCompleter(new CustomTabCompleterListener(plugin, cmd));
                logDebug("Registered tab completer → ''{0}''", cmd);
            }
            else
            {
                plugin.getLogger().log(Level.WARNING, "[NSC TabCompleter] Command not found on server: {0}", cmd);
            }
        }
    }

    // ─────────────────────────────────────────────
    //  Player Group Resolution
    // ─────────────────────────────────────────────

    public List<String> getPlayerGroups(Player player)
    {
        List<String> result = groups.keySet().stream()
                .filter(g -> player.hasPermission(PERM_GROUP + g))
                .collect(Collectors.toList());

        if (result.isEmpty() && groups.containsKey(DEFAULT_GROUP))
        {
            result.add(DEFAULT_GROUP);
            logDebug("Player {0} has no explicit groups — assigned ''{1}''.", player.getName(), DEFAULT_GROUP);
        }
        else
        {
            logDebug("Player {0} groups: {1}", player.getName(), result);
        }

        return result;
    }

    // ─────────────────────────────────────────────
    //  Command Visibility (Tab-Complete Filter)
    // ─────────────────────────────────────────────

    public Set<String> getGroupsCommands(List<String> playerGroups, Set<String> serverCommands, Player player)
    {
        List<GroupData> effective = resolveEffectiveGroups(playerGroups);

        Set<String> whitelist  = new LinkedHashSet<>();
        Set<String> blacklist  = new HashSet<>();
        boolean hasWhitelist   = false;
        boolean hasBlacklist   = false;

        for (GroupData g : effective)
        {
            if (g.isWhitelist())
            {
                hasWhitelist = true;
                whitelist.addAll(g.getCommands());
            }
            else
            {
                hasBlacklist = true;
                blacklist.addAll(g.getCommands());
            }
        }

        // Per-command permission overrides
        Set<String> permWhitelist = new HashSet<>();
        Set<String> permBlacklist = new HashSet<>();

        for (String cmd : serverCommands)
        {
            String lower = cmd.toLowerCase();
            String wp = PERM_WHITELIST + lower;
            String bp = PERM_BLACKLIST + lower;

            if (player.isPermissionSet(wp) && player.hasPermission(wp)) permWhitelist.add(cmd);
            if (player.isPermissionSet(bp) && player.hasPermission(bp)) permBlacklist.add(cmd);
        }

        // Merge permission overrides
        whitelist.addAll(permWhitelist);
        blacklist.addAll(permBlacklist);

        // Resolve final command set
        Set<String> result;

        if (hasWhitelist && hasBlacklist)
        {
            result = new LinkedHashSet<>(whitelist);
            result.removeAll(blacklist);
            result.addAll(permWhitelist); // re-add explicit whitelist perms
        }
        else if (hasWhitelist)
        {
            result = new LinkedHashSet<>(whitelist);
            result.removeAll(permBlacklist);
        }
        else if (hasBlacklist)
        {
            result = new LinkedHashSet<>(serverCommands);
            result.removeAll(blacklist);
            result.addAll(permWhitelist);
        }
        else
        {
            result = new LinkedHashSet<>(serverCommands);
        }

        logDebug("Player {0} → visible commands ({1}): {2}", player.getName(), result.size(), result);
        return result;
    }

    // ─────────────────────────────────────────────
    //  Sub-argument Resolution
    // ─────────────────────────────────────────────

    public Set<String> getSubArgsForCommand(List<String> playerGroups, String command, String[] args)
    {
        List<GroupData> effective = resolveEffectiveGroups(playerGroups);

        Set<String> whitelist = new LinkedHashSet<>();
        Set<String> blacklist = new HashSet<>();

        for (GroupData g : effective)
        {
            Set<String> sub = g.getSubArgs(command, args);
            if (g.isWhitelist()) whitelist.addAll(sub);
            else                  blacklist.addAll(sub);
        }

        whitelist.removeAll(blacklist);
        logDebug("Sub-args for ''{0}'' args={1} → {2}", command, Arrays.toString(args), whitelist);
        return whitelist;
    }

    // ─────────────────────────────────────────────
    //  Command Execution Check
    // ─────────────────────────────────────────────

    /**
     * Returns true if the player is allowed to execute the given command
     * based on their group memberships.
     */
    public boolean isCommandAllowed(List<String> playerGroups, String command)
    {
        List<GroupData> effective = resolveEffectiveGroups(playerGroups);

        boolean hasWhitelist      = false;
        boolean hasBlacklist      = false;
        boolean inWhitelist       = false;
        boolean inBlacklist       = false;

        for (GroupData g : effective)
        {
            boolean contains = g.getCommands().contains(command);
            if (g.isWhitelist())
            {
                hasWhitelist = true;
                if (contains) inWhitelist = true;
            }
            else
            {
                hasBlacklist = true;
                if (contains) inBlacklist = true;
            }
        }

        if (hasWhitelist && hasBlacklist) return inWhitelist && !inBlacklist;
        if (hasWhitelist)                 return inWhitelist;
        if (hasBlacklist)                 return !inBlacklist;
        return true;
    }

    // ─────────────────────────────────────────────
    //  Priority Resolution
    // ─────────────────────────────────────────────

    /**
     * Filters the player's group list down to only the highest-priority group(s).
     * Falls back to 'default' if none found.
     */
    private List<GroupData> resolveEffectiveGroups(List<String> playerGroups)
    {
        if (playerGroups.isEmpty())
        {
            GroupData def = groups.get(DEFAULT_GROUP);
            return def != null ? List.of(def) : Collections.emptyList();
        }

        int maxPriority = playerGroups.stream()
                .map(groups::get)
                .filter(Objects::nonNull)
                .mapToInt(GroupData::getPriority)
                .max()
                .orElse(Integer.MIN_VALUE);

        List<GroupData> effective = playerGroups.stream()
                .map(groups::get)
                .filter(g -> g != null && g.getPriority() == maxPriority)
                .collect(Collectors.toList());

        if (effective.isEmpty())
        {
            GroupData def = groups.get(DEFAULT_GROUP);
            if (def != null) effective = List.of(def);
        }

        return effective;
    }

    // ─────────────────────────────────────────────
    //  Settings Accessors  (cached-read helpers)
    // ─────────────────────────────────────────────

    public boolean isTabCompletionEnabled()   { return getSetting("tab-complation",         true);  }
    public boolean isBlockExecutionEnabled()  { return getSetting("block-execution",         true);  }
    public boolean isOpBypassEnabled()        { return getSetting("op-bypass",               false); }
    public boolean isPrioritizationEnabled()  { return getSetting("prioritization-enabled",  true);  }
    public boolean isDebugEnabled()           { return getSetting("debug",                   false); }
    public boolean isQuickStatisticsEnabled() { return getSetting("quick-statistics", true); }

    public String insufficientPermissionMessage()
    {
        return config.getString(SECTION_SETTINGS + ".insufficient-permission-message",
                "§cYou don't have permission to use this command.");
    }

    private boolean getSetting(String key, boolean def)
    {
        return config != null && config.getBoolean(SECTION_SETTINGS + "." + key, def);
    }

    // ─────────────────────────────────────────────
    //  Public API
    // ─────────────────────────────────────────────

    public Map<String, GroupData> getCurrentGroups()    { return Collections.unmodifiableMap(groups); }
    public boolean isGroupExist(String name)             { return groups.containsKey(name);            }

    public GroupData getGroupManager(String name)
    {
        return groups.getOrDefault(name, new GroupData("blacklist", Collections.emptyList(), 0));
    }

    /** Returns the set of commands that have sub-arguments defined (used for tab-completer registration). */
    public Set<String> getCommandsWithSubArgs()
    {
        return groups.values().stream()
                .flatMap(g -> g.commandsWithSubArgs().stream())
                .collect(Collectors.toSet());
    }

    /** @deprecated use {@link #getCommandsWithSubArgs()} */
    @Deprecated
    public Set<String> getCommands() { return getCommandsWithSubArgs(); }

    public int getGroupPlayerCount(String groupName)
    {
        String perm = PERM_GROUP + groupName;
        return (int) Bukkit.getOnlinePlayers().stream()
                .filter(p -> p.hasPermission(perm))
                .count();
    }

    // ─────────────────────────────────────────────
    //  Logging
    // ─────────────────────────────────────────────

    public void logDebug(String message, Object... args)
    {
        if (isDebugEnabled())
        {
            plugin.getLogger().log(Level.INFO, "[DEBUG] " + message, args);
        }
    }

    private void sendConsole(String message)
    {
        Bukkit.getConsoleSender().sendMessage(message);
    }

    // ═════════════════════════════════════════════
    //  Inner Class: GroupData
    // ═════════════════════════════════════════════

    /**
     * Represents a single permission group with its mode, command list, priority,
     * and a tree of allowed sub-arguments per command.
     */
    public class GroupData
    {
        private String mode;
        private int priority;
        private final List<String> commands;
        private final Map<String, ArgumentNode> commandTree = new HashMap<>();

        public GroupData(String mode, List<String> commands, int priority)
        {
            this.mode     = mode.toLowerCase();
            this.commands = new ArrayList<>(commands);
            this.priority = priority;
        }

        // ── Getters / Setters ──────────────────────

        public String       getMode()      { return mode;     }
        public int          getPriority()  { return priority; }
        public List<String> getCommands()  { return Collections.unmodifiableList(commands); }
        public boolean      isWhitelist()  { return mode.equals("whitelist"); }

        public void setMode(String mode)       { this.mode = mode.toLowerCase();  }
        public void setPriority(int priority)  { this.priority = priority;        }

        // ── Sub-argument Tree ──────────────────────

        public void addSubArgs(String command, String[] subArgs)
        {
            ArgumentNode node = commandTree.computeIfAbsent(command, k -> new ArgumentNode());
            ArgumentNode cur  = node;
            for (String arg : subArgs) cur = cur.addChild(arg);
        }

        public Set<String> getSubArgs(String command, String[] args)
        {
            ArgumentNode node = commandTree.get(command);
            if (node == null) return Collections.emptySet();

            for (int i = 0; i < args.length - 1; i++)
            {
                node = node.getChild(args[i].toLowerCase());
                if (node == null) return Collections.emptySet();
            }

            return node.getChildKeys();
        }

        /** Returns the set of commands in this group that have sub-argument definitions. */
        public Set<String> commandsWithSubArgs()
        {
            return commandTree.entrySet().stream()
                    .filter(e -> !e.getValue().getChildKeys().isEmpty())
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toSet());
        }

        // ── Inner Class: ArgumentNode ──────────────

        private class ArgumentNode
        {
            private final Map<String, ArgumentNode> children = new HashMap<>();

            public ArgumentNode addChild(String key)
            {
                return children.computeIfAbsent(key, k -> new ArgumentNode());
            }

            public ArgumentNode getChild(String key)
            {
                return children.get(key);
            }

            public Set<String> getChildKeys()
            {
                return Collections.unmodifiableSet(children.keySet());
            }
        }
    }
}