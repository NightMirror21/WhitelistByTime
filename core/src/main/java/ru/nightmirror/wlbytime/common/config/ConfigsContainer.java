package ru.nightmirror.wlbytime.common.config;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import ru.nightmirror.wlbytime.common.config.configs.DatabaseConfig;
import ru.nightmirror.wlbytime.common.config.configs.MessagesConfig;
import ru.nightmirror.wlbytime.common.config.configs.PlaceholdersConfig;
import ru.nightmirror.wlbytime.common.config.configs.SettingsConfig;

import java.io.File;

@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Getter
public class ConfigsContainer {

    @Getter(value = AccessLevel.NONE)
    final File folder;

    MessagesConfig messages;
    DatabaseConfig database;
    PlaceholdersConfig placeholders;
    SettingsConfig settings;

    public void load() {
        messages = new MessagesConfig();
        messages.reload(new File(folder, "messages.yml").toPath());

        database = new DatabaseConfig();
        database.reload(new File(folder, "database.yml").toPath());

        placeholders = new PlaceholdersConfig();
        placeholders.reload(new File(folder, "placeholders.yml").toPath());

        settings = new SettingsConfig();
        settings.reload(new File(folder, "settings.yml").toPath());
    }
}
