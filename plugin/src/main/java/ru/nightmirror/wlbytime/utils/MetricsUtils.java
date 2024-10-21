package ru.nightmirror.wlbytime.utils;


import lombok.experimental.UtilityClass;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

@UtilityClass
public class MetricsUtils {
    public static void tryToLoad(JavaPlugin plugin) {
        try {
            new Metrics(plugin, 13834);
        } catch (Exception exception) {
            plugin.getLogger().info("Failed to start collecting metrics. " +
                    "The plugin will continue working, but metrics will not be collected.");
        }
    }
}
