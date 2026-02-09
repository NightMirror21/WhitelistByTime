package ru.nightmirror.wlbytime.monitor.monitors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.nightmirror.wlbytime.config.configs.SettingsConfig;
import ru.nightmirror.wlbytime.entry.EntryImpl;
import ru.nightmirror.wlbytime.impl.dao.EntryDaoImpl;

import java.util.HashSet;
import java.util.Set;

import static org.mockito.Mockito.*;

public class ExpireMonitorTest {

    private EntryDaoImpl dao;
    private SettingsConfig settings;
    private EntryImpl activeEntry;
    private EntryImpl inactiveEntry;

    private ExpireMonitor expireMonitor;

    @BeforeEach
    public void setUp() {
        dao = mock(EntryDaoImpl.class);
        settings = mock(SettingsConfig.class);
        activeEntry = mock(EntryImpl.class);
        inactiveEntry = mock(EntryImpl.class);

        when(settings.isExpireMonitorEnabled()).thenReturn(true);
        when(settings.getExpireMonitorIntervalMs()).thenReturn(1000);

        when(activeEntry.isInactive()).thenReturn(false);
        when(activeEntry.getId()).thenReturn(1L);
        when(activeEntry.getNickname()).thenReturn("activeUser");

        when(inactiveEntry.isInactive()).thenReturn(true);
        when(inactiveEntry.getId()).thenReturn(2L);
        when(inactiveEntry.getNickname()).thenReturn("inactiveUser");

        expireMonitor = new ExpireMonitor(dao, settings);
    }

    @Test
    public void shouldRemoveInactiveEntries() {
        Set<EntryImpl> entries = new HashSet<>();
        entries.add(activeEntry);
        entries.add(inactiveEntry);
        when(dao.getAll()).thenReturn(entries);

        expireMonitor.run();

        verify(dao, times(1)).remove(inactiveEntry);
        verify(dao, never()).remove(activeEntry);
    }

    @Test
    public void shouldNotRemoveAnyEntriesWhenAllActive() {
        Set<EntryImpl> entries = new HashSet<>();
        entries.add(activeEntry);
        when(dao.getAll()).thenReturn(entries);

        expireMonitor.run();

        verify(dao, never()).remove(any());
    }

    @Test
    public void shouldHandleEmptyEntryList() {
        Set<EntryImpl> entries = new HashSet<>();
        when(dao.getAll()).thenReturn(entries);

        expireMonitor.run();

        verify(dao, never()).remove(any());
    }

    @Test
    public void shouldShutdownCorrectly() {
        expireMonitor.shutdown();
    }
}
