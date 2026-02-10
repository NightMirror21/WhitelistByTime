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

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class RemoveCommandTest {

    private CommandsConfig commandsConfig;
    private RemoveCommand removeCommand;
    private MessagesConfig messages;
    private EntryService service;
    private CommandIssuer issuer;
    private PlayerIdentityResolver identityResolver;
    private EntryIdentityService identityService;

    @BeforeEach
    public void setUp() {
        commandsConfig = mock(CommandsConfig.class);
        messages = mock(MessagesConfig.class);
        service = mock(EntryService.class);
        issuer = mock(CommandIssuer.class);
        identityResolver = mock(PlayerIdentityResolver.class);
        identityService = mock(EntryIdentityService.class);
        removeCommand = new RemoveCommand(commandsConfig, messages, service, identityResolver, identityService);

        when(messages.getIncorrectArguments()).thenReturn("Incorrect arguments provided.");
        when(messages.getPlayerRemovedFromWhitelist()).thenReturn("Player %nickname% has been removed from the whitelist.");
        when(messages.getPlayerNotInWhitelist()).thenReturn("Player %nickname% is not in the whitelist.");
    }

    @Test
    public void getPermissionsReturnsConfiguredPermissions() {
        when(commandsConfig.getRemovePermission()).thenReturn(Set.of("whitelistbytime.remove", "wlbytime.remove"));
        assertEquals(Set.of("whitelistbytime.remove", "wlbytime.remove"), removeCommand.getPermissions());
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
        when(identityResolver.resolveByNickname(nickname))
                .thenReturn(new ResolvedPlayer(
                        PlayerKey.nickname(nickname), nickname, null));
        when(identityService.findOrMigrate(any(), anyString())).thenReturn(Optional.empty());
        when(messages.getPlayerNotInWhitelist()).thenReturn("Player %nickname% is not in the whitelist.");

        removeCommand.execute(issuer, new String[]{nickname});

        verify(issuer).sendMessage("Player nonexistentPlayer is not in the whitelist.");
        verifyNoInteractions(service);
    }

    @Test
    public void executePlayerInWhitelistRemovesPlayerAndSendsSuccessMessage() {
        String nickname = "existingPlayer";
        EntryImpl entry = mock(EntryImpl.class);
        when(identityResolver.resolveByNickname(nickname))
                .thenReturn(new ResolvedPlayer(
                        PlayerKey.nickname(nickname), nickname, null));
        when(identityService.findOrMigrate(any(), anyString())).thenReturn(Optional.of(entry));
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
