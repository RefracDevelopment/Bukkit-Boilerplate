package me.yourname.example;

import com.cryptomorin.xseries.ReflectionUtils;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.tcoded.folialib.FoliaLib;
import lombok.Getter;
import me.yourname.example.commands.HelpCommand;
import me.yourname.example.commands.ReloadCommand;
import me.yourname.example.commands.VersionCommand;
import me.yourname.example.listeners.JoinListener;
import me.yourname.example.manager.CommandManager;
import me.yourname.example.manager.MenuManager;
import me.yourname.example.manager.ProfileManager;
import me.yourname.example.manager.configuration.ConfigFile;
import me.yourname.example.manager.configuration.cache.Commands;
import me.yourname.example.manager.configuration.cache.Config;
import me.yourname.example.manager.data.DataType;
import me.yourname.example.manager.data.MySQLManager;
import me.yourname.example.manager.data.SQLiteManager;
import me.yourname.example.utilities.DownloadUtil;
import me.yourname.example.utilities.chat.Color;
import me.yourname.example.utilities.command.CommandList;
import me.yourname.example.utilities.command.SubCommand;
import org.bstats.bukkit.Metrics;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
public final class ExamplePlugin extends JavaPlugin {

    @Getter private static ExamplePlugin instance;

    // Managers
    private DataType dataType;
    private MySQLManager mySQLManager;
    private SQLiteManager sqLiteManager;
    private ProfileManager profileManager;
    private MenuManager menuManager;
    private CommandManager commandManager;

    // Files
    private ConfigFile configFile;
    private ConfigFile commandsFile;
    private ConfigFile localeFile;

    // Cache
    private Config settings;
    private Commands commands;

    // Utilities
    private FoliaLib foliaLib;
    private final List<SubCommand> subCommands = new ArrayList<>();

    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;
        long startTiming = System.currentTimeMillis();
        PluginManager pluginManager = getServer().getPluginManager();

        foliaLib = new FoliaLib(this);

        DownloadUtil.downloadAndEnable();

        loadFiles();

        // Replace with your metrics plugin id
        new Metrics(this, -1);

        // Check if the server is on 1.7
        if (ReflectionUtils.MINOR_NUMBER <= 7) {
            Color.log("&c" + getDescription().getName() + " 1.7 is in legacy mode, please update to 1.8+");
            pluginManager.disablePlugin(this);
            return;
        }

        // Make sure the server has PlaceholderAPI
        if (!pluginManager.isPluginEnabled("PlaceholderAPI")) {
            Color.log("&cPlease install PlaceholderAPI onto your server to use this plugin.");
            pluginManager.disablePlugin(this);
            return;
        }

        // Make sure the server has NBTAPI
        if (!pluginManager.isPluginEnabled("NBTAPI")) {
            Color.log("&cPlease install NBTAPI onto your server to use this plugin.");
            pluginManager.disablePlugin(this);
            return;
        }

        if (pluginManager.isPluginEnabled("Skulls")) {
            Color.log("&aSkulls Detected!");
        }

        if (pluginManager.isPluginEnabled("HeadDatabase")) {
            Color.log("&aHeadDatabase Detected!");
        }

        loadManagers();
        loadCommands();
        loadListeners();

        Color.log("&8&m==&c&m=====&f&m======================&c&m=====&8&m==");
        Color.log("&e" + getDescription().getName() + " has been enabled. (took " + (System.currentTimeMillis() - startTiming) + "ms)");
        Color.log(" &f[*] &6Version&f: &b" + getDescription().getVersion());
        Color.log(" &f[*] &6Name&f: &b" + getDescription().getName());
        Color.log(" &f[*] &6Author&f: &b" + getDescription().getAuthors().get(0));
        Color.log("&8&m==&c&m=====&f&m======================&c&m=====&8&m==");

