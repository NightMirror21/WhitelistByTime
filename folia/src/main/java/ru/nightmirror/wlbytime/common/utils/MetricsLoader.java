package ru.nightmirror.wlbytime.common.utils;


import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

public class MetricsLoader {

    public MetricsLoader(JavaPlugin plugin) throws ClassNotFoundException {
        new Metrics(plugin, 13834);
    }
}
