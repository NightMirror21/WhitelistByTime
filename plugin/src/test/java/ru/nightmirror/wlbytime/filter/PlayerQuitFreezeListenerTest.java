package ru.nightmirror.wlbytime.filter;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerQuitEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.nightmirror.wlbytime.config.configs.SettingsConfig;
import ru.nightmirror.wlbytime.entry.EntryImpl;
import ru.nightmirror.wlbytime.entry.Freezing;
import ru.nightmirror.wlbytime.identity.PlayerKey;
import ru.nightmirror.wlbytime.interfaces.finder.EntryFinder;
import ru.nightmirror.wlbytime.interfaces.services.EntryService;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;

public class PlayerQuitFreezeListenerTest {

    private SettingsConfig settings;
    private EntryFinder entryFinder;
    private EntryService entryService;
    private PlayerQuitFreezeListener listener;
    private Player player;

    @BeforeEach
    public void setUp() {
        settings = mock(SettingsConfig.class);
        entryFinder = mock(EntryFinder.class);
        entryService = mock(EntryService.class);
        listener = new PlayerQuitFreezeListener(settings, entryFinder, entryService);

        player = mock(Player.class);
        UUID uuid = UUID.randomUUID();
        when(player.getUniqueId()).thenReturn(uuid);
        when(player.getName()).thenReturn("TestPlayer");
    }

    @Test
    public void doesNothingWhenFreezeOnlineTimeDisabled() {
        when(settings.isFreezeCountsOnlyOnlineTime()).thenReturn(false);
        PlayerQuitEvent event = mock(PlayerQuitEvent.class);
        when(event.getPlayer()).thenReturn(player);

        listener.onPlayerQuit(event);

        verifyNoInteractions(entryFinder, entryService);
    }

    @Test
    public void doesNothingWhenPlayerHasNoEntry() {
        when(settings.isFreezeCountsOnlyOnlineTime()).thenReturn(true);
        when(entryFinder.find(any(PlayerKey.class))).thenReturn(Optional.empty());
        PlayerQuitEvent event = mock(PlayerQuitEvent.class);
        when(event.getPlayer()).thenReturn(player);

        listener.onPlayerQuit(event);

        verifyNoInteractions(entryService);
    }

    @Test
    public void doesNothingWhenPlayerIsNotFrozen() {
        when(settings.isFreezeCountsOnlyOnlineTime()).thenReturn(true);
        EntryImpl entry = mock(EntryImpl.class);
        when(entry.isFreezeActive()).thenReturn(false);
        when(entryFinder.find(any(PlayerKey.class))).thenReturn(Optional.of(entry));
        PlayerQuitEvent event = mock(PlayerQuitEvent.class);
        when(event.getPlayer()).thenReturn(player);

        listener.onPlayerQuit(event);

        verifyNoInteractions(entryService);
    }

    @Test
    public void doesNothingWhenFreezeAlreadyPaused() {
        when(settings.isFreezeCountsOnlyOnlineTime()).thenReturn(true);
        EntryImpl entry = mock(EntryImpl.class);
        Freezing freezing = mock(Freezing.class);
        when(entry.isFreezeActive()).thenReturn(true);
        when(entry.getFreezing()).thenReturn(freezing);
        when(freezing.isPaused()).thenReturn(true);
        when(entryFinder.find(any(PlayerKey.class))).thenReturn(Optional.of(entry));
        PlayerQuitEvent event = mock(PlayerQuitEvent.class);
        when(event.getPlayer()).thenReturn(player);

        listener.onPlayerQuit(event);

        verifyNoInteractions(entryService);
    }

    @Test
    public void pausesFreezeWhenPlayerQuitsWithActiveFreeze() {
        when(settings.isFreezeCountsOnlyOnlineTime()).thenReturn(true);
        EntryImpl entry = mock(EntryImpl.class);
        Freezing freezing = mock(Freezing.class);
        when(entry.isFreezeActive()).thenReturn(true);
        when(entry.getFreezing()).thenReturn(freezing);
        when(freezing.isPaused()).thenReturn(false);
        when(entryFinder.find(any(PlayerKey.class))).thenReturn(Optional.of(entry));
        PlayerQuitEvent event = mock(PlayerQuitEvent.class);
        when(event.getPlayer()).thenReturn(player);

        listener.onPlayerQuit(event);

        verify(entryService).pauseFreeze(entry);
    }

    @Test
    public void fallsBackToNicknameWhenUuidLookupFails() {
        when(settings.isFreezeCountsOnlyOnlineTime()).thenReturn(true);
        EntryImpl entry = mock(EntryImpl.class);
        Freezing freezing = mock(Freezing.class);
        when(entry.isFreezeActive()).thenReturn(true);
        when(entry.getFreezing()).thenReturn(freezing);
        when(freezing.isPaused()).thenReturn(false);
        // UUID lookup fails, nickname lookup succeeds (sequential stubbing)
        when(entryFinder.find(any(PlayerKey.class)))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(entry));
        PlayerQuitEvent event = mock(PlayerQuitEvent.class);
        when(event.getPlayer()).thenReturn(player);

        listener.onPlayerQuit(event);

        verify(entryService).pauseFreeze(entry);
    }
}
