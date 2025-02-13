package ru.nightmirror.wlbytime.command.commands;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import ru.nightmirror.wlbytime.config.configs.MessagesConfig;
import ru.nightmirror.wlbytime.entry.EntryImpl;
import ru.nightmirror.wlbytime.interfaces.command.CommandIssuer;
import ru.nightmirror.wlbytime.interfaces.finder.EntryFinder;
import ru.nightmirror.wlbytime.interfaces.services.EntryService;
import ru.nightmirror.wlbytime.time.TimeConvertor;
import ru.nightmirror.wlbytime.time.TimeRandom;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
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
    public void getPermissionReturnsCorrectPermission() {
        assertEquals("wlbytime.freeze", freezeCommand.getPermission());
    }

    @Test
    public void getNameReturnsCorrectName() {
        assertEquals("freeze", freezeCommand.getName());
    }

    @Test
    public void executeWithInsufficientArgumentsSendsIncorrectArgumentsMessage() {
        when(messages.getIncorrectArguments()).thenReturn("Incorrect arguments provided!");

        freezeCommand.execute(issuer, new String[]{});

        verify(issuer).sendMessage("Incorrect arguments provided!");
        verifyNoMoreInteractions(issuer);
        verifyNoInteractions(finder, convertor, service);
    }

    @Test
    public void executePlayerNotInWhitelistSendsPlayerNotInWhitelistMessage() {
        String nickname = "nonExistentPlayer";
        when(issuer.getNickname()).thenReturn(nickname);
        when(finder.find(nickname)).thenReturn(Optional.empty());
        when(messages.getPlayerNotInWhitelist()).thenReturn("Player %nickname% is not in the whitelist!");

        freezeCommand.execute(issuer, new String[]{nickname, "1h"});

        verify(issuer).sendMessage("Player nonExistentPlayer is not in the whitelist!");
        verifyNoMoreInteractions(issuer);
        verify(finder).find(nickname);
        verifyNoInteractions(convertor, service);
    }

    @Test
    public void executeWithInvalidTimeSendsTimeIsIncorrectMessage() {
        String nickname = "validPlayer";
        String timeString = "invalidTime";
        EntryImpl activeEntry = mock(EntryImpl.class);
        when(issuer.getNickname()).thenReturn(nickname);
        when(finder.find(nickname)).thenReturn(Optional.of(activeEntry));
        when(activeEntry.isActive()).thenReturn(true);
        when(activeEntry.isFreezeActive()).thenReturn(false);
        when(convertor.getTime(timeString)).thenReturn(Duration.ZERO);
        when(messages.getTimeIsIncorrect()).thenReturn("The provided time is incorrect!");

        freezeCommand.execute(issuer, new String[]{nickname, timeString});

        verify(issuer).sendMessage("The provided time is incorrect!");
        verify(finder).find(nickname);
        verify(convertor).getTime(timeString);
        verifyNoMoreInteractions(issuer);
        verifyNoInteractions(service);
    }

    @Test
    public void executePlayerInactiveSendsPlayerExpiredMessage() {
        String nickname = "expiredPlayer";
        EntryImpl expiredEntry = mock(EntryImpl.class);
        when(issuer.getNickname()).thenReturn(nickname);
        when(finder.find(nickname)).thenReturn(Optional.of(expiredEntry));
        when(expiredEntry.isActive()).thenReturn(false);
        when(messages.getPlayerExpired()).thenReturn("Player %nickname% has expired!");

        freezeCommand.execute(issuer, new String[]{nickname, "1h"});

        verify(issuer).sendMessage("Player expiredPlayer has expired!");
        verifyNoMoreInteractions(issuer);
        verify(finder).find(nickname);
        verifyNoInteractions(convertor, service);
    }

    @Test
    public void executePlayerAlreadyFrozenSendsPlayerAlreadyFrozenMessage() {
        String nickname = "frozenPlayer";
        EntryImpl frozenEntry = mock(EntryImpl.class);
        when(issuer.getNickname()).thenReturn(nickname);
        when(finder.find(nickname)).thenReturn(Optional.of(frozenEntry));
        when(frozenEntry.isActive()).thenReturn(true);
        when(frozenEntry.isFreezeActive()).thenReturn(true);
        when(messages.getPlayerAlreadyFrozen()).thenReturn("Player %nickname% is already frozen!");

        freezeCommand.execute(issuer, new String[]{nickname, "2h"});

        verify(frozenEntry).isActive();
        verify(frozenEntry).isFreezeActive();
        verify(issuer).sendMessage("Player frozenPlayer is already frozen!");
        verifyNoMoreInteractions(issuer);
        verify(finder).find(nickname);
        verifyNoInteractions(convertor, service);
    }

    @Test
    public void executeSuccessfullyFreezesPlayerSendsPlayerFrozenMessage() {
        String nickname = "activePlayer";
        String timeString = "3h";
        EntryImpl activeEntry = mock(EntryImpl.class);
        when(issuer.getNickname()).thenReturn(nickname);
        when(finder.find(nickname)).thenReturn(Optional.of(activeEntry));
        when(activeEntry.isActive()).thenReturn(true);
        when(activeEntry.isFreezeActive()).thenReturn(false);
        when(convertor.getTime(timeString)).thenReturn(Duration.ofHours(3));
        String formattedTime = "3 hours";
        when(convertor.getTimeLine(Duration.ofHours(3))).thenReturn(formattedTime);
        when(messages.getPlayerFrozen()).thenReturn("Player %nickname% has been frozen for %time%!");

        freezeCommand.execute(issuer, new String[]{nickname, timeString});

        verify(activeEntry).isActive();
        verify(activeEntry).isFreezeActive();
        verify(convertor).getTime(timeString);
        verify(convertor).getTimeLine(Duration.ofHours(3));
        verify(service).freeze(activeEntry, Duration.ofHours(3));
        verify(issuer).sendMessage("Player activePlayer has been frozen for 3 hours!");
        verifyNoMoreInteractions(issuer);
    }

    @Test
    public void executeWithMultipleTimeArgumentsConcatenatesAndFreezesPlayer() {
        String nickname = "multiTimePlayer";
        String[] args = {nickname, "1d", "2h"};
        String concatenatedTime = "1d2h";
        EntryImpl activeEntry = mock(EntryImpl.class);
        String formattedTime = "1 day 2 hours";
        Duration duration = Duration.ofDays(1).plusHours(2);

        when(issuer.getNickname()).thenReturn(nickname);
        when(finder.find(nickname)).thenReturn(Optional.of(activeEntry));
        when(activeEntry.isActive()).thenReturn(true);
        when(activeEntry.isFreezeActive()).thenReturn(false);
        when(convertor.getTime(concatenatedTime)).thenReturn(duration);
        when(convertor.getTimeLine(duration)).thenReturn(formattedTime);
        when(messages.getPlayerFrozen()).thenReturn("Player %nickname% has been frozen for %time%!");

        freezeCommand.execute(issuer, args);

        verify(finder).find(nickname);
        verify(convertor).getTime(concatenatedTime);
        verify(convertor).getTimeLine(duration);
        verify(activeEntry).isActive();
        verify(activeEntry).isFreezeActive();
        verify(service).freeze(activeEntry, duration);
        verify(issuer).sendMessage("Player multiTimePlayer has been frozen for 1 day 2 hours!");
        verifyNoMoreInteractions(issuer);
    }

    @Test
    public void getTabulateNoArgsReturnsIssuerNickname() {
        String nickname = "issuerUser";
        when(issuer.getNickname()).thenReturn(nickname);

        Set<String> tabulate = freezeCommand.getTabulate(issuer, new String[]{});

        assertEquals(Set.of(nickname), tabulate);
    }

    @Test
    public void getTabulateWithArgsReturnsRandomTime() {
        String randomTime = "45m";
        when(timeRandom.getRandomOneTime()).thenReturn(randomTime);

        Set<String> tabulate = freezeCommand.getTabulate(issuer, new String[]{"arg1", "arg2"});

        assertEquals(Set.of(randomTime), tabulate);
    }

    @Test
    public void executeFreezePlayerConstructsCorrectMessage() {
        String nickname = "testFreezePlayer";
        String timeString = "30m";
        Duration duration = Duration.ofMinutes(30);
        EntryImpl activeEntry = mock(EntryImpl.class);
        String formattedTime = "30 minutes";

        when(issuer.getNickname()).thenReturn(nickname);
        when(finder.find(nickname)).thenReturn(Optional.of(activeEntry));
        when(activeEntry.isActive()).thenReturn(true);
        when(activeEntry.isFreezeActive()).thenReturn(false);
        when(convertor.getTime(timeString)).thenReturn(duration);
        when(convertor.getTimeLine(duration)).thenReturn(formattedTime);
        when(messages.getPlayerFrozen()).thenReturn("Player %nickname% has been frozen for %time%!");

        freezeCommand.execute(issuer, new String[]{nickname, timeString});

        ArgumentCaptor<EntryImpl> entryCaptor = ArgumentCaptor.forClass(EntryImpl.class);
        ArgumentCaptor<Duration> timeCaptor = ArgumentCaptor.forClass(Duration.class);
        verify(service).freeze(entryCaptor.capture(), timeCaptor.capture());

        assertEquals(activeEntry, entryCaptor.getValue());
        assertEquals(duration, timeCaptor.getValue());
        verify(issuer).sendMessage("Player testFreezePlayer has been frozen for 30 minutes!");
    }

    @Test
    public void executeWithEmptyTimeArgumentsConcatenatesToEmptyString() {
        String nickname = "emptyTimePlayer";
        String[] args = {nickname};
        when(messages.getIncorrectArguments()).thenReturn("Incorrect arguments provided!");

        freezeCommand.execute(issuer, args);

        verify(issuer).sendMessage("Incorrect arguments provided!");
        verifyNoMoreInteractions(issuer);
    }

    @Test
    public void executeWithNegativeTimeSendsTimeIsIncorrectMessage() {
        String nickname = "negativeTimePlayer";
        String timeString = "-1h";
        Duration duration = Duration.ofHours(-1);

        EntryImpl activeEntry = mock(EntryImpl.class);
        when(issuer.getNickname()).thenReturn(nickname);
        when(finder.find(nickname)).thenReturn(Optional.of(activeEntry));
        when(activeEntry.isActive()).thenReturn(true);
        when(activeEntry.isFreezeActive()).thenReturn(false);
        when(convertor.getTime(timeString)).thenReturn(duration);
        when(messages.getTimeIsIncorrect()).thenReturn("The provided time is incorrect!");

        freezeCommand.execute(issuer, new String[]{nickname, timeString});

        verify(issuer).sendMessage("The provided time is incorrect!");
        verify(finder).find(nickname);
        verify(convertor).getTime(timeString);
        verifyNoMoreInteractions(issuer);
        verifyNoInteractions(service);
    }

    @Test
    public void executeWithZeroTimeSendsTimeIsIncorrectMessage() {
        String nickname = "zeroTimePlayer";
        String timeString = "0h";
        Duration duration = Duration.ofHours(0);

        EntryImpl activeEntry = mock(EntryImpl.class);
        when(issuer.getNickname()).thenReturn(nickname);
        when(finder.find(nickname)).thenReturn(Optional.of(activeEntry));
        when(activeEntry.isActive()).thenReturn(true);
        when(activeEntry.isFreezeActive()).thenReturn(false);
        when(convertor.getTime(timeString)).thenReturn(duration);
        when(messages.getTimeIsIncorrect()).thenReturn("The provided time is incorrect!");

        freezeCommand.execute(issuer, new String[]{nickname, timeString});

        verify(issuer).sendMessage("The provided time is incorrect!");
        verify(finder).find(nickname);
        verify(convertor).getTime(timeString);
        verifyNoMoreInteractions(issuer);
        verifyNoInteractions(service);
    }

    @Test
    public void executeSuccessfulFreezeDoesNotThrowException() {
        String nickname = "safeFreezePlayer";
        String timeString = "15m";
        Duration duration = Duration.ofMinutes(15);
        EntryImpl activeEntry = mock(EntryImpl.class);
        String formattedTime = "15 minutes";

        when(issuer.getNickname()).thenReturn(nickname);
        when(finder.find(nickname)).thenReturn(Optional.of(activeEntry));
        when(activeEntry.isActive()).thenReturn(true);
        when(activeEntry.isFreezeActive()).thenReturn(false);
        when(convertor.getTime(timeString)).thenReturn(duration);
        when(convertor.getTimeLine(duration)).thenReturn(formattedTime);
        when(messages.getPlayerFrozen()).thenReturn("Player %nickname% has been frozen for %time%!");

        assertDoesNotThrow(() -> freezeCommand.execute(issuer, new String[]{nickname, timeString}));

        verify(service).freeze(activeEntry, duration);
        verify(issuer).sendMessage("Player safeFreezePlayer has been frozen for 15 minutes!");
    }
}
