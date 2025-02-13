package ru.nightmirror.wlbytime.entry;

import org.junit.jupiter.api.Test;

import java.time.Duration;

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
}
