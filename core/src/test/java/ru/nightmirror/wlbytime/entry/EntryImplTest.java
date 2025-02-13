package ru.nightmirror.wlbytime.entry;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

public class EntryImplTest {

    private EntryImpl entry;
    private static final long TEST_ID = 123L;
    private static final String TEST_NICKNAME = "TestNickname";

    @BeforeEach
    public void setUp() {
        entry = EntryImpl.builder().id(TEST_ID).nickname(TEST_NICKNAME).build();
    }

    @Test
    public void testIsForeverWhenNoExpiration() {
        assertTrue(entry.isForever());
    }

    @Test
    public void testIsForeverWhenExpirationSet() {
        entry.setExpiration(Instant.now().plus(Duration.ofSeconds(10)));
        assertFalse(entry.isForever());
    }

    @Test
    public void testSetForever() {
        entry.setExpiration(Instant.now().plus(Duration.ofSeconds(5)));
        entry.setForever();
        assertTrue(entry.isForever());
    }

    @Test
    public void testIsActiveForForeverEntry() {
        assertTrue(entry.isActive());
    }

    @Test
    public void testIsActiveForNonExpiredEntry() {
        entry.setExpiration(Instant.now().plus(Duration.ofSeconds(10)));
        assertTrue(entry.isActive());
    }

    @Test
    public void testIsActiveForExpiredEntryWithoutFreeze() {
        entry.setExpiration(Instant.now().minus(Duration.ofSeconds(5)));
        assertFalse(entry.isActive());
    }

    @Test
    public void testIsActiveForExpiredEntryWithActiveFreeze() {
        Instant expiration = Instant.now().minus(Duration.ofMillis(50));
        entry.setExpiration(expiration);
        entry.freeze(Duration.ofMillis(200));
        assertTrue(entry.isActive());
    }

    @Test
    public void testFreezeSetsFreezing() {
        entry.setExpiration(Instant.now().plus(Duration.ofSeconds(10)));
        entry.freeze(Duration.ofSeconds(5));
        assertTrue(entry.isFrozen());
    }

    @Test
    public void testFreezeThrowsWhenAlreadyFrozen() {
        entry.setExpiration(Instant.now().plus(Duration.ofSeconds(10)));
        entry.freeze(Duration.ofSeconds(5));
        assertThrows(IllegalStateException.class, () -> entry.freeze(Duration.ofSeconds(5)));
    }

    @Test
    public void testFreezeThrowsForForeverEntry() {
        assertThrows(IllegalStateException.class, () -> entry.freeze(Duration.ofSeconds(5)));
    }

    @Test
    public void testFreezeThrowsForZeroDuration() {
        entry.setExpiration(Instant.now().plus(Duration.ofSeconds(10)));
        assertThrows(IllegalArgumentException.class, () -> entry.freeze(Duration.ZERO));
    }

    @Test
    public void testFreezeThrowsForNegativeDuration() {
        entry.setExpiration(Instant.now().plus(Duration.ofSeconds(10)));
        assertThrows(IllegalArgumentException.class, () -> entry.freeze(Duration.ofSeconds(-1)));
    }

    @Test
    public void testUnfreezeWhenFrozen() {
        Instant future = Instant.now().plus(Duration.ofSeconds(20));
        entry.setExpiration(future);
        entry.freeze(Duration.ofSeconds(10));
        Instant oldExpiration = entry.getExpiration().getExpirationTime();
        Duration freezeDuration = entry.getFreezing().getDurationOfFreeze();
        entry.unfreeze();
        assertFalse(entry.isFrozen());
        Instant expected = oldExpiration.plus(freezeDuration);
        Instant actual = entry.getExpiration().getExpirationTime();
        assertEquals(expected, actual);
    }

    @Test
    public void testUnfreezeThrowsWhenNotFrozen() {
        assertThrows(IllegalStateException.class, () -> entry.unfreeze());
    }

    @Test
    public void testUpdateLastJoinAndIsJoined() {
        assertFalse(entry.isJoined());
        entry.updateLastJoin();
        assertTrue(entry.isJoined());
        assertNotNull(entry.getLastJoin());
        assertEquals(TEST_ID, entry.getLastJoin().getEntryId());
    }

    @Test
    public void testGetLeftActiveDurationForNonForeverEntry() {
        entry.setExpiration(Instant.now().plus(Duration.ofSeconds(5)));
        Duration left = entry.getLeftActiveDuration();
        assertFalse(left.isNegative());
    }

    @Test
    public void testGetLeftActiveDurationThrowsForForeverEntry() {
        assertThrows(IllegalStateException.class, () -> entry.getLeftActiveDuration());
    }

    @Test
    public void testGetLeftFreezeDurationForActiveFreeze() {
        entry.setExpiration(Instant.now().plus(Duration.ofSeconds(10)));
        entry.freeze(Duration.ofSeconds(5));
        Duration left = entry.getLeftFreezeDuration();
        assertFalse(left.isNegative());
    }

    @Test
    public void testGetLeftFreezeDurationThrowsForInactiveFreeze() throws InterruptedException {
        entry.setExpiration(Instant.now().plus(Duration.ofSeconds(10)));
        entry.freeze(Duration.ofMillis(50));
        Thread.sleep(100);
        assertTrue(entry.isFreezeInactive());
        assertThrows(IllegalStateException.class, () -> entry.getLeftFreezeDuration());
    }

    @Test
    public void testIsFreezeActiveAndInactive() throws InterruptedException {
        entry.setExpiration(Instant.now().plus(Duration.ofSeconds(10)));
        entry.freeze(Duration.ofMillis(200));
        assertTrue(entry.isFreezeActive());
        Thread.sleep(250);
        assertFalse(entry.isFreezeActive());
        assertTrue(entry.isFreezeInactive());
    }
}
