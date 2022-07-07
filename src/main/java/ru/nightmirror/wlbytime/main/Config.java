package ru.nightmirror.wlbytime.main;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Config {

    private FileConfiguration config;
    private static Config instance;

    public static Config getInstance() {
        if (instance == null) instance = new Config();
        return instance;
    }

    public void checkConfig(WhitelistByTime plugin) {
        File configFile = new File(plugin.getDataFolder() + File.separator + "config.yml");

        if (!configFile.exists()) {
            plugin.saveDefaultConfig();
        }

        plugin.reloadConfig();
        config = plugin.getConfig();
    }

//    public List<String> getList(String id) {
//        List<String> result = new ArrayList<>();
//
//        for (String str : config.getStringList(id)) {
//            result.add(str.replaceAll("&", "§"));
//        }
//
//        return result;
//    }
//
//    public String getLine(String id) {
//        return config.getString(id, "null").replaceAll("&", "§");
//    }

}
