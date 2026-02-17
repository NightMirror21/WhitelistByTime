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
import java.util.Set;

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

    @Test
    public void loadMigratesLegacyStringPermissionsConfig() throws Exception {
        Path dir = Files.createTempDirectory("wlbytime_config_container_legacy");
        new MessagesConfig().save(dir.resolve("messages.yml"));
        new DatabaseConfig().save(dir.resolve("database.yml"));
        new PlaceholdersConfig().save(dir.resolve("placeholders.yml"));
        new SettingsConfig().save(dir.resolve("settings.yml"));

        Files.writeString(dir.resolve("commands.yml"), """
                add-permission: "wlbytime.add"
                check-permission: "wlbytime.check"
                check-me-permission: "wlbytime.checkme"
                freeze-permission: "wlbytime.freeze"
                unfreeze-permission: "wlbytime.unfreeze"
                get-all-permission: "wlbytime.getall"
                reload-permission: "wlbytime.reload"
                toggle-permission: "wlbytime.toggle"
                remove-permission: "wlbytime.remove"
                time-permission: "wlbytime.time"
                """);

        ConfigsContainer container = new ConfigsContainer(dir.toFile());
        container.load();

        assertEquals(Set.of("wlbytime.add", "whitelistbytime.add"), container.getCommandsConfig().getAddPermission());
        assertEquals(Set.of("wlbytime.reload", "whitelistbytime.reload"), container.getCommandsConfig().getReloadPermission());

        CommandsConfig migratedConfig = new CommandsConfig();
        migratedConfig.reload(dir.resolve("commands.yml"));
        assertEquals(Set.of("wlbytime.add", "whitelistbytime.add"), migratedConfig.getAddPermission());
        assertEquals(Set.of("wlbytime.reload", "whitelistbytime.reload"), migratedConfig.getReloadPermission());
    }
}
