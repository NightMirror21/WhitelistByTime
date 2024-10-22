package ru.nightmirror.wlbytime.command.commands;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
import static org.mockito.Mockito.*;

public class AddCommandTest {

    private AddCommand addCommand;
    private MessagesConfig messages;
    private EntryFinder finder;
    private TimeConvertor convertor;
    private EntryService service;
    private TimeRandom random;
    private CommandIssuer issuer;

    @BeforeEach
    public void setUp() {
        messages = mock(MessagesConfig.class);
        finder = mock(EntryFinder.class);
        convertor = mock(TimeConvertor.class);
        service = mock(EntryService.class);
        random = mock(TimeRandom.class);
        issuer = mock(CommandIssuer.class);

        addCommand = new AddCommand(messages, finder, convertor, service, random);
    }

    @Test
    public void testExecute_WhenNoArgumentsProvided_ShouldSendIncorrectArgumentsMessage() {
        String[] args = {};
        when(messages.getIncorrectArguments()).thenReturn("Incorrect arguments!");

        addCommand.execute(issuer, args);

        verify(issuer).sendMessage("Incorrect arguments!");
        verify(service, never()).create(anyString());
        verify(service, never()).create(anyString(), anyLong());
    }

    @Test
    public void testExecute_WhenPlayerAlreadyInWhitelist_ShouldSendPlayerAlreadyInWhitelistMessage() {
        String nickname = "Player1";
        String[] args = {nickname};
        when(finder.find(nickname)).thenReturn(Optional.of(mock(Entry.class)));
        when(messages.getPlayerAlreadyInWhitelist()).thenReturn("Player %nickname% is already whitelisted.");

        addCommand.execute(issuer, args);

        verify(finder).find(nickname);
        verify(issuer).sendMessage("Player Player1 is already whitelisted.");
        verify(service, never()).create(anyString());
    }

    @Test
    public void testExecute_WhenPlayerIsNotInWhitelistAndNoTimeProvided_ShouldAddPlayerWithoutTime() {
        String nickname = "Player1";
        String[] args = {nickname};
        when(finder.find(nickname)).thenReturn(Optional.empty());
        when(messages.getSuccessfullyAdded()).thenReturn("Player %nickname% added successfully.");

        addCommand.execute(issuer, args);

        verify(finder).find(nickname);
        verify(service).create(nickname);
        verify(issuer).sendMessage("Player Player1 added successfully.");
    }

    @Test
    public void testExecute_WhenPlayerIsNotInWhitelistAndTimeProvided_ShouldAddPlayerWithTime() {
        String nickname = "Player1";
        String[] args = {nickname, "2h", "30m"};
        when(finder.find(nickname)).thenReturn(Optional.empty());
        when(messages.getSuccessfullyAdded()).thenReturn("Player %nickname% added for %time%.");
        when(convertor.getTimeMs("2h30m")).thenReturn(9000000L);
        when(convertor.getTimeLine(9000000L)).thenReturn("2 hours 30 minutes");

        addCommand.execute(issuer, args);

        verify(finder).find(nickname);
        verify(service, never()).create(anyString());
        verify(service).create(eq(nickname), anyLong());
        verify(issuer).sendMessage("Player Player1 added for 2 hours 30 minutes.");
    }

    @Test
    public void testGetTabulate_WhenNoArgumentsProvided_ShouldReturnIssuerNickname() {
        String issuerNickname = "IssuerNickname";
        when(issuer.getNickname()).thenReturn(issuerNickname);
        String[] args = {};

        Set<String> result = addCommand.getTabulate(issuer, args);

        assertEquals(Set.of(issuerNickname), result);
    }

    @Test
    public void testGetTabulate_WhenArgumentsProvided_ShouldReturnRandomTime() {
        String randomTime = "1h30m";
        when(random.getRandomOneTime()).thenReturn(randomTime);
        String[] args = {"Player1"};

        Set<String> result = addCommand.getTabulate(issuer, args);

        assertEquals(Set.of(randomTime), result);
    }
}
