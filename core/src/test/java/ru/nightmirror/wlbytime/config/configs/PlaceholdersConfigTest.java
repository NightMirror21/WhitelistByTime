package ru.nightmirror.wlbytime.config.configs;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class PlaceholdersConfigTest {

    @Test
    public void saveAndReloadPreservesValues() throws Exception {
        Path file = Files.createTempFile("placeholders", ".yml");
        PlaceholdersConfig config = new PlaceholdersConfig();
        config.save(file);

        PlaceholdersConfig reloaded = new PlaceholdersConfig();
        reloaded.reload(file);

        assertEquals(config.isPlaceholdersEnabled(), reloaded.isPlaceholdersEnabled());
        assertNotNull(reloaded.getForever());
        assertEquals(config.getTimeLeft(), reloaded.getTimeLeft());
    }
}
