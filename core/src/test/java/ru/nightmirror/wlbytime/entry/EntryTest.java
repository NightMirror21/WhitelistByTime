package ru.nightmirror.wlbytime.entry;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;

import static org.junit.jupiter.api.Assertions.*;

public class EntryTest {

    private Entry entry;

    @BeforeEach
    public void setup() {
        entry = Entry.builder()
                .id(1L)
                .nickname("testUser")
                .until(System.currentTimeMillis() + 100000)
                .build();
    }

    @Test
    public void testApplyFreeze_setsFrozenTimestamps() {
        entry.applyFreeze(10000L);
        assertNotNull(entry.getFreezeStartTime());
        assertNotNull(entry.getFreezeEndTime());
    }

    @Test
    public void testGetFrozenAt_throws_whenNotFrozen() {
        assertThrows(UnsupportedOperationException.class, entry::getFreezeStartTime);
    }

    @Test
    public void testGetFrozenAt_returnsTimestamp_whenFrozen() {
        entry.applyFreeze(10000L);
        assertNotNull(entry.getFreezeStartTime());
    }

    @Test
    public void testGetFrozenUntil_throws_whenNotFrozen() {
        assertThrows(UnsupportedOperationException.class, entry::getFreezeEndTime);
    }

    @Test
    public void testGetFrozenUntil_returnsTimestamp_whenFrozen() {
        entry.applyFreeze(10000L);
        assertNotNull(entry.getFreezeEndTime());
    }

    @Test
    public void testIsExcludedFromWhitelist() {
        assertFalse(entry.isExcludedFromWhitelist());
    }

    @Test
    public void testIsNotInWhitelist_true_whenExcludedFromWhitelist() {
        entry.markAsNotInWhitelist();
        assertTrue(entry.isExcludedFromWhitelist());
    }

    @Test
    public void testMarkAsNotInWhitelist_throws_whenFrozen() {
        entry.applyFreeze(10000L);
        assertThrows(UnsupportedOperationException.class, entry::markAsNotInWhitelist);
    }

    @Test
    public void testMarkAsNotInWhitelist_throws_whenForever() {
        entry.setForever();
        assertThrows(UnsupportedOperationException.class, entry::markAsNotInWhitelist);
    }

    @Test
    public void testHasNoExpiration() {
        entry.setForever();
        assertTrue(entry.hasNoExpiration());
    }

    @Test
    public void testSetForever_throws_whenFrozen() {
        entry.applyFreeze(10000L);
        assertThrows(UnsupportedOperationException.class, entry::setForever);
    }

    @Test
    public void testSetForever_throws_whenNotInWhitelist() {
        entry.markAsNotInWhitelist();
        assertThrows(UnsupportedOperationException.class, entry::setForever);
    }

    @Test
    public void testIsExpiredIncludeApplyFreeze_false_whenNotExpired() {
        assertFalse(entry.isExpiredConsideringFreeze());
    }

    @Test
    public void testIsExpiredIncludeApplyFreeze_true_whenExpired() {
        entry = Entry.builder()
                .id(1L)
                .nickname("testUser")
                .until(System.currentTimeMillis() - 10000)
                .build();

        assertTrue(entry.isExpiredConsideringFreeze());
    }

    @Test
    public void testIsExpiredIncludeApplyFreeze_false_whenFrozenTimePassed() {
        entry.applyFreeze(-10000L);
        assertTrue(entry.isExpiredConsideringFreeze());
    }

    @Test
    public void testIsCurrentlyActive_true_whenNotExpiredAndNotFrozen() {
        assertTrue(entry.isCurrentlyActive());
    }

    @Test
    public void testIsCurrentlyActive_false_whenExpired() {
        entry = Entry.builder()
                .id(1L)
                .nickname("testUser")
                .until(System.currentTimeMillis() - 10000)
                .build();

        assertFalse(entry.isCurrentlyActive());
    }

    @Test
    public void testGetRemainingActiveTime_throws_whenFrozen() {
        entry.applyFreeze(10000L);
        assertThrows(UnsupportedOperationException.class, entry::getRemainingActiveTime);
    }

    @Test
    public void testGetRemainingTime_returnsActiveTime_whenNotFrozen() {
        long remainingTime = entry.getRemainingActiveTime();
        assertTrue(remainingTime > 0);
    }

    @Test
    public void testGetRemainingActiveTime_throws_whenForever() {
        entry.setForever();
        assertThrows(UnsupportedOperationException.class, entry::getRemainingActiveTime);
    }

    @Test
    public void testGetRemainingActiveTimeOfApplyFreeze_throws_whenNotFrozen() {
        assertThrows(UnsupportedOperationException.class, entry::getRemainingFreezeTime);
    }

    @Test
    public void testGetRemainingTimeOfApplyFreeze_returnsActiveTime_whenFrozen() {
        entry.applyFreeze(10000L);
        long remainingFreezeTime = entry.getRemainingFreezeTime();
        assertTrue(remainingFreezeTime > 0);
    }

    @Test
    public void testApplyFreeze_throws_whenAlreadyFrozen() {
        entry.applyFreeze(10000L);
        assertThrows(UnsupportedOperationException.class, () -> entry.applyFreeze(10000L));
    }

    @Test
    public void testApplyFreeze_throws_whenForever() {
        entry.setForever();
        assertThrows(UnsupportedOperationException.class, () -> entry.applyFreeze(10000L));
    }

    @Test
    public void testRemoveFreeze_resetsFrozenFields() {
        entry.applyFreeze(10000L);
        entry.removeFreeze();
        assertFalse(entry.isCurrentlyFrozen());
        assertThrows(UnsupportedOperationException.class, () -> entry.getFreezeStartTime());
        assertThrows(UnsupportedOperationException.class, () -> entry.getFreezeEndTime());
    }

    @Test
    public void testRemoveFreeze_throws_whenNotFrozen() {
        assertThrows(UnsupportedOperationException.class, entry::removeFreeze);
    }

    @Test
    public void testGetUntilIncludeApplyFreeze_throws_whenFrozen() {
        entry.applyFreeze(10000L);
        assertThrows(UnsupportedOperationException.class, entry::getEffectiveUntilTimestamp);
    }

    @Test
    public void testGetUntilIncludeApplyFreeze_returnsTimestamp_whenNotFrozen() {
        Timestamp untilTimestamp = entry.getEffectiveUntilTimestamp();
        assertNotNull(untilTimestamp);
    }

    @Test
    public void testGetUntilIncludeApplyFreeze_throws_whenForever() {
        entry.setForever();
        assertThrows(UnsupportedOperationException.class, entry::getEffectiveUntilTimestamp);
    }
}
