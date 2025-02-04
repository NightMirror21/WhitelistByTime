package ru.nightmirror.wlbytime.entry;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class EntryTest {
    private static final long TEST_ID = 123L;
    private static final String TEST_NICKNAME = "TestNickname";
    private EntryImpl entry;
    private Expiration expirationMock;
    private Freezing freezingMock;

    @BeforeEach
    public void setUp() {
        entry = new EntryImpl(TEST_ID, TEST_NICKNAME, null, null, null);
        expirationMock = mock(Expiration.class);
        freezingMock = mock(Freezing.class);
    }

    @Test
    public void testIsForever_WhenExpirationIsNull_ShouldReturnTrue() {
        assertTrue(entry.isForever());
    }

    @Test
    public void testIsForever_WhenExpirationIsNotNull_ShouldReturnFalse() {
        entry.setExpiration(new Timestamp(System.currentTimeMillis() + 1000));
        assertFalse(entry.isForever());
    }

    @Test
    public void testIsActive_WhenForever_ShouldReturnTrue() {
        assertTrue(entry.isActive());
    }

    @Test
    public void testIsActive_WhenExpirationIsNotExpired_ShouldReturnTrue() {
        entry.setExpiration(new Timestamp(System.currentTimeMillis() + 1000));
        when(expirationMock.isNotExpired()).thenReturn(true);
        entry = new EntryImpl(TEST_ID, TEST_NICKNAME, expirationMock, null, null);
        assertTrue(entry.isActive());
    }

    @Test
    public void testIsActive_WhenExpirationIsExpired_ShouldReturnFalse() {
        entry.setExpiration(new Timestamp(System.currentTimeMillis() - 1000));
        when(expirationMock.isNotExpired()).thenReturn(false);
        entry = new EntryImpl(TEST_ID, TEST_NICKNAME, expirationMock, null, null);
        assertFalse(entry.isActive());
    }

    @Test
    public void testIsActive_WhenFreezeActiveAndNotExpired_ShouldReturnTrue() {
        entry.setExpiration(new Timestamp(System.currentTimeMillis() - 1000));
        entry.freeze(5000L);
        entry = new EntryImpl(TEST_ID, TEST_NICKNAME, expirationMock, freezingMock, null);

        when(expirationMock.isNotExpired(anyLong())).thenReturn(true);
        when(freezingMock.isFrozen()).thenReturn(true);

        assertTrue(entry.isActive());
    }

    @Test
    public void testIsActive_WhenFreezeInactiveAndExpired_ShouldReturnFalse() {
        entry.setExpiration(new Timestamp(System.currentTimeMillis() - 1000));
        entry.freeze(5000L);

        when(expirationMock.isNotExpired(anyLong())).thenReturn(false);
        when(freezingMock.isFrozen()).thenReturn(false);

        entry = new EntryImpl(TEST_ID, TEST_NICKNAME, expirationMock, freezingMock, null);
        assertFalse(entry.isActive());
    }

    @Test
    public void testIsInactive() {
        assertTrue(entry.isActive());

        entry.setExpiration(new Timestamp(System.currentTimeMillis() + 10000));
        assertFalse(entry.isInactive());

        entry.setExpiration(new Timestamp(System.currentTimeMillis() - 10000));
        assertTrue(entry.isInactive());
    }


    @Test
    public void testSetForever() {
        entry.setExpiration(new Timestamp(System.currentTimeMillis() + 1000));
        entry.setForever();
        assertTrue(entry.isForever());
    }

    @Test
    public void testIsFrozen_WhenFreezingIsNull_ShouldReturnFalse() {
        assertFalse(entry.isFrozen());
    }

    @Test
    public void testIsFrozen_WhenFreezingIsNotNull_ShouldReturnTrue() {
        entry.setExpiration(new Timestamp(System.currentTimeMillis() + 10000L));
        entry.freeze(1000);
        assertTrue(entry.isFrozen());
    }

    @Test
    public void testIsFreezeActive() {
        entry.setExpiration(new Timestamp(System.currentTimeMillis() + 10000L));
        entry.freeze(5000L);
        when(freezingMock.isFrozen()).thenReturn(true);
        entry = new EntryImpl(TEST_ID, TEST_NICKNAME, null, freezingMock, null);
        assertTrue(entry.isFreezeActive());
    }

    @Test
    public void testIsFreezeInactive() {
        entry.setExpiration(new Timestamp(System.currentTimeMillis() + 10000L));
        entry.freeze(5000L);
        when(freezingMock.isFrozen()).thenReturn(false);
        entry = new EntryImpl(TEST_ID, TEST_NICKNAME, null, freezingMock, null);
        assertTrue(entry.isFreezeInactive());
    }

    @Test
    public void testFreeze_WhenNotFrozen_ShouldSetFreezing() {
        entry.setExpiration(new Timestamp(System.currentTimeMillis() + 10000L));
        entry.freeze(1000L);
        assertNotNull(entry.getFreezing());
    }

    @Test
    public void testFreeze_WhenAlreadyFrozen_ShouldThrowException() {
        entry.setExpiration(new Timestamp(System.currentTimeMillis() + 10000L));
        entry.freeze(1000L);
        assertThrows(IllegalStateException.class, () -> entry.freeze(1000L));
    }

    @Test
    public void testUnfreeze_WhenNotFrozen_ShouldThrowException() {
        assertThrows(IllegalStateException.class, entry::unfreeze);
    }

    @Test
    public void testUnfreeze_WhenFrozen_ShouldAddFreezeDurationToExpiration() {
        long initialExpirationTime = System.currentTimeMillis() + 20000L;
        entry.setExpiration(new Timestamp(initialExpirationTime));

        entry.freeze(10000L);

        assertTrue(entry.isFrozen());

        entry.unfreeze();

        assertNotNull(entry.getExpiration());
        assertEquals(initialExpirationTime + 10000L, entry.getExpiration().getExpirationTime().getTime());

        assertFalse(entry.isFrozen());
    }

    @Test
    public void testFreeze_WhenForever_ShouldThrowException() {
        entry.setForever();
        assertThrows(IllegalStateException.class, () -> entry.freeze(1000L));
    }

    @Test
    public void testUpdateLastJoin() {
        entry.updateLastJoin();
        assertNotNull(entry.getLastJoin());
    }

    @Test
    public void testIsJoined() {
        assertFalse(entry.isJoined());
        entry.updateLastJoin();
        assertTrue(entry.isJoined());
    }

    @Test
    public void testGetLeftActiveTime_WhenForever_ShouldThrowException() {
        assertThrows(IllegalStateException.class, entry::getLeftActiveTime);
    }

    @Test
    public void testGetLeftActiveTime_WhenNotForever_ShouldReturnPositiveTime() {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis() + 5000);
        entry.setExpiration(timestamp);
        assertTrue(entry.getLeftActiveTime() > 0);
    }

    @Test
    public void testGetLeftFreezeTime_WhenFreezeInactive_ShouldThrowException() {
        entry.setExpiration(new Timestamp(System.currentTimeMillis() + 10000L));
        entry.freeze(1000L);
        when(freezingMock.isFrozen()).thenReturn(false);
        entry = new EntryImpl(TEST_ID, TEST_NICKNAME, null, freezingMock, null);
        assertThrows(IllegalStateException.class, entry::getLeftFreezeTime);
    }

    @Test
    public void testIsActive_WhenExpirationIsExpiredAndFreezeIsNull_ShouldReturnFalse() {
        entry.setExpiration(new Timestamp(System.currentTimeMillis() - 1000));
        assertFalse(entry.isActive());
    }

    @Test
    public void testIsActive_WhenExpirationIsExpiredAndFreezeIsActive_ShouldReturnTrue() {
        entry.setExpiration(new Timestamp(System.currentTimeMillis() - 1000));
        entry.freeze(5000L);
        when(expirationMock.isNotExpired(anyLong())).thenReturn(true);
        when(freezingMock.isFrozen()).thenReturn(true);

        entry = new EntryImpl(TEST_ID, TEST_NICKNAME, expirationMock, freezingMock, null);
        assertTrue(entry.isActive());
    }

    @Test
    public void testIsActive_WhenExpirationIsExpiredAndFreezeIsInactive_ShouldReturnFalse() {
        entry.setExpiration(new Timestamp(System.currentTimeMillis() - 1000));
        entry.freeze(5000L);
        when(expirationMock.isNotExpired(anyLong())).thenReturn(false);
        when(freezingMock.isFrozen()).thenReturn(false);

        entry = new EntryImpl(TEST_ID, TEST_NICKNAME, expirationMock, freezingMock, null);
        assertFalse(entry.isActive());
    }

    @Test
    public void testIsFrozen_WhenFreezingIsNotNullAndFrozen_ShouldReturnTrue() {
        entry.setExpiration(new Timestamp(System.currentTimeMillis() + 10000L));
        entry.freeze(1000L);
        assertTrue(entry.isFrozen());
    }

    @Test
    public void testIsFreezeInactive_WhenFreezingIsPresentButNotActive_ShouldReturnTrue() {
        entry.setExpiration(new Timestamp(System.currentTimeMillis() + 10000L));
        entry.freeze(5000L);
        when(freezingMock.isFrozen()).thenReturn(false);
        entry = new EntryImpl(TEST_ID, TEST_NICKNAME, null, freezingMock, null);
        assertTrue(entry.isFreezeInactive());
    }

    @Test
    public void testFreeze_WithNegativeDuration_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> entry.freeze(-1000L));
    }

    @Test
    public void testUpdateLastJoin_ShouldCreateNewLastJoinInstance() {
        assertNull(entry.getLastJoin());
        entry.updateLastJoin();
        assertNotNull(entry.getLastJoin());
        assertEquals(TEST_ID, entry.getLastJoin().getEntryId());
    }

    @Test
    public void testIsJoined_WhenLastJoinIsNotNull_ShouldReturnTrue() {
        entry.updateLastJoin();
        assertTrue(entry.isJoined());
    }

    @Test
    public void testGetLeftActiveTime_WhenExpirationIsInFuture_ShouldReturnPositiveValue() {
        Timestamp futureTime = new Timestamp(System.currentTimeMillis() + 10000L);
        entry.setExpiration(futureTime);
        assertTrue(entry.getLeftActiveTime() > 0);
    }

    @Test
    public void testGetLeftActiveTime_WhenFrozenAndExpirationInFuture_ShouldIncludeFreezeDuration() {
        entry.setExpiration(new Timestamp(System.currentTimeMillis() + 10000L));
        entry.freeze(5000L);
        assertTrue(entry.getLeftActiveTime() > 5000L);
    }

    @Test
    public void testGetLeftFreezeTime_WhenFreezeActive_ShouldReturnRemainingFreezeTime() {
        entry.setExpiration(new Timestamp(System.currentTimeMillis() + 10000L));
        entry.freeze(5000L);
        assertTrue(entry.getLeftFreezeTime() > 0);
    }

}
