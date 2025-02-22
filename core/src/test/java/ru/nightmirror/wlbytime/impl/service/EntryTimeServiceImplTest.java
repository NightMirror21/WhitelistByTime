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

public class EntryTimeServiceImplTest {

    private EntryDao entryDao;
    private EntryTimeServiceImpl service;

    @BeforeEach
    public void setUp() {
        entryDao = mock(EntryDao.class);
        service = new EntryTimeServiceImpl(entryDao);
    }

    @Test
    public void addUpdatesExpirationAndDao() {
        EntryImpl entry = mock(EntryImpl.class);
        Expiration expiration = mock(Expiration.class);
        when(entry.isForever()).thenReturn(false);
        when(entry.getExpiration()).thenReturn(expiration);
        service.add(entry, Duration.ofSeconds(1));
        verify(expiration).add(Duration.ofSeconds(1));
        verify(entryDao).update(entry);
    }

    @Test
    public void addThrowsForForeverEntry() {
        EntryImpl entry = mock(EntryImpl.class);
        when(entry.isForever()).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> service.add(entry, Duration.ofSeconds(5)));
        verify(entryDao, never()).update(any());
    }

    @Test
    public void canAddReturnsTrueWhenAllowed() {
        EntryImpl entry = mock(EntryImpl.class);
        Expiration expiration = mock(Expiration.class);
        when(entry.isForever()).thenReturn(false);
        when(entry.getExpiration()).thenReturn(expiration);
        when(expiration.canAdd(Duration.ofSeconds(1))).thenReturn(true);
        assertTrue(service.canAdd(entry, Duration.ofSeconds(1)));
    }

    @Test
    public void canAddReturnsFalseForForeverEntry() {
        EntryImpl entry = mock(EntryImpl.class);
        when(entry.isForever()).thenReturn(true);
        assertFalse(service.canAdd(entry, Duration.ofSeconds(1)));
    }

    @Test
    public void canAddReturnsFalseWhenExpirationDisallows() {
        EntryImpl entry = mock(EntryImpl.class);
        Expiration expiration = mock(Expiration.class);
        when(entry.isForever()).thenReturn(false);
        when(entry.getExpiration()).thenReturn(expiration);
        when(expiration.canAdd(Duration.ofSeconds(1))).thenReturn(false);
        assertFalse(service.canAdd(entry, Duration.ofSeconds(1)));
    }

    @Test
    public void removeUpdatesExpirationAndDao() {
        EntryImpl entry = mock(EntryImpl.class);
        Expiration expiration = mock(Expiration.class);
        when(entry.isForever()).thenReturn(false);
        when(entry.getExpiration()).thenReturn(expiration);
        service.remove(entry, Duration.ofSeconds(1));
        verify(expiration).remove(Duration.ofSeconds(1));
        verify(entryDao).update(entry);
    }

    @Test
    public void removeThrowsForForeverEntry() {
        EntryImpl entry = mock(EntryImpl.class);
        when(entry.isForever()).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> service.remove(entry, Duration.ofSeconds(5)));
        verify(entryDao, never()).update(any());
    }

    @Test
    public void canRemoveReturnsTrueWhenAllowed() {
        EntryImpl entry = mock(EntryImpl.class);
        Expiration expiration = mock(Expiration.class);
        when(entry.isForever()).thenReturn(false);
        when(entry.getExpiration()).thenReturn(expiration);
        when(expiration.canRemove(Duration.ofSeconds(1))).thenReturn(true);
        assertTrue(service.canRemove(entry, Duration.ofSeconds(1)));
    }

    @Test
    public void canRemoveReturnsFalseForForeverEntry() {
        EntryImpl entry = mock(EntryImpl.class);
        when(entry.isForever()).thenReturn(true);
        assertFalse(service.canRemove(entry, Duration.ofSeconds(1)));
    }

    @Test
    public void canRemoveReturnsFalseWhenExpirationDisallows() {
        EntryImpl entry = mock(EntryImpl.class);
        Expiration expiration = mock(Expiration.class);
        when(entry.isForever()).thenReturn(false);
        when(entry.getExpiration()).thenReturn(expiration);
        when(expiration.canRemove(Duration.ofSeconds(1))).thenReturn(false);
        assertFalse(service.canRemove(entry, Duration.ofSeconds(1)));
    }

    @Test
    public void setUpdatesExpirationAndDao() {
        EntryImpl entry = mock(EntryImpl.class);
        Expiration expiration = mock(Expiration.class);
        when(entry.getExpiration()).thenReturn(expiration);
        Instant instant = Instant.now().plus(Duration.ofSeconds(1));
        service.set(entry, instant);
        verify(expiration).set(instant);
        verify(entryDao).update(entry);
    }
}
