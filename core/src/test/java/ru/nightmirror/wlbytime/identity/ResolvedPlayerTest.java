package ru.nightmirror.wlbytime.identity;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ResolvedPlayerTest {

    @Test
    public void isUuidKeyReflectsKeyType() {
        ResolvedPlayer nick = new ResolvedPlayer(PlayerKey.nickname("Steve"), "Steve", null);
        ResolvedPlayer uuid = new ResolvedPlayer(PlayerKey.uuid(UUID.randomUUID()), "Alex", UUID.randomUUID());

        assertFalse(nick.isUuidKey());
        assertTrue(uuid.isUuidKey());
    }
}
