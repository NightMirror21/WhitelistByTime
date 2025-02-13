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
}
