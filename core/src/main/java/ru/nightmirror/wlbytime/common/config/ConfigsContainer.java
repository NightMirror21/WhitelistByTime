package ru.nightmirror.wlbytime.common.config;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.elytrium.serializer.language.object.YamlSerializable;
import ru.nightmirror.wlbytime.common.config.configs.*;

import java.io.File;

@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class ConfigsContainer<M extends YamlSerializable> {

    boolean isPaperFamily;
    final File folder;

    @Getter
    M messages;
    @Getter
    DatabaseConfig database;
    @Getter
    PlaceholdersConfig placeholders;
    @Getter
    SettingsConfig settings;

    public ConfigsContainer<M> load() {
        messages = isPaperFamily ? (M) new MessagesConfigForPaperFamily() : (M) new MessagesConfigForNotPaperFamily();
        messages.reload(new File(folder, "messages.yml").toPath());

        database = new DatabaseConfig();
        database.reload(new File(folder, "database.yml").toPath());

        placeholders = new PlaceholdersConfig();
        placeholders.reload(new File(folder, "placeholders.yml").toPath());

        settings = new SettingsConfig();
        settings.reload(new File(folder, "settings.yml").toPath());
        return this;
    }
}
