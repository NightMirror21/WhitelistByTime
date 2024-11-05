package ru.nightmirror.wlbytime.command.commands;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.nightmirror.wlbytime.config.configs.MessagesConfig;
import ru.nightmirror.wlbytime.entry.Entry;
import ru.nightmirror.wlbytime.interfaces.command.CommandIssuer;
import ru.nightmirror.wlbytime.interfaces.finder.EntryFinder;
import ru.nightmirror.wlbytime.interfaces.services.EntryService;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class RemoveCommandTest {

    private RemoveCommand removeCommand;
    private MessagesConfig messages;
    private EntryFinder finder;
    private EntryService service;
    private CommandIssuer issuer;

    @BeforeEach
    void setUp() {
        messages = mock(MessagesConfig.class);
        finder = mock(EntryFinder.class);
        service = mock(EntryService.class);
        issuer = mock(CommandIssuer.class);
        removeCommand = new RemoveCommand(messages, finder, service);

        // Mock common message responses
        when(messages.getIncorrectArguments()).thenReturn("Incorrect arguments provided.");
        when(messages.getPlayerRemovedFromWhitelist()).thenReturn("Player %nickname% has been removed from the whitelist.");
        when(messages.getPlayerNotInWhitelist()).thenReturn("Player %nickname% is not in the whitelist.");
    }

    @Test
    void testExecute_WithNoArguments_ShouldSendIncorrectArgumentsMessage() {
        removeCommand.execute(issuer, new String[]{});

        verify(issuer).sendMessage("Incorrect arguments provided.");
        verifyNoMoreInteractions(issuer);
    }

    @Test
    void testExecute_WithMoreThanOneArgument_ShouldSendIncorrectArgumentsMessage() {
        removeCommand.execute(issuer, new String[]{"player1", "extraArg"});

        verify(issuer).sendMessage("Incorrect arguments provided.");
        verifyNoMoreInteractions(issuer);
    }

    @Test
    void testExecute_PlayerNotInWhitelist_ShouldSendPlayerNotInWhitelistMessage() {
        String nickname = "nonexistentPlayer";
        when(finder.find(nickname)).thenReturn(Optional.empty());
        when(messages.getPlayerNotInWhitelist()).thenReturn("Player %nickname% is not in the whitelist.");

        removeCommand.execute(issuer, new String[]{nickname});

        verify(issuer).sendMessage("Player nonexistentPlayer is not in the whitelist.");
        verifyNoInteractions(service);
    }

    @Test
    void testExecute_PlayerInWhitelist_ShouldRemovePlayerAndSendSuccessMessage() {
        String nickname = "existingPlayer";
        Entry entry = mock(Entry.class);
        when(finder.find(nickname)).thenReturn(Optional.of(entry));
        when(messages.getPlayerRemovedFromWhitelist()).thenReturn("Player %nickname% has been removed from the whitelist.");

        removeCommand.execute(issuer, new String[]{nickname});

        verify(service).remove(entry);
        verify(issuer).sendMessage("Player existingPlayer has been removed from the whitelist.");
    }

    @Test
    void testGetTabulate_WithNoArguments_ShouldReturnNickname() {
        when(issuer.getNickname()).thenReturn("testNickname");

        Set<String> tabulationResult = removeCommand.getTabulate(issuer, new String[]{});

        assertEquals(Set.of("testNickname"), tabulationResult);
    }

    @Test
    void testGetTabulate_WithArguments_ShouldReturnEmptySet() {
        Set<String> tabulationResult = removeCommand.getTabulate(issuer, new String[]{"someArg"});

        assertEquals(Set.of(), tabulationResult);
    }
}
