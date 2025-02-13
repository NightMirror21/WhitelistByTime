package ru.nightmirror.wlbytime.command.commands;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.nightmirror.wlbytime.config.configs.MessagesConfig;
import ru.nightmirror.wlbytime.entry.EntryImpl;
import ru.nightmirror.wlbytime.interfaces.command.CommandIssuer;
import ru.nightmirror.wlbytime.interfaces.services.EntryService;
import ru.nightmirror.wlbytime.time.TimeConvertor;

import java.time.Duration;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class GetAllCommandTest {

    private GetAllCommand getAllCommand;
    private MessagesConfig messages;
    private EntryService service;
    private TimeConvertor convertor;
    private CommandIssuer issuer;

    @BeforeEach
    public void setUp() {
        messages = mock(MessagesConfig.class);
        service = mock(EntryService.class);
        convertor = mock(TimeConvertor.class);
        issuer = mock(CommandIssuer.class);
        getAllCommand = new GetAllCommand(messages, service, convertor);

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
    public void testExecute_WithNoEntries_ShouldSendEmptyListMessage() {
        when(service.getEntries()).thenReturn(Set.of());

        getAllCommand.execute(issuer, new String[]{});

        verify(issuer).sendMessage("The list is empty.");
        verifyNoMoreInteractions(issuer);
    }

    @Test
    public void testExecute_WithEntriesOnFirstPage_ShouldSendEntriesWithHeaderAndFooter() {
        EntryImpl entry1 = mockEntry("player1", true, false, false, Duration.ofSeconds(5));
        EntryImpl entry2 = mockEntry("player2", true, true, false, Duration.ofSeconds(3));
        EntryImpl entry3 = mockEntry("player3", false, false, false, Duration.ZERO);

        when(service.getEntries()).thenReturn(Set.of(entry1, entry2, entry3));
        when(convertor.getTimeLine(Duration.ofSeconds(5))).thenReturn("5 seconds");
        when(convertor.getTimeLine(Duration.ofSeconds(3))).thenReturn("3 seconds");

        getAllCommand.execute(issuer, new String[]{});

        verify(issuer).sendMessage("List of entries:");
        verify(issuer).sendMessage("Nickname: player1, Status: Active for 5 seconds");
        verify(issuer).sendMessage("Nickname: player2, Status: Frozen for 3 seconds");
        verify(issuer).sendMessage("Nickname: player3, Status: Expired");
        verify(issuer).sendMessage("Page 1 of 1");
    }

    @Test
    public void testExecute_WithPageArgument_ShouldDisplayCorrectPageEntries() {
        List<EntryImpl> entries = List.of(
                mockEntry("player1", true, false, false, Duration.ofSeconds(5)),
                mockEntry("player2", true, false, true, Duration.ZERO),
                mockEntry("player3", false, false, false, Duration.ZERO),
                mockEntry("player4", true, false, false, Duration.ofSeconds(7)),
                mockEntry("player5", true, false, false, Duration.ofSeconds(2)),
                mockEntry("player6", true, false, false, Duration.ofSeconds(6))
        );

        when(service.getEntries()).thenReturn(Set.copyOf(entries));
        when(messages.getEntriesForPage()).thenReturn(3);
        when(convertor.getTimeLine(Duration.ofSeconds(5))).thenReturn("5 seconds");
        when(convertor.getTimeLine(Duration.ofSeconds(7))).thenReturn("7 seconds");
        when(convertor.getTimeLine(Duration.ofSeconds(2))).thenReturn("2 seconds");
        when(convertor.getTimeLine(Duration.ofSeconds(6))).thenReturn("6 seconds");

        getAllCommand.execute(issuer, new String[]{"2"});

        verify(issuer).sendMessage("List of entries:");
        verify(issuer).sendMessage("Nickname: player4, Status: Active for 7 seconds");
        verify(issuer).sendMessage("Nickname: player5, Status: Active for 2 seconds");
        verify(issuer).sendMessage("Nickname: player6, Status: Active for 6 seconds");
        verify(issuer).sendMessage("Page 2 of 2");
    }

    @Test
    public void testExecute_WithInvalidPageNumber_ShouldSendPageNotExistsMessage() {
        List<EntryImpl> entries = List.of(
                mockEntry("player1", true, false, false, Duration.ofSeconds(5)),
                mockEntry("player2", true, false, true, Duration.ZERO)
        );

        when(service.getEntries()).thenReturn(Set.copyOf(entries));
        when(messages.getEntriesForPage()).thenReturn(1);

        getAllCommand.execute(issuer, new String[]{"5"});

        verify(issuer).sendMessage("Page 5 does not exist. Max page is 2");
        verifyNoMoreInteractions(issuer);
    }

    @Test
    public void testExecute_WithInvalidPageArgument_ShouldSendIncorrectArgumentsMessage() {
        getAllCommand.execute(issuer, new String[]{"abc"});

        verify(issuer).sendMessage("Incorrect arguments provided.");
        verifyNoMoreInteractions(issuer);
    }

    @Test
    public void testTabulate_WithNoArgs_ShouldReturnPagesUpTo20() {
        Set<String> tabulationResult = getAllCommand.getTabulate(issuer, new String[]{});

        assertEquals(20, tabulationResult.size());
        for (int i = 1; i <= 20; i++) {
            assert(tabulationResult.contains(String.valueOf(i)));
        }
    }

    @Test
    public void testTabulate_WithArgs_ShouldReturnEmptySet() {
        Set<String> tabulationResult = getAllCommand.getTabulate(issuer, new String[]{"1"});

        assertEquals(Set.of(), tabulationResult);
    }

    private EntryImpl mockEntry(String nickname, boolean isActive, boolean isFrozen, boolean isForever, Duration left) {
        EntryImpl entry = mock(EntryImpl.class);
        when(entry.getNickname()).thenReturn(nickname);
        when(entry.isActive()).thenReturn(isActive);
        when(entry.isFreezeActive()).thenReturn(isFrozen);
        when(entry.isForever()).thenReturn(isForever);
        when(entry.getLeftActiveDuration()).thenReturn(left);
        when(entry.getLeftFreezeDuration()).thenReturn(left);
        return entry;
    }
}
