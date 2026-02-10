package ru.nightmirror.wlbytime.monitor.monitors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.nightmirror.wlbytime.config.configs.SettingsConfig;
import ru.nightmirror.wlbytime.entry.EntryImpl;
import ru.nightmirror.wlbytime.impl.dao.EntryDaoImpl;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import static org.mockito.Mockito.*;

public class ExpireMonitorTest {

    private EntryDaoImpl dao;
    private SettingsConfig settings;
    private Consumer<String> playerKicker;
    private EntryImpl activeEntry;
    private EntryImpl inactiveEntry;
    private ExpireMonitor expireMonitor;

    @BeforeEach
    public void setUp() {
        dao = mock(EntryDaoImpl.class);
        settings = mock(SettingsConfig.class);
        playerKicker = mock(Consumer.class);
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
    }

    @Test
    public void shouldRemoveInactiveEntriesAndKickPlayersWhenKickEnabled() {
        when(settings.isKickPlayerOnTimeExpire()).thenReturn(true);
        expireMonitor = new ExpireMonitor(dao, settings, playerKicker);

        Set<EntryImpl> entries = new HashSet<>();
        entries.add(activeEntry);
        entries.add(inactiveEntry);
        when(dao.getAll()).thenReturn(entries);

        expireMonitor.run();

        verify(dao).remove(inactiveEntry);
        verify(playerKicker).accept("inactiveUser");
        verify(dao, never()).remove(activeEntry);
        verify(playerKicker, never()).accept("activeUser");
    }

    @Test
    public void shouldRemoveInactiveEntriesWithoutKickWhenKickDisabled() {
        when(settings.isKickPlayerOnTimeExpire()).thenReturn(false);
        expireMonitor = new ExpireMonitor(dao, settings, playerKicker);

        Set<EntryImpl> entries = new HashSet<>();
        entries.add(inactiveEntry);
        when(dao.getAll()).thenReturn(entries);

        expireMonitor.run();

        verify(dao).remove(inactiveEntry);
        verifyNoInteractions(playerKicker);
    }

    @Test
    public void shouldNotRemoveAnyEntriesWhenAllActive() {
        when(settings.isKickPlayerOnTimeExpire()).thenReturn(true);
        expireMonitor = new ExpireMonitor(dao, settings, playerKicker);

        Set<EntryImpl> entries = new HashSet<>();
        entries.add(activeEntry);
        when(dao.getAll()).thenReturn(entries);

        expireMonitor.run();

        verify(dao, never()).remove(any());
        verifyNoInteractions(playerKicker);
    }

    @Test
    public void shouldHandleEmptyEntryList() {
        when(settings.isKickPlayerOnTimeExpire()).thenReturn(true);
        expireMonitor = new ExpireMonitor(dao, settings, playerKicker);

        when(dao.getAll()).thenReturn(new HashSet<>());

        expireMonitor.run();

        verify(dao, never()).remove(any());
        verifyNoInteractions(playerKicker);
    }

    @Test
    public void shouldShutdownCorrectly() {
        when(settings.isKickPlayerOnTimeExpire()).thenReturn(true);
        expireMonitor = new ExpireMonitor(dao, settings, playerKicker);

        expireMonitor.shutdown();
    }
}
