package com.nikolai.nsctabcompleter;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import com.nikolai.nsctabcompleter.commands.NSCTabCompleterCommand;
import com.nikolai.nsctabcompleter.listeners.CommandBlockerListener;
import com.nikolai.nsctabcompleter.listeners.TabCompleteListener;

public class Main extends JavaPlugin
{
    public static Main plugin;
    public static ConfigurationFileManager configManager;

    @SuppressWarnings("deprecation")
    @Override
    public void onEnable()
    {
        plugin = this;

        configManager = new ConfigurationFileManager(this);
        getServer().getPluginManager().registerEvents(new TabCompleteListener(this), this);
        getServer().getPluginManager().registerEvents(new CommandBlockerListener(this), this);

        this.getCommand("nsctabcompleter").setExecutor(new NSCTabCompleterCommand(this));

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

    }
    @Override
    public void onDisable()
    {
        configManager.saveConfigurationFile();
    }
    public ConfigurationFileManager getConfigManager()
    {
        return configManager;
    }
}