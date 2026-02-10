package ru.nightmirror.wlbytime.identity;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PlayerKeyTest {

    @Test
    public void nicknameCreatesNonUuidKey() {
        PlayerKey key = PlayerKey.nickname("Steve");
        assertEquals("Steve", key.value());
        assertFalse(key.uuid());
    }

    @Test
    public void uuidCreatesLowercaseUuidKey() {
        UUID uuid = UUID.fromString("12345678-1234-1234-1234-1234567890AB");
        PlayerKey key = PlayerKey.uuid(uuid);
        assertEquals("12345678-1234-1234-1234-1234567890ab", key.value());
        assertTrue(key.uuid());
    }
}
