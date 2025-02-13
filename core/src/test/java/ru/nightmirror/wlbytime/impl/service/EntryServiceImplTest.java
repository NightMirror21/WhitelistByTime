package ru.nightmirror.wlbytime.impl.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.nightmirror.wlbytime.entry.EntryImpl;
import ru.nightmirror.wlbytime.interfaces.dao.EntryDao;

import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class EntryServiceImplTest {

    private EntryDao entryDao;
    private EntryServiceImpl entryService;

    @BeforeEach
    public void setUp() {
        entryDao = mock(EntryDao.class);
        entryService = new EntryServiceImpl(entryDao);
    }

    @Test
    public void createWithoutUntilReturnsEntry() {
        String nickname = "test_user";
        EntryImpl entry = EntryImpl.builder().nickname(nickname).build();
        when(entryDao.create(nickname)).thenReturn(entry);
        EntryImpl result = entryService.create(nickname);
        assertNotNull(result);
        assertEquals(nickname, result.getNickname());
        verify(entryDao).create(nickname);
    }

    @Test
    public void createWithFutureUntilReturnsEntry() {
        String nickname = "future_user";
        Instant until = Instant.now().plus(Duration.ofSeconds(10));
        EntryImpl entry = EntryImpl.builder().nickname(nickname).build();
        when(entryDao.create(nickname, until)).thenReturn(entry);
        EntryImpl result = entryService.create(nickname, until);
        assertNotNull(result);
        assertEquals(nickname, result.getNickname());
        verify(entryDao).create(nickname, until);
    }

    @Test
    public void createWithPastUntilThrowsException() {
        String nickname = "past_user";
        Instant pastUntil = Instant.now().minus(Duration.ofSeconds(10));
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> entryService.create(nickname, pastUntil));
        assertEquals("Until must be in the future", exception.getMessage());
        verify(entryDao, never()).create(anyString(), any(Instant.class));
    }

    @Test
    public void removeCallsDaoRemove() {
        EntryImpl entry = EntryImpl.builder().nickname("test_user").build();
        entryService.remove(entry);
        verify(entryDao).remove(entry);
    }

    @Test
    public void freezeCallsEntryFreezeAndDaoUpdate() {
        EntryImpl entry = mock(EntryImpl.class);
        Duration duration = Duration.ofSeconds(10);
        entryService.freeze(entry, duration);
        verify(entry).freeze(duration);
        verify(entryDao).update(entry);
    }

    @Test
    public void unfreezeCallsEntryUnfreezeAndDaoUpdate() {
        EntryImpl entry = mock(EntryImpl.class);
        entryService.unfreeze(entry);
        verify(entry).unfreeze();
        verify(entryDao).update(entry);
    }

    @Test
    public void getEntriesReturnsUnmodifiableSet() {
        Set<EntryImpl> entries = new HashSet<>();
        EntryImpl entry1 = EntryImpl.builder().nickname("user1").build();
        EntryImpl entry2 = EntryImpl.builder().nickname("user2").build();
        entries.add(entry1);
        entries.add(entry2);
        when(entryDao.getAll()).thenReturn(entries);
        Set<EntryImpl> result = entryService.getEntries();
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.containsAll(entries));
        assertThrows(UnsupportedOperationException.class, () -> result.add(EntryImpl.builder().nickname("new_user").build()));
        verify(entryDao).getAll();
    }
}
