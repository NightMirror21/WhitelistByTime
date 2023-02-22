package ru.nightmirror.wlbytime.interfaces;

import org.bukkit.configuration.file.FileConfiguration;

public interface IPlugin {
    boolean isWhitelistEnabled();
    void setWhitelistEnabled(boolean mode);
    FileConfiguration getPluginConfig();
}
