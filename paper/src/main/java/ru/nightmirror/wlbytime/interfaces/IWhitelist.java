package ru.nightmirror.wlbytime.interfaces;

import net.elytrium.serializer.language.object.YamlSerializable;
import org.bukkit.configuration.file.FileConfiguration;
import ru.nightmirror.wlbytime.common.config.ConfigsContainer;

public interface IWhitelist<M extends YamlSerializable> {
    boolean isWhitelistEnabled();
    void setWhitelistEnabled(boolean mode);
    ConfigsContainer<M> getConfigs();
    void reload();
}
