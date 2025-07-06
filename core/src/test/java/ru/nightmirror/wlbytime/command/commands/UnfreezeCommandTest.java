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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UnfreezeCommandTest {

    private CommandsConfig commandsConfig;
    private MessagesConfig messages;
    private EntryFinder finder;
    private EntryService service;
    private CommandIssuer issuer;
    private UnfreezeCommand unfreezeCommand;

    @BeforeEach
    public void setUp() {
        commandsConfig = mock(CommandsConfig.class);
        messages = mock(MessagesConfig.class);
        finder = mock(EntryFinder.class);
        service = mock(EntryService.class);
        issuer = mock(CommandIssuer.class);

        unfreezeCommand = new UnfreezeCommand(commandsConfig, messages, finder, service);
    }

    @Test
    public void getPermissionReturnsCorrectPermission() {
        when(commandsConfig.getUnfreezePermission()).thenReturn("wlbytime.unfreeze");
        assertEquals("wlbytime.unfreeze", unfreezeCommand.getPermission());
    }

    @Test
    public void getNameReturnsCorrectName() {
        assertEquals("unfreeze", unfreezeCommand.getName());
    }

    @Test
    public void executeWithInsufficientArgumentsSendsIncorrectArgumentsMessage() {
        when(messages.getIncorrectArguments()).thenReturn("Incorrect arguments!");

        unfreezeCommand.execute(issuer, new String[]{});

        verify(issuer).sendMessage("Incorrect arguments!");
        verifyNoMoreInteractions(issuer);
        verifyNoInteractions(finder, service);
    }

    @Test
    public void executePlayerNotInWhitelistSendsPlayerNotInWhitelistMessage() {
        String nickname = "ghost";
        when(finder.find(nickname)).thenReturn(Optional.empty());
        when(messages.getPlayerNotInWhitelist()).thenReturn("Player %nickname% not in whitelist!");

        unfreezeCommand.execute(issuer, new String[]{nickname});

        verify(issuer).sendMessage("Player ghost not in whitelist!");
        verify(finder).find(nickname);
        verifyNoMoreInteractions(issuer);
        verifyNoInteractions(service);
    }

    @Test
    public void executePlayerNotFrozenSendsPlayerNotFrozenMessage() {
        String nickname = "notFrozen";
        EntryImpl entry = mock(EntryImpl.class);
        when(finder.find(nickname)).thenReturn(Optional.of(entry));
        when(entry.isFrozen()).thenReturn(false);
        when(messages.getPlayerNotFrozen()).thenReturn("Player %nickname% not frozen!");

        unfreezeCommand.execute(issuer, new String[]{nickname});

        verify(issuer).sendMessage("Player notFrozen not frozen!");
        verify(finder).find(nickname);
        verify(entry).isFrozen();
        verifyNoInteractions(service);
        verifyNoMoreInteractions(issuer);
    }

    @Test
    public void executeFreezeAlreadyExpiredSendsFreezeExpiredMessage() {
        String nickname = "expiredFreeze";
        EntryImpl entry = mock(EntryImpl.class);
        when(finder.find(nickname)).thenReturn(Optional.of(entry));
        when(entry.isFrozen()).thenReturn(true);
        when(entry.isFreezeInactive()).thenReturn(true);
        when(messages.getPlayerFreezeExpired()).thenReturn("Freeze of %nickname% already expired!");

        unfreezeCommand.execute(issuer, new String[]{nickname});

        verify(issuer).sendMessage("Freeze of expiredFreeze already expired!");
        verify(entry).isFrozen();
        verify(entry).isFreezeInactive();
        verifyNoInteractions(service);
        verifyNoMoreInteractions(issuer);
    }

    @Test
    public void executeSuccessfullyUnfreezesPlayerSendsPlayerUnfrozenMessage() {
        String nickname = "frozenGuy";
        EntryImpl entry = mock(EntryImpl.class);
        when(finder.find(nickname)).thenReturn(Optional.of(entry));
        when(entry.isFrozen()).thenReturn(true);
        when(entry.isFreezeInactive()).thenReturn(false);
        when(messages.getPlayerUnfrozen()).thenReturn("Player %nickname% unfrozen!");

        unfreezeCommand.execute(issuer, new String[]{nickname});

        verify(entry).isFrozen();
        verify(entry).isFreezeInactive();
        verify(service).unfreeze(entry);
        verify(issuer).sendMessage("Player frozenGuy unfrozen!");
        verifyNoMoreInteractions(issuer);
    }

    @Test
    public void executeSuccessfulUnfreezeDoesNotThrowException() {
        String nickname = "safePlayer";
        EntryImpl entry = mock(EntryImpl.class);
        when(finder.find(nickname)).thenReturn(Optional.of(entry));
        when(entry.isFrozen()).thenReturn(true);
        when(entry.isFreezeInactive()).thenReturn(false);
        when(messages.getPlayerUnfrozen()).thenReturn("Player %nickname% unfrozen!");

        assertDoesNotThrow(() -> unfreezeCommand.execute(issuer, new String[]{nickname}));

        verify(service).unfreeze(entry);
        verify(issuer).sendMessage("Player safePlayer unfrozen!");
    }

    @Test
    public void getTabulateNoArgsReturnsIssuerNickname() {
        when(issuer.getNickname()).thenReturn("issuerNick");

        Set<String> tabulate = unfreezeCommand.getTabulate(issuer, new String[]{});

        assertEquals(Set.of("issuerNick"), tabulate);
    }

    @Test
    public void getTabulateWithArgsReturnsEmptySet() {
        Set<String> tabulate = unfreezeCommand.getTabulate(issuer, new String[]{"arg"});

        assertEquals(Set.of(), tabulate);
    }
}
