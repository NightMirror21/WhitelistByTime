package ru.nightmirror.wlbytime.impl.parser;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.nightmirror.wlbytime.config.configs.PlaceholdersConfig;
import ru.nightmirror.wlbytime.entry.Entry;
import ru.nightmirror.wlbytime.entry.Expiration;
import ru.nightmirror.wlbytime.entry.Freezing;
import ru.nightmirror.wlbytime.interfaces.finder.EntryFinder;
import ru.nightmirror.wlbytime.interfaces.parser.PlaceholderParserImpl;
import ru.nightmirror.wlbytime.time.TimeConvertor;

import java.sql.Timestamp;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PlaceholderParserImplTest {

    @Mock
    private EntryFinder entryFinder;

    @Mock
    private TimeConvertor timeConvertor;

    @Mock
    private PlaceholdersConfig placeholdersConfig;

    @InjectMocks
    private PlaceholderParserImpl parser;

    private static final String PLAYER = "testPlayer";
    private static final String IN_WHITELIST_TRUE = "true";
    private static final String IN_WHITELIST_FALSE = "false";
    private static final String FROZEN_MSG = "frozen";
    private static final String TIME_LEFT = "Time: %time%";
    private static final String TIME_LEFT_WITH_FREEZE = "Frozen Time: %time%";
    private static final String MOCKED_TIME = "10 seconds";

    @Test
    public void parse_PlayerNotFound_ReturnsInWhitelistFalse() {
        when(entryFinder.find(PLAYER)).thenReturn(Optional.empty());
        when(placeholdersConfig.getInWhitelistFalse()).thenReturn(IN_WHITELIST_FALSE);

        String result = parser.parse(PLAYER, "in_whitelist");

        assertThat(result).isEqualTo(IN_WHITELIST_FALSE);
    }

    @Test
    public void parse_InWhitelistParam_ActiveNotFrozen_ReturnsTrue() {
        Expiration expiration = new Expiration(1L, new Timestamp(System.currentTimeMillis() + 3600000));
        Entry entry = Entry.builder().expiration(expiration).freezing(null).build();

        when(entryFinder.find(PLAYER)).thenReturn(Optional.of(entry));
        when(placeholdersConfig.getInWhitelistTrue()).thenReturn(IN_WHITELIST_TRUE);

        String result = parser.parse(PLAYER, "in_whitelist");

        assertThat(result).isEqualTo(IN_WHITELIST_TRUE);
    }

    @Test
    public void parse_InWhitelistParam_ActiveFrozen_ReturnsFrozen() {
        Expiration expiration = new Expiration(1L, new Timestamp(System.currentTimeMillis() + 3600000));
        Freezing freezing = new Freezing(1L, 10000);
        Entry entry = Entry.builder().expiration(expiration).freezing(freezing).build();

        when(entryFinder.find(PLAYER)).thenReturn(Optional.of(entry));
        when(placeholdersConfig.getFrozen()).thenReturn(FROZEN_MSG);

        String result = parser.parse(PLAYER, "in_whitelist");

        assertThat(result).isEqualTo(FROZEN_MSG);
    }

    @Test
    public void parse_InWhitelistParam_Inactive_ReturnsFalse() {
        Expiration expiration = new Expiration(1L, new Timestamp(System.currentTimeMillis() - 1000));
        Entry entry = Entry.builder().expiration(expiration).freezing(null).build();

        when(entryFinder.find(PLAYER)).thenReturn(Optional.of(entry));
        when(placeholdersConfig.getInWhitelistFalse()).thenReturn(IN_WHITELIST_FALSE);

        String result = parser.parse(PLAYER, "in_whitelist");

        assertThat(result).isEqualTo(IN_WHITELIST_FALSE);
    }

    @Test
    public void parse_TimeLeftParam_Frozen_ReturnsTimeLeftWithFreeze() {
        Expiration expiration = new Expiration(1L, new Timestamp(System.currentTimeMillis() + 3600000));
        Freezing freezing = new Freezing(1L, 10000);
        Entry entry = Entry.builder().expiration(expiration).freezing(freezing).build();

        when(entryFinder.find(PLAYER)).thenReturn(Optional.of(entry));
        when(timeConvertor.getTimeLine(anyLong())).thenReturn(MOCKED_TIME);
        when(placeholdersConfig.getTimeLeftWithFreeze()).thenReturn(TIME_LEFT_WITH_FREEZE);

        String expected = TIME_LEFT_WITH_FREEZE.replace("%time%", MOCKED_TIME);
        String result = parser.parse(PLAYER, "time_left");

        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void parse_TimeLeftParam_ActiveNotFrozen_ReturnsTimeLeft() {
        Expiration expiration = new Expiration(1L, new Timestamp(System.currentTimeMillis() + 3600000));
        Entry entry = Entry.builder().expiration(expiration).freezing(null).build();

        when(entryFinder.find(PLAYER)).thenReturn(Optional.of(entry));
        when(timeConvertor.getTimeLine(anyLong())).thenReturn(MOCKED_TIME);
        when(placeholdersConfig.getTimeLeft()).thenReturn(TIME_LEFT);

        String expected = TIME_LEFT.replace("%time%", MOCKED_TIME);
        String result = parser.parse(PLAYER, "time_left");

        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void parse_TimeLeftParam_Inactive_ReturnsEmpty() {
        Expiration expiration = new Expiration(1L, new Timestamp(System.currentTimeMillis() - 1000));
        Entry entry = Entry.builder().expiration(expiration).freezing(null).build();

        when(entryFinder.find(PLAYER)).thenReturn(Optional.of(entry));

        String result = parser.parse(PLAYER, "time_left");

        assertThat(result).isEmpty();
    }

    @Test
    public void parse_UnknownParam_ReturnsEmpty() {
        Expiration expiration = new Expiration(1L, new Timestamp(System.currentTimeMillis() + 3600000));
        Entry entry = Entry.builder().expiration(expiration).build();

        when(entryFinder.find(PLAYER)).thenReturn(Optional.of(entry));

        String result = parser.parse(PLAYER, "unknown_param");

        assertThat(result).isEmpty();
    }

    @Test
    void parse_TimeLeftParam_Forever_ReturnsEmpty() {
        Entry entry = Entry.builder().expiration(null).build();

        when(entryFinder.find(PLAYER)).thenReturn(Optional.of(entry));

        String result = parser.parse(PLAYER, "time_left");

        assertThat(result).isEmpty();
    }

    @Test
    public void parse_TimeLeftParam_ForeverFrozen_ReturnsTimeLeftWithFreeze() {
        Freezing freezing = new Freezing(1L, 10000);
        Entry entry = Entry.builder().expiration(null).freezing(freezing).build();

        when(entryFinder.find(PLAYER)).thenReturn(Optional.of(entry));
        when(timeConvertor.getTimeLine(anyLong())).thenReturn(MOCKED_TIME);
        when(placeholdersConfig.getTimeLeftWithFreeze()).thenReturn(TIME_LEFT_WITH_FREEZE);

        String expected = TIME_LEFT_WITH_FREEZE.replace("%time%", MOCKED_TIME);
        String result = parser.parse(PLAYER, "time_left");

        assertThat(result).isEqualTo(expected);
    }
}
