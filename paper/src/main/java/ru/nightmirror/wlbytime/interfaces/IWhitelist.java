package ru.nightmirror.wlbytime.interfaces;

import net.elytrium.serializer.language.object.YamlSerializable;
import org.bukkit.configuration.file.FileConfiguration;
import ru.nightmirror.wlbytime.common.config.ConfigsContainer;
import ru.nightmirror.wlbytime.common.config.configs.MessagesConfig;
import ru.nightmirror.wlbytime.common.config.configs.SettingsConfig;

public interface IWhitelist {
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
}
