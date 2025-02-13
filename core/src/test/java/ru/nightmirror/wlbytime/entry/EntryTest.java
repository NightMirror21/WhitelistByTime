package ru.nightmirror.wlbytime.entry;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

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
        entry.setExpiration(Instant.now().plus(Duration.ofSeconds(10)));
        assertFalse(entry.isForever());
    }

    @Test
    public void testIsActive_WhenForever_ShouldReturnTrue() {
        assertTrue(entry.isActive());
    }

    @Test
    public void testIsActive_WhenExpirationIsNotExpired_ShouldReturnTrue() {
        entry.setExpiration(Instant.now().plus(Duration.ofSeconds(10)));
        when(expirationMock.isNotExpired()).thenReturn(true);
        entry = new EntryImpl(TEST_ID, TEST_NICKNAME, expirationMock, null, null);
        assertTrue(entry.isActive());
    }

    @Test
    public void testIsActive_WhenExpirationIsExpired_ShouldReturnFalse() {
        entry.setExpiration(Instant.now().minus(Duration.ofSeconds(10)));
        when(expirationMock.isNotExpired()).thenReturn(false);
        entry = new EntryImpl(TEST_ID, TEST_NICKNAME, expirationMock, null, null);
        assertFalse(entry.isActive());
    }

    @Test
    public void testIsActive_WhenFreezeActiveAndNotExpired_ShouldReturnTrue() {
        entry.setExpiration(Instant.now().minus(Duration.ofSeconds(10)));
        entry.freeze(Duration.ofSeconds(5));
        entry = new EntryImpl(TEST_ID, TEST_NICKNAME, expirationMock, freezingMock, null);

        when(expirationMock.isNotExpired(any())).thenReturn(true);
        when(freezingMock.isFrozen()).thenReturn(true);

        assertTrue(entry.isActive());
    }

    @Test
    public void testIsActive_WhenFreezeInactiveAndExpired_ShouldReturnFalse() {
        entry.setExpiration(Instant.now().minus(Duration.ofSeconds(1)));
        entry.freeze(Duration.ofSeconds(5));

        when(expirationMock.isNotExpired(any(Duration.class))).thenReturn(false);
        when(freezingMock.isFrozen()).thenReturn(false);

        entry = new EntryImpl(TEST_ID, TEST_NICKNAME, expirationMock, freezingMock, null);
        assertFalse(entry.isActive());
    }

    @Test
    public void testIsInactive() {
        assertTrue(entry.isActive());

        entry.setExpiration(Instant.now().plus(Duration.ofSeconds(10)));
        assertFalse(entry.isInactive());

        entry.setExpiration(Instant.now().minus(Duration.ofSeconds(10)));
        assertTrue(entry.isInactive());
    }


    @Test
    public void testSetForever() {
        entry.setExpiration(Instant.now().plus(Duration.ofSeconds(1)));
        entry.setForever();
        assertTrue(entry.isForever());
    }

    @Test
    public void testIsFrozen_WhenFreezingIsNull_ShouldReturnFalse() {
        assertFalse(entry.isFrozen());
    }

    @Test
    public void testIsFrozen_WhenFreezingIsNotNull_ShouldReturnTrue() {
        entry.setExpiration(Instant.now().plus(Duration.ofSeconds(10)));
        entry.freeze(Duration.ofSeconds(1));
        assertTrue(entry.isFrozen());
    }

    @Test
    public void testIsFreezeActive() {
        entry.setExpiration(Instant.now().plus(Duration.ofSeconds(10)));
        entry.freeze(Duration.ofSeconds(5));
        when(freezingMock.isFrozen()).thenReturn(true);
        entry = new EntryImpl(TEST_ID, TEST_NICKNAME, null, freezingMock, null);
        assertTrue(entry.isFreezeActive());
    }

    @Test
    public void testIsFreezeInactive() {
        entry.setExpiration(Instant.now().plus(Duration.ofSeconds(10)));
        entry.freeze(Duration.ofSeconds(5));
        when(freezingMock.isFrozen()).thenReturn(false);
        entry = new EntryImpl(TEST_ID, TEST_NICKNAME, null, freezingMock, null);
        assertTrue(entry.isFreezeInactive());
    }

    @Test
    public void testFreeze_WhenNotFrozen_ShouldSetFreezing() {
        entry.setExpiration(Instant.now().plus(Duration.ofSeconds(10)));
        entry.freeze(Duration.ofSeconds(1));
        assertNotNull(entry.getFreezing());
    }

    @Test
    public void testFreeze_WhenAlreadyFrozen_ShouldThrowException() {
        entry.setExpiration(Instant.now().plus(Duration.ofSeconds(10)));
        entry.freeze(Duration.ofSeconds(1));
        assertThrows(IllegalStateException.class, () -> entry.freeze(Duration.ofSeconds(1)));
    }

    @Test
    public void testUnfreeze_WhenNotFrozen_ShouldThrowException() {
        assertThrows(IllegalStateException.class, entry::unfreeze);
    }

    @Test
    public void testUnfreeze_WhenFrozen_ShouldAddFreezeDurationToExpiration() {
        Instant initialExpirationTime = Instant.now().plus(Duration.ofSeconds(20));
        entry.setExpiration(initialExpirationTime);

        entry.freeze(Duration.ofSeconds(10));

        assertTrue(entry.isFrozen());

        entry.unfreeze();

        assertNotNull(entry.getExpiration());
        assertEquals(initialExpirationTime.plus(Duration.ofSeconds(10)), entry.getExpiration().getExpirationTime());

        assertFalse(entry.isFrozen());
    }

    @Test
    public void testFreeze_WhenForever_ShouldThrowException() {
        entry.setForever();
        assertThrows(IllegalStateException.class, () -> entry.freeze(Duration.ofSeconds(1)));
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
        assertThrows(IllegalStateException.class, entry::getLeftActiveDuration);
    }

    @Test
    public void testGetLeftActiveTime_WhenNotForever_ShouldReturnPositiveTime() {
        entry.setExpiration(Instant.now().plus(Duration.ofSeconds(5)));
        assertTrue(entry.getLeftActiveDuration().isPositive());
    }

    @Test
    public void testGetLeftFreezeTime_WhenFreezeInactive_ShouldThrowException() {
        entry.setExpiration(Instant.now().plus(Duration.ofSeconds(10)));
        entry.freeze(Duration.ofSeconds(1));
        when(freezingMock.isFrozen()).thenReturn(false);
        entry = new EntryImpl(TEST_ID, TEST_NICKNAME, null, freezingMock, null);
        assertThrows(IllegalStateException.class, entry::getLeftFreezeDuration);
    }

    @Test
    public void testIsActive_WhenExpirationIsExpiredAndFreezeIsNull_ShouldReturnFalse() {
        entry.setExpiration(Instant.now().minusSeconds(1));
        assertFalse(entry.isActive());
    }

    @Test
    public void testIsActive_WhenExpirationIsExpiredAndFreezeIsActive_ShouldReturnTrue() {
        entry.setExpiration(Instant.now().minusSeconds(1));
        entry.freeze(Duration.ofSeconds(5));
        when(expirationMock.isNotExpired(any())).thenReturn(true);
        when(freezingMock.isFrozen()).thenReturn(true);

        entry = new EntryImpl(TEST_ID, TEST_NICKNAME, expirationMock, freezingMock, null);
        assertTrue(entry.isActive());
    }

    @Test
    public void testIsActive_WhenExpirationIsExpiredAndFreezeIsInactive_ShouldReturnFalse() {
        entry.setExpiration(Instant.now().minusSeconds(1));
        entry.freeze(Duration.ofSeconds(5));
        when(expirationMock.isNotExpired(any())).thenReturn(false);
        when(freezingMock.isFrozen()).thenReturn(false);

        entry = new EntryImpl(TEST_ID, TEST_NICKNAME, expirationMock, freezingMock, null);
        assertFalse(entry.isActive());
    }

    @Test
    public void testIsFrozen_WhenFreezingIsNotNullAndFrozen_ShouldReturnTrue() {
        entry.setExpiration(Instant.now().plus(Duration.ofSeconds(10)));
        entry.freeze(Duration.ofSeconds(1));
        assertTrue(entry.isFrozen());
    }

    @Test
    public void testIsFreezeInactive_WhenFreezingIsPresentButNotActive_ShouldReturnTrue() {
        entry.setExpiration(Instant.now().plus(Duration.ofSeconds(10)));
        entry.freeze(Duration.ofSeconds(5));
        when(freezingMock.isFrozen()).thenReturn(false);
        entry = new EntryImpl(TEST_ID, TEST_NICKNAME, null, freezingMock, null);
        assertTrue(entry.isFreezeInactive());
    }

    @Test
    public void testFreeze_WithNegativeDuration_ShouldThrowException() {
        assertThrows(IllegalStateException.class, () -> entry.freeze(Duration.ofSeconds(-1)));
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
        entry.setExpiration(Instant.now().plus(Duration.ofSeconds(10)));
        assertTrue(entry.getLeftActiveDuration().isPositive());
    }

    @Test
    public void testGetLeftActiveTime_WhenFrozenAndExpirationInFuture_ShouldIncludeFreezeDuration() {
        entry.setExpiration(Instant.now().plus(Duration.ofSeconds(10)));
        entry.freeze(Duration.ofSeconds(5));
        assertTrue(entry.getLeftActiveDuration().minus(Duration.ofSeconds(5)).isPositive());
    }

    @Test
    public void testGetLeftFreezeTime_WhenFreezeActive_ShouldReturnRemainingFreezeTime() {
        entry.setExpiration(Instant.now().plus(Duration.ofSeconds(10)));
        entry.freeze(Duration.ofSeconds(5));
        assertTrue(entry.getLeftFreezeDuration().isPositive());
    }

}
