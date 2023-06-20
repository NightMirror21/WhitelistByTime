package ru.nightmirror.wlbytime.common.utils;

import org.bukkit.plugin.Plugin;

import java.io.File;

public class ConfigUtils {
    public static void checkConfig(Plugin plugin) {
        File configFile = new File(plugin.getDataFolder() + File.separator + "config.yml");

        if (!configFile.exists()) {
            plugin.saveDefaultConfig();
        }

        plugin.reloadConfig();
    }
}
