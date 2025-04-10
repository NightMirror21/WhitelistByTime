package ru.nightmirror.wlbytime.command.commands;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.nightmirror.wlbytime.config.configs.CommandsConfig;
import ru.nightmirror.wlbytime.config.configs.MessagesConfig;
import ru.nightmirror.wlbytime.entry.EntryImpl;
import ru.nightmirror.wlbytime.interfaces.command.CommandIssuer;
import ru.nightmirror.wlbytime.interfaces.finder.EntryFinder;
import ru.nightmirror.wlbytime.interfaces.services.EntryService;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class RemoveCommandTest {

    private CommandsConfig commandsConfig;
    private RemoveCommand removeCommand;
    private MessagesConfig messages;
    private EntryFinder finder;
    private EntryService service;
    private CommandIssuer issuer;

    @BeforeEach
    public void setUp() {
        commandsConfig = mock(CommandsConfig.class);
        messages = mock(MessagesConfig.class);
        finder = mock(EntryFinder.class);
        service = mock(EntryService.class);
        issuer = mock(CommandIssuer.class);
        removeCommand = new RemoveCommand(commandsConfig, messages, finder, service);

        when(messages.getIncorrectArguments()).thenReturn("Incorrect arguments provided.");
        when(messages.getPlayerRemovedFromWhitelist()).thenReturn("Player %nickname% has been removed from the whitelist.");
        when(messages.getPlayerNotInWhitelist()).thenReturn("Player %nickname% is not in the whitelist.");
    }

    @Test
    public void getPermissionReturnsCorrectPermission() {
        when(commandsConfig.getRemovePermission()).thenReturn("wlbytime.remove");
        assertEquals("wlbytime.remove", removeCommand.getPermission());
    }

    @Test
    public void executeNoArgumentsSendsIncorrectArgumentsMessage() {
        removeCommand.execute(issuer, new String[]{});

        verify(issuer).sendMessage("Incorrect arguments provided.");
        verifyNoMoreInteractions(issuer);
    }

    @Test
    public void executeMoreThanOneArgumentSendsIncorrectArgumentsMessage() {
        removeCommand.execute(issuer, new String[]{"player1", "extraArg"});

        verify(issuer).sendMessage("Incorrect arguments provided.");
        verifyNoMoreInteractions(issuer);
    }

    @Test
    public void executePlayerNotInWhitelistSendsPlayerNotInWhitelistMessage() {
        String nickname = "nonexistentPlayer";
        when(finder.find(nickname)).thenReturn(Optional.empty());
        when(messages.getPlayerNotInWhitelist()).thenReturn("Player %nickname% is not in the whitelist.");

        removeCommand.execute(issuer, new String[]{nickname});

        verify(issuer).sendMessage("Player nonexistentPlayer is not in the whitelist.");
        verifyNoInteractions(service);
    }

    @Test
    public void executePlayerInWhitelistRemovesPlayerAndSendsSuccessMessage() {
        String nickname = "existingPlayer";
        EntryImpl entry = mock(EntryImpl.class);
        when(finder.find(nickname)).thenReturn(Optional.of(entry));
        when(messages.getPlayerRemovedFromWhitelist()).thenReturn("Player %nickname% has been removed from the whitelist.");

        removeCommand.execute(issuer, new String[]{nickname});

        verify(service).remove(entry);
        verify(issuer).sendMessage("Player existingPlayer has been removed from the whitelist.");
    }

    @Test
    public void tabulateNoArgumentsReturnsNickname() {
        when(issuer.getNickname()).thenReturn("testNickname");

        Set<String> tabulationResult = removeCommand.getTabulate(issuer, new String[]{});

        assertEquals(Set.of("testNickname"), tabulationResult);
    }

    @Test
    public void tabulateWithArgumentsReturnsEmptySet() {
        Set<String> tabulationResult = removeCommand.getTabulate(issuer, new String[]{"someArg"});

        assertEquals(Set.of(), tabulationResult);
    }
}
