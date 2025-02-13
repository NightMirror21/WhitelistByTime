package ru.nightmirror.wlbytime.impl.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.nightmirror.wlbytime.entry.EntryImpl;
import ru.nightmirror.wlbytime.entry.Expiration;
import ru.nightmirror.wlbytime.interfaces.dao.EntryDao;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EntryTimeServiceImplTest {

    EntryDao entryDao;
    EntryTimeServiceImpl service;

    @BeforeEach
    public void setUp() {
        entryDao = mock(EntryDao.class);
        service = new EntryTimeServiceImpl(entryDao);
    }

    @Test
    public void add_shouldUpdateExpirationAndDao() {
        EntryImpl entry = mock(EntryImpl.class);
        Expiration expiration = mock(Expiration.class);

        when(entry.isForever()).thenReturn(false);
        when(entry.getExpiration()).thenReturn(expiration);

        service.add(entry, Duration.ofSeconds(1));

        verify(expiration).add(Duration.ofSeconds(1));
        verify(entryDao).update(entry);
    }

    @Test
    public void add_shouldThrowForForeverEntry() {
        EntryImpl entry = mock(EntryImpl.class);

        when(entry.isForever()).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> service.add(entry, Duration.ofSeconds(5)));
        verifyNoMoreInteractions(entryDao);
    }

    @Test
    public void canAdd_shouldReturnTrueWhenValid() {
        EntryImpl entry = mock(EntryImpl.class);
        Expiration expiration = mock(Expiration.class);

        when(entry.isForever()).thenReturn(false);
        when(entry.getExpiration()).thenReturn(expiration);
        when(expiration.canAdd(Duration.ofSeconds(1))).thenReturn(true);

        assertTrue(service.canAdd(entry, Duration.ofSeconds(1)));
    }

    @Test
    public void canAdd_shouldReturnFalseForForeverEntry() {
        EntryImpl entry = mock(EntryImpl.class);

        when(entry.isForever()).thenReturn(true);

        assertFalse(service.canAdd(entry, Duration.ofSeconds(1)));
    }

    @Test
    public void remove_shouldUpdateExpirationAndDao() {
        EntryImpl entry = mock(EntryImpl.class);
        Expiration expiration = mock(Expiration.class);

        when(entry.isForever()).thenReturn(false);
        when(entry.getExpiration()).thenReturn(expiration);

        service.remove(entry, Duration.ofSeconds(1));

        verify(expiration).remove(Duration.ofSeconds(1));
        verify(entryDao).update(entry);
    }

    @Test
    public void remove_shouldThrowForForeverEntry() {
        EntryImpl entry = mock(EntryImpl.class);

        when(entry.isForever()).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> service.remove(entry, Duration.ofSeconds(5)));
        verifyNoMoreInteractions(entryDao);
    }

    @Test
    public void canRemove_shouldReturnTrueWhenValid() {
        EntryImpl entry = mock(EntryImpl.class);
        Expiration expiration = mock(Expiration.class);

        when(entry.isForever()).thenReturn(false);
        when(entry.getExpiration()).thenReturn(expiration);
        when(expiration.canRemove(Duration.ofSeconds(1))).thenReturn(true);

        assertTrue(service.canRemove(entry, Duration.ofSeconds(1)));
    }

    @Test
    public void canRemove_shouldReturnFalseForForeverEntry() {
        EntryImpl entry = mock(EntryImpl.class);

        when(entry.isForever()).thenReturn(true);

        assertFalse(service.canRemove(entry, Duration.ofSeconds(1)));
    }

    @Test
    public void set_shouldUpdateExpirationAndDao() {
        EntryImpl entry = mock(EntryImpl.class);
        Expiration expiration = mock(Expiration.class);

        when(entry.getExpiration()).thenReturn(expiration);

        Instant expirationTime = Instant.now().plus(Duration.ofSeconds(1));
        service.set(entry, expirationTime);

        verify(expiration).set(expirationTime);
        verify(entryDao).update(entry);
    }
}