package me.yourname.example.manager.configuration.cache;

import me.yourname.example.ExamplePlugin;
import me.yourname.example.utilities.chat.Color;

public class Config {

    public String DATA_TYPE;

    public void loadConfig() {
        DATA_TYPE = ExamplePlugin.getInstance().getConfigFile().getString("data-type");
    }
}