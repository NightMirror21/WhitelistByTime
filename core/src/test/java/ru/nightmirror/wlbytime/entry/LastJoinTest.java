package ru.nightmirror.wlbytime.entry;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

public class LastJoinTest {

    @Test
    public void testInitialization() {
        LastJoin lastJoin = new LastJoin(123L);
        assertEquals(123L, lastJoin.getEntryId());
        assertNotNull(lastJoin.getLastJoinTime());
        assertTrue(Duration.between(lastJoin.getLastJoinTime(), Instant.now()).abs().getSeconds() < 1);
    }
}
