package ru.nightmirror.wlbytime.config.configs;

import org.junit.jupiter.api.Test;
import ru.nightmirror.wlbytime.identity.PlayerIdMode;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SettingsConfigTest {

    @Test
    public void saveAndReloadPreservesValues() throws Exception {
        Path file = Files.createTempFile("settings", ".yml");
        SettingsConfig config = new SettingsConfig();
        config.setWhitelistEnabled(false);
        config.setPlayerIdMode(PlayerIdMode.AUTO);
        config.setMojangLookupEnabled(false);
        config.setMojangTimeoutMs(1234);
        config.setYearTimeUnits(Set.of("yy"));
        config.save(file);

        SettingsConfig reloaded = new SettingsConfig();
        reloaded.reload(file);

        assertEquals(false, reloaded.isWhitelistEnabled());
        assertEquals(PlayerIdMode.AUTO, reloaded.getPlayerIdMode());
        assertEquals(false, reloaded.isMojangLookupEnabled());
        assertEquals(1234, reloaded.getMojangTimeoutMs());
        assertEquals(Set.of("yy"), reloaded.getYearTimeUnits());
    }
}
