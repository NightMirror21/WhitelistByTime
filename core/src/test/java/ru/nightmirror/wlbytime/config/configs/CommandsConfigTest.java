package ru.nightmirror.wlbytime.config.configs;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CommandsConfigTest {

    @Test
    public void saveAndReloadPreservesValues() throws Exception {
        Path file = Files.createTempFile("commands", ".yml");
        CommandsConfig config = new CommandsConfig();
        config.setAddPermission("perm.add");
        config.setCheckPermission("perm.check");
        config.setTogglePermission("perm.toggle");
        config.save(file);

        CommandsConfig reloaded = new CommandsConfig();
        reloaded.reload(file);

        assertEquals("perm.add", reloaded.getAddPermission());
        assertEquals("perm.check", reloaded.getCheckPermission());
        assertEquals("perm.toggle", reloaded.getTogglePermission());
    }
}
