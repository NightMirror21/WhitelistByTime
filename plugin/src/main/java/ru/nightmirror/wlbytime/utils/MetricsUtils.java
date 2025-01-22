package ru.nightmirror.wlbytime.utils;


import lombok.experimental.UtilityClass;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

@UtilityClass
public class MetricsUtils {

    private static final int BSTATS_PLUGIN_ID = 13834;

    public static void tryToLoad(JavaPlugin plugin) {
        try {
            new Metrics(plugin, BSTATS_PLUGIN_ID);
        } catch (Exception exception) {
            plugin.getLogger().info("Failed to start collecting metrics. " +
                    "The plugin will continue working, but metrics will not be collected.");
        }
    }
}
