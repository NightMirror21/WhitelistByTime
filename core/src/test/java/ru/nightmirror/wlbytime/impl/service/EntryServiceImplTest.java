package ru.nightmirror.wlbytime.impl.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.nightmirror.wlbytime.entry.Entry;
import ru.nightmirror.wlbytime.interfaces.dao.EntryDao;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class EntryServiceImplTest {

    private EntryServiceImpl entryService;
    private EntryDao entryDao;

    @BeforeEach
    public void setUp() {
        entryDao = mock(EntryDao.class);
        entryService = new EntryServiceImpl(entryDao);
    }

    @Test
    public void testCreate_WithValidNickname_ShouldReturnEntry() {
        String nickname = "test_user";
        Entry mockEntry = Entry.builder().nickname(nickname).build();
        when(entryDao.create(nickname)).thenReturn(mockEntry);

        Entry result = entryService.create(nickname);

        assertNotNull(result);
        assertEquals(nickname, result.getNickname());
        verify(entryDao, times(1)).create(nickname);
    }

    @Test
    public void testCreate_WithUntilInFuture_ShouldReturnEntry() {
        String nickname = "future_user";
        long validUntil = System.currentTimeMillis() + 10000;
        Entry mockEntry = Entry.builder().nickname(nickname).build();
        when(entryDao.create(nickname, validUntil)).thenReturn(mockEntry);

        Entry result = entryService.create(nickname, validUntil);

        assertNotNull(result);
        assertEquals(nickname, result.getNickname());
        verify(entryDao, times(1)).create(nickname, validUntil);
    }

    @Test
    public void testCreate_WithUntilInPast_ShouldThrowException() {
        String nickname = "past_user";
        long pastUntil = System.currentTimeMillis() - 10000;

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            entryService.create(nickname, pastUntil);
        });

        assertEquals("Until must be in the future", exception.getMessage());
        verify(entryDao, never()).create(anyString(), anyLong());
    }

    @Test
    public void testRemove_ShouldCallDaoRemove() {
        Entry entry = Entry.builder().nickname("test_user").build();

        entryService.remove(entry);

        verify(entryDao, times(1)).remove(entry);
    }

    @Test
    public void testFreeze_WithValidDuration_ShouldFreezeEntry() {
        Entry entry = mock(Entry.class);
        long duration = 10000;

        entryService.freeze(entry, duration);

        verify(entry, times(1)).freeze(duration);
        verify(entryDao, times(1)).update(entry);
    }

    @Test
    public void testFreeze_WithInvalidDuration_ShouldThrowException_AndShouldNotUpdateEntry() {
        Entry entry = mock(Entry.class);
        long invalidDuration = -5000;

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            entryService.freeze(entry, invalidDuration);
        });

        assertEquals("Duration must be positive", exception.getMessage());
        verify(entry, never()).freeze(anyLong());
        verify(entryDao, never()).update(entry);
    }

    @Test
    public void testUnfreeze_ShouldUnfreezeEntry() {
        Entry entry = mock(Entry.class);

        entryService.unfreeze(entry);

        verify(entry, times(1)).unfreeze();
        verify(entryDao, times(1)).update(entry);
    }

    @Test
    public void testGetEntries_ShouldReturnUnmodifiableSet() {
        Set<Entry> mockEntries = new HashSet<>();
        mockEntries.add(Entry.builder().nickname("user1").build());
        mockEntries.add(Entry.builder().nickname("user2").build());
        when(entryDao.getAll()).thenReturn(mockEntries);

        Set<Entry> result = entryService.getEntries();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.containsAll(mockEntries));

        assertThrows(UnsupportedOperationException.class, () -> result.add(Entry.builder().nickname("new_user").build()));

        verify(entryDao, times(1)).getAll();
    }
}