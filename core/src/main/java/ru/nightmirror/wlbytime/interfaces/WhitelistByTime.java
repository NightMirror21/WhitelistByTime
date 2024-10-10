package ru.nightmirror.wlbytime.interfaces;

import ru.nightmirror.wlbytime.config.ConfigsContainer;
import ru.nightmirror.wlbytime.config.configs.MessagesConfig;
import ru.nightmirror.wlbytime.config.configs.SettingsConfig;
import ru.nightmirror.wlbytime.misc.VersionGetter;

public interface WhitelistByTime extends VersionGetter {
    boolean isWhitelistEnabled();

    void setWhitelistEnabled(boolean mode);

    ConfigsContainer getConfigs();

    void reload();

    default SettingsConfig getPluginConfig() {
        return getConfigs().getSettings();
    }

    default MessagesConfig getMessages() {
        return getConfigs().getMessages();
    }

    public static String getPAPIIdentifier() {
        return "wlbytime";
    }
}
