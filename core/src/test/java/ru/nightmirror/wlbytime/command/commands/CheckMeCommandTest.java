package ru.nightmirror.wlbytime.command.commands;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.nightmirror.wlbytime.config.configs.CommandsConfig;
import ru.nightmirror.wlbytime.config.configs.MessagesConfig;
import ru.nightmirror.wlbytime.entry.EntryImpl;
import ru.nightmirror.wlbytime.identity.PlayerKey;
import ru.nightmirror.wlbytime.identity.ResolvedPlayer;
import ru.nightmirror.wlbytime.interfaces.command.CommandIssuer;
import ru.nightmirror.wlbytime.interfaces.identity.PlayerIdentityResolver;
import ru.nightmirror.wlbytime.interfaces.services.EntryIdentityService;
import ru.nightmirror.wlbytime.time.TimeConvertor;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class CheckMeCommandTest {

    private CommandsConfig commandsConfig;
    private CheckMeCommand checkMeCommand;
    private MessagesConfig messages;
    private TimeConvertor convertor;
    private CommandIssuer issuer;
    private PlayerIdentityResolver identityResolver;
    private EntryIdentityService identityService;

    @BeforeEach
    public void setUp() {
        commandsConfig = mock(CommandsConfig.class);
        messages = mock(MessagesConfig.class);
        convertor = mock(TimeConvertor.class);
        issuer = mock(CommandIssuer.class);
        identityResolver = mock(PlayerIdentityResolver.class);
        identityService = mock(EntryIdentityService.class);

        checkMeCommand = new CheckMeCommand(commandsConfig, messages, convertor, identityResolver, identityService);
    }

    @Test
    public void getPermissionReturnsCorrectPermission() {
        when(commandsConfig.getCheckMePermission()).thenReturn("wlbytime.checkme");
        assertEquals("wlbytime.checkme", checkMeCommand.getPermission());
    }

    @Test
    public void getNameReturnsCorrectName() {
        assertEquals("checkme", checkMeCommand.getName());
    }

    @Test
    public void executeWhenEntryNotFoundSendsNotInWhitelistMessage() {
        when(issuer.getNickname()).thenReturn("testUser");
        when(identityResolver.resolveByIssuer(issuer))
                .thenReturn(new ResolvedPlayer(
                        PlayerKey.nickname("testUser"), "testUser", null));
        when(identityService.findOrMigrate(any(), anyString())).thenReturn(Optional.empty());
        when(messages.getCheckMeNotInWhitelist()).thenReturn("You are not in the whitelist!");

        checkMeCommand.execute(issuer, new String[]{});

        verify(issuer).sendMessage("You are not in the whitelist!");
    }

    @Test
    public void executeWhenEntryInactiveSendsNotInWhitelistMessage() {
        EntryImpl inactiveEntry = mock(EntryImpl.class);
        when(inactiveEntry.isInactive()).thenReturn(true);
        when(issuer.getNickname()).thenReturn("testUser");
        when(identityResolver.resolveByIssuer(issuer))
                .thenReturn(new ResolvedPlayer(
                        PlayerKey.nickname("testUser"), "testUser", null));
        when(identityService.findOrMigrate(any(), anyString())).thenReturn(Optional.of(inactiveEntry));
        when(messages.getCheckMeNotInWhitelist()).thenReturn("You are not in the whitelist!");

        checkMeCommand.execute(issuer, new String[]{});

        verify(issuer).sendMessage("You are not in the whitelist!");
    }

    @Test
    public void executeWhenEntryIsForeverSendsForeverMessage() {
        EntryImpl foreverEntry = mock(EntryImpl.class);
        when(foreverEntry.isInactive()).thenReturn(false);
        when(foreverEntry.isForever()).thenReturn(true);
        when(issuer.getNickname()).thenReturn("foreverUser");
        when(identityResolver.resolveByIssuer(issuer))
                .thenReturn(new ResolvedPlayer(
                        PlayerKey.nickname("foreverUser"), "foreverUser", null));
        when(identityService.findOrMigrate(any(), anyString())).thenReturn(Optional.of(foreverEntry));
        when(messages.getCheckMeStillInWhitelistForever()).thenReturn("You are in the whitelist forever!");

        checkMeCommand.execute(issuer, new String[]{});

        verify(issuer).sendMessage("You are in the whitelist forever!");
    }

    @Test
    public void executeWhenEntryIsFrozenSendsFrozenMessage() {
        EntryImpl frozenEntry = mock(EntryImpl.class);
        when(frozenEntry.isInactive()).thenReturn(false);
        when(frozenEntry.isForever()).thenReturn(false);
        when(frozenEntry.isFreezeActive()).thenReturn(true);
        when(frozenEntry.getLeftFreezeDuration()).thenReturn(Duration.ofHours(1));
        when(issuer.getNickname()).thenReturn("frozenUser");
        when(identityResolver.resolveByIssuer(issuer))
                .thenReturn(new ResolvedPlayer(
                        PlayerKey.nickname("frozenUser"), "frozenUser", null));
        when(identityService.findOrMigrate(any(), anyString())).thenReturn(Optional.of(frozenEntry));
        when(convertor.getTimeLine(Duration.ofHours(1))).thenReturn("1 hour");
        when(messages.getCheckMeFrozen()).thenReturn("You are frozen for %time%!");

        checkMeCommand.execute(issuer, new String[]{});

        verify(issuer).sendMessage("You are frozen for 1 hour!");
    }

    @Test
    public void executeWhenEntryIsActiveForTimeSendsWhitelistForTimeMessage() {
        EntryImpl timedEntry = mock(EntryImpl.class);
        when(timedEntry.isInactive()).thenReturn(false);
        when(timedEntry.isForever()).thenReturn(false);
        when(timedEntry.isFreezeActive()).thenReturn(false);
        when(timedEntry.getLeftActiveDuration()).thenReturn(Duration.ofHours(2));
        when(issuer.getNickname()).thenReturn("timedUser");
        when(identityResolver.resolveByIssuer(issuer))
                .thenReturn(new ResolvedPlayer(
                        PlayerKey.nickname("timedUser"), "timedUser", null));
        when(identityService.findOrMigrate(any(), anyString())).thenReturn(Optional.of(timedEntry));
        when(convertor.getTimeLine(Duration.ofHours(2))).thenReturn("2 hours");
        when(messages.getCheckMeStillInWhitelistForTime()).thenReturn("You are in the whitelist for %time%!");

        checkMeCommand.execute(issuer, new String[]{});

        verify(issuer).sendMessage("You are in the whitelist for 2 hours!");
    }

    @Test
    public void getTabulateReturnsEmptySet() {
        Set<String> tabulate = checkMeCommand.getTabulate(issuer, new String[]{});
        assertTrue(tabulate.isEmpty());
    }
}
