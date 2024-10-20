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
                .until(System.currentTimeMillis() + 100000) // entry is not expired
                .build();
    }

    @Test
    public void testIsFrozen_false_whenNotFrozen() {
        assertFalse(entry.isFrozen());
    }

    @Test
    public void testIsFrozen_true_whenFrozen() {
        entry.freeze(10000L);
        assertTrue(entry.isFrozen());
    }

    @Test
    public void testGetFrozenAt_throws_whenNotFrozen() {
        assertThrows(UnsupportedOperationException.class, entry::getFrozenAt);
    }

    @Test
    public void testGetFrozenAt_returnsTimestamp_whenFrozen() {
        entry.freeze(10000L);
        assertNotNull(entry.getFrozenAt());
    }

    @Test
    public void testGetFrozenUntil_throws_whenNotFrozen() {
        assertThrows(UnsupportedOperationException.class, entry::getFrozenUntil);
    }

    @Test
    public void testGetFrozenUntil_returnsTimestamp_whenFrozen() {
        entry.freeze(10000L);
        assertNotNull(entry.getFrozenUntil());
    }

    @Test
    public void testIsNotInWhitelist_false_whenInWhitelist() {
        assertFalse(entry.isNotInWhitelist());
    }

    @Test
    public void testIsNotInWhitelist_true_whenNotInWhitelist() {
        entry.setNotInWhitelist();
        assertTrue(entry.isNotInWhitelist());
    }

    @Test
    public void testSetNotInWhitelist_throws_whenFrozen() {
        entry.freeze(10000L);
        assertThrows(UnsupportedOperationException.class, entry::setNotInWhitelist);
    }

    @Test
    public void testSetNotInWhitelist_throws_whenForever() {
        entry.setForever();
        assertThrows(UnsupportedOperationException.class, entry::setNotInWhitelist);
    }

    @Test
    public void testIsForever_true_whenForever() {
        entry.setForever();
        assertTrue(entry.isForever());
    }

    @Test
    public void testIsForever_false_whenNotForever() {
        assertFalse(entry.isForever());
    }

    @Test
    public void testSetForever_throws_whenFrozen() {
        entry.freeze(10000L);
        assertThrows(UnsupportedOperationException.class, entry::setForever);
    }

    @Test
    public void testSetForever_throws_whenNotInWhitelist() {
        entry.setNotInWhitelist();
        assertThrows(UnsupportedOperationException.class, entry::setForever);
    }

    @Test
    public void testIsExpiredIncludeFreeze_false_whenNotExpired() {
        assertFalse(entry.isExpiredIncludeFreeze());
    }

    @Test
    public void testIsExpiredIncludeFreeze_true_whenExpired() {
        entry = Entry.builder()
                .id(1L)
                .nickname("testUser")
                .until(System.currentTimeMillis() - 10000) // already expired
                .build();

        assertTrue(entry.isExpiredIncludeFreeze());
    }

    @Test
    public void testIsExpiredIncludeFreeze_false_whenFrozenTimePassed() {
        entry.freeze(-10000L); // simulate frozen time in the past
        assertTrue(entry.isExpiredIncludeFreeze());
    }

    @Test
    public void testIsActive_true_whenNotExpiredAndNotFrozen() {
        assertTrue(entry.isActive());
    }

    @Test
    public void testIsActive_false_whenExpired() {
        entry = Entry.builder()
                .id(1L)
                .nickname("testUser")
                .until(System.currentTimeMillis() - 10000) // already expired
                .build();

        assertFalse(entry.isActive());
    }

    @Test
    public void testGetRemainingTime_throws_whenFrozen() {
        entry.freeze(10000L);
        assertThrows(UnsupportedOperationException.class, entry::getRemainingTime);
    }

    @Test
    public void testGetRemainingTime_returnsTime_whenNotFrozen() {
        long remainingTime = entry.getRemainingTime();
        assertTrue(remainingTime > 0);
    }

    @Test
    public void testGetRemainingTime_throws_whenForever() {
        entry.setForever();
        assertThrows(UnsupportedOperationException.class, entry::getRemainingTime);
    }

    @Test
    public void testGetRemainingTimeOfFreeze_throws_whenNotFrozen() {
        assertThrows(UnsupportedOperationException.class, entry::getRemainingTimeOfFreeze);
    }

    @Test
    public void testGetRemainingTimeOfFreeze_returnsTime_whenFrozen() {
        entry.freeze(10000L);
        long remainingFreezeTime = entry.getRemainingTimeOfFreeze();
        assertTrue(remainingFreezeTime > 0);
    }

    @Test
    public void testFreeze_throws_whenAlreadyFrozen() {
        entry.freeze(10000L);
        assertThrows(UnsupportedOperationException.class, () -> entry.freeze(10000L));
    }

    @Test
    public void testFreeze_throws_whenForever() {
        entry.setForever();
        assertThrows(UnsupportedOperationException.class, () -> entry.freeze(10000L));
    }

    @Test
    public void testUnfreeze_resetsFrozenFields() {
        entry.freeze(10000L);
        entry.unfreeze();
        assertFalse(entry.isFrozen());
        assertThrows(UnsupportedOperationException.class, () -> entry.getFrozenAt());
        assertThrows(UnsupportedOperationException.class, () -> entry.getFrozenUntil());
    }

    @Test
    public void testUnfreeze_throws_whenNotFrozen() {
        assertThrows(UnsupportedOperationException.class, entry::unfreeze);
    }

    @Test
    public void testGetUntilIncludeFreeze_throws_whenFrozen() {
        entry.freeze(10000L);
        assertThrows(UnsupportedOperationException.class, entry::getUntilIncludeFreeze);
    }

    @Test
    public void testGetUntilIncludeFreeze_returnsTimestamp_whenNotFrozen() {
        Timestamp untilTimestamp = entry.getUntilIncludeFreeze();
        assertNotNull(untilTimestamp);
    }

    @Test
    public void testGetUntilIncludeFreeze_throws_whenForever() {
        entry.setForever();
        assertThrows(UnsupportedOperationException.class, entry::getUntilIncludeFreeze);
    }
}
