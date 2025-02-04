package ru.nightmirror.wlbytime.impl.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.nightmirror.wlbytime.entry.EntryImpl;
import ru.nightmirror.wlbytime.entry.Expiration;
import ru.nightmirror.wlbytime.interfaces.dao.EntryDao;

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

        service.add(entry, 1000L);

        verify(expiration).add(1000L);
        verify(entryDao).update(entry);
    }

    @Test
    public void add_shouldThrowForForeverEntry() {
        EntryImpl entry = mock(EntryImpl.class);

        when(entry.isForever()).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> service.add(entry, 5000L));
        verifyNoMoreInteractions(entryDao);
    }

    @Test
    public void canAdd_shouldReturnTrueWhenValid() {
        EntryImpl entry = mock(EntryImpl.class);
        Expiration expiration = mock(Expiration.class);

        when(entry.isForever()).thenReturn(false);
        when(entry.getExpiration()).thenReturn(expiration);
        when(expiration.canAdd(1000L)).thenReturn(true);

        assertTrue(service.canAdd(entry, 1000L));
    }

    @Test
    public void canAdd_shouldReturnFalseForForeverEntry() {
        EntryImpl entry = mock(EntryImpl.class);

        when(entry.isForever()).thenReturn(true);

        assertFalse(service.canAdd(entry, 1000L));
    }

    @Test
    public void remove_shouldUpdateExpirationAndDao() {
        EntryImpl entry = mock(EntryImpl.class);
        Expiration expiration = mock(Expiration.class);

        when(entry.isForever()).thenReturn(false);
        when(entry.getExpiration()).thenReturn(expiration);

        service.remove(entry, 1000L);

        verify(expiration).remove(1000L);
        verify(entryDao).update(entry);
    }

    @Test
    public void remove_shouldThrowForForeverEntry() {
        EntryImpl entry = mock(EntryImpl.class);

        when(entry.isForever()).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> service.remove(entry, 5000L));
        verifyNoMoreInteractions(entryDao);
    }

    @Test
    public void canRemove_shouldReturnTrueWhenValid() {
        EntryImpl entry = mock(EntryImpl.class);
        Expiration expiration = mock(Expiration.class);

        when(entry.isForever()).thenReturn(false);
        when(entry.getExpiration()).thenReturn(expiration);
        when(expiration.canRemove(1000L)).thenReturn(true);

        assertTrue(service.canRemove(entry, 1000L));
    }

    @Test
    public void canRemove_shouldReturnFalseForForeverEntry() {
        EntryImpl entry = mock(EntryImpl.class);

        when(entry.isForever()).thenReturn(true);

        assertFalse(service.canRemove(entry, 1000L));
    }

    @Test
    public void set_shouldUpdateExpirationAndDao() {
        EntryImpl entry = mock(EntryImpl.class);
        Expiration expiration = mock(Expiration.class);

        when(entry.getExpiration()).thenReturn(expiration);

        service.set(entry, 1000L);

        verify(expiration).set(1000L);
        verify(entryDao).update(entry);
    }
}