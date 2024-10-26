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

public class FreezeCommandTest {

    private FreezeCommand freezeCommand;
    private MessagesConfig messages;
    private EntryFinder finder;
    private TimeConvertor convertor;
    private TimeRandom timeRandom;
    private EntryService service;
    private CommandIssuer issuer;

    @BeforeEach
    public void setUp() {
        messages = mock(MessagesConfig.class);
        finder = mock(EntryFinder.class);
        convertor = mock(TimeConvertor.class);
        timeRandom = mock(TimeRandom.class);
        service = mock(EntryService.class);
        issuer = mock(CommandIssuer.class);

        freezeCommand = new FreezeCommand(messages, finder, convertor, timeRandom, service);
    }

    @Test
    public void testExecute_WhenNoArgumentsProvided_ShouldSendIncorrectArgumentsMessage() {
        String[] args = {};
        when(messages.getIncorrectArguments()).thenReturn("Incorrect arguments!");

        freezeCommand.execute(issuer, args);

        verify(issuer).sendMessage("Incorrect arguments!");
        verify(finder, never()).find(anyString());
        verify(service, never()).freeze(any(), anyLong());
    }

    @Test
    public void testExecute_WhenOnlyOneArgumentProvided_ShouldSendIncorrectArgumentsMessage() {
        String[] args = {"Player1"};
        when(messages.getIncorrectArguments()).thenReturn("Incorrect arguments!");

        freezeCommand.execute(issuer, args);

        verify(issuer).sendMessage("Incorrect arguments!");
        verify(finder, never()).find(anyString());
        verify(service, never()).freeze(any(), anyLong());
    }

    @Test
    public void testExecute_WhenPlayerNotInWhitelist_ShouldSendPlayerNotInWhitelistMessage() {
        String nickname = "Player1";
        String[] args = {nickname, "1h"};
        when(finder.find(nickname)).thenReturn(Optional.empty());
        when(messages.getPlayerNotInWhitelist()).thenReturn("Player %nickname% is not in the whitelist.");

        freezeCommand.execute(issuer, args);

        verify(finder).find(nickname);
        verify(issuer).sendMessage("Player Player1 is not in the whitelist.");
        verify(service, never()).freeze(any(), anyLong());
    }

    @Test
    public void testExecute_WhenTimeIsIncorrect_ShouldSendTimeIsIncorrectMessage() {
        String nickname = "Player1";
        String[] args = {nickname, "invalidTime"};
        Entry entry = mock(Entry.class);
        when(finder.find(nickname)).thenReturn(Optional.of(entry));
        when(convertor.getTimeMs("invalidTime")).thenReturn(-1L);
        when(messages.getTimeIsIncorrect()).thenReturn("The provided time is incorrect.");

        freezeCommand.execute(issuer, args);

        verify(finder).find(nickname);
        verify(convertor).getTimeMs("invalidTime");
        verify(issuer).sendMessage("The provided time is incorrect.");
        verify(service, never()).freeze(any(), anyLong());
    }

    @Test
    public void testExecute_WhenPlayerIsNotActive_ShouldSendPlayerExpiredMessage() {
        String nickname = "Player1";
        String[] args = {nickname, "1h"};
        Entry entry = mock(Entry.class);
        when(finder.find(nickname)).thenReturn(Optional.of(entry));
        when(convertor.getTimeMs("1h")).thenReturn(3600000L);
        when(entry.isActive()).thenReturn(false);
        when(messages.getPlayerExpired()).thenReturn("Player %nickname% has expired.");

        freezeCommand.execute(issuer, args);

        verify(finder).find(nickname);
        verify(convertor).getTimeMs("1h");
        verify(entry).isActive();
        verify(issuer).sendMessage("Player Player1 has expired.");
        verify(service, never()).freeze(any(), anyLong());
    }

    @Test
    public void testExecute_WhenPlayerIsAlreadyFrozen_ShouldSendPlayerAlreadyFrozenMessage() {
        String nickname = "Player1";
        String[] args = {nickname, "2h"};
        Entry entry = mock(Entry.class);
        when(finder.find(nickname)).thenReturn(Optional.of(entry));
        when(convertor.getTimeMs("2h")).thenReturn(7200000L);
        when(entry.isActive()).thenReturn(true);
        when(entry.isFrozen()).thenReturn(true);
        when(messages.getPlayerAlreadyFrozen()).thenReturn("Player %nickname% is already frozen.");

        freezeCommand.execute(issuer, args);

        verify(finder).find(nickname);
        verify(convertor).getTimeMs("2h");
        verify(entry).isActive();
        verify(entry).isFrozen();
        verify(issuer).sendMessage("Player Player1 is already frozen.");
        verify(service, never()).freeze(any(), anyLong());
    }

    @Test
    public void testExecute_WhenPlayerCanBeFrozen_ShouldFreezePlayerAndSendConfirmationMessage() {
        String nickname = "Player1";
        String[] args = {nickname, "3h"};
        Entry entry = mock(Entry.class);
        when(finder.find(nickname)).thenReturn(Optional.of(entry));
        when(convertor.getTimeMs("3h")).thenReturn(10800000L);
        when(entry.isActive()).thenReturn(true);
        when(entry.isFrozen()).thenReturn(false);
        when(convertor.getTimeLine(10800000L)).thenReturn("3 hours");
        when(messages.getPlayerFrozen()).thenReturn("Player %nickname% has been frozen for %time%.");

        freezeCommand.execute(issuer, args);

        verify(finder).find(nickname);
        verify(convertor).getTimeMs("3h");
        verify(entry).isActive();
        verify(entry).isFrozen();
        verify(service).freeze(entry, 10800000L);
        verify(convertor).getTimeLine(10800000L);
        verify(issuer).sendMessage("Player Player1 has been frozen for 3 hours.");
    }

    @Test
    public void testGetTabulate_WhenNoArgumentsProvided_ShouldReturnIssuerNickname() {
        String issuerNickname = "IssuerNickname";
        when(issuer.getNickname()).thenReturn(issuerNickname);
        String[] args = {};

        Set<String> result = freezeCommand.getTabulate(issuer, args);

        assertEquals(Set.of(issuerNickname), result);
    }

    @Test
    public void testGetTabulate_WhenArgumentsProvided_ShouldReturnRandomTime() {
        String randomTime = "1h30m";
        when(timeRandom.getRandomOneTime()).thenReturn(randomTime);
        String[] args = {"Player1"};

        Set<String> result = freezeCommand.getTabulate(issuer, args);

        assertEquals(Set.of(randomTime), result);
    }
}
