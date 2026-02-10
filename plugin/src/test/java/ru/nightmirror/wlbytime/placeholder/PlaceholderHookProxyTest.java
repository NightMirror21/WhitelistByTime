package ru.nightmirror.wlbytime.placeholder;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;
import ru.nightmirror.wlbytime.interfaces.parser.PlaceholderParser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PlaceholderHookProxyTest {

    @Test
    public void onRequestUsesParser() {
        PlaceholderParser parser = mock(PlaceholderParser.class);
        OfflinePlayer player = mock(OfflinePlayer.class);
        when(player.getName()).thenReturn("Steve");
        when(parser.parse("Steve", "time_left")).thenReturn("10m");

        PlaceholderHookProxy proxy = new PlaceholderHookProxy(parser, "1.0");
        String result = proxy.onRequest(player, "time_left");

        assertEquals("10m", result);
    }

    @Test
    public void onPlaceholderRequestReturnsEmptyWhenPlayerNull() {
        PlaceholderParser parser = mock(PlaceholderParser.class);
        when(parser.getEmpty()).thenReturn("-");

        PlaceholderHookProxy proxy = new PlaceholderHookProxy(parser, "1.0");
        String result = proxy.onPlaceholderRequest(null, "time_left");

        assertEquals("-", result);
    }
}