        updateCheck(getServer().getConsoleSender(), true);
    }

    @Override
    public void onDisable() {
        // unused
    }

    private void loadFiles() {
        // Files
        configFile = new ConfigFile("config.yml");
        commandsFile = new ConfigFile("commands/example.yml");
        localeFile = new ConfigFile("locale/" + getConfigFile().getString("locale") + ".yml");

        // Cache
        settings = new Config();
        commands = new Commands();

        Color.log("&c==========================================");
        Color.log("&aAll files have been loaded correctly!");
        Color.log("&c==========================================");
    }

    public void reloadFiles() {
        // Files
        getConfigFile().reload();
        getCommandsFile().reload();
        getLocaleFile().reload();

        // Cache
        getSettings().loadConfig();
        getCommands().loadConfig();

        Color.log("&c==========================================");
        Color.log("&aAll files have been reloaded correctly!");
        Color.log("&c==========================================");
    }

    private void loadManagers() {
        switch (getSettings().DATA_TYPE.toUpperCase()) {
            case "MARIADB":
            case "MYSQL":
                dataType = DataType.MYSQL;
                mySQLManager = new MySQLManager();
                getMySQLManager().connect();
                getMySQLManager().createT();
                Color.log("&aEnabled MySQL support!");
                break;
            default:
                dataType = DataType.SQLITE;
                sqLiteManager = new SQLiteManager();
                getSqLiteManager().connect(getDataFolder().getAbsolutePath() + File.separator + "example.db");
                getSqLiteManager().createT();
                Color.log("&aEnabled SQLite support!");
                break;
        }

        profileManager = new ProfileManager();
        menuManager = new MenuManager();
        commandManager = new CommandManager();
        Color.log("&aLoaded manager.");
    }

    private void loadCommands() {
        try {
            getCommandManager().createCoreCommand(this, getCommands().EXAMPLE_COMMAND_NAME, "An Example plugin command",
                    "/" + getCommands().EXAMPLE_COMMAND_NAME, new CommandList() {
                        @Override
                        public void displayCommandList(CommandSender commandSender, List<SubCommand> list) {
                            String baseColor = getConfigFile().getString("messages.base-command-color");
                            Color.sendCustomMessage(commandSender, baseColor + "Running <g:#8A2387:#E94057:#F27121>" + getDescription().getName() + baseColor + " v" + getDescription().getVersion());
                            Color.sendCustomMessage(commandSender, baseColor + "Plugin created by: <g:#41E0F0:#FF8DCE>" + getDescription().getAuthors().get(0));
                            Color.sendMessage(commandSender, "messages.base-command-help");
                        }
                    }, getCommands().EXAMPLE_COMMAND_ALIASES,
                    HelpCommand.class,
                    VersionCommand.class,
                    ReloadCommand.class
            );

            getSubCommands().addAll(Arrays.asList(
                    new HelpCommand(),
                    new VersionCommand(),
                    new ReloadCommand()
            ));
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            return;
        }
        Color.log("&aLoaded commands.");
    }

    private void loadListeners() {
        getServer().getPluginManager().registerEvents(new JoinListener(), this);
        Color.log("&aLoaded listeners.");
    }

    // This uses Cloudflare Workers with JavaScript
    public void updateCheck(CommandSender sender, boolean console) {
        try {
            // Change to your update-checker url
            String urlString = "https://refracdev-updatecheck.refracdev.workers.dev/";
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
                JsonObject info = plugins.get(getDescription().getName()).getAsJsonObject();
                String version = info.get("version").getAsString();
                if (version.equals(getDescription().getVersion())) {
                    if (console) {
                        Color.sendCustomMessage(sender, "&a" + getDescription().getName() + " is on the latest version.");
                    }
                } else {
                    Color.sendCustomMessage(sender, "");
                    Color.sendCustomMessage(sender, "");
                    Color.sendCustomMessage(sender, "&cYour " + getDescription().getName() + " version is out of date!");
                    Color.sendCustomMessage(sender, "&cWe recommend updating ASAP!");
                    Color.sendCustomMessage(sender, "");
                    Color.sendCustomMessage(sender, "&cYour Version: &e" + getDescription().getVersion());
                    Color.sendCustomMessage(sender, "&aNewest Version: &e" + version);
                    Color.sendCustomMessage(sender, "");
                    Color.sendCustomMessage(sender, "");
                    return;
                }
                return;
            } else {
                Color.sendCustomMessage(sender, "&cWrong response from update API, contact plugin developer!");
                return;
            }
        } catch (
                Exception ex) {
            Color.sendCustomMessage(sender, "&cFailed to get updater check. (" + ex.getMessage() + ")");
            return;
        }
    }
}
