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

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

public class PlayerLoginFilterTest {

    @Test
    public void deniesLoginWhenNotAllowed() throws Exception {
        MessagesConfig messages = mock(MessagesConfig.class);
        SettingsConfig settings = mock(SettingsConfig.class);
        UnfreezeEntryChecker unfreezeChecker = mock(UnfreezeEntryChecker.class);
        AccessEntryChecker accessChecker = mock(AccessEntryChecker.class);
        PlayerIdentityResolver identityResolver = mock(PlayerIdentityResolver.class);
        EntryIdentityService identityService = mock(EntryIdentityService.class);
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

        PlayerLoginFilter filter = new PlayerLoginFilter(messages, settings, unfreezeChecker, accessChecker, identityResolver, identityService);
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

        AsyncPlayerPreLoginEvent event = mock(AsyncPlayerPreLoginEvent.class);
        UUID uuid = UUID.randomUUID();
        when(event.getName()).thenReturn("Alex");
        when(event.getUniqueId()).thenReturn(uuid);
        when(settings.isWhitelistEnabled()).thenReturn(false);
        ResolvedPlayer resolved = new ResolvedPlayer(PlayerKey.nickname("Alex"), "Alex", uuid);
        when(identityResolver.resolveByLogin("Alex", uuid)).thenReturn(resolved);
        when(identityService.findOrMigrate(resolved, "Alex")).thenReturn(Optional.empty());

        PlayerLoginFilter filter = new PlayerLoginFilter(messages, settings, unfreezeChecker, accessChecker, identityResolver, identityService);
        Method method = PlayerLoginFilter.class.getDeclaredMethod("filter", AsyncPlayerPreLoginEvent.class);
        method.setAccessible(true);
        method.invoke(filter, event);

        verifyNoInteractions(accessChecker);
    }
}
