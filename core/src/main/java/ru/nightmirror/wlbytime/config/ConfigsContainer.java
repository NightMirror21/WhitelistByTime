package ru.nightmirror.wlbytime.config;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import ru.nightmirror.wlbytime.config.configs.*;
import ru.nightmirror.wlbytime.time.TimeUnitsConvertorSettings;

import java.io.File;
import java.nio.file.Path;

@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Getter
@Slf4j
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

        Path commandsPath = new File(folder, "commands.yml").toPath();
        commandsConfig = new CommandsConfig();
        try {
            commandsConfig.reload(commandsPath);
        } catch (RuntimeException currentFormatException) {
            log.warn("Failed to load commands.yml in current format, trying legacy parser", currentFormatException);

            LegacyCommandsConfig legacyCommandsConfig = new LegacyCommandsConfig();
            try {
                legacyCommandsConfig.reload(commandsPath);
            } catch (RuntimeException legacyFormatException) {
                currentFormatException.addSuppressed(legacyFormatException);
                throw currentFormatException;
            }

            commandsConfig = CommandsConfig.fromLegacy(legacyCommandsConfig);
            commandsConfig.save(commandsPath);
            log.info("commands.yml was migrated from legacy string permissions to list format");
        }
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
