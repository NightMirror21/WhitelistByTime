package ru.nightmirror.wlbytime.config;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import ru.nightmirror.wlbytime.config.configs.*;
import ru.nightmirror.wlbytime.time.TimeUnitsConvertorSettings;

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
    CommandsConfig commandsConfig;

    public void load() {
        messages = new MessagesConfig();
        messages.reload(new File(folder, "messages.yml").toPath());

        database = new DatabaseConfig();
        database.reload(new File(folder, "database.yml").toPath());

        placeholders = new PlaceholdersConfig();
        placeholders.reload(new File(folder, "placeholders.yml").toPath());

        settings = new SettingsConfig();
        settings.reload(new File(folder, "settings.yml").toPath());

        commandsConfig = new CommandsConfig();
        commandsConfig.reload(new File(folder, "commands.yml").toPath());
    }

    public TimeUnitsConvertorSettings getTimeUnitsConvertorSettings() {
        return TimeUnitsConvertorSettings.builder()
                .forever(messages.getForever())
                .year(settings.getYearTimeUnits())
                .month(settings.getMonthTimeUnits())
                .week(settings.getWeekTimeUnits())
                .day(settings.getDayTimeUnits())
                .hour(settings.getHourTimeUnits())
                .minute(settings.getMinuteTimeUnits())
                .second(settings.getSecondTimeUnits())
                .build();
    }
}
