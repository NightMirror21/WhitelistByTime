package ru.nightmirror.wlbytime.entry;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

public class ExpirationTest {

    private static final long TEST_ID = 123L;

    @Test
    public void testIsNotExpiredWhenFuture() {
        Instant future = Instant.now().plus(Duration.ofSeconds(5));
        Expiration expiration = new Expiration(TEST_ID, future);
        assertFalse(expiration.isExpired());
        assertTrue(expiration.isNotExpired());
    }

    @Test
    public void testIsExpiredWhenPast() {
        Instant past = Instant.now().minus(Duration.ofSeconds(5));
        Expiration expiration = new Expiration(TEST_ID, past);
        assertTrue(expiration.isExpired());
    }

    @Test
    public void testAddIncreasesExpirationTime() {
        Instant future = Instant.now().plus(Duration.ofSeconds(5));
        Expiration expiration = new Expiration(TEST_ID, future);
        expiration.add(Duration.ofSeconds(3));
        assertTrue(expiration.getExpirationTime().isAfter(future));
    }

    @Test
    public void testAddThrowsWhenResultingInPast() {
        Instant nearFuture = Instant.now().plus(Duration.ofMillis(50));
        Expiration expiration = new Expiration(TEST_ID, nearFuture);
        assertThrows(IllegalArgumentException.class, () -> expiration.add(Duration.ofSeconds(-1)));
    }

    @Test
    public void testRemoveDecreasesExpirationTime() {
        Instant future = Instant.now().plus(Duration.ofSeconds(10));
        Expiration expiration = new Expiration(TEST_ID, future);
        expiration.remove(Duration.ofSeconds(3));
        assertTrue(expiration.getExpirationTime().isBefore(future));
    }

    @Test
    public void testRemoveThrowsWhenResultingInPast() {
        Instant future = Instant.now().plus(Duration.ofSeconds(2));
        Expiration expiration = new Expiration(TEST_ID, future);
        assertThrows(IllegalArgumentException.class, () -> expiration.remove(Duration.ofSeconds(3)));
    }

    @Test
    public void testSetUpdatesExpirationTime() {
        Instant newTime = Instant.now().plus(Duration.ofSeconds(20));
        Expiration expiration = new Expiration(TEST_ID, Instant.now().plus(Duration.ofSeconds(10)));
        expiration.set(newTime);
        assertEquals(newTime, expiration.getExpirationTime());
    }

    @Test
    public void testSetThrowsWhenNewTimeIsPast() {
        Expiration expiration = new Expiration(TEST_ID, Instant.now().plus(Duration.ofSeconds(10)));
        assertThrows(IllegalArgumentException.class, () -> expiration.set(Instant.now().minus(Duration.ofSeconds(1))));
    }

    @Test
    public void testCanAddReturnsTrue() {
        Instant future = Instant.now().plus(Duration.ofSeconds(10));
        Expiration expiration = new Expiration(TEST_ID, future);
        assertTrue(expiration.canAdd(Duration.ofSeconds(5)));
    }

    @Test
    public void testCanAddReturnsFalse() {
        Instant nearFuture = Instant.now().plus(Duration.ofMillis(50));
        Expiration expiration = new Expiration(TEST_ID, nearFuture);
        assertFalse(expiration.canAdd(Duration.ofSeconds(-1)));
    }

    @Test
    public void testCanRemoveReturnsTrue() {
        Instant future = Instant.now().plus(Duration.ofSeconds(10));
        Expiration expiration = new Expiration(TEST_ID, future);
        assertTrue(expiration.canRemove(Duration.ofSeconds(5)));
    }

    @Test
    public void testCanRemoveReturnsFalse() {
        Instant future = Instant.now().plus(Duration.ofSeconds(5));
        Expiration expiration = new Expiration(TEST_ID, future);
        assertFalse(expiration.canRemove(Duration.ofSeconds(10)));
    }

