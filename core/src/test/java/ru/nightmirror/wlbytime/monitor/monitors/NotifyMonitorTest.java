package ru.nightmirror.wlbytime.monitor.monitors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.nightmirror.wlbytime.config.ConfigsContainer;
import ru.nightmirror.wlbytime.config.configs.MessagesConfig;
import ru.nightmirror.wlbytime.config.configs.SettingsConfig;
import ru.nightmirror.wlbytime.entry.EntryImpl;
import ru.nightmirror.wlbytime.impl.dao.EntryDaoImpl;
import ru.nightmirror.wlbytime.time.TimeUnitsConvertorSettings;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;

import static org.mockito.Mockito.*;

public class NotifyMonitorTest {

    private EntryDaoImpl dao;
    private ConfigsContainer configs;
    private SettingsConfig settings;
    private MessagesConfig messages;
    private TimeUnitsConvertorSettings units;
    private BiConsumer<String, String> sender;

    private EntryImpl notifyEntry;
    private EntryImpl longEntry;
    private EntryImpl foreverEntry;
    private EntryImpl frozenEntry;

    private NotifyMonitor notifyMonitor;

    @BeforeEach
    public void setUp() {
        dao = mock(EntryDaoImpl.class);
        configs = mock(ConfigsContainer.class);
        settings = mock(SettingsConfig.class);
        messages = mock(MessagesConfig.class);
        units = mock(TimeUnitsConvertorSettings.class);
        sender = mock(BiConsumer.class);

        when(configs.getSettings()).thenReturn(settings);
        when(configs.getMessages()).thenReturn(messages);
        when(configs.getTimeUnitsConvertorSettings()).thenReturn(units);

        when(settings.isNotifyPlayersHowMuchLeft()).thenReturn(true);
        when(settings.getNotifyPlayerMonitorIntervalMs()).thenReturn(1000);
        when(settings.getNotifyPlayerTimeLeftThresholdSeconds()).thenReturn(3600);

        when(messages.getTimeLeftInWhitelistNotify()).thenReturn("Left %time% in whitelist");

        when(units.getFirstYearOrDefault()).thenReturn("y");
        when(units.getFirstMonthOrDefault()).thenReturn("mo");
        when(units.getFirstWeekOrDefault()).thenReturn("w");
        when(units.getFirstDayOrDefault()).thenReturn("d");
        when(units.getFirstHourOrDefault()).thenReturn("h");
        when(units.getFirstMinuteOrDefault()).thenReturn("m");
        when(units.getFirstSecondOrDefault()).thenReturn("s");
        when(units.getForever()).thenReturn("forever");

        notifyEntry = mock(EntryImpl.class);
        when(notifyEntry.isForever()).thenReturn(false);
        when(notifyEntry.isFrozen()).thenReturn(false);
        when(notifyEntry.getLeftActiveDuration()).thenReturn(Duration.ofMinutes(30));
        when(notifyEntry.getNickname()).thenReturn("notifyUser");

        longEntry = mock(EntryImpl.class);
        when(longEntry.isForever()).thenReturn(false);
        when(longEntry.isFrozen()).thenReturn(false);
        when(longEntry.getLeftActiveDuration()).thenReturn(Duration.ofHours(5));
        when(longEntry.getNickname()).thenReturn("longUser");

        foreverEntry = mock(EntryImpl.class);
        when(foreverEntry.isForever()).thenReturn(true);

        frozenEntry = mock(EntryImpl.class);
        when(frozenEntry.isForever()).thenReturn(false);
        when(frozenEntry.isFrozen()).thenReturn(true);

        notifyMonitor = new NotifyMonitor(dao, configs, sender);
    }

    @Test
    public void shouldNotifyPlayerWhenTimeBelowThreshold() {
        Set<EntryImpl> entries = new HashSet<>();
        entries.add(notifyEntry);
        when(dao.getAll()).thenReturn(entries);

        notifyMonitor.run();

        verify(sender).accept("notifyUser", "Left 30m in whitelist");
        verifyNoMoreInteractions(sender);
    }

    @Test
    public void shouldNotNotifyWhenTimeAboveThresholdOrForeverOrFrozen() {
        Set<EntryImpl> entries = new HashSet<>();
        entries.add(longEntry);
        entries.add(foreverEntry);
        entries.add(frozenEntry);
        when(dao.getAll()).thenReturn(entries);

        notifyMonitor.run();

        verifyNoInteractions(sender);
    }
}
