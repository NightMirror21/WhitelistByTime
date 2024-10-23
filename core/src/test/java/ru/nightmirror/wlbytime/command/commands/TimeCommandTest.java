package ru.nightmirror.wlbytime.command.commands;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.nightmirror.wlbytime.config.configs.MessagesConfig;
import ru.nightmirror.wlbytime.entry.Entry;
import ru.nightmirror.wlbytime.interfaces.command.CommandIssuer;
import ru.nightmirror.wlbytime.interfaces.finder.EntryFinder;
import ru.nightmirror.wlbytime.interfaces.services.EntryTimeService;
import ru.nightmirror.wlbytime.time.TimeConvertor;
import ru.nightmirror.wlbytime.time.TimeRandom;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class TimeCommandTest {

    private TimeCommand timeCommand;
    private MessagesConfig messages;
    private EntryFinder finder;
    private TimeConvertor convertor;
    private EntryTimeService timeService;
    private CommandIssuer issuer;
    private TimeRandom timeRandom;

    @BeforeEach
    public void setUp() {
        messages = mock(MessagesConfig.class);
        finder = mock(EntryFinder.class);
        convertor = mock(TimeConvertor.class);
        timeService = mock(EntryTimeService.class);
        issuer = mock(CommandIssuer.class);
        timeRandom = mock(TimeRandom.class);

        timeCommand = new TimeCommand(messages, finder, convertor, timeRandom, timeService);
    }

    @Test
    public void testExecute_WhenInsufficientArguments_ShouldSendIncorrectArgumentsMessage() {
        String[] args = {"add", "Player1"};
        when(messages.getIncorrectArguments()).thenReturn("Incorrect arguments!");

        timeCommand.execute(issuer, args);

        verify(issuer).sendMessage("Incorrect arguments!");
        verifyNoInteractions(finder, timeService);
    }

    @Test
    public void testExecute_WhenPlayerNotFound_ShouldSendPlayerNotInWhitelistMessage() {
        String[] args = {"add", "Player1", "2h"};
        when(finder.find("Player1")).thenReturn(Optional.empty());
        when(messages.getPlayerNotInWhitelist()).thenReturn("Player %nickname% not in whitelist.");

        timeCommand.execute(issuer, args);

        verify(issuer).sendMessage("Player Player1 not in whitelist.");
        verifyNoMoreInteractions(timeService);
    }

    @Test
    public void testExecute_WhenInvalidOperation_ShouldSendIncorrectArgumentsMessage() {
        String[] args = {"invalidOperation", "Player1", "2h"};
        when(finder.find("Player1")).thenReturn(Optional.of(mock(Entry.class)));
        when(messages.getIncorrectArguments()).thenReturn("Incorrect arguments!");

        timeCommand.execute(issuer, args);

        verify(issuer).sendMessage("Incorrect arguments!");
        verifyNoMoreInteractions(timeService);
    }

    @Test
    public void testExecute_WhenTimeIsIncorrect_ShouldSendTimeIsIncorrectMessage() {
        String[] args = {"add", "Player1", "invalidTime"};
        when(finder.find("Player1")).thenReturn(Optional.of(mock(Entry.class)));
        when(convertor.getTimeMs("invalidTime")).thenReturn(0L);
        when(messages.getTimeIsIncorrect()).thenReturn("Time is incorrect!");

        timeCommand.execute(issuer, args);

        verify(issuer).sendMessage("Time is incorrect!");
        verifyNoMoreInteractions(timeService);
    }

    @Test
    public void testExecute_AddOperation_ShouldAddTimeSuccessfully() {
        String[] args = {"add", "Player1", "2h"};
        Entry entry = mock(Entry.class);
        when(finder.find("Player1")).thenReturn(Optional.of(entry));
        when(convertor.getTimeMs("2h")).thenReturn(7200000L);
        when(convertor.getTimeLine(7200000L)).thenReturn("2 hours");
        when(messages.getAddTime()).thenReturn("Added %time% to %nickname%.");
        when(timeService.canAdd(entry, 7200000L)).thenReturn(true);

        timeCommand.execute(issuer, args);

        verify(timeService).add(entry, 7200000L);
        verify(issuer).sendMessage("Added 2 hours to Player1.");
    }

    @Test
    public void testExecute_RemoveOperation_ShouldRemoveTimeSuccessfully() {
        String[] args = {"remove", "Player1", "1h"};
        Entry entry = mock(Entry.class);
        when(finder.find("Player1")).thenReturn(Optional.of(entry));
        when(convertor.getTimeMs("1h")).thenReturn(3600000L);
        when(convertor.getTimeLine(3600000L)).thenReturn("1 hour");
        when(messages.getRemoveTime()).thenReturn("Removed %time% from %nickname%.");
        when(timeService.canRemove(entry, 3600000L)).thenReturn(true);

        timeCommand.execute(issuer, args);

        verify(timeService).remove(entry, 3600000L);
        verify(issuer).sendMessage("Removed 1 hour from Player1.");
    }

    @Test
    public void testExecute_SetOperation_ShouldSetTimeSuccessfully() {
        String[] args = {"set", "Player1", "3h"};
        Entry entry = mock(Entry.class);
        when(finder.find("Player1")).thenReturn(Optional.of(entry));
        when(convertor.getTimeMs("3h")).thenReturn(10800000L);
        when(convertor.getTimeLine(10800000L)).thenReturn("3 hours");
        when(messages.getSetTime()).thenReturn("Set %time% for %nickname%.");

        timeCommand.execute(issuer, args);

        verify(timeService).set(entry, 10800000L);
        verify(issuer).sendMessage("Set 3 hours for Player1.");
    }

    @Test
    public void testExecute_AddOperationCantAddTime_ShouldSendCantAddTimeMessage() {
        String[] args = {"add", "Player1", "2h"};
        Entry entry = mock(Entry.class);
        when(finder.find("Player1")).thenReturn(Optional.of(entry));
        when(convertor.getTimeMs("2h")).thenReturn(7200000L);
        when(messages.getCantAddTime()).thenReturn("Can't add time!");
        when(timeService.canAdd(entry, 7200000L)).thenReturn(false);

        timeCommand.execute(issuer, args);

        verify(issuer).sendMessage("Can't add time!");
        verify(timeService, never()).add(any(), anyLong());
    }

    @Test
    public void testExecute_RemoveOperationCantRemoveTime_ShouldSendCantRemoveTimeMessage() {
        String[] args = {"remove", "Player1", "1h"};
        Entry entry = mock(Entry.class);
        when(finder.find("Player1")).thenReturn(Optional.of(entry));
        when(convertor.getTimeMs("1h")).thenReturn(3600000L);
        when(messages.getCantRemoveTime()).thenReturn("Can't remove time!");
        when(timeService.canRemove(entry, 3600000L)).thenReturn(false);

        timeCommand.execute(issuer, args);

        verify(issuer).sendMessage("Can't remove time!");
        verify(timeService, never()).remove(any(), anyLong());
    }

    @Test
    public void testGetTabulate_WhenNoArgs_ShouldReturnOperations() {
        String[] args = {};

        Set<String> result = timeCommand.getTabulate(issuer, args);

        assertEquals(TimeCommand.OPERATIONS, result);
    }

    @Test
    public void testGetTabulate_WhenInvalidOperation_ShouldReturnEmptySet() {
        String[] args = {"invalidOperation"};

        Set<String> result = timeCommand.getTabulate(issuer, args);

        assertEquals(Set.of(), result);
    }

    @Test
    public void testGetTabulate_WhenValidOperationNoNickname_ShouldReturnIssuerNickname() {
        String[] args = {"add"};
        when(issuer.getNickname()).thenReturn("Player1");

        Set<String> result = timeCommand.getTabulate(issuer, args);

        assertEquals(Set.of("Player1"), result);
    }

    @Test
    public void testGetTabulate_WhenValidOperationAndNickname_ShouldReturnRandomTime() {
        String[] args = {"add", "Player1"};
        when(timeRandom.getRandomOneTime()).thenReturn("1h");

        Set<String> result = timeCommand.getTabulate(issuer, args);

        assertEquals(Set.of("1h"), result);
    }
}