    @Test
    public void testIsNotPausedByDefault() {
        Expiration expiration = new Expiration(TEST_ID, Instant.now().plus(Duration.ofSeconds(10)));
        assertFalse(expiration.isPaused());
        assertNull(expiration.getPausedAt());
    }

    @Test
    public void testIsPausedAfterPause() {
        Expiration expiration = new Expiration(TEST_ID, Instant.now().plus(Duration.ofSeconds(10)));
        expiration.pause();
        assertTrue(expiration.isPaused());
        assertNotNull(expiration.getPausedAt());
    }

    @Test
    public void testIsNotExpiredWhenPausedWithTimeLeft() {
        Expiration expiration = new Expiration(TEST_ID, Instant.now().plus(Duration.ofSeconds(10)));
        expiration.pause();
        assertFalse(expiration.isExpired());
        assertTrue(expiration.isNotExpired());
    }

    @Test
    public void testIsExpiredWhenPausedAndExpirationAlreadyPast() {
        // Simulate: expiration was in the past at the moment pause() was called
        Instant pastExpiry = Instant.now().minus(Duration.ofSeconds(5));
        Instant pausedAtMoment = Instant.now().minus(Duration.ofSeconds(10)); // paused before expiry
        Expiration expiration = Expiration.builder()
                .entryId(TEST_ID)
                .expirationTime(pastExpiry)
                .pausedAt(pausedAtMoment)
                .build();
        // expirationTime (now-5s) is AFTER pausedAt (now-10s) → not expired at pause moment
        assertFalse(expiration.isExpired());
    }

    @Test
    public void testIsExpiredWhenPausedAndWasAlreadyExpiredAtPauseTime() {
        Instant expiryBeforePause = Instant.now().minus(Duration.ofSeconds(10));
        Instant pausedAfterExpiry = Instant.now().minus(Duration.ofSeconds(5));
        Expiration expiration = Expiration.builder()
                .entryId(TEST_ID)
                .expirationTime(expiryBeforePause)
                .pausedAt(pausedAfterExpiry)
                .build();
        // expirationTime (now-10s) is BEFORE pausedAt (now-5s) → was expired at pause moment
        assertTrue(expiration.isExpired());
    }

    @Test
    public void testPauseIsIdempotent() throws InterruptedException {
        Expiration expiration = new Expiration(TEST_ID, Instant.now().plus(Duration.ofSeconds(10)));
        expiration.pause();
        Instant firstPausedAt = expiration.getPausedAt();
        Thread.sleep(20);
        expiration.pause(); // second pause should be no-op
        assertEquals(firstPausedAt, expiration.getPausedAt());
    }

    @Test
    public void testResumeShiftsExpirationTimeAndClearsPausedAt() throws InterruptedException {
        Instant future = Instant.now().plus(Duration.ofSeconds(10));
        Expiration expiration = new Expiration(TEST_ID, future);
        expiration.pause();
        Thread.sleep(50);
        expiration.resume();
        assertFalse(expiration.isPaused());
        assertNull(expiration.getPausedAt());
        // expirationTime should have shifted forward by ~50ms
        assertTrue(expiration.getExpirationTime().isAfter(future));
    }

    @Test
    public void testResumeIsIdempotentWhenNotPaused() {
        Instant future = Instant.now().plus(Duration.ofSeconds(10));
        Expiration expiration = new Expiration(TEST_ID, future);
        expiration.resume(); // no-op
        assertEquals(future, expiration.getExpirationTime());
        assertFalse(expiration.isPaused());
    }

    @Test
    public void testBuilderWithPausedAt() {
        Instant pausedAt = Instant.now().minusSeconds(5);
        Expiration expiration = Expiration.builder()
                .entryId(TEST_ID)
                .expirationTime(Instant.now().plusSeconds(10))
                .pausedAt(pausedAt)
                .build();
        assertTrue(expiration.isPaused());
        assertEquals(pausedAt, expiration.getPausedAt());
    }
}
