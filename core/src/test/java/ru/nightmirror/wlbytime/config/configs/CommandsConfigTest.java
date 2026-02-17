package ru.nightmirror.wlbytime.config.configs;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CommandsConfigTest {

    @Test
    public void defaultsIncludeWhitelistByTimeAndWlbyTime() {
        CommandsConfig config = new CommandsConfig();

        assertEquals(Set.of("whitelistbytime.add", "wlbytime.add"), config.getAddPermission());
        assertEquals(Set.of("whitelistbytime.check", "wlbytime.check"), config.getCheckPermission());
        assertEquals(Set.of("whitelistbytime.checkme", "wlbytime.checkme"), config.getCheckMePermission());
        assertEquals(Set.of("whitelistbytime.freeze", "wlbytime.freeze"), config.getFreezePermission());
        assertEquals(Set.of("whitelistbytime.unfreeze", "wlbytime.unfreeze"), config.getUnfreezePermission());
        assertEquals(Set.of("whitelistbytime.getall", "wlbytime.getall"), config.getGetAllPermission());
        assertEquals(Set.of("whitelistbytime.reload", "wlbytime.reload"), config.getReloadPermission());
        assertEquals(Set.of("whitelistbytime.toggle", "wlbytime.toggle"), config.getTogglePermission());
        assertEquals(Set.of("whitelistbytime.remove", "wlbytime.remove"), config.getRemovePermission());
        assertEquals(Set.of("whitelistbytime.time", "wlbytime.time"), config.getTimePermission());
    }

    @Test
    public void saveAndReloadPreservesValues() throws Exception {
        Path file = Files.createTempFile("commands", ".yml");
        CommandsConfig config = new CommandsConfig();
        config.setAddPermission(Set.of("perm.add", "perm.add.alt"));
        config.setCheckPermission(Set.of("perm.check", "perm.check.alt"));
        config.setTogglePermission(Set.of("perm.toggle", "perm.toggle.alt"));
        config.save(file);

        CommandsConfig reloaded = new CommandsConfig();
        reloaded.reload(file);

        assertEquals(Set.of("perm.add", "perm.add.alt"), reloaded.getAddPermission());
        assertEquals(Set.of("perm.check", "perm.check.alt"), reloaded.getCheckPermission());
        assertEquals(Set.of("perm.toggle", "perm.toggle.alt"), reloaded.getTogglePermission());
    }

    @Test
    public void fromLegacyMapsStringPermissionsToSets() {
        LegacyCommandsConfig legacyConfig = new LegacyCommandsConfig();
        legacyConfig.setAddPermission("custom.add");

        CommandsConfig converted = CommandsConfig.fromLegacy(legacyConfig);

        assertEquals(Set.of("custom.add"), converted.getAddPermission());
        assertEquals(Set.of("wlbytime.check", "whitelistbytime.check"), converted.getCheckPermission());
        assertEquals(Set.of("wlbytime.toggle", "whitelistbytime.toggle"), converted.getTogglePermission());
    }
}
