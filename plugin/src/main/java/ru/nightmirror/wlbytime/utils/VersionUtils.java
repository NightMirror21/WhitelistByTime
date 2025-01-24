package ru.nightmirror.wlbytime.utils;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.Optional;

public class VersionUtils {

    private static final String VERSION_DEFAULT = "null";

    public static String getVersion(JavaPlugin plugin) {
        return getVersionByExperimental(plugin)
                .or(() -> getVersionByDeprecated(plugin))
                .orElse(VERSION_DEFAULT);
    }

    private static Optional<String> getVersionByExperimental(JavaPlugin plugin) {
        try {
            return Optional.of(plugin.getPluginMeta().getVersion());
        } catch (Exception exception) {
            return Optional.empty();
        }
    }

    private static Optional<String> getVersionByDeprecated(JavaPlugin plugin) {
        try {
            return Optional.of(plugin.getDescription().getVersion());
        } catch (Exception exception) {
            return Optional.empty();
        }
    }
}
