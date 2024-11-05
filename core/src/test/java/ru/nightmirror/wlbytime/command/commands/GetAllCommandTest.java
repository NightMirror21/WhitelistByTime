package ru.nightmirror.wlbytime.command.commands;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.nightmirror.wlbytime.config.configs.MessagesConfig;
import ru.nightmirror.wlbytime.entry.Entry;
import ru.nightmirror.wlbytime.interfaces.command.CommandIssuer;
import ru.nightmirror.wlbytime.interfaces.services.EntryService;
import ru.nightmirror.wlbytime.time.TimeConvertor;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class GetAllCommandTest {

    private GetAllCommand getAllCommand;
    private MessagesConfig messages;
    private EntryService service;
    private TimeConvertor convertor;
    private CommandIssuer issuer;

    @BeforeEach
    void setUp() {
        messages = mock(MessagesConfig.class);
        service = mock(EntryService.class);
        convertor = mock(TimeConvertor.class);
        issuer = mock(CommandIssuer.class);
        getAllCommand = new GetAllCommand(messages, service, convertor);

        // Mock common message responses
        when(messages.getListEmpty()).thenReturn("The list is empty.");
        when(messages.getListHeader()).thenReturn("List of entries:");
        when(messages.getListFooter()).thenReturn("Page %page% of %max-page%");
        when(messages.getIncorrectArguments()).thenReturn("Incorrect arguments provided.");
        when(messages.getPageNotExists()).thenReturn("Page %page% does not exist. Max page is %max-page%");
        when(messages.getEntriesForPage()).thenReturn(5);
        when(messages.getListElement()).thenReturn("Nickname: %nickname%, Status: %time-or-status%");
        when(messages.getForever()).thenReturn("Forever");
        when(messages.getFrozen()).thenReturn("Frozen for %time%");
        when(messages.getActive()).thenReturn("Active for %time%");
        when(messages.getExpired()).thenReturn("Expired");
    }

    @Test
    void testExecute_WithNoEntries_ShouldSendEmptyListMessage() {
        when(service.getEntries()).thenReturn(Set.of());

        getAllCommand.execute(issuer, new String[]{});

        verify(issuer).sendMessage("The list is empty.");
        verifyNoMoreInteractions(issuer);
    }

    @Test
    void testExecute_WithEntriesOnFirstPage_ShouldSendEntriesWithHeaderAndFooter() {
        Entry entry1 = mockEntry("player1", true, false, false, 5000);
        Entry entry2 = mockEntry("player2", true, true, false, 3000);
        Entry entry3 = mockEntry("player3", false, false, false, 0);

        when(service.getEntries()).thenReturn(Set.of(entry1, entry2, entry3));
        when(convertor.getTimeLine(5000)).thenReturn("5 seconds");
        when(convertor.getTimeLine(3000)).thenReturn("3 seconds");

        getAllCommand.execute(issuer, new String[]{});

        verify(issuer).sendMessage("List of entries:");
        verify(issuer).sendMessage("Nickname: player1, Status: Active for 5 seconds");
        verify(issuer).sendMessage("Nickname: player2, Status: Frozen for 3 seconds");
        verify(issuer).sendMessage("Nickname: player3, Status: Expired");
        verify(issuer).sendMessage("Page 1 of 1");
    }

    @Test
    void testExecute_WithPageArgument_ShouldDisplayCorrectPageEntries() {
        List<Entry> entries = List.of(
                mockEntry("player1", true, false, false, 5000),
                mockEntry("player2", true, false, true, 0),
                mockEntry("player3", false, false, false, 0),
                mockEntry("player4", true, false, false, 7000),
                mockEntry("player5", true, false, false, 2000),
                mockEntry("player6", true, false, false, 6000)
        );

        when(service.getEntries()).thenReturn(Set.copyOf(entries));
        when(messages.getEntriesForPage()).thenReturn(3);
        when(convertor.getTimeLine(5000)).thenReturn("5 seconds");
        when(convertor.getTimeLine(7000)).thenReturn("7 seconds");
        when(convertor.getTimeLine(2000)).thenReturn("2 seconds");
        when(convertor.getTimeLine(6000)).thenReturn("6 seconds");

        getAllCommand.execute(issuer, new String[]{"2"});

        verify(issuer).sendMessage("List of entries:");
        verify(issuer).sendMessage("Nickname: player4, Status: Active for 7 seconds");
        verify(issuer).sendMessage("Nickname: player5, Status: Active for 2 seconds");
        verify(issuer).sendMessage("Nickname: player6, Status: Active for 6 seconds");
        verify(issuer).sendMessage("Page 2 of 2");
    }

    @Test
    void testExecute_WithInvalidPageNumber_ShouldSendPageNotExistsMessage() {
        List<Entry> entries = List.of(
                mockEntry("player1", true, false, false, 5000),
                mockEntry("player2", true, false, true, 0)
        );

        when(service.getEntries()).thenReturn(Set.copyOf(entries));
        when(messages.getEntriesForPage()).thenReturn(1);

        getAllCommand.execute(issuer, new String[]{"5"});

        verify(issuer).sendMessage("Page 5 does not exist. Max page is 2");
        verifyNoMoreInteractions(issuer);
    }

    @Test
    void testExecute_WithInvalidPageArgument_ShouldSendIncorrectArgumentsMessage() {
        getAllCommand.execute(issuer, new String[]{"abc"});

        verify(issuer).sendMessage("Incorrect arguments provided.");
        verifyNoMoreInteractions(issuer);
    }

    @Test
    void testTabulate_WithNoArgs_ShouldReturnPagesUpTo20() {
        Set<String> tabulationResult = getAllCommand.getTabulate(issuer, new String[]{});

        assertEquals(20, tabulationResult.size());
        for (int i = 1; i <= 20; i++) {
            assert(tabulationResult.contains(String.valueOf(i)));
        }
    }

    @Test
    void testTabulate_WithArgs_ShouldReturnEmptySet() {
        Set<String> tabulationResult = getAllCommand.getTabulate(issuer, new String[]{"1"});

        assertEquals(Set.of(), tabulationResult);
    }

    private Entry mockEntry(String nickname, boolean isActive, boolean isFrozen, boolean isForever, long timeLeft) {
        Entry entry = mock(Entry.class);
        when(entry.getNickname()).thenReturn(nickname);
        when(entry.isActive()).thenReturn(isActive);
        when(entry.isFreezeActive()).thenReturn(isFrozen);
        when(entry.isForever()).thenReturn(isForever);
        when(entry.getLeftActiveTime()).thenReturn(timeLeft);
        when(entry.getLeftFreezeTime()).thenReturn(timeLeft);
        return entry;
    }
}
