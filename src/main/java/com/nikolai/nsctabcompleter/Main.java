package com.nikolai.nsctabcompleter;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import com.nikolai.nsctabcompleter.commands.NSCTabCompleterCommand;
import com.nikolai.nsctabcompleter.listeners.CommandBlockerListener;
import com.nikolai.nsctabcompleter.listeners.TabCompleteListener;

public class Main extends JavaPlugin
{
    public static Main plugin;
    public ConfigurationFileManager configManager;

    @SuppressWarnings("deprecation")
    @Override
    public void onEnable()
    {
        plugin = this;

        configManager = new ConfigurationFileManager(this);
        getServer().getPluginManager().registerEvents(new TabCompleteListener(this), this);
        getServer().getPluginManager().registerEvents(new CommandBlockerListener(this), this);

        this.getCommand("nsctabcompleter").setExecutor(new NSCTabCompleterCommand(this));
        this.getCommand("nsctabcompleter").setTabCompleter(new NSCTabCompleterCommand(this));

        getConfigManager().registerTabCompleters();

        Bukkit.getConsoleSender().sendMessage(" ");
        Bukkit.getConsoleSender().sendMessage("§8----------------------------------------------");
        Bukkit.getConsoleSender().sendMessage(" ");

        Bukkit.getConsoleSender().sendMessage(" §7[§d!§7] NSC TabCompleter §dEnabled §7| §5Made by §dNikolai");
        Bukkit.getConsoleSender().sendMessage(" ");
        Bukkit.getConsoleSender().sendMessage(" §7[§d!§7] §8Version: " + getDescription().getVersion());
        Bukkit.getConsoleSender().sendMessage(" §7[§d!§7] §8Running On: " + getServer().getVersion());

        Bukkit.getConsoleSender().sendMessage(" ");
        Bukkit.getConsoleSender().sendMessage("§8----------------------------------------------");
        Bukkit.getConsoleSender().sendMessage(" ");

        new UpdateChecker(this, "iNikolaiDev", "NSC-TabCompleter", "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").getVersion(version ->
        {
            String currentVersion = getDescription().getVersion();

            if (!currentVersion.equals(version))
            {
                Bukkit.getConsoleSender().sendMessage("§8[NSC TabCompleter]: §dYou are using outdated version (§c" + currentVersion + "§d). A new update is available: §6" + version + "§d.");
                Bukkit.getConsoleSender().sendMessage("§8[NSC TabCompleter]: §dDownload Link: https://github.com/Nikolai/NSCTabCompleter/releases/latest");
            }
            else
            {
                Bukkit.getConsoleSender().sendMessage("§8[NSC TabCompleter]: §dYou are running the latest version (§6" + version + "§d).");
            }
        });
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
}