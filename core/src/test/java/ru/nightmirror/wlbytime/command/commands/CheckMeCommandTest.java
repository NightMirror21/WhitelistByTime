package ru.nightmirror.wlbytime.command.commands;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.nightmirror.wlbytime.config.configs.MessagesConfig;
import ru.nightmirror.wlbytime.entry.Entry;
import ru.nightmirror.wlbytime.interfaces.command.CommandIssuer;
import ru.nightmirror.wlbytime.interfaces.finder.EntryFinder;
import ru.nightmirror.wlbytime.time.TimeConvertor;

import java.util.Optional;

import static org.mockito.Mockito.*;

public class CheckMeCommandTest {

    private CheckMeCommand checkMeCommand;
    private MessagesConfig messages;
    private EntryFinder finder;
    private TimeConvertor convertor;
    private CommandIssuer issuer;
    private Entry entry;

    @BeforeEach
    public void setUp() {
        messages = mock(MessagesConfig.class);
        finder = mock(EntryFinder.class);
        convertor = mock(TimeConvertor.class);
        issuer = mock(CommandIssuer.class);
        entry = mock(Entry.class);

        checkMeCommand = new CheckMeCommand(messages, finder, convertor);
    }

    @Test
    public void testExecute_WhenPlayerNotInWhitelistOrExpired() {
        when(issuer.getNickname()).thenReturn("Player1");
        when(finder.find("Player1")).thenReturn(Optional.empty());
        when(messages.getCheckMeNotInWhitelist()).thenReturn("You are not in the whitelist.");

        checkMeCommand.execute(issuer, new String[0]);

        verify(issuer).sendMessage("You are not in the whitelist.");
    }

    @Test
    public void testExecute_WhenPlayerInWhitelistForever() {
        when(issuer.getNickname()).thenReturn("Player1");
        when(finder.find("Player1")).thenReturn(Optional.of(entry));
        when(entry.isExpiredConsideringFreeze()).thenReturn(false);
        when(entry.hasNoExpiration()).thenReturn(true);
        when(messages.getCheckMeStillInWhitelistForever()).thenReturn("You are in the whitelist forever.");

        checkMeCommand.execute(issuer, new String[0]);

        verify(issuer).sendMessage("You are in the whitelist forever.");
    }

    @Test
    public void testExecute_WhenPlayerIsFrozen() {
        when(issuer.getNickname()).thenReturn("Player1");
        when(finder.find("Player1")).thenReturn(Optional.of(entry));
        when(entry.isExpiredConsideringFreeze()).thenReturn(false);
        when(entry.isCurrentlyFrozen()).thenReturn(true);
        when(entry.getRemainingFreezeTime()).thenReturn(60000L);
        when(convertor.getTimeLine(60000L)).thenReturn("1 minute");
        when(messages.getCheckMeFrozen()).thenReturn("You are frozen for %time%.");

        checkMeCommand.execute(issuer, new String[0]);

        verify(issuer).sendMessage("You are frozen for 1 minute.");
    }

    @Test
    public void testExecute_WhenPlayerInWhitelistForTime() {
        when(issuer.getNickname()).thenReturn("Player1");
        when(finder.find("Player1")).thenReturn(Optional.of(entry));
        when(entry.isExpiredConsideringFreeze()).thenReturn(false);
        when(entry.hasNoExpiration()).thenReturn(false);
        when(entry.isCurrentlyFrozen()).thenReturn(false);
        when(entry.getRemainingActiveTime()).thenReturn(120000L);
        when(convertor.getTimeLine(120000L)).thenReturn("2 minutes");
        when(messages.getCheckMeStillInWhitelistForTime()).thenReturn("You are in the whitelist for %time%.");

        checkMeCommand.execute(issuer, new String[0]);

        verify(issuer).sendMessage("You are in the whitelist for 2 minutes.");
    }
}
