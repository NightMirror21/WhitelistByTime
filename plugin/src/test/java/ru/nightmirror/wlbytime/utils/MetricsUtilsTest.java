package ru.nightmirror.wlbytime.utils;

import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.Test;

import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MetricsUtilsTest {

    @Test
    public void tryToLoadDoesNotThrow() {
        JavaPlugin plugin = mock(JavaPlugin.class);
        when(plugin.getLogger()).thenReturn(Logger.getAnonymousLogger());

        assertDoesNotThrow(() -> MetricsUtils.tryToLoad(plugin));
    }
}
