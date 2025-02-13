package ru.nightmirror.wlbytime.impl.parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.nightmirror.wlbytime.config.configs.PlaceholdersConfig;
import ru.nightmirror.wlbytime.entry.EntryImpl;
import ru.nightmirror.wlbytime.entry.Expiration;
import ru.nightmirror.wlbytime.entry.Freezing;
import ru.nightmirror.wlbytime.interfaces.finder.EntryFinder;
import ru.nightmirror.wlbytime.time.TimeConvertor;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PlaceholderParserImplTest {

    private EntryFinder entryFinder;
    private TimeConvertor timeConvertor;
    private PlaceholdersConfig placeholdersConfig;
    private PlaceholderParserImpl parser;

    @BeforeEach
    public void setUp() {
        entryFinder = mock(EntryFinder.class);
        timeConvertor = mock(TimeConvertor.class);
        placeholdersConfig = mock(PlaceholdersConfig.class);
        parser = new PlaceholderParserImpl(entryFinder, timeConvertor, placeholdersConfig);
    }

    @Test
    public void parsePlayerNotFoundReturnsInWhitelistFalse() {
        String player = "testPlayer";
        String inWhitelistFalse = "false";

        when(entryFinder.find(player)).thenReturn(Optional.empty());
        when(placeholdersConfig.getInWhitelistFalse()).thenReturn(inWhitelistFalse);

        String result = parser.parse(player, "in_whitelist");

        assertEquals(inWhitelistFalse, result);
    }

    @Test
    public void parseInWhitelistActiveNotFrozenReturnsTrue() {
        String player = "testPlayer";
        String inWhitelistTrue = "true";
        Expiration expiration = new Expiration(1L, Instant.now().plus(Duration.ofHours(1)));
        EntryImpl entry = EntryImpl.builder().expiration(expiration).build();

        when(entryFinder.find(player)).thenReturn(Optional.of(entry));
        when(placeholdersConfig.getInWhitelistTrue()).thenReturn(inWhitelistTrue);

        String result = parser.parse(player, "in_whitelist");

        assertEquals(inWhitelistTrue, result);
    }

    @Test
    public void parseInWhitelistActiveFrozenReturnsFrozen() {
        String player = "testPlayer";
        String frozenMsg = "frozen";
        Expiration expiration = new Expiration(1L, Instant.now().plus(Duration.ofHours(1)));
        Freezing freezing = new Freezing(1L, Duration.ofSeconds(10));
        EntryImpl entry = EntryImpl.builder().expiration(expiration).freezing(freezing).build();

        when(entryFinder.find(player)).thenReturn(Optional.of(entry));
        when(placeholdersConfig.getFrozen()).thenReturn(frozenMsg);

        String result = parser.parse(player, "in_whitelist");

        assertEquals(frozenMsg, result);
    }

    @Test
    public void parseInWhitelistInactiveReturnsFalse() {
        String player = "testPlayer";
        String inWhitelistFalse = "false";
        Expiration expiration = new Expiration(1L, Instant.now().minus(Duration.ofSeconds(1)));
        EntryImpl entry = EntryImpl.builder().expiration(expiration).build();

        when(entryFinder.find(player)).thenReturn(Optional.of(entry));
        when(placeholdersConfig.getInWhitelistFalse()).thenReturn(inWhitelistFalse);

        String result = parser.parse(player, "in_whitelist");

        assertEquals(inWhitelistFalse, result);
    }

    @Test
    public void parseTimeLeftFrozenReturnsTimeLeftWithFreeze() {
        String player = "testPlayer";
        String timeLeftWithFreeze = "Frozen Time: %time%";
        String mockedTime = "10 seconds";
        Expiration expiration = new Expiration(1L, Instant.now().plus(Duration.ofHours(1)));
        Freezing freezing = new Freezing(1L, Duration.ofSeconds(10));
        EntryImpl entry = EntryImpl.builder().expiration(expiration).freezing(freezing).build();

        when(entryFinder.find(player)).thenReturn(Optional.of(entry));
        when(timeConvertor.getTimeLine(any(Duration.class))).thenReturn(mockedTime);
        when(placeholdersConfig.getTimeLeftWithFreeze()).thenReturn(timeLeftWithFreeze);

        String expected = timeLeftWithFreeze.replace("%time%", mockedTime);
        String result = parser.parse(player, "time_left");

        assertEquals(expected, result);
    }

    @Test
    public void parseTimeLeftActiveNotFrozenReturnsTimeLeft() {
        String player = "testPlayer";
        String timeLeft = "Time: %time%";
        String mockedTime = "10 seconds";
        Expiration expiration = new Expiration(1L, Instant.now().plus(Duration.ofHours(1)));
        EntryImpl entry = EntryImpl.builder().expiration(expiration).build();

        when(entryFinder.find(player)).thenReturn(Optional.of(entry));
        when(timeConvertor.getTimeLine(any(Duration.class))).thenReturn(mockedTime);
        when(placeholdersConfig.getTimeLeft()).thenReturn(timeLeft);

        String expected = timeLeft.replace("%time%", mockedTime);
        String result = parser.parse(player, "time_left");

        assertEquals(expected, result);
    }

    @Test
    public void parseTimeLeftInactiveReturnsEmpty() {
        String player = "testPlayer";
        Expiration expiration = new Expiration(1L, Instant.now().minus(Duration.ofSeconds(1)));
        EntryImpl entry = EntryImpl.builder().expiration(expiration).build();

        when(entryFinder.find(player)).thenReturn(Optional.of(entry));

        String result = parser.parse(player, "time_left");

        assertTrue(result.isEmpty());
    }

    @Test
    public void parseUnknownParamReturnsEmpty() {
        String player = "testPlayer";
        Expiration expiration = new Expiration(1L, Instant.now().plus(Duration.ofHours(1)));
        EntryImpl entry = EntryImpl.builder().expiration(expiration).build();

        when(entryFinder.find(player)).thenReturn(Optional.of(entry));

        String result = parser.parse(player, "unknown_param");

        assertTrue(result.isEmpty());
    }

    @Test
    public void parseTimeLeftForeverReturnsForever() {
        String player = "testPlayer";
        String timeLeft = "Time: %time%";
        String mockedTime = "Forever";
        EntryImpl entry = EntryImpl.builder().expiration(null).build();

        when(entryFinder.find(player)).thenReturn(Optional.of(entry));
        when(timeConvertor.getTimeLine(any(Duration.class))).thenReturn(mockedTime);
        when(placeholdersConfig.getTimeLeft()).thenReturn(timeLeft);

        String expected = timeLeft.replace("%time%", mockedTime);
        String result = parser.parse(player, "time_left");

        assertEquals(expected, result);
    }

    @Test
    public void parseTimeLeftForeverFrozenReturnsTimeLeftWithFreeze() {
        String player = "testPlayer";
        String timeLeftWithFreeze = "Frozen Time: %time%";
        String mockedTime = "10 seconds";
        Freezing freezing = new Freezing(1L, Duration.ofSeconds(10));
        EntryImpl entry = EntryImpl.builder().expiration(null).freezing(freezing).build();

        when(entryFinder.find(player)).thenReturn(Optional.of(entry));
        when(timeConvertor.getTimeLine(any(Duration.class))).thenReturn(mockedTime);
        when(placeholdersConfig.getTimeLeftWithFreeze()).thenReturn(timeLeftWithFreeze);

        String expected = timeLeftWithFreeze.replace("%time%", mockedTime);
        String result = parser.parse(player, "time_left");

        assertEquals(expected, result);
    }
}
