package ru.nightmirror.wlbytime.command.commands;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.nightmirror.wlbytime.config.configs.CommandsConfig;
import ru.nightmirror.wlbytime.config.configs.MessagesConfig;
import ru.nightmirror.wlbytime.entry.EntryImpl;
import ru.nightmirror.wlbytime.interfaces.command.CommandIssuer;
import ru.nightmirror.wlbytime.interfaces.finder.EntryFinder;
import ru.nightmirror.wlbytime.interfaces.services.EntryTimeService;
import ru.nightmirror.wlbytime.time.TimeConvertor;
import ru.nightmirror.wlbytime.time.TimeRandom;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class TimeCommandTest {

    private CommandsConfig commandsConfig;
    private TimeCommand timeCommand;
    private MessagesConfig messages;
    private EntryFinder finder;
    private TimeConvertor convertor;
    private TimeRandom timeRandom;
    private EntryTimeService timeService;
    private CommandIssuer issuer;
    private EntryImpl entry;

    @BeforeEach
    public void setUp() {
        commandsConfig = mock(CommandsConfig.class);
        messages = mock(MessagesConfig.class);
        finder = mock(EntryFinder.class);
        convertor = mock(TimeConvertor.class);
        timeRandom = mock(TimeRandom.class);
        timeService = mock(EntryTimeService.class);
        issuer = mock(CommandIssuer.class);
        entry = mock(EntryImpl.class);

        timeCommand = new TimeCommand(commandsConfig, messages, finder, convertor, timeRandom, timeService);

        when(commandsConfig.getTimePermission()).thenReturn("wlbytime.time");
        when(messages.getIncorrectArguments()).thenReturn("Incorrect arguments.");
        when(messages.getPlayerNotInWhitelist()).thenReturn("Player %nickname% is not in the whitelist.");
        when(messages.getTimeIsIncorrect()).thenReturn("Time provided is incorrect.");
        when(messages.getAddTime()).thenReturn("Added %time% to %nickname%'s time.");
        when(messages.getCantAddTime()).thenReturn("Cannot add time.");
        when(messages.getRemoveTime()).thenReturn("Removed %time% from %nickname%'s time.");
        when(messages.getCantRemoveTime()).thenReturn("Cannot remove time.");
        when(messages.getSetTime()).thenReturn("Set %nickname%'s time to %time%.");
        when(messages.getCantAddTimeCausePlayerIsForever()).thenReturn("Cannot add time because player is forever.");
        when(messages.getCantRemoveTimeCausePlayerIsForever()).thenReturn("Cannot remove time because player is forever.");
    }

    @Test
    public void getPermissionReturnsCorrectPermission() {
        when(commandsConfig.getTimePermission()).thenReturn("wlbytime.time");
        assertEquals("wlbytime.time", timeCommand.getPermission());
    }

    @Test
    public void executeWithInsufficientArgumentsSendsIncorrectArgumentsMessage() {
        timeCommand.execute(issuer, new String[]{});

        verify(issuer).sendMessage("Incorrect arguments.");
    }

    @Test
    public void executePlayerNotInWhitelistSendsPlayerNotInWhitelistMessage() {
        String nickname = "nonexistentPlayer";
        when(finder.find(nickname)).thenReturn(Optional.empty());

        timeCommand.execute(issuer, new String[]{"add", nickname, "1h"});

        verify(issuer).sendMessage("Player nonexistentPlayer is not in the whitelist.");
    }

    @Test
    public void executeWithInvalidOperationSendsIncorrectArgumentsMessage() {
        String nickname = "somePlayer";
        when(finder.find(nickname)).thenReturn(Optional.of(entry));

        timeCommand.execute(issuer, new String[]{"invalidOp", nickname, "1h"});

        verify(issuer).sendMessage("Incorrect arguments.");
    }

    @Test
    public void executeWithInvalidTimeSendsTimeIsIncorrectMessage() {
        String nickname = "somePlayer";
        when(finder.find(nickname)).thenReturn(Optional.of(entry));
        when(convertor.getTime("1x")).thenReturn(Duration.ZERO);

        timeCommand.execute(issuer, new String[]{"add", nickname, "1x"});

        verify(issuer).sendMessage("Time provided is incorrect.");
    }

    @Test
    public void executeAddOperationEntryIsForeverSendsCantAddTimeForeverMessage() {
        String nickname = "somePlayer";
        when(finder.find(nickname)).thenReturn(Optional.of(entry));
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
        when(finder.find(nickname)).thenReturn(Optional.of(entry));
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
        when(finder.find(nickname)).thenReturn(Optional.of(entry));
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
        when(finder.find(nickname)).thenReturn(Optional.of(entry));
        when(entry.isForever()).thenReturn(false);
        when(convertor.getTime("1h")).thenReturn(Duration.ofHours(1));
        when(timeService.canAdd(entry, Duration.ofHours(1))).thenReturn(false);

        timeCommand.execute(issuer, new String[]{"add", nickname, "1h"});

        verify(issuer).sendMessage("Cannot add time.");
    }

    @Test
    public void executeRemoveOperationCanRemoveSendsRemoveTimeMessage() {
        String nickname = "somePlayer";
        when(finder.find(nickname)).thenReturn(Optional.of(entry));
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
        when(finder.find(nickname)).thenReturn(Optional.of(entry));
        when(entry.isForever()).thenReturn(false);
        when(convertor.getTime("1h")).thenReturn(Duration.ofHours(1));
        when(timeService.canRemove(entry, Duration.ofHours(1))).thenReturn(false);

        timeCommand.execute(issuer, new String[]{"remove", nickname, "1h"});

        verify(issuer).sendMessage("Cannot remove time.");
    }

    @Test
    public void executeSetOperationSendsSetTimeMessage() {
        String nickname = "somePlayer";
        when(finder.find(nickname)).thenReturn(Optional.of(entry));
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
