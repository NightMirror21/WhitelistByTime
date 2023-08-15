package ru.nightmirror.wlbytime.interfaces;

import org.bukkit.configuration.file.FileConfiguration;

public interface IWhitelist {
    boolean isWhitelistEnabled();
    void setWhitelistEnabled(boolean mode);
    FileConfiguration getPluginConfig();
    void reload();
}
