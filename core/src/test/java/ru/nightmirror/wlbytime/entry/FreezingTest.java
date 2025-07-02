package ru.nightmirror.wlbytime.entry;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FreezingTest {

    @Test
    public void testIsFrozenWhenActive() {
        Freezing freezing = new Freezing(123L, Duration.ofMillis(200));
        assertTrue(freezing.isFrozen());
    }

    @Test
    public void testIsFrozenWhenExpired() throws InterruptedException {
        Freezing freezing = new Freezing(123L, Duration.ofMillis(50));
        Thread.sleep(100);
        assertFalse(freezing.isFrozen());
    }

    @Test
    public void testGetLeftTimePositiveWhenActive() {
        Freezing freezing = new Freezing(123L, Duration.ofSeconds(1));
        assertFalse(freezing.getLeftTime().isNegative());
    }

    @Test
    public void testGetLeftTimeNonPositiveWhenExpired() throws InterruptedException {
        Freezing freezing = new Freezing(123L, Duration.ofMillis(50));
        Thread.sleep(100);
        assertFalse(freezing.getLeftTime().isPositive());
    }

    @Test
    public void testGetDurationOfFreeze() {
        Duration duration = Duration.ofMillis(200);
        Freezing freezing = new Freezing(123L, duration);
        Duration freezeDuration = freezing.getDurationOfFreeze();
        assertTrue(Math.abs(freezeDuration.toMillis() - duration.toMillis()) < 50);
    }

    @Test
    public void testLeftTimeDecreasesOverTime() throws InterruptedException {
        Freezing freezing = new Freezing(1L, Duration.ofMillis(120));
        Duration leftAtStart = freezing.getLeftTime();
        Thread.sleep(60);
        Duration leftAfter60ms = freezing.getLeftTime();
        assertTrue(leftAfter60ms.compareTo(leftAtStart) < 0);
    }

    @Test
    public void testIsFrozenExactlyAtBoundary() throws InterruptedException {
        Freezing freezing = new Freezing(1L, Duration.ofMillis(80));
        Thread.sleep(70);
        assertTrue(freezing.isFrozen());
        Thread.sleep(20);
        assertFalse(freezing.isFrozen());
    }

    @Test
    public void testGetLeftTimeNegativeAfterExpired() throws InterruptedException {
        Freezing freezing = new Freezing(1L, Duration.ofMillis(40));
        Thread.sleep(60);
        assertTrue(freezing.getLeftTime().isZero() || freezing.getLeftTime().isNegative());
    }

    @Test
    public void testFreezeAgainAfterPreviousFreezeExpired() throws InterruptedException {
        EntryImpl entry = EntryImpl.builder()
                .id(1L)
                .nickname("player")
                .build();
        entry.setExpiration(Instant.now().plusSeconds(2));

        entry.freeze(Duration.ofMillis(30));
        Thread.sleep(40);
        assertTrue(entry.isFreezeInactive());

        entry.freeze(Duration.ofMillis(50));
        assertTrue(entry.isFreezeActive());
    }
}
