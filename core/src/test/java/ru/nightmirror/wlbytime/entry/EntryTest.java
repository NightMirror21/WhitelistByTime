package ru.nightmirror.wlbytime.entry;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class EntryTest {

    private Entry entry;

    @BeforeEach
    public void setUp() {
        entry = Entry.builder()
                .id(1L)
                .nickname("TestPlayer")
                .build();
    }

    @Test
    public void testIsForever_WhenExpirationIsNull_ShouldReturnTrue() {
        entry.setForever();
        assertTrue(entry.isForever());
    }

    @Test
    public void testIsForever_WhenExpirationIsNotNull_ShouldReturnFalse() {
        entry.setExpiration(new Timestamp(System.currentTimeMillis()));
        assertFalse(entry.isForever());
    }

    @Test
    public void testIsActive_WhenForever_ShouldReturnTrue() {
        entry.setForever();
        assertTrue(entry.isActive());
    }

    @Test
    public void testIsActive_WhenNotExpired_ShouldReturnTrue() {
        Expiration expiration = mock(Expiration.class);
        when(expiration.isNotExpired()).thenReturn(true);
        entry.setExpiration(new Timestamp(System.currentTimeMillis()));
        entry.setExpiration(expiration.getExpirationTime());
        assertTrue(entry.isActive());
    }

    @Test
    public void testIsActive_WhenExpiredAndFrozen_ShouldCheckFreezeTime() {
        Expiration expiration = mock(Expiration.class);
        when(expiration.isNotExpired()).thenReturn(false);
        when(expiration.isNotExpired(any(Long.class))).thenReturn(true);
        entry.setExpiration(expiration.getExpirationTime());
        entry.freeze(1000L);
        assertTrue(entry.isActive());
    }

    @Test
    public void testIsInactive_WhenNotActive_ShouldReturnTrue() {
        entry.setExpiration(new Timestamp(System.currentTimeMillis() - 10000));
        assertTrue(entry.isInactive());
    }

    @Test
    public void testSetForever_ShouldSetExpirationToNull() {
        entry.setForever();
        assertNull(entry.getExpiration());
    }

    @Test
    public void testSetExpiration_ShouldSetExpirationToNonNull() {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis() + 10000);
        entry.setExpiration(timestamp);
        assertNotNull(entry.getExpiration());
    }

    @Test
    public void testIsFrozen_WhenFreezingIsNotNull_ShouldReturnTrue() {
        entry.freeze(1000L);
        assertTrue(entry.isFrozen());
    }

    @Test
    public void testIsNotFrozen_WhenFreezingIsNull_ShouldReturnTrue() {
        assertTrue(entry.isNotFrozen());
    }

    @Test
    public void testIsFreezeActive_WhenFreezingIsActive_ShouldReturnTrue() {
        Freezing freezing = mock(Freezing.class);
        when(freezing.isFrozen()).thenReturn(true);
        entry.freeze(1000L);
        assertTrue(entry.isFreezeActive());
    }

    @Test
    public void testIsFreezeInactive_WhenFreezingIsInactive_ShouldReturnTrue() {
        Freezing freezing = mock(Freezing.class);
        when(freezing.isFrozen()).thenReturn(false);
        entry.freeze(1000L);
        assertTrue(entry.isFreezeInactive());
    }

    @Test
    public void testFreeze_WhenAlreadyFrozen_ShouldThrowIllegalStateException() {
        entry.freeze(1000L);
        assertThrows(IllegalStateException.class, () -> entry.freeze(5000L));
    }

    @Test
    public void testUpdateLastJoin_ShouldSetLastJoinToNonNull() {
        entry.updateLastJoin();
        assertNotNull(entry.getLastJoin());
    }

    @Test
    public void testIsJoined_WhenLastJoinIsNotNull_ShouldReturnTrue() {
        entry.updateLastJoin();
        assertTrue(entry.isJoined());
    }

    @Test
    public void testIsJoined_WhenLastJoinIsNull_ShouldReturnFalse() {
        assertFalse(entry.isJoined());
    }
}
