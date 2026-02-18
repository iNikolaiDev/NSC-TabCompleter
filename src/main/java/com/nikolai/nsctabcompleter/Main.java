package com.nikolai.nsctabcompleter;

import com.nikolai.nsctabcompleter.commands.NSCTabCompleterCommand;
import com.nikolai.nsctabcompleter.listeners.CommandBlockerListener;
import com.nikolai.nsctabcompleter.listeners.TabCompleteListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin
{
    public static Main plugin;
    public boolean availableUpdate;
    public ConfigurationFileManager configManager;

    @Override
    public void onEnable()
    {
        plugin = this;

        printStartupBanner();
        runUpdateCheck();

        configManager = new ConfigurationFileManager(this);

        getServer().getPluginManager().registerEvents(new TabCompleteListener(this), this);
        getServer().getPluginManager().registerEvents(new CommandBlockerListener(this), this);

        NSCTabCompleterCommand commandHandler = new NSCTabCompleterCommand(this);
        this.getCommand("nsctabcompleter").setExecutor(commandHandler);
        this.getCommand("nsctabcompleter").setTabCompleter(commandHandler);

        // Tab completers are already registered inside loadConfigurationFile(),
        // but we call it once more after listeners are registered to be safe.
        configManager.registerTabCompleters();
    }

    @Override
    public void onDisable()
    {
        if (configManager != null)
        {
            configManager.saveConfigurationFile();
        }
    }

    public ConfigurationFileManager getConfigManager()
    {
        return configManager;
    }

    public boolean isAvailableUpdate()
    {
        return availableUpdate;
    }

    // ─────────────────────────────────────────────

    private void printStartupBanner()
    {
        Bukkit.getConsoleSender().sendMessage(" ");
        Bukkit.getConsoleSender().sendMessage(" ╔═══════════════════════════════════════════════════╗");
        Bukkit.getConsoleSender().sendMessage(" ║                                                   ║");
        Bukkit.getConsoleSender().sendMessage(" ║    ███╗   ██╗███████╗ ██████╗                     ║");
        Bukkit.getConsoleSender().sendMessage(" ║    ████╗  ██║██╔════╝██╔════╝                     ║");
        Bukkit.getConsoleSender().sendMessage(" ║    ██╔██╗ ██║███████╗██║                          ║");
        Bukkit.getConsoleSender().sendMessage(" ║    ██║╚██╗██║╚════██║██║                          ║");
        Bukkit.getConsoleSender().sendMessage(" ║    ██║ ╚████║███████║╚██████╗                     ║");
        Bukkit.getConsoleSender().sendMessage(" ║    ╚═╝  ╚═══╝╚══════╝ ╚═════╝  TabCompleter       ║");
        Bukkit.getConsoleSender().sendMessage(" ║                                                   ║");
        Bukkit.getConsoleSender().sendMessage(" ║               D E V   N I K O L A I               ║");
        Bukkit.getConsoleSender().sendMessage(" ║                    2024 – 2026                    ║");
        Bukkit.getConsoleSender().sendMessage(" ║                                                   ║");
        Bukkit.getConsoleSender().sendMessage(" ╚═══════════════════════════════════════════════════╝");
        Bukkit.getConsoleSender().sendMessage(" ");
    }

    private void runUpdateCheck()
    {
        new UpdateChecker(this, "repo-owner", "repo-name", "githubToken").getVersion(latestVersion ->
        {
            String current = getPluginMeta().getVersion();

            if (!current.equals(latestVersion))
            {
                Bukkit.getConsoleSender().sendMessage(
                    "§8[NSC TabCompleter]: §dOutdated version detected (§c" + current + "§d). " +
                    "Latest: §6" + latestVersion + "§d."
                );
                Bukkit.getConsoleSender().sendMessage(
                    "§8[NSC TabCompleter]: §dDownload: https://github.com/Nikolai/NSCTabCompleter/releases/latest"
                );

                availableUpdate = true;
            }
            else
            {
                Bukkit.getConsoleSender().sendMessage(
                    "§8[NSC TabCompleter]: §dRunning latest version §6" + latestVersion + "§d."
                );
            }
        });
    }
}