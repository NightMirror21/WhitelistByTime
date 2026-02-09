package ru.nightmirror.wlbytime.config.configs;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class MessagesConfigTest {

    @Test
    public void saveAndReloadPreservesValues() throws Exception {
        Path file = Files.createTempFile("messages", ".yml");
        MessagesConfig config = new MessagesConfig();
        config.save(file);

        MessagesConfig reloaded = new MessagesConfig();
        reloaded.reload(file);

        assertNotNull(reloaded.getNotPermission());
        assertNotNull(reloaded.getIncorrectArguments());
        assertEquals(config.getForever(), reloaded.getForever());
        assertEquals(config.getCheckMeStillInWhitelistForever(), reloaded.getCheckMeStillInWhitelistForever());
    }
}
