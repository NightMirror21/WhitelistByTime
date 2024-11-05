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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class FreezeCommandTest {

    private FreezeCommand freezeCommand;
    private MessagesConfig messages;
    private EntryFinder finder;
    private TimeConvertor convertor;
    private TimeRandom timeRandom;
    private EntryService service;
    private CommandIssuer issuer;

    @BeforeEach
    void setUp() {
        messages = mock(MessagesConfig.class);
        finder = mock(EntryFinder.class);
        convertor = mock(TimeConvertor.class);
        timeRandom = mock(TimeRandom.class);
        service = mock(EntryService.class);
        issuer = mock(CommandIssuer.class);

        freezeCommand = new FreezeCommand(messages, finder, convertor, timeRandom, service);
    }

    // Test getPermission method
    @Test
    void testGetPermission_ShouldReturnCorrectPermission() {
        assertEquals("wlbytime.freeze", freezeCommand.getPermission());
    }

    // Test getName method
    @Test
    void testGetName_ShouldReturnCorrectName() {
        assertEquals("freeze", freezeCommand.getName());
    }

    // Test execute method with insufficient arguments
    @Test
    void testExecute_WithInsufficientArguments_ShouldSendIncorrectArgumentsMessage() {
        when(messages.getIncorrectArguments()).thenReturn("Incorrect arguments provided!");

        freezeCommand.execute(issuer, new String[]{});

        verify(issuer).sendMessage("Incorrect arguments provided!");
        verifyNoMoreInteractions(issuer);
        verifyNoInteractions(finder, convertor, service);
    }

    // Test execute method when player is not in whitelist
    @Test
    void testExecute_PlayerNotInWhitelist_ShouldSendPlayerNotInWhitelistMessage() {
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

    // Test execute method with invalid time string (timeInMillis <= 0)
    @Test
    void testExecute_WithInvalidTime_ShouldSendTimeIsIncorrectMessage() {
        String nickname = "validPlayer";
        String timeString = "invalidTime";
        Entry activeEntry = mock(Entry.class);
        when(issuer.getNickname()).thenReturn(nickname);
        when(finder.find(nickname)).thenReturn(Optional.of(activeEntry));
        when(activeEntry.isActive()).thenReturn(true);
        when(activeEntry.isFreezeActive()).thenReturn(false);
        when(convertor.getTimeMs(timeString)).thenReturn(0L);
        when(messages.getTimeIsIncorrect()).thenReturn("The provided time is incorrect!");

        freezeCommand.execute(issuer, new String[]{nickname, timeString});

        verify(issuer).sendMessage("The provided time is incorrect!");
        verify(finder).find(nickname);
        verify(convertor).getTimeMs(timeString);
        verifyNoMoreInteractions(issuer);
        verifyNoInteractions(service);
    }

    // Test execute method when player is inactive (expired)
    @Test
    void testExecute_PlayerIsInactive_ShouldSendPlayerExpiredMessage() {
        String nickname = "expiredPlayer";
        Entry expiredEntry = mock(Entry.class);
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

    // Test execute method when player is already frozen
    @Test
    void testExecute_PlayerAlreadyFrozen_ShouldSendPlayerAlreadyFrozenMessage() {
        String nickname = "frozenPlayer";
        Entry frozenEntry = mock(Entry.class);
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

    // Test execute method successfully freezing a player
    @Test
    void testExecute_SuccessfullyFreezePlayer_ShouldSendPlayerFrozenMessage() {
        String nickname = "activePlayer";
        String timeString = "3h";
        long timeInMillis = 10800000L; // 3 hours in milliseconds
        Entry activeEntry = mock(Entry.class);
        when(issuer.getNickname()).thenReturn(nickname);
        when(finder.find(nickname)).thenReturn(Optional.of(activeEntry));
        when(activeEntry.isActive()).thenReturn(true);
        when(activeEntry.isFreezeActive()).thenReturn(false);
        when(convertor.getTimeMs(timeString)).thenReturn(timeInMillis);
        String formattedTime = "3 hours";
        when(convertor.getTimeLine(timeInMillis)).thenReturn(formattedTime);
        when(messages.getPlayerFrozen()).thenReturn("Player %nickname% has been frozen for %time%!");

        freezeCommand.execute(issuer, new String[]{nickname, timeString});

        verify(activeEntry).isActive();
        verify(activeEntry).isFreezeActive();
        verify(convertor).getTimeMs(timeString);
        verify(convertor).getTimeLine(timeInMillis);
        verify(service).freeze(activeEntry, timeInMillis);
        verify(issuer).sendMessage("Player activePlayer has been frozen for 3 hours!");
        verifyNoMoreInteractions(issuer);
    }

    // Test execute method with multiple time arguments (e.g., "1d2h")
    @Test
    void testExecute_WithMultipleTimeArguments_ShouldConcatenateAndFreezePlayer() {
        String nickname = "multiTimePlayer";
        String[] args = {nickname, "1d", "2h"};
        String concatenatedTime = "1d2h";
        long timeInMillis = 93600000L; // 1 day 2 hours in milliseconds
        Entry activeEntry = mock(Entry.class);
        String formattedTime = "1 day 2 hours";

        when(issuer.getNickname()).thenReturn(nickname);
        when(finder.find(nickname)).thenReturn(Optional.of(activeEntry));
        when(activeEntry.isActive()).thenReturn(true);
        when(activeEntry.isFreezeActive()).thenReturn(false);
        when(convertor.getTimeMs(concatenatedTime)).thenReturn(timeInMillis);
        when(convertor.getTimeLine(timeInMillis)).thenReturn(formattedTime);
        when(messages.getPlayerFrozen()).thenReturn("Player %nickname% has been frozen for %time%!");

        freezeCommand.execute(issuer, args);

        verify(finder).find(nickname);
        verify(convertor).getTimeMs(concatenatedTime);
        verify(convertor).getTimeLine(timeInMillis);
        verify(activeEntry).isActive();
        verify(activeEntry).isFreezeActive();
        verify(service).freeze(activeEntry, timeInMillis);
        verify(issuer).sendMessage("Player multiTimePlayer has been frozen for 1 day 2 hours!");
        verifyNoMoreInteractions(issuer);
    }

    // Test getTabulate method with no arguments
    @Test
    void testGetTabulate_WithNoArguments_ShouldReturnIssuerNickname() {
        String nickname = "issuerUser";
        when(issuer.getNickname()).thenReturn(nickname);

        Set<String> tabulate = freezeCommand.getTabulate(issuer, new String[]{});

        assertEquals(Set.of(nickname), tabulate);
    }

    // Test getTabulate method with arguments (should return random time)
    @Test
    void testGetTabulate_WithArguments_ShouldReturnRandomTime() {
        String randomTime = "45m";
        when(timeRandom.getRandomOneTime()).thenReturn(randomTime);

        Set<String> tabulate = freezeCommand.getTabulate(issuer, new String[]{"arg1", "arg2"});

        assertEquals(Set.of(randomTime), tabulate);
    }

    // Additional Test: Ensure that freezePlayer constructs the correct message
    @Test
    void testExecute_FreezePlayer_ConstructsCorrectMessage() {
        String nickname = "testFreezePlayer";
        String timeString = "30m";
        long timeInMillis = 1800000L; // 30 minutes in milliseconds
        Entry activeEntry = mock(Entry.class);
        String formattedTime = "30 minutes";

        when(issuer.getNickname()).thenReturn(nickname);
        when(finder.find(nickname)).thenReturn(Optional.of(activeEntry));
        when(activeEntry.isActive()).thenReturn(true);
        when(activeEntry.isFreezeActive()).thenReturn(false);
        when(convertor.getTimeMs(timeString)).thenReturn(timeInMillis);
        when(convertor.getTimeLine(timeInMillis)).thenReturn(formattedTime);
        when(messages.getPlayerFrozen()).thenReturn("Player %nickname% has been frozen for %time%!");

        freezeCommand.execute(issuer, new String[]{nickname, timeString});

        ArgumentCaptor<Entry> entryCaptor = ArgumentCaptor.forClass(Entry.class);
        ArgumentCaptor<Long> timeCaptor = ArgumentCaptor.forClass(Long.class);
        verify(service).freeze(entryCaptor.capture(), timeCaptor.capture());

        assertEquals(activeEntry, entryCaptor.getValue());
        assertEquals(timeInMillis, timeCaptor.getValue());
        verify(issuer).sendMessage("Player testFreezePlayer has been frozen for 30 minutes!");
    }

    // Additional Test: Ensure concatenateArgs works correctly
    @Test
    void testExecute_WithEmptyTimeArguments_ShouldConcatenateToEmptyString() {
        String nickname = "emptyTimePlayer";
        String[] args = {nickname};
        // Since args.length < 2, it should send incorrect arguments message
        when(messages.getIncorrectArguments()).thenReturn("Incorrect arguments provided!");

        freezeCommand.execute(issuer, args);

        verify(issuer).sendMessage("Incorrect arguments provided!");
        verifyNoMoreInteractions(issuer);
    }

    // Additional Test: Time conversion returns negative value
    @Test
    void testExecute_WithNegativeTime_ShouldSendTimeIsIncorrectMessage() {
        String nickname = "negativeTimePlayer";
        String timeString = "-1h";
        long timeInMillis = -3600000L; // -1 hour in milliseconds

        Entry activeEntry = mock(Entry.class);
        when(issuer.getNickname()).thenReturn(nickname);
        when(finder.find(nickname)).thenReturn(Optional.of(activeEntry));
        when(activeEntry.isActive()).thenReturn(true);
        when(activeEntry.isFreezeActive()).thenReturn(false);
        when(convertor.getTimeMs(timeString)).thenReturn(timeInMillis);
        when(messages.getTimeIsIncorrect()).thenReturn("The provided time is incorrect!");

        freezeCommand.execute(issuer, new String[]{nickname, timeString});

        verify(issuer).sendMessage("The provided time is incorrect!");
        verify(finder).find(nickname);
        verify(convertor).getTimeMs(timeString);
        verifyNoMoreInteractions(issuer);
        verifyNoInteractions(service);
    }

    // Additional Test: Time conversion returns zero
    @Test
    void testExecute_WithZeroTime_ShouldSendTimeIsIncorrectMessage() {
        String nickname = "zeroTimePlayer";
        String timeString = "0h";
        long timeInMillis = 0L;

        Entry activeEntry = mock(Entry.class);
        when(issuer.getNickname()).thenReturn(nickname);
        when(finder.find(nickname)).thenReturn(Optional.of(activeEntry));
        when(activeEntry.isActive()).thenReturn(true);
        when(activeEntry.isFreezeActive()).thenReturn(false);
        when(convertor.getTimeMs(timeString)).thenReturn(timeInMillis);
        when(messages.getTimeIsIncorrect()).thenReturn("The provided time is incorrect!");

        freezeCommand.execute(issuer, new String[]{nickname, timeString});

        verify(issuer).sendMessage("The provided time is incorrect!");
        verify(finder).find(nickname);
        verify(convertor).getTimeMs(timeString);
        verifyNoMoreInteractions(issuer);
        verifyNoInteractions(service);
    }

    // Additional Test: Ensure that no exception is thrown when service.freeze is called successfully
    @Test
    void testExecute_SuccessfulFreeze_ShouldNotThrowException() {
        String nickname = "safeFreezePlayer";
        String timeString = "15m";
        long timeInMillis = 900000L; // 15 minutes in milliseconds
        Entry activeEntry = mock(Entry.class);
        String formattedTime = "15 minutes";

        when(issuer.getNickname()).thenReturn(nickname);
        when(finder.find(nickname)).thenReturn(Optional.of(activeEntry));
        when(activeEntry.isActive()).thenReturn(true);
        when(activeEntry.isFreezeActive()).thenReturn(false);
        when(convertor.getTimeMs(timeString)).thenReturn(timeInMillis);
        when(convertor.getTimeLine(timeInMillis)).thenReturn(formattedTime);
        when(messages.getPlayerFrozen()).thenReturn("Player %nickname% has been frozen for %time%!");

        assertDoesNotThrow(() -> freezeCommand.execute(issuer, new String[]{nickname, timeString}));

        verify(service).freeze(activeEntry, timeInMillis);
        verify(issuer).sendMessage("Player safeFreezePlayer has been frozen for 15 minutes!");
    }
}
