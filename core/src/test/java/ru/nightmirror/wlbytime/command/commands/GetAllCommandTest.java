package ru.nightmirror.wlbytime.command.commands;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.nightmirror.wlbytime.config.configs.MessagesConfig;
import ru.nightmirror.wlbytime.entry.Entry;
import ru.nightmirror.wlbytime.interfaces.command.CommandIssuer;
import ru.nightmirror.wlbytime.interfaces.services.EntryService;
import ru.nightmirror.wlbytime.time.TimeConvertor;

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
    }

    @Test
    public void testExecute_WhenNoEntries_ShouldSendListEmptyMessage() {
        when(service.getEntries()).thenReturn(Set.of());
        when(messages.getEntriesForPage()).thenReturn(5);
        when(messages.getListEmpty()).thenReturn("No entries found.");

        getAllCommand.execute(issuer, new String[]{});

        verify(issuer).sendMessage("No entries found.");
    }

    @Test
    public void testExecute_WithInvalidPageArgument_ShouldSendIncorrectArgumentsMessage() {
        when(messages.getIncorrectArguments()).thenReturn("Incorrect arguments provided.");

        getAllCommand.execute(issuer, new String[]{"invalid"});

        verify(issuer).sendMessage("Incorrect arguments provided.");
    }

    @Test
    public void testExecute_WithPageOutOfRange_ShouldSendPageNotExistsMessage() {
        when(service.getEntries()).thenReturn(Set.of(mock(Entry.class)));
        when(messages.getEntriesForPage()).thenReturn(1);
        when(messages.getPageNotExists()).thenReturn("Page %page% does not exist. Max page is %max-page%.");

        getAllCommand.execute(issuer, new String[]{"2"});

        verify(issuer).sendMessage("Page 2 does not exist. Max page is 1.");
    }

    @Test
    public void testExecute_WhenEntriesAvailable_ShouldSendListHeaderAndFooter() {
        Entry entry1 = createMockEntry("Player1", true, false, 1000L, false);
        Entry entry2 = createMockEntry("Player2", false, true, 1000L, false);

        when(service.getEntries()).thenReturn(Set.of(entry1, entry2));
        when(messages.getEntriesForPage()).thenReturn(2);
        when(messages.getListHeader()).thenReturn("=== Whitelist Entries ===");
        when(messages.getListFooter()).thenReturn("Page %page% of %max-page%");
        when(messages.getListElement()).thenReturn("Nickname: %nickname% Status: %time-or-status%");
        when(messages.getActive()).thenReturn("Active: %time%");
        when(messages.getFrozen()).thenReturn("Frozen: %time%");
        when(messages.getForever()).thenReturn("Forever");
        when(convertor.getTimeLine(1000L)).thenReturn("1 minute");

        getAllCommand.execute(issuer, new String[]{"1"});

        verify(issuer).sendMessage("=== Whitelist Entries ===");
        verify(issuer).sendMessage("Nickname: Player1 Status: Active: 1 minute");
        verify(issuer).sendMessage("Nickname: Player2 Status: Frozen: 1 minute");
        verify(issuer).sendMessage("Page 1 of 1");
    }

    @Test
    public void testExecute_WhenSingleEntryIsForever_ShouldDisplayForeverMessage() {
        Entry entry = createMockEntry("Player1", false, false, 0L, true);
        when(service.getEntries()).thenReturn(Set.of(entry));
        when(messages.getEntriesForPage()).thenReturn(1);
        when(messages.getListHeader()).thenReturn("=== Whitelist Entries ===");
        when(messages.getListFooter()).thenReturn("Page %page% of %max-page%");
        when(messages.getListElement()).thenReturn("Nickname: %nickname% Status: %time-or-status%");
        when(messages.getForever()).thenReturn("Forever");

        getAllCommand.execute(issuer, new String[]{});

        verify(issuer).sendMessage("=== Whitelist Entries ===");
        verify(issuer).sendMessage("Nickname: Player1 Status: Forever");
        verify(issuer).sendMessage("Page 1 of 1");
    }

    @Test
    public void testGetTabulate_WhenNoArguments_ShouldReturnPages1To20() {
        Set<String> result = getAllCommand.getTabulate(issuer, new String[]{});

        assertEquals(20, result.size());
        assertEquals(Set.of("1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
                "11", "12", "13", "14", "15", "16", "17", "18", "19", "20"), result);
    }

    @Test
    public void testGetTabulate_WhenArgumentsProvided_ShouldReturnEmptySet() {
        Set<String> result = getAllCommand.getTabulate(issuer, new String[]{"1"});

        assertEquals(Set.of(), result);
    }

    private Entry createMockEntry(String nickname, boolean active, boolean frozen, long remainingTime, boolean forever) {
        Entry entry = mock(Entry.class);
        when(entry.getNickname()).thenReturn(nickname);
        when(entry.isCurrentlyActive()).thenReturn(active);
        when(entry.isCurrentlyFrozen()).thenReturn(frozen);
        when(entry.getRemainingActiveTime()).thenReturn(remainingTime);
        when(entry.getRemainingFreezeTime()).thenReturn(remainingTime);
        when(entry.hasNoExpiration()).thenReturn(forever);
        return entry;
    }
}