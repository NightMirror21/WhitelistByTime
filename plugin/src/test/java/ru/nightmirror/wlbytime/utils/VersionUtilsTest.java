package ru.nightmirror.wlbytime.utils;

import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class VersionUtilsTest {

    @Test
    public void getVersionFallsBackToDescription() {
        JavaPlugin plugin = mock(JavaPlugin.class);
        PluginDescriptionFile description = mock(PluginDescriptionFile.class);
        when(plugin.getPluginMeta()).thenThrow(new RuntimeException("no meta"));
        when(plugin.getDescription()).thenReturn(description);
        when(description.getVersion()).thenReturn("2.0.0");

        assertEquals("2.0.0", VersionUtils.getVersion(plugin));
    }
}
