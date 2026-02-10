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
import ru.nightmirror.wlbytime.interfaces.services.EntryService;
import ru.nightmirror.wlbytime.interfaces.services.EntryTimeService;
import ru.nightmirror.wlbytime.time.TimeConvertor;
import ru.nightmirror.wlbytime.time.TimeRandom;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class TimeCommandTest {

    private CommandsConfig commandsConfig;
    private TimeCommand timeCommand;
    private MessagesConfig messages;
    private TimeConvertor convertor;
    private TimeRandom timeRandom;
    private EntryService entryService;
    private EntryTimeService timeService;
    private CommandIssuer issuer;
    private EntryImpl entry;
    private PlayerIdentityResolver identityResolver;
    private EntryIdentityService identityService;

    @BeforeEach
    public void setUp() {
        commandsConfig = mock(CommandsConfig.class);
        messages = mock(MessagesConfig.class);
        convertor = mock(TimeConvertor.class);
        timeRandom = mock(TimeRandom.class);
        entryService = mock(EntryService.class);
        timeService = mock(EntryTimeService.class);
        issuer = mock(CommandIssuer.class);
        entry = mock(EntryImpl.class);
        identityResolver = mock(PlayerIdentityResolver.class);
        identityService = mock(EntryIdentityService.class);

        timeCommand = new TimeCommand(commandsConfig, messages, convertor, timeRandom, entryService, timeService, identityResolver, identityService);

        when(identityResolver.resolveByNickname(anyString()))
                .thenAnswer(invocation -> new ResolvedPlayer(
                        PlayerKey.nickname(invocation.getArgument(0)), invocation.getArgument(0), null));

        when(commandsConfig.getTimePermission()).thenReturn(Set.of("whitelistbytime.time", "wlbytime.time"));
        when(messages.getIncorrectArguments()).thenReturn("Incorrect arguments.");
        when(messages.getPlayerNotInWhitelist()).thenReturn("Player %nickname% is not in the whitelist.");
        when(messages.getTimeIsIncorrect()).thenReturn("Time provided is incorrect.");
        when(messages.getAddTime()).thenReturn("Added %time% to %nickname%'s time.");
        when(messages.getCantAddTime()).thenReturn("Cannot add time.");
        when(messages.getRemoveTime()).thenReturn("Removed %time% from %nickname%'s time.");
        when(messages.getCantRemoveTime()).thenReturn("Cannot remove time.");
        when(messages.getSetTime()).thenReturn("Set %nickname%'s time to %time%.");
        when(messages.getSuccessfullyAddedForTime()).thenReturn("%nickname% added to whitelist for %time%");
        when(messages.getCantAddTimeCausePlayerIsForever()).thenReturn("Cannot add time because player is forever.");
        when(messages.getCantRemoveTimeCausePlayerIsForever()).thenReturn("Cannot remove time because player is forever.");
    }

    @Test
    public void getPermissionsReturnsConfiguredPermissions() {
        when(commandsConfig.getTimePermission()).thenReturn(Set.of("whitelistbytime.time", "wlbytime.time"));
        assertEquals(Set.of("whitelistbytime.time", "wlbytime.time"), timeCommand.getPermissions());
    }

    @Test
    public void executeWithInsufficientArgumentsSendsIncorrectArgumentsMessage() {
        timeCommand.execute(issuer, new String[]{});

        verify(issuer).sendMessage("Incorrect arguments.");
    }

    @Test
    public void executePlayerNotInWhitelistSendsPlayerNotInWhitelistMessage() {
        String nickname = "nonexistentPlayer";
        when(identityService.findOrMigrate(any(), anyString())).thenReturn(Optional.empty());
        when(convertor.getTime("1h")).thenReturn(Duration.ofHours(1));

        timeCommand.execute(issuer, new String[]{"remove", nickname, "1h"});

        verify(issuer).sendMessage("Player nonexistentPlayer is not in the whitelist.");
    }

    @Test
    public void executeAddOperationPlayerNotInWhitelistCreatesEntry() {
        String nickname = "nonexistentPlayer";
        when(identityService.findOrMigrate(any(), anyString())).thenReturn(Optional.empty());
        when(convertor.getTime("1h")).thenReturn(Duration.ofHours(1));
        when(convertor.getTimeLine(Duration.ofHours(1))).thenReturn("1 hour");

        Instant now = Instant.now();
        timeCommand.execute(issuer, new String[]{"add", nickname, "1h"});

        verify(entryService).create(eq(nickname), argThat((Instant instant) ->
                instant.isAfter(now.plus(Duration.ofHours(1)).minusMillis(1)) &&
                        instant.isBefore(now.plus(Duration.ofHours(1)).plusMillis(1000))
        ));
        verify(issuer).sendMessage("nonexistentPlayer added to whitelist for 1 hour");
        verifyNoInteractions(timeService);
    }

    @Test
    public void executeWithInvalidOperationSendsIncorrectArgumentsMessage() {
        String nickname = "somePlayer";
        when(identityService.findOrMigrate(any(), anyString())).thenReturn(Optional.of(entry));

        timeCommand.execute(issuer, new String[]{"invalidOp", nickname, "1h"});

        verify(issuer).sendMessage("Incorrect arguments.");
    }

    @Test
    public void executeWithInvalidTimeSendsTimeIsIncorrectMessage() {
        String nickname = "somePlayer";
        when(identityService.findOrMigrate(any(), anyString())).thenReturn(Optional.of(entry));
        when(convertor.getTime("1x")).thenReturn(Duration.ZERO);

        timeCommand.execute(issuer, new String[]{"add", nickname, "1x"});

        verify(issuer).sendMessage("Time provided is incorrect.");
    }

    @Test
    public void executeAddOperationEntryIsForeverSendsCantAddTimeForeverMessage() {
        String nickname = "somePlayer";
        when(identityService.findOrMigrate(any(), anyString())).thenReturn(Optional.of(entry));
        when(entry.isForever()).thenReturn(true);
        when(convertor.getTime("1h")).thenReturn(Duration.ofHours(1));

        timeCommand.execute(issuer, new String[]{"add", nickname, "1h"});

        verify(issuer).sendMessage("Cannot add time because player is forever.");
        verify(timeService, never()).canAdd(any(), any(Duration.class));
        verify(timeService, never()).add(any(), any(Duration.class));
    }

    @Test
    public void executeRemoveOperationEntryIsForeverSendsCantRemoveTimeForeverMessage() {
        String nickname = "somePlayer";
        when(identityService.findOrMigrate(any(), anyString())).thenReturn(Optional.of(entry));
        when(entry.isForever()).thenReturn(true);
        when(convertor.getTime("1h")).thenReturn(Duration.ofHours(1));

        timeCommand.execute(issuer, new String[]{"remove", nickname, "1h"});

        verify(issuer).sendMessage("Cannot remove time because player is forever.");
        verify(timeService, never()).canRemove(any(), any(Duration.class));
        verify(timeService, never()).remove(any(), any(Duration.class));
    }

    @Test
    public void executeAddOperationCanAddSendsAddTimeMessage() {
        String nickname = "somePlayer";
        when(identityService.findOrMigrate(any(), anyString())).thenReturn(Optional.of(entry));
        when(entry.isForever()).thenReturn(false);
        when(convertor.getTime("1h")).thenReturn(Duration.ofHours(1));
        when(convertor.getTimeLine(Duration.ofHours(1))).thenReturn("1 hour");
        when(timeService.canAdd(entry, Duration.ofHours(1))).thenReturn(true);

        timeCommand.execute(issuer, new String[]{"add", nickname, "1h"});

        verify(timeService).add(entry, Duration.ofHours(1));
        verify(issuer).sendMessage("Added 1 hour to somePlayer's time.");
    }

    @Test
    public void executeAddOperationCannotAddSendsCantAddTimeMessage() {
        String nickname = "somePlayer";
        when(identityService.findOrMigrate(any(), anyString())).thenReturn(Optional.of(entry));
        when(entry.isForever()).thenReturn(false);
        when(convertor.getTime("1h")).thenReturn(Duration.ofHours(1));
        when(timeService.canAdd(entry, Duration.ofHours(1))).thenReturn(false);

        timeCommand.execute(issuer, new String[]{"add", nickname, "1h"});

        verify(issuer).sendMessage("Cannot add time.");
    }

    @Test
    public void executeRemoveOperationCanRemoveSendsRemoveTimeMessage() {
        String nickname = "somePlayer";
        when(identityService.findOrMigrate(any(), anyString())).thenReturn(Optional.of(entry));
        when(entry.isForever()).thenReturn(false);
        when(convertor.getTime("1h")).thenReturn(Duration.ofHours(1));
        when(convertor.getTimeLine(Duration.ofHours(1))).thenReturn("1 hour");
        when(timeService.canRemove(entry, Duration.ofHours(1))).thenReturn(true);

        timeCommand.execute(issuer, new String[]{"remove", nickname, "1h"});

        verify(timeService).remove(entry, Duration.ofHours(1));
        verify(issuer).sendMessage("Removed 1 hour from somePlayer's time.");
    }

    @Test
    public void executeRemoveOperationCannotRemoveSendsCantRemoveTimeMessage() {
        String nickname = "somePlayer";
        when(identityService.findOrMigrate(any(), anyString())).thenReturn(Optional.of(entry));
        when(entry.isForever()).thenReturn(false);
        when(convertor.getTime("1h")).thenReturn(Duration.ofHours(1));
        when(timeService.canRemove(entry, Duration.ofHours(1))).thenReturn(false);

        timeCommand.execute(issuer, new String[]{"remove", nickname, "1h"});

        verify(issuer).sendMessage("Cannot remove time.");
    }

    @Test
    public void executeSetOperationSendsSetTimeMessage() {
        String nickname = "somePlayer";
        when(identityService.findOrMigrate(any(), anyString())).thenReturn(Optional.of(entry));
        when(entry.isForever()).thenReturn(false);
        when(convertor.getTime("1h")).thenReturn(Duration.ofHours(1));
        when(convertor.getTimeLine(Duration.ofHours(1))).thenReturn("1 hour");

        Instant now = Instant.now();
        timeCommand.execute(issuer, new String[]{"set", nickname, "1h"});

        verify(timeService).set(eq(entry), argThat(instant ->
                instant.isAfter(now.plus(Duration.ofHours(1)).minusMillis(1)) &&
                        instant.isBefore(now.plus(Duration.ofHours(1)).plusMillis(1000))
        ));

        verify(issuer).sendMessage("Set somePlayer's time to 1 hour.");
    }

    @Test
    public void tabulateWithoutArgumentsReturnsOperations() {
        Set<String> expected = Set.of("add", "remove", "set");

        Set<String> result = timeCommand.getTabulate(issuer, new String[]{});

        assertEquals(expected, result);
    }

    @Test
    public void tabulateWithInvalidOperationReturnsEmptySet() {
        Set<String> result = timeCommand.getTabulate(issuer, new String[]{"invalid"});

        assertEquals(Set.of(), result);
    }

    @Test
    public void tabulateWithValidOperationReturnsIssuerNickname() {
        when(issuer.getNickname()).thenReturn("nickname");

        Set<String> result = timeCommand.getTabulate(issuer, new String[]{"add"});

        assertEquals(Set.of("nickname"), result);
    }

    @Test
    public void tabulateWithOperationAndNicknameReturnsRandomTime() {
        when(timeRandom.getRandomOneTime()).thenReturn("1h");
        when(timeRandom.getTimes()).thenReturn(Set.of("1h", "2h", "3h"));

        Set<String> tabulate = timeCommand.getTabulate(issuer, new String[]{"add", "nickname"});

        assertEquals(Set.of("1h", "2h", "3h"), tabulate);
    }
}
