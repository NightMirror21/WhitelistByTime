package ru.nightmirror.wlbytime;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class WhitelistPluginTest {

    @Test
    public void whitelistCommandsContainAliases() {
        assertTrue(WhitelistPlugin.WHITELIST_COMMANDS.contains("whitelist"));
        assertTrue(WhitelistPlugin.WHITELIST_COMMANDS.contains("wl"));
        assertTrue(WhitelistPlugin.WHITELIST_COMMANDS.contains("wlbytime"));
        assertTrue(WhitelistPlugin.WHITELIST_COMMANDS.contains("whitelistbytime"));
    }
}
