package com.nikolai.nsctabcompleter;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.function.Consumer;

public class UpdateChecker
{
    private final JavaPlugin plugin;
    private final String repoOwner;
    private final String repoName;
    private final String githubToken;

    public UpdateChecker(JavaPlugin plugin, String repoOwner, String repoName, String githubToken)
    {
        this.plugin = plugin;
        this.repoOwner = repoOwner;
        this.repoName = repoName;
        this.githubToken = githubToken;
    }

    public void getVersion(final Consumer<String> consumer)
    {
        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () ->
        {
            try
            {
                String apiUrl = "https://api.github.com/repos/" + repoOwner + "/" + repoName + "/releases/latest";
                URL url = new URL(apiUrl);
                HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

                connection.setRequestMethod("GET");
                connection.setRequestProperty("Accept", "application/vnd.github+json");
                connection.setRequestProperty("User-Agent", "NSCTabCompleter-UpdateChecker");

                if (githubToken != null && !githubToken.isEmpty())
                {
                    connection.setRequestProperty("Authorization", "Bearer " + githubToken);
                }

                int responseCode = connection.getResponseCode();

                if (responseCode == HttpsURLConnection.HTTP_OK)
                {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null)
                    {
                        response.append(line);
                    }
                    reader.close();

                    String jsonResponse = response.toString();
                    JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();
                    String latestVersion = jsonObject.get("tag_name").getAsString();

                    if (latestVersion.startsWith("v"))
                    {
                        latestVersion = latestVersion.substring(1);
                    }

                    consumer.accept(latestVersion);
                }
                else
                {
                    plugin.getLogger().warning("[NSC TabCompleter] Failed to check for updates: HTTP " + responseCode);
                    
                    if (responseCode == 403)
                    {
                        plugin.getLogger().warning("[NSC TabCompleter] Possibly rate-limited or invalid GitHub token.");
                    }
                }
            }

            catch (IOException e)
            {
                plugin.getLogger().warning("[NSC TabCompleter] Unable to check for updates: " + e.getMessage());
            }
            catch (Exception e)
            {
                plugin.getLogger().warning("[NSC TabCompleter] Error parsing JSON response: " + e.getMessage());
            }

        });
    }
}