package ru.nightmirror.wlbytime.filter;

import net.kyori.adventure.text.Component;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.junit.jupiter.api.Test;
import ru.nightmirror.wlbytime.config.configs.MessagesConfig;
import ru.nightmirror.wlbytime.config.configs.SettingsConfig;
import ru.nightmirror.wlbytime.entry.EntryImpl;
import ru.nightmirror.wlbytime.identity.PlayerKey;
import ru.nightmirror.wlbytime.identity.ResolvedPlayer;
import ru.nightmirror.wlbytime.interfaces.checker.AccessEntryChecker;
import ru.nightmirror.wlbytime.interfaces.checker.UnfreezeEntryChecker;
import ru.nightmirror.wlbytime.interfaces.identity.PlayerIdentityResolver;
import ru.nightmirror.wlbytime.interfaces.services.EntryIdentityService;
import ru.nightmirror.wlbytime.interfaces.services.EntryService;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;


public class PlayerLoginFilterTest {

    private PlayerLoginFilter createFilter(MessagesConfig messages, SettingsConfig settings,
                                           UnfreezeEntryChecker unfreezeChecker, AccessEntryChecker accessChecker,
                                           PlayerIdentityResolver identityResolver, EntryIdentityService identityService,
                                           EntryService entryService) {
        return new PlayerLoginFilter(messages, settings, unfreezeChecker, accessChecker, identityResolver, identityService, entryService);
    }

    @Test
    public void deniesLoginWhenNotAllowed() throws Exception {
        MessagesConfig messages = mock(MessagesConfig.class);
        SettingsConfig settings = mock(SettingsConfig.class);
        UnfreezeEntryChecker unfreezeChecker = mock(UnfreezeEntryChecker.class);
        AccessEntryChecker accessChecker = mock(AccessEntryChecker.class);
        PlayerIdentityResolver identityResolver = mock(PlayerIdentityResolver.class);
        EntryIdentityService identityService = mock(EntryIdentityService.class);
        EntryService entryService = mock(EntryService.class);
        EntryImpl entry = mock(EntryImpl.class);

        AsyncPlayerPreLoginEvent event = mock(AsyncPlayerPreLoginEvent.class);
        UUID uuid = UUID.randomUUID();
        when(event.getName()).thenReturn("Alex");
        when(event.getUniqueId()).thenReturn(uuid);
        when(settings.isWhitelistEnabled()).thenReturn(true);
        when(messages.getYouNotInWhitelistOrFrozenKick()).thenReturn("nope");
        ResolvedPlayer resolved = new ResolvedPlayer(PlayerKey.nickname("Alex"), "Alex", uuid);
        when(identityResolver.resolveByLogin("Alex", uuid)).thenReturn(resolved);
        when(identityService.findOrMigrate(resolved, "Alex")).thenReturn(Optional.of(entry));
        when(accessChecker.isAllowed(entry)).thenReturn(false);

        PlayerLoginFilter filter = createFilter(messages, settings, unfreezeChecker, accessChecker, identityResolver, identityService, entryService);
        Method method = PlayerLoginFilter.class.getDeclaredMethod("filter", AsyncPlayerPreLoginEvent.class);
        method.setAccessible(true);
        method.invoke(filter, event);

        verify(event).disallow(AsyncPlayerPreLoginEvent.Result.KICK_WHITELIST, Component.text("nope"));
    }

    @Test
    public void doesNothingWhenWhitelistDisabled() throws Exception {
        MessagesConfig messages = mock(MessagesConfig.class);
        SettingsConfig settings = mock(SettingsConfig.class);
        UnfreezeEntryChecker unfreezeChecker = mock(UnfreezeEntryChecker.class);
        AccessEntryChecker accessChecker = mock(AccessEntryChecker.class);
        PlayerIdentityResolver identityResolver = mock(PlayerIdentityResolver.class);
        EntryIdentityService identityService = mock(EntryIdentityService.class);
        EntryService entryService = mock(EntryService.class);

        AsyncPlayerPreLoginEvent event = mock(AsyncPlayerPreLoginEvent.class);
        UUID uuid = UUID.randomUUID();
        when(event.getName()).thenReturn("Alex");
        when(event.getUniqueId()).thenReturn(uuid);
        when(settings.isWhitelistEnabled()).thenReturn(false);
        ResolvedPlayer resolved = new ResolvedPlayer(PlayerKey.nickname("Alex"), "Alex", uuid);
        when(identityResolver.resolveByLogin("Alex", uuid)).thenReturn(resolved);
        when(identityService.findOrMigrate(resolved, "Alex")).thenReturn(Optional.empty());

        PlayerLoginFilter filter = createFilter(messages, settings, unfreezeChecker, accessChecker, identityResolver, identityService, entryService);
        Method method = PlayerLoginFilter.class.getDeclaredMethod("filter", AsyncPlayerPreLoginEvent.class);
        method.setAccessible(true);
        method.invoke(filter, event);

        verifyNoInteractions(accessChecker);
    }

