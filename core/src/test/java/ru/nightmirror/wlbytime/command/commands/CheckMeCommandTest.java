package ru.nightmirror.wlbytime.command.commands;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.nightmirror.wlbytime.config.configs.MessagesConfig;
import ru.nightmirror.wlbytime.entry.Entry;
import ru.nightmirror.wlbytime.interfaces.command.CommandIssuer;
import ru.nightmirror.wlbytime.interfaces.finder.EntryFinder;
import ru.nightmirror.wlbytime.time.TimeConvertor;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class CheckMeCommandTest {

    private CheckMeCommand checkMeCommand;
    private MessagesConfig messages;
    private EntryFinder finder;
    private TimeConvertor convertor;
    private CommandIssuer issuer;

    @BeforeEach
    void setUp() {
        messages = mock(MessagesConfig.class);
        finder = mock(EntryFinder.class);
        convertor = mock(TimeConvertor.class);
        issuer = mock(CommandIssuer.class);

        checkMeCommand = new CheckMeCommand(messages, finder, convertor);
    }

    @Test
    void testGetPermission_ShouldReturnCorrectPermission() {
        assertEquals("wlbytime.checkme", checkMeCommand.getPermission());
    }

    @Test
    void testGetName_ShouldReturnCorrectName() {
        assertEquals("checkme", checkMeCommand.getName());
    }

    @Test
    void testExecute_WhenEntryNotFound_ShouldSendNotInWhitelistMessage() {
        when(issuer.getNickname()).thenReturn("testUser");
        when(finder.find("testUser")).thenReturn(Optional.empty());
        when(messages.getCheckMeNotInWhitelist()).thenReturn("You are not in the whitelist!");

        checkMeCommand.execute(issuer, new String[]{});

        verify(issuer).sendMessage("You are not in the whitelist!");
    }

    @Test
    void testExecute_WhenEntryInactive_ShouldSendNotInWhitelistMessage() {
        Entry inactiveEntry = mock(Entry.class);
        when(inactiveEntry.isInactive()).thenReturn(true);
        when(issuer.getNickname()).thenReturn("testUser");
        when(finder.find("testUser")).thenReturn(Optional.of(inactiveEntry));
        when(messages.getCheckMeNotInWhitelist()).thenReturn("You are not in the whitelist!");

        checkMeCommand.execute(issuer, new String[]{});

        verify(issuer).sendMessage("You are not in the whitelist!");
    }

    @Test
    void testExecute_WhenEntryIsForever_ShouldSendForeverMessage() {
        Entry foreverEntry = mock(Entry.class);
        when(foreverEntry.isInactive()).thenReturn(false);
        when(foreverEntry.isForever()).thenReturn(true);
        when(issuer.getNickname()).thenReturn("foreverUser");
        when(finder.find("foreverUser")).thenReturn(Optional.of(foreverEntry));
        when(messages.getCheckMeStillInWhitelistForever()).thenReturn("You are in the whitelist forever!");

        checkMeCommand.execute(issuer, new String[]{});

        verify(issuer).sendMessage("You are in the whitelist forever!");
    }

    @Test
    void testExecute_WhenEntryIsFrozen_ShouldSendFrozenMessage() {
        Entry frozenEntry = mock(Entry.class);
        when(frozenEntry.isInactive()).thenReturn(false);
        when(frozenEntry.isForever()).thenReturn(false);
        when(frozenEntry.isFreezeActive()).thenReturn(true);
        when(frozenEntry.getLeftFreezeTime()).thenReturn(3600000L); // 1 hour left
        when(issuer.getNickname()).thenReturn("frozenUser");
        when(finder.find("frozenUser")).thenReturn(Optional.of(frozenEntry));
        when(convertor.getTimeLine(3600000L)).thenReturn("1 hour");
        when(messages.getCheckMeFrozen()).thenReturn("You are frozen for %time%!");

        checkMeCommand.execute(issuer, new String[]{});

        verify(issuer).sendMessage("You are frozen for 1 hour!");
    }

    @Test
    void testExecute_WhenEntryIsActiveForTime_ShouldSendWhitelistForTimeMessage() {
        Entry timedEntry = mock(Entry.class);
        when(timedEntry.isInactive()).thenReturn(false);
        when(timedEntry.isForever()).thenReturn(false);
        when(timedEntry.isFreezeActive()).thenReturn(false);
        when(timedEntry.getLeftActiveTime()).thenReturn(7200000L); // 2 hours left
        when(issuer.getNickname()).thenReturn("timedUser");
        when(finder.find("timedUser")).thenReturn(Optional.of(timedEntry));
        when(convertor.getTimeLine(7200000L)).thenReturn("2 hours");
        when(messages.getCheckMeStillInWhitelistForTime()).thenReturn("You are in the whitelist for %time%!");

        checkMeCommand.execute(issuer, new String[]{});

        verify(issuer).sendMessage("You are in the whitelist for 2 hours!");
    }

    @Test
    void testGetTabulate_ShouldReturnEmptySet() {
        Set<String> tabulate = checkMeCommand.getTabulate(issuer, new String[]{});
        assertTrue(tabulate.isEmpty());
    }
}
