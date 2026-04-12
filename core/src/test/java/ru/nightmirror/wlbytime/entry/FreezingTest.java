package ru.nightmirror.wlbytime.entry;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;


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
        Thread.sleep(50);
        assertTrue(freezing.isFrozen());
        Thread.sleep(50);
        assertFalse(freezing.isFrozen());
    }

    @Test
    public void testGetLeftTimeNegativeAfterExpired() throws InterruptedException {
        Freezing freezing = new Freezing(1L, Duration.ofMillis(40));
        Thread.sleep(60);
        assertTrue(freezing.getLeftTime().isZero() || freezing.getLeftTime().isNegative());
    }

    @Test
    public void testIsPausedWhenNotPaused() {
        Freezing freezing = new Freezing(1L, Duration.ofSeconds(10));
        assertFalse(freezing.isPaused());
    }

    @Test
    public void testIsPausedAfterPause() {
        Freezing freezing = new Freezing(1L, Duration.ofSeconds(10));
        freezing.pause();
        assertTrue(freezing.isPaused());
    }

    @Test
    public void testIsFrozenWhenPaused() {
        Freezing freezing = new Freezing(1L, Duration.ofSeconds(10));
        freezing.pause();
        assertTrue(freezing.isFrozen());
    }

    @Test
    public void testGetLeftTimeDoesNotDecreaseWhenPaused() throws InterruptedException {
        Freezing freezing = new Freezing(1L, Duration.ofSeconds(10));
        freezing.pause();
        Duration leftAtPause = freezing.getLeftTime();
        Thread.sleep(50);
        Duration leftAfterSleep = freezing.getLeftTime();
        assertEquals(leftAtPause, leftAfterSleep);
    }

    @Test
    public void testResumeUnpausesFreezing() {
        Freezing freezing = new Freezing(1L, Duration.ofSeconds(10));
        freezing.pause();
        assertTrue(freezing.isPaused());
        freezing.resume();
        assertFalse(freezing.isPaused());
    }

    @Test
    public void testResumeShiftsEndTimeByOfflineDuration() throws InterruptedException {
        Freezing freezing = new Freezing(1L, Duration.ofSeconds(10));
        freezing.pause();
        Thread.sleep(60);
        Duration leftBeforeResume = freezing.getLeftTime();
        freezing.resume();
        Duration leftAfterResume = freezing.getLeftTime();
        // After resume, endTime was shifted by offline duration, so leftTime should be close to leftBeforeResume
        long diff = Math.abs(leftAfterResume.toMillis() - leftBeforeResume.toMillis());
        assertTrue(diff < 50, "Left time after resume should be close to left time at pause moment");
    }

    @Test
    public void testPauseIsIdempotent() throws InterruptedException {
        Freezing freezing = new Freezing(1L, Duration.ofSeconds(10));
        freezing.pause();
        Duration leftFirst = freezing.getLeftTime();
        Thread.sleep(20);
        freezing.pause(); // second pause should be no-op
        Duration leftSecond = freezing.getLeftTime();
        assertEquals(leftFirst, leftSecond);
    }

    @Test
    public void testResumeIsIdempotentWhenNotPaused() {
        Freezing freezing = new Freezing(1L, Duration.ofSeconds(10));
        Duration leftBefore = freezing.getLeftTime();
        freezing.resume(); // should be no-op
        Duration leftAfter = freezing.getLeftTime();
        long diff = Math.abs(leftAfter.toMillis() - leftBefore.toMillis());
        assertTrue(diff < 50);
    }

    @Test
    public void testBuilderWithPausedAt() {
        Instant pausedAt = Instant.now().minusSeconds(5);
        Freezing freezing = Freezing.builder()
                .entryId(1L)
                .startTime(Instant.now().minusSeconds(10))
                .endTime(Instant.now().plusSeconds(10))
                .pausedAt(pausedAt)
                .build();
        assertTrue(freezing.isPaused());
        assertEquals(pausedAt, freezing.getPausedAt());
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
