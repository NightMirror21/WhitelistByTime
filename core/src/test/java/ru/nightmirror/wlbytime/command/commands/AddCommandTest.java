package ru.nightmirror.wlbytime.command.commands;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import ru.nightmirror.wlbytime.config.configs.MessagesConfig;
import ru.nightmirror.wlbytime.entry.Entry;
import ru.nightmirror.wlbytime.interfaces.command.CommandIssuer;
import ru.nightmirror.wlbytime.interfaces.finder.EntryFinder;
import ru.nightmirror.wlbytime.interfaces.services.EntryService;
import ru.nightmirror.wlbytime.time.TimeConvertor;
import ru.nightmirror.wlbytime.time.TimeRandom;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class AddCommandTest {

    private AddCommand addCommand;
    private MessagesConfig messages;
    private EntryFinder finder;
    private TimeConvertor convertor;
    private EntryService service;
    private TimeRandom random;
    private CommandIssuer issuer;

    @BeforeEach
    void setUp() {
        messages = mock(MessagesConfig.class);
        finder = mock(EntryFinder.class);
        convertor = mock(TimeConvertor.class);
        service = mock(EntryService.class);
        random = mock(TimeRandom.class);
        issuer = mock(CommandIssuer.class);

        addCommand = new AddCommand(messages, finder, convertor, service, random);
    }

    @Test
    void testGetPermission_ShouldReturnCorrectPermission() {
        assertEquals("wlbytime.add", addCommand.getPermission());
    }

    @Test
    void testGetName_ShouldReturnCorrectName() {
        assertEquals("add", addCommand.getName());
    }

    @Test
    void testExecute_WithNoArguments_ShouldSendIncorrectArgumentsMessage() {
        when(messages.getIncorrectArguments()).thenReturn("Incorrect arguments!");

        addCommand.execute(issuer, new String[]{});

        verify(issuer).sendMessage("Incorrect arguments!");
    }

    @Test
    void testExecute_WithPlayerAlreadyInWhitelist_ShouldSendAlreadyInWhitelistMessage() {
        String nickname = "existingPlayer";
        when(finder.find(nickname)).thenReturn(Optional.of(Entry.builder().build()));  // Simulate existing player
        when(messages.getPlayerAlreadyInWhitelist()).thenReturn("%nickname% is already in the whitelist!");

        addCommand.execute(issuer, new String[]{nickname});

        verify(issuer).sendMessage("existingPlayer is already in the whitelist!");
    }

    @Test
    void testExecute_WithValidNickname_ShouldAddPlayerWithoutTime() {
        String nickname = "newPlayer";
        when(finder.find(nickname)).thenReturn(Optional.empty());
        when(messages.getSuccessfullyAdded()).thenReturn("Player %nickname% successfully added!");

        addCommand.execute(issuer, new String[]{nickname});

        verify(service).create(nickname);
        verify(issuer).sendMessage("Player newPlayer successfully added!");
    }

    @Test
    void testExecute_WithNicknameAndTime_ShouldAddPlayerWithTime() {
        String nickname = "timedPlayer";
        String timeArgument = "1d2h";  // Sample time input
        when(finder.find(nickname)).thenReturn(Optional.empty());
        when(convertor.getTimeMs(timeArgument)).thenReturn(90000000L);  // Mock time conversion
        when(convertor.getTimeLine(90000000L)).thenReturn("1 day 2 hours");
        when(messages.getSuccessfullyAdded()).thenReturn("Player %nickname% added for %time%!");

        addCommand.execute(issuer, new String[]{nickname, timeArgument});

        ArgumentCaptor<Long> timeCaptor = ArgumentCaptor.forClass(Long.class);
        verify(service).create(eq(nickname), timeCaptor.capture());
        verify(issuer).sendMessage("Player timedPlayer added for 1 day 2 hours!");

        // Check that timeCaptor captured the correct adjusted timestamp (current time + mock duration)
        assertTrue(timeCaptor.getValue() > System.currentTimeMillis());
    }

    @Test
    void testExecute_WithMultipleArgumentsForTime_ShouldConcatenateAndConvertTime() {
        String nickname = "concatPlayer";
        String[] args = {"concatPlayer", "1d", "2h"};
        when(finder.find(nickname)).thenReturn(Optional.empty());
        when(convertor.getTimeMs("1d2h")).thenReturn(93600000L);  // Mock time conversion
        when(convertor.getTimeLine(93600000L)).thenReturn("1 day 2 hours");
        when(messages.getSuccessfullyAdded()).thenReturn("Player %nickname% added for %time%!");

        addCommand.execute(issuer, args);

        verify(service).create(eq(nickname), anyLong());
        verify(issuer).sendMessage("Player concatPlayer added for 1 day 2 hours!");
    }

    @Test
    void testGetTabulate_WithNoArgs_ShouldReturnIssuerNickname() {
        when(issuer.getNickname()).thenReturn("issuerNickname");

        Set<String> tabulate = addCommand.getTabulate(issuer, new String[]{});

        assertEquals(Set.of("issuerNickname"), tabulate);
    }

    @Test
    void testGetTabulate_WithArgs_ShouldReturnRandomOneTime() {
        when(random.getRandomOneTime()).thenReturn("1h");

        Set<String> tabulate = addCommand.getTabulate(issuer, new String[]{"someArg"});

        assertEquals(Set.of("1h"), tabulate);
    }

    @Test
    void testAddPlayerWithoutTime_ShouldSendSuccessMessage() {
        String nickname = "noTimePlayer";
        when(messages.getSuccessfullyAdded()).thenReturn("Player %nickname% successfully added!");

        addCommand.execute(issuer, new String[]{nickname});

        verify(service).create(nickname);
        verify(issuer).sendMessage("Player noTimePlayer successfully added!");
    }

    @Test
    void testAddPlayerWithTime_ShouldSendFormattedSuccessMessage() {
        String nickname = "timePlayer";
        String timeArgument = "3d";
        when(finder.find(nickname)).thenReturn(Optional.empty());
        when(convertor.getTimeMs(timeArgument)).thenReturn(259200000L);
        when(convertor.getTimeLine(259200000L)).thenReturn("3 days");
        when(messages.getSuccessfullyAdded()).thenReturn("Player %nickname% added for %time%!");

        addCommand.execute(issuer, new String[]{nickname, timeArgument});

        verify(service).create(eq(nickname), anyLong());
        verify(issuer).sendMessage("Player timePlayer added for 3 days!");
    }
}
