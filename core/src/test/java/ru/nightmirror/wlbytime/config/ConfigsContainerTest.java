package ru.nightmirror.wlbytime.config;

import org.junit.jupiter.api.Test;
import ru.nightmirror.wlbytime.config.configs.CommandsConfig;
import ru.nightmirror.wlbytime.config.configs.DatabaseConfig;
import ru.nightmirror.wlbytime.config.configs.MessagesConfig;
import ru.nightmirror.wlbytime.config.configs.PlaceholdersConfig;
import ru.nightmirror.wlbytime.config.configs.SettingsConfig;
import ru.nightmirror.wlbytime.time.TimeUnitsConvertorSettings;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ConfigsContainerTest {

    @Test
    public void loadInitializesConfigsAndTimeUnitsSettings() throws Exception {
        Path dir = Files.createTempDirectory("wlbytime_config_container");
        new MessagesConfig().save(dir.resolve("messages.yml"));
        new DatabaseConfig().save(dir.resolve("database.yml"));
        new PlaceholdersConfig().save(dir.resolve("placeholders.yml"));
        new SettingsConfig().save(dir.resolve("settings.yml"));
        new CommandsConfig().save(dir.resolve("commands.yml"));

        ConfigsContainer container = new ConfigsContainer(dir.toFile());
        container.load();

        assertNotNull(container.getMessages());
        assertNotNull(container.getDatabase());
        assertNotNull(container.getPlaceholders());
        assertNotNull(container.getSettings());
        assertNotNull(container.getCommandsConfig());

        TimeUnitsConvertorSettings settings = container.getTimeUnitsConvertorSettings();
        assertEquals(container.getMessages().getForever(), settings.getForever());
        assertEquals(container.getSettings().getYearTimeUnits(), settings.getYear());
        assertEquals(container.getSettings().getMonthTimeUnits(), settings.getMonth());
        assertEquals(container.getSettings().getWeekTimeUnits(), settings.getWeek());
        assertEquals(container.getSettings().getDayTimeUnits(), settings.getDay());
        assertEquals(container.getSettings().getHourTimeUnits(), settings.getHour());
        assertEquals(container.getSettings().getMinuteTimeUnits(), settings.getMinute());
        assertEquals(container.getSettings().getSecondTimeUnits(), settings.getSecond());
    }
}
