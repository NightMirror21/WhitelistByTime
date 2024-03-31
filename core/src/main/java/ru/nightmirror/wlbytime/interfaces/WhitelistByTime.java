package ru.nightmirror.wlbytime.interfaces;

import ru.nightmirror.wlbytime.common.config.ConfigsContainer;
import ru.nightmirror.wlbytime.common.config.configs.MessagesConfig;
import ru.nightmirror.wlbytime.common.config.configs.SettingsConfig;
import ru.nightmirror.wlbytime.interfaces.misc.VersionGetter;

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
