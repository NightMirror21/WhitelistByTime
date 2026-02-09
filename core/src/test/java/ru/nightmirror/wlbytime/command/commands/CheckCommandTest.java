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

public class CheckCommandTest {

    private CommandsConfig commandsConfig;
    private CheckCommand checkCommand;
    private MessagesConfig messages;
    private TimeConvertor convertor;
    private CommandIssuer issuer;
    private EntryImpl entry;
    private PlayerIdentityResolver identityResolver;
    private EntryIdentityService identityService;

    @BeforeEach
    public void setUp() {
        commandsConfig = mock(CommandsConfig.class);
        messages = mock(MessagesConfig.class);
        convertor = mock(TimeConvertor.class);
        issuer = mock(CommandIssuer.class);
        entry = mock(EntryImpl.class);
        identityResolver = mock(PlayerIdentityResolver.class);
        identityService = mock(EntryIdentityService.class);

        checkCommand = new CheckCommand(commandsConfig, messages, convertor, identityResolver, identityService);

        when(messages.getIncorrectArguments()).thenReturn("Incorrect arguments!");
        when(messages.getPlayerNotInWhitelist()).thenReturn("Player %nickname% not found in whitelist!");
        when(messages.getPlayerFrozen()).thenReturn("Player %nickname% is frozen for %time%!");
        when(messages.getCheckStillInWhitelist()).thenReturn("Player %nickname% is still in whitelist forever!");
        when(messages.getCheckStillInWhitelistForTime()).thenReturn("Player %nickname% is in whitelist for %time%!");
        when(messages.getPlayerExpired()).thenReturn("Player %nickname%'s time has expired!");
    }

    @Test
    public void getPermissionReturnsCorrectPermission() {
        when(commandsConfig.getCheckPermission()).thenReturn("wlbytime.check");
        assertEquals("wlbytime.check", checkCommand.getPermission());
    }

    @Test
    public void getNameReturnsCorrectName() {
        assertEquals("check", checkCommand.getName());
    }

    @Test
    public void executeNoArgumentsSendsIncorrectArgumentsMessage() {
        checkCommand.execute(issuer, new String[]{});

        verify(issuer).sendMessage("Incorrect arguments!");
        verifyNoMoreInteractions(issuer);
    }

    @Test
    public void executePlayerNotInWhitelistSendsPlayerNotInWhitelistMessage() {
        String nickname = "unknownPlayer";
        when(identityResolver.resolveByNickname(nickname))
                .thenReturn(new ResolvedPlayer(PlayerKey.nickname(nickname), nickname, null));
        when(identityService.findOrMigrate(any(), anyString())).thenReturn(Optional.empty());

        checkCommand.execute(issuer, new String[]{nickname});

        verify(issuer).sendMessage("Player unknownPlayer not found in whitelist!");
        verifyNoMoreInteractions(issuer);
    }

    @Test
    public void executePlayerIsFrozenSendsPlayerFrozenMessage() {
        String nickname = "frozenPlayer";
        when(identityResolver.resolveByNickname(nickname))
                .thenReturn(new ResolvedPlayer(PlayerKey.nickname(nickname), nickname, null));
        when(identityService.findOrMigrate(any(), anyString())).thenReturn(Optional.of(entry));
        when(entry.isFreezeActive()).thenReturn(true);
        when(entry.getLeftFreezeDuration()).thenReturn(Duration.ofHours(1));
        when(convertor.getTimeLine(Duration.ofHours(1))).thenReturn("1 hour");

        checkCommand.execute(issuer, new String[]{nickname});

        verify(issuer).sendMessage("Player frozenPlayer is frozen for 1 hour!");
    }

    @Test
    public void executePlayerActiveAndForeverSendsStillInWhitelistMessage() {
        String nickname = "foreverPlayer";
        when(identityResolver.resolveByNickname(nickname))
                .thenReturn(new ResolvedPlayer(PlayerKey.nickname(nickname), nickname, null));
        when(identityService.findOrMigrate(any(), anyString())).thenReturn(Optional.of(entry));
        when(entry.isFreezeActive()).thenReturn(false);
        when(entry.isActive()).thenReturn(true);
        when(entry.isForever()).thenReturn(true);

        checkCommand.execute(issuer, new String[]{nickname});

        verify(issuer).sendMessage("Player foreverPlayer is still in whitelist forever!");
    }

    @Test
    public void executePlayerActiveAndNotForeverSendsStillInWhitelistForTimeMessage() {
        String nickname = "tempPlayer";
        when(identityResolver.resolveByNickname(nickname))
                .thenReturn(new ResolvedPlayer(PlayerKey.nickname(nickname), nickname, null));
        when(identityService.findOrMigrate(any(), anyString())).thenReturn(Optional.of(entry));
        when(entry.isFreezeActive()).thenReturn(false);
        when(entry.isActive()).thenReturn(true);
        when(entry.isForever()).thenReturn(false);
        when(entry.getLeftActiveDuration()).thenReturn(Duration.ofHours(2));
        when(convertor.getTimeLine(Duration.ofHours(2))).thenReturn("2 hours left");

        checkCommand.execute(issuer, new String[]{nickname});

        verify(issuer).sendMessage("Player tempPlayer is in whitelist for 2 hours left!");
    }

    @Test
    public void executePlayerNotActiveSendsPlayerExpiredMessage() {
        String nickname = "expiredPlayer";
        when(identityResolver.resolveByNickname(nickname))
                .thenReturn(new ResolvedPlayer(PlayerKey.nickname(nickname), nickname, null));
        when(identityService.findOrMigrate(any(), anyString())).thenReturn(Optional.of(entry));
        when(entry.isFreezeActive()).thenReturn(false);
        when(entry.isActive()).thenReturn(false);

        checkCommand.execute(issuer, new String[]{nickname});

        verify(issuer).sendMessage("Player expiredPlayer's time has expired!");
    }

    @Test
    public void getTabulateNoArgsReturnsIssuerNickname() {
        when(issuer.getNickname()).thenReturn("myNickname");

        Set<String> result = checkCommand.getTabulate(issuer, new String[]{});

        assertEquals(Set.of("myNickname"), result);
    }

    @Test
    public void getTabulateWithArgsReturnsEmptySet() {
        Set<String> result = checkCommand.getTabulate(issuer, new String[]{"arg1"});

        assertTrue(result.isEmpty());
    }
}
