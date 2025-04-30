package ru.nightmirror.wlbytime.monitor.monitors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.nightmirror.wlbytime.config.configs.SettingsConfig;
import ru.nightmirror.wlbytime.entry.EntryImpl;
import ru.nightmirror.wlbytime.entry.LastJoin;
import ru.nightmirror.wlbytime.impl.dao.EntryDaoImpl;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import static org.mockito.Mockito.*;

public class LastJoinMonitorTest {

    private EntryDaoImpl dao;
    private SettingsConfig settings;
    private EntryImpl entryWithRecentJoin;
    private EntryImpl entryWithOldJoin;
    private EntryImpl entryWithoutLastJoin;
    private LastJoin recentLastJoin;
    private LastJoin oldLastJoin;

    private LastJoinMonitor lastJoinMonitor;

    @BeforeEach
    public void setUp() {
        dao = mock(EntryDaoImpl.class);
        settings = mock(SettingsConfig.class);
        entryWithRecentJoin = mock(EntryImpl.class);
        entryWithOldJoin = mock(EntryImpl.class);
        entryWithoutLastJoin = mock(EntryImpl.class);
        recentLastJoin = mock(LastJoin.class);
        oldLastJoin = mock(LastJoin.class);

        when(settings.isLastJoinMonitorEnabled()).thenReturn(true);
        when(settings.getLastJoinMonitorIntervalMs()).thenReturn(1000);
        when(settings.getLastJoinExpirationThresholdSeconds()).thenReturn(86400); // 1 day

        when(recentLastJoin.getLastJoinTime()).thenReturn(Instant.now().minusSeconds(3600)); // 1 hour ago
        when(entryWithRecentJoin.getLastJoin()).thenReturn(recentLastJoin);
        when(entryWithRecentJoin.getId()).thenReturn(1L);
        when(entryWithRecentJoin.getNickname()).thenReturn("recentUser");

        when(oldLastJoin.getLastJoinTime()).thenReturn(Instant.now().minusSeconds(172800)); // 2 days ago
        when(entryWithOldJoin.getLastJoin()).thenReturn(oldLastJoin);
        when(entryWithOldJoin.getId()).thenReturn(2L);
        when(entryWithOldJoin.getNickname()).thenReturn("oldUser");

        when(entryWithoutLastJoin.getLastJoin()).thenReturn(null);
        when(entryWithoutLastJoin.getId()).thenReturn(3L);
        when(entryWithoutLastJoin.getNickname()).thenReturn("noJoinUser");

        lastJoinMonitor = new LastJoinMonitor(dao, settings);
    }

    @Test
    public void shouldRemoveEntriesWithOldLastJoin() {
        Set<EntryImpl> entries = new HashSet<>();
        entries.add(entryWithRecentJoin);
        entries.add(entryWithOldJoin);
        when(dao.getAll()).thenReturn(entries);

        lastJoinMonitor.run();

        verify(dao, times(1)).remove(entryWithOldJoin);
        verify(dao, never()).remove(entryWithRecentJoin);
    }

    @Test
    public void shouldNotRemoveEntriesWithRecentLastJoin() {
        Set<EntryImpl> entries = new HashSet<>();
        entries.add(entryWithRecentJoin);
        when(dao.getAll()).thenReturn(entries);

        lastJoinMonitor.run();

        verify(dao, never()).remove(any());
    }

    @Test
    public void shouldNotRemoveEntriesWithoutLastJoin() {
        Set<EntryImpl> entries = new HashSet<>();
        entries.add(entryWithoutLastJoin);
        when(dao.getAll()).thenReturn(entries);

        lastJoinMonitor.run();

        verify(dao, never()).remove(any());
    }

    @Test
    public void shouldHandleEmptyEntryList() {
        Set<EntryImpl> entries = new HashSet<>();
        when(dao.getAll()).thenReturn(entries);

        lastJoinMonitor.run();

        verify(dao, never()).remove(any());
    }

    @Test
    public void shouldShutdownCorrectly() {
        lastJoinMonitor.shutdown();
        // No exceptions should be thrown
    }
}
