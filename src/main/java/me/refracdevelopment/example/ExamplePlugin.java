package me.refracdevelopment.example;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.manager.Manager;
import dev.rosewood.rosegarden.utils.NMSUtil;
import lombok.Getter;
import me.refracdevelopment.example.listeners.JoinListener;
import me.refracdevelopment.example.manager.CommandManager;
import me.refracdevelopment.example.manager.ConfigurationManager;
import me.refracdevelopment.example.manager.LocaleManager;
import me.refracdevelopment.example.utilities.chat.Color;
import me.refracdevelopment.example.config.Config;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginManager;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.List;

@Getter
public final class ExamplePlugin extends RosePlugin {

    @Getter
    private static ExamplePlugin instance;

    public ExamplePlugin() {
        super(0, 0, ConfigurationManager.class, null, LocaleManager.class, CommandManager.class);
        instance = this;
    }

    @Override
    public void enable() {
        // Plugin startup logic
        long startTiming = System.currentTimeMillis();
        PluginManager pluginManager = this.getServer().getPluginManager();

        Config.loadConfig();

        // Make sure the server has PlaceholderAPI
        if (!pluginManager.isPluginEnabled("PlaceholderAPI")) {
            Color.log("&cPlease install PlaceholderAPI onto your server to use this plugin.");
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Make sure the server is on MC 1.16
        if (NMSUtil.getVersionNumber() < 16) {
            Color.log("&cThis plugin only supports 1.16+ Minecraft.");
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }

        Color.log("&aLoaded commands.");
        loadListeners();

        Color.log("&8&m==&c&m=====&f&m======================&c&m=====&8&m==");
        Color.log("&e" + this.getDescription().getName() + " has been enabled. (" + (System.currentTimeMillis() - startTiming) + "ms)");
        Color.log(" &f[*] &6Version&f: &b" + this.getDescription().getVersion());
        Color.log(" &f[*] &6Name&f: &b" + this.getDescription().getName());
        Color.log(" &f[*] &6Author&f: &b" + this.getDescription().getAuthors().get(0));
        Color.log("&8&m==&c&m=====&f&m======================&c&m=====&8&m==");

        updateCheck(Bukkit.getConsoleSender(), true);
    }

    @Override
    public void disable() {
        // unused
    }

    @Override
    public void reload() {
        super.reload();
        Config.loadConfig();
    }

    @Override
    protected List<Class<? extends Manager>> getManagerLoadPriority() {
        return Collections.emptyList();
    }

    private void loadManagers() {
        Color.log("&aLoaded managers.");
    }

    private void loadListeners() {
        this.getServer().getPluginManager().registerEvents(new JoinListener(), this);
        Color.log("&aLoaded listeners.");
    }

    public void updateCheck(CommandSender sender, boolean console) {
        Color.log("&aChecking for updates!");
        try {
            String urlString = "https://updatecheck.refracdev.ml";
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String input;
            StringBuffer response = new StringBuffer();
            while ((input = reader.readLine()) != null) {
                response.append(input);
            }
            reader.close();
            JsonObject object = new JsonParser().parse(response.toString()).getAsJsonObject();

            if (object.has("plugins")) {
                JsonObject plugins = object.get("plugins").getAsJsonObject();
                JsonObject info = plugins.get(this.getDescription().getName()).getAsJsonObject();
                String version = info.get("version").getAsString();
                if (version.equals(this.getDescription().getVersion())) {
                    if (console) {
                        sender.sendMessage(Color.translate("&a" + this.getDescription().getName() + " is on the latest version."));
                    }
                } else {
                    sender.sendMessage(Color.translate(""));
                    sender.sendMessage(Color.translate(""));
                    sender.sendMessage(Color.translate("&cYour " + this.getDescription().getName() + " version is out of date!"));
                    sender.sendMessage(Color.translate("&cWe recommend updating ASAP!"));
                    sender.sendMessage(Color.translate(""));
                    sender.sendMessage(Color.translate("&cYour Version: &e" + this.getDescription().getVersion()));
                    sender.sendMessage(Color.translate("&aNewest Version: &e" + version));
                    sender.sendMessage(Color.translate(""));
                    sender.sendMessage(Color.translate(""));
                    return;
                }
                return;
            } else {
                sender.sendMessage(Color.translate("&cWrong response from update API, contact plugin developer!"));
                return;
            }
        } catch (
                Exception ex) {
            sender.sendMessage(Color.translate("&cFailed to get updater check. (" + ex.getMessage() + ")"));
            return;
        }
    }
}
