package ru.nightmirror.wlbytime.identity;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FloodgateBridgeTest {

    @Test
    public void returnsEmptyWhenNotAvailable() throws Exception {
        FloodgateBridge bridge = createBridge(false);
        UUID uuid = UUID.randomUUID();

        assertFalse(bridge.isFloodgatePlayer(uuid));
        assertEquals(Optional.empty(), bridge.getCorrectUniqueId(uuid));
        assertEquals(Optional.empty(), bridge.getCorrectUsername(uuid));
    }

    private static FloodgateBridge createBridge(boolean available) throws Exception {
        Constructor<FloodgateBridge> constructor = FloodgateBridge.class.getDeclaredConstructor(boolean.class, Logger.class);
        constructor.setAccessible(true);
        return constructor.newInstance(available, Logger.getAnonymousLogger());
    }
}