    @Test
    public void resumesFreezeOnLoginWhenFreezeOnlineTimeEnabled() throws Exception {
        MessagesConfig messages = mock(MessagesConfig.class);
        SettingsConfig settings = mock(SettingsConfig.class);
        UnfreezeEntryChecker unfreezeChecker = mock(UnfreezeEntryChecker.class);
        AccessEntryChecker accessChecker = mock(AccessEntryChecker.class);
        PlayerIdentityResolver identityResolver = mock(PlayerIdentityResolver.class);
        EntryIdentityService identityService = mock(EntryIdentityService.class);
        EntryService entryService = mock(EntryService.class);
        EntryImpl entry = mock(EntryImpl.class);
        ru.nightmirror.wlbytime.entry.Freezing freezing = mock(ru.nightmirror.wlbytime.entry.Freezing.class);

        AsyncPlayerPreLoginEvent event = mock(AsyncPlayerPreLoginEvent.class);
        UUID uuid = UUID.randomUUID();
        when(event.getName()).thenReturn("Alex");
        when(event.getUniqueId()).thenReturn(uuid);
        when(settings.isWhitelistEnabled()).thenReturn(true);
        when(settings.isFreezeCountsOnlyOnlineTime()).thenReturn(true);
        when(messages.getYouNotInWhitelistOrFrozenKick()).thenReturn("nope");
        ResolvedPlayer resolved = new ResolvedPlayer(PlayerKey.nickname("Alex"), "Alex", uuid);
        when(identityResolver.resolveByLogin("Alex", uuid)).thenReturn(resolved);
        when(identityService.findOrMigrate(resolved, "Alex")).thenReturn(Optional.of(entry));
        when(entry.isFrozen()).thenReturn(true);
        when(entry.getFreezing()).thenReturn(freezing);
        when(freezing.isPaused()).thenReturn(true);
        when(accessChecker.isAllowed(entry)).thenReturn(true);

        PlayerLoginFilter filter = createFilter(messages, settings, unfreezeChecker, accessChecker, identityResolver, identityService, entryService);
        Method method = PlayerLoginFilter.class.getDeclaredMethod("filter", AsyncPlayerPreLoginEvent.class);
        method.setAccessible(true);
        method.invoke(filter, event);

        verify(entryService).resumeFreeze(entry);
    }

    @Test
    public void doesNotResumeFreezeWhenFreezeOnlineTimeDisabled() throws Exception {
        MessagesConfig messages = mock(MessagesConfig.class);
        SettingsConfig settings = mock(SettingsConfig.class);
        UnfreezeEntryChecker unfreezeChecker = mock(UnfreezeEntryChecker.class);
        AccessEntryChecker accessChecker = mock(AccessEntryChecker.class);
        PlayerIdentityResolver identityResolver = mock(PlayerIdentityResolver.class);
        EntryIdentityService identityService = mock(EntryIdentityService.class);
        EntryService entryService = mock(EntryService.class);
        EntryImpl entry = mock(EntryImpl.class);

        AsyncPlayerPreLoginEvent event = mock(AsyncPlayerPreLoginEvent.class);
        UUID uuid = UUID.randomUUID();
        when(event.getName()).thenReturn("Alex");
        when(event.getUniqueId()).thenReturn(uuid);
        when(settings.isWhitelistEnabled()).thenReturn(true);
        when(settings.isFreezeCountsOnlyOnlineTime()).thenReturn(false);
        when(messages.getYouNotInWhitelistOrFrozenKick()).thenReturn("nope");
        ResolvedPlayer resolved = new ResolvedPlayer(PlayerKey.nickname("Alex"), "Alex", uuid);
        when(identityResolver.resolveByLogin("Alex", uuid)).thenReturn(resolved);
        when(identityService.findOrMigrate(resolved, "Alex")).thenReturn(Optional.of(entry));
        when(accessChecker.isAllowed(entry)).thenReturn(true);

        PlayerLoginFilter filter = createFilter(messages, settings, unfreezeChecker, accessChecker, identityResolver, identityService, entryService);
        Method method = PlayerLoginFilter.class.getDeclaredMethod("filter", AsyncPlayerPreLoginEvent.class);
        method.setAccessible(true);
        method.invoke(filter, event);

        verify(entryService, never()).resumeFreeze(any());
    }

