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
    private TimeRandom timeRandom;
    private EntryTimeService timeService;
    private CommandIssuer issuer;
    private Entry entry;

    @BeforeEach
    public void setUp() {
        messages = mock(MessagesConfig.class);
        finder = mock(EntryFinder.class);
        convertor = mock(TimeConvertor.class);
        timeRandom = mock(TimeRandom.class);
        timeService = mock(EntryTimeService.class);
        issuer = mock(CommandIssuer.class);
        entry = mock(Entry.class);

        timeCommand = new TimeCommand(messages, finder, convertor, timeRandom, timeService);

        when(messages.getIncorrectArguments()).thenReturn("Incorrect arguments.");
        when(messages.getPlayerNotInWhitelist()).thenReturn("Player %nickname% is not in the whitelist.");
        when(messages.getTimeIsIncorrect()).thenReturn("Time provided is incorrect.");
        when(messages.getAddTime()).thenReturn("Added %time% to %nickname%'s time.");
        when(messages.getCantAddTime()).thenReturn("Cannot add time.");
        when(messages.getRemoveTime()).thenReturn("Removed %time% from %nickname%'s time.");
        when(messages.getCantRemoveTime()).thenReturn("Cannot remove time.");
        when(messages.getSetTime()).thenReturn("Set %nickname%'s time to %time%.");
    }

    @Test
    public void testExecute_WithInsufficientArguments_ShouldSendIncorrectArgumentsMessage() {
        timeCommand.execute(issuer, new String[]{});

        verify(issuer).sendMessage("Incorrect arguments.");
    }

    @Test
    public void testExecute_PlayerNotInWhitelist_ShouldSendPlayerNotInWhitelistMessage() {
        String nickname = "nonexistentPlayer";
        when(finder.find(nickname)).thenReturn(Optional.empty());
        when(messages.getPlayerNotInWhitelist()).thenReturn("Player %nickname% is not in the whitelist.");

        timeCommand.execute(issuer, new String[]{"add", nickname, "1h"});

        verify(issuer).sendMessage("Player nonexistentPlayer is not in the whitelist.");
    }

    @Test
    public void testExecute_WithInvalidOperation_ShouldSendIncorrectArgumentsMessage() {
        String nickname = "somePlayer";
        when(finder.find(nickname)).thenReturn(Optional.of(entry));

        timeCommand.execute(issuer, new String[]{"invalidOp", nickname, "1h"});

        verify(issuer).sendMessage("Incorrect arguments.");
    }

    @Test
    public void testExecute_WithInvalidTime_ShouldSendTimeIsIncorrectMessage() {
        String nickname = "somePlayer";
        when(finder.find(nickname)).thenReturn(Optional.of(entry));
        when(convertor.getTimeMs("1x")).thenReturn(-1L);

        timeCommand.execute(issuer, new String[]{"add", nickname, "1x"});

        verify(issuer).sendMessage("Time provided is incorrect.");
    }

    @Test
    public void testExecute_AddOperation_CanAdd_ShouldSendAddTimeMessage() {
        String nickname = "somePlayer";
        when(finder.find(nickname)).thenReturn(Optional.of(entry));
        when(convertor.getTimeMs("1h")).thenReturn(3600000L);
        when(convertor.getTimeLine(3600000L)).thenReturn("1 hour");
        when(timeService.canAdd(entry, 3600000L)).thenReturn(true);

        timeCommand.execute(issuer, new String[]{"add", nickname, "1h"});

        verify(timeService).add(entry, 3600000L);
        verify(issuer).sendMessage("Added 1 hour to somePlayer's time.");
    }

    @Test
    public void testExecute_AddOperation_CannotAdd_ShouldSendCantAddTimeMessage() {
        String nickname = "somePlayer";
        when(finder.find(nickname)).thenReturn(Optional.of(entry));
        when(convertor.getTimeMs("1h")).thenReturn(3600000L);
        when(timeService.canAdd(entry, 3600000L)).thenReturn(false);

        timeCommand.execute(issuer, new String[]{"add", nickname, "1h"});

        verify(issuer).sendMessage("Cannot add time.");
    }

    @Test
    public void testExecute_RemoveOperation_CanRemove_ShouldSendRemoveTimeMessage() {
        String nickname = "somePlayer";
        when(finder.find(nickname)).thenReturn(Optional.of(entry));
        when(convertor.getTimeMs("1h")).thenReturn(3600000L);
        when(convertor.getTimeLine(3600000L)).thenReturn("1 hour");
        when(timeService.canRemove(entry, 3600000L)).thenReturn(true);

        timeCommand.execute(issuer, new String[]{"remove", nickname, "1h"});

        verify(timeService).remove(entry, 3600000L);
        verify(issuer).sendMessage("Removed 1 hour from somePlayer's time.");
    }

    @Test
    public void testExecute_RemoveOperation_CannotRemove_ShouldSendCantRemoveTimeMessage() {
        String nickname = "somePlayer";
        when(finder.find(nickname)).thenReturn(Optional.of(entry));
        when(convertor.getTimeMs("1h")).thenReturn(3600000L);
        when(timeService.canRemove(entry, 3600000L)).thenReturn(false);

        timeCommand.execute(issuer, new String[]{"remove", nickname, "1h"});

        verify(issuer).sendMessage("Cannot remove time.");
    }

    @Test
    public void testExecute_SetOperation_ShouldSendSetTimeMessage() {
        String nickname = "somePlayer";
        when(finder.find(nickname)).thenReturn(Optional.of(entry));
        when(convertor.getTimeMs("1h")).thenReturn(3600000L);
        when(convertor.getTimeLine(3600000L)).thenReturn("1 hour");

        timeCommand.execute(issuer, new String[]{"set", nickname, "1h"});

        verify(timeService).set(entry, 3600000L);
        verify(issuer).sendMessage("Set somePlayer's time to 1 hour.");
    }

    @Test
    public void testGetTabulate_WithoutArguments_ShouldReturnOperations() {
        Set<String> expected = Set.of("add", "remove", "set");

        Set<String> result = timeCommand.getTabulate(issuer, new String[]{});

        assertEquals(expected, result);
    }

    @Test
    public void testGetTabulate_WithInvalidOperation_ShouldReturnEmptySet() {
        Set<String> result = timeCommand.getTabulate(issuer, new String[]{"invalid"});

        assertEquals(Set.of(), result);
    }

    @Test
    public void testGetTabulate_WithValidOperation_ShouldReturnIssuerNickname() {
        when(issuer.getNickname()).thenReturn("nickname");

        Set<String> result = timeCommand.getTabulate(issuer, new String[]{"add"});

        assertEquals(Set.of("nickname"), result);
    }

    @Test
    public void testGetTabulate_WithOperationAndNickname_ShouldReturnRandomTime() {
        when(timeRandom.getRandomOneTime()).thenReturn("1h");

        Set<String> result = timeCommand.getTabulate(issuer, new String[]{"add", "nickname"});

        assertEquals(Set.of("1h"), result);
    }
}
