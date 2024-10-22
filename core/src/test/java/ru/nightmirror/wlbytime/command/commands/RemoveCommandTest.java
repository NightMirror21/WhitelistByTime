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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class RemoveCommandTest {

    private RemoveCommand removeCommand;
    private MessagesConfig messagesConfig;
    private EntryFinder entryFinder;
    private EntryService entryService;
    private CommandIssuer issuer;
    private Entry entry;

    @BeforeEach
    public void setUp() {
        messagesConfig = mock(MessagesConfig.class);
        entryFinder = mock(EntryFinder.class);
        entryService = mock(EntryService.class);
        issuer = mock(CommandIssuer.class);
        entry = mock(Entry.class);

        removeCommand = new RemoveCommand(messagesConfig, entryFinder, entryService);
    }

    @Test
    public void testExecute_WhenArgumentsLengthIsIncorrect_ShouldSendIncorrectArgumentsMessage() {
        String[] args = {};
        when(messagesConfig.getIncorrectArguments()).thenReturn("Incorrect arguments!");

        removeCommand.execute(issuer, args);

        verify(issuer).sendMessage("Incorrect arguments!");
        verify(entryFinder, never()).find(anyString());
        verify(entryService, never()).remove(any(Entry.class));
    }

    @Test
    public void testExecute_WhenPlayerIsFound_ShouldRemovePlayerAndSendSuccessMessage() {
        String nickname = "Player1";
        String[] args = {nickname};
        when(entryFinder.find(nickname)).thenReturn(Optional.of(entry));
        when(messagesConfig.getPlayerRemovedFromWhitelist()).thenReturn("Player %nickname% removed.");

        removeCommand.execute(issuer, args);

        verify(entryFinder).find(nickname);
        verify(entryService).remove(entry);
        verify(issuer).sendMessage("Player Player1 removed.");
    }

    @Test
    public void testExecute_WhenPlayerIsNotFound_ShouldSendPlayerNotInWhitelistMessage() {
        String nickname = "Player1";
        String[] args = {nickname};
        when(entryFinder.find(nickname)).thenReturn(Optional.empty());
        when(messagesConfig.getPlayerNotInWhitelist()).thenReturn("Player %nickname% is not in the whitelist.");

        removeCommand.execute(issuer, args);

        verify(entryFinder).find(nickname);
        verify(entryService, never()).remove(any(Entry.class));
        verify(issuer).sendMessage("Player Player1 is not in the whitelist.");
    }

    @Test
    public void testGetTabulate_WhenNoArgumentsProvided_ShouldReturnIssuerNickname() {
        String issuerNickname = "IssuerNickname";
        when(issuer.getNickname()).thenReturn(issuerNickname);
        String[] args = {};

        Set<String> result = removeCommand.getTabulate(issuer, args);

        assertEquals(Set.of(issuerNickname), result);
    }

    @Test
    public void testGetTabulate_WhenArgumentsProvided_ShouldReturnEmptySet() {
        String[] args = {"arg1"};

        Set<String> result = removeCommand.getTabulate(issuer, args);

        assertTrue(result.isEmpty());
    }
}