    @Test
    public void unfreezesWhenFreezeInactiveOnLogin() throws Exception {
        MessagesConfig messages = mock(MessagesConfig.class);
        SettingsConfig settings = mock(SettingsConfig.class);
        UnfreezeEntryChecker unfreezeChecker = mock(UnfreezeEntryChecker.class);
        AccessEntryChecker accessChecker = mock(AccessEntryChecker.class);
        PlayerIdentityResolver identityResolver = mock(PlayerIdentityResolver.class);
        EntryIdentityService identityService = mock(EntryIdentityService.class);
        EntryService entryService = mock(EntryService.class);
        EntryImpl entry = mock(EntryImpl.class);

        AsyncPlayerPreLoginEvent event = mock(AsyncPlayerPreLoginEvent.class);
        UUID uuid = UUID.randomUUID();
        when(event.getName()).thenReturn("Alex");
        when(event.getUniqueId()).thenReturn(uuid);
        when(settings.isWhitelistEnabled()).thenReturn(true);
        when(messages.getYouNotInWhitelistOrFrozenKick()).thenReturn("nope");
        ResolvedPlayer resolved = new ResolvedPlayer(PlayerKey.nickname("Alex"), "Alex", uuid);
        when(identityResolver.resolveByLogin("Alex", uuid)).thenReturn(resolved);
        when(identityService.findOrMigrate(resolved, "Alex")).thenReturn(Optional.of(entry));
        when(entry.isFreezeInactive()).thenReturn(true);
        when(accessChecker.isAllowed(entry)).thenReturn(true);

        PlayerLoginFilter filter = createFilter(messages, settings, unfreezeChecker, accessChecker, identityResolver, identityService, entryService);
        Method method = PlayerLoginFilter.class.getDeclaredMethod("filter", AsyncPlayerPreLoginEvent.class);
        method.setAccessible(true);
        method.invoke(filter, event);

        verify(entryService).unfreeze(entry);
    }

    @Test
    public void doesNotUnfreezeWhenFreezeNotInactiveOnLogin() throws Exception {
        MessagesConfig messages = mock(MessagesConfig.class);
        SettingsConfig settings = mock(SettingsConfig.class);
        UnfreezeEntryChecker unfreezeChecker = mock(UnfreezeEntryChecker.class);
        AccessEntryChecker accessChecker = mock(AccessEntryChecker.class);
        PlayerIdentityResolver identityResolver = mock(PlayerIdentityResolver.class);
        EntryIdentityService identityService = mock(EntryIdentityService.class);
        EntryService entryService = mock(EntryService.class);
        EntryImpl entry = mock(EntryImpl.class);

        AsyncPlayerPreLoginEvent event = mock(AsyncPlayerPreLoginEvent.class);
        UUID uuid = UUID.randomUUID();
        when(event.getName()).thenReturn("Alex");
        when(event.getUniqueId()).thenReturn(uuid);
        when(settings.isWhitelistEnabled()).thenReturn(true);
        when(messages.getYouNotInWhitelistOrFrozenKick()).thenReturn("nope");
        ResolvedPlayer resolved = new ResolvedPlayer(PlayerKey.nickname("Alex"), "Alex", uuid);
        when(identityResolver.resolveByLogin("Alex", uuid)).thenReturn(resolved);
        when(identityService.findOrMigrate(resolved, "Alex")).thenReturn(Optional.of(entry));
        when(entry.isFreezeInactive()).thenReturn(false);
        when(accessChecker.isAllowed(entry)).thenReturn(true);

        PlayerLoginFilter filter = createFilter(messages, settings, unfreezeChecker, accessChecker, identityResolver, identityService, entryService);
        Method method = PlayerLoginFilter.class.getDeclaredMethod("filter", AsyncPlayerPreLoginEvent.class);
        method.setAccessible(true);
        method.invoke(filter, event);

        verify(entryService, never()).unfreeze(entry);
    }
}
