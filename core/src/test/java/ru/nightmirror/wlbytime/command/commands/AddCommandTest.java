package ru.nightmirror.wlbytime.command.commands;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import ru.nightmirror.wlbytime.config.configs.CommandsConfig;
import ru.nightmirror.wlbytime.config.configs.MessagesConfig;
import ru.nightmirror.wlbytime.entry.EntryImpl;
import ru.nightmirror.wlbytime.identity.PlayerKey;
import ru.nightmirror.wlbytime.identity.ResolvedPlayer;
import ru.nightmirror.wlbytime.interfaces.command.CommandIssuer;
import ru.nightmirror.wlbytime.interfaces.identity.PlayerIdentityResolver;
import ru.nightmirror.wlbytime.interfaces.services.EntryIdentityService;
import ru.nightmirror.wlbytime.interfaces.services.EntryService;
import ru.nightmirror.wlbytime.time.TimeConvertor;
import ru.nightmirror.wlbytime.time.TimeRandom;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class AddCommandTest {

    private CommandsConfig commandsConfig;
    private AddCommand addCommand;
    private MessagesConfig messages;
    private TimeConvertor convertor;
    private EntryService service;
    private TimeRandom random;
    private CommandIssuer issuer;
    private PlayerIdentityResolver identityResolver;
    private EntryIdentityService identityService;

    @BeforeEach
    public void setUp() {
        commandsConfig = mock(CommandsConfig.class);
        messages = mock(MessagesConfig.class);
        convertor = mock(TimeConvertor.class);
        service = mock(EntryService.class);
        random = mock(TimeRandom.class);
        issuer = mock(CommandIssuer.class);
        identityResolver = mock(PlayerIdentityResolver.class);
        identityService = mock(EntryIdentityService.class);

        addCommand = new AddCommand(commandsConfig, messages, convertor, service, random, identityResolver, identityService);
    }

    @Test
    public void getPermissionReturnsCorrectPermission() {
        when(commandsConfig.getAddPermission()).thenReturn("wlbytime.add");
        assertEquals("wlbytime.add", addCommand.getPermission());
    }

    @Test
    public void getNameReturnsCorrectName() {
        assertEquals("add", addCommand.getName());
    }

    @Test
    public void executeNoArgumentsSendsIncorrectArgumentsMessage() {
        when(messages.getIncorrectArguments()).thenReturn("Incorrect arguments!");

        addCommand.execute(issuer, new String[]{});

        verify(issuer).sendMessage("Incorrect arguments!");
    }

    @Test
    public void executePlayerAlreadyInWhitelistSendsAlreadyInWhitelistMessage() {
        String nickname = "existingPlayer";
        when(identityResolver.resolveByNickname(nickname))
                .thenReturn(new ResolvedPlayer(PlayerKey.nickname(nickname), nickname, null));
        when(identityService.findOrMigrate(any(), anyString()))
                .thenReturn(Optional.of(EntryImpl.builder().build()));
        when(messages.getPlayerAlreadyInWhitelist()).thenReturn("%nickname% is already in the whitelist!");

        addCommand.execute(issuer, new String[]{nickname});

        verify(issuer).sendMessage("existingPlayer is already in the whitelist!");
    }

    @Test
    public void executeValidNicknameAddsPlayerWithoutTime() {
        String nickname = "newPlayer";
        when(identityResolver.resolveByNickname(nickname))
                .thenReturn(new ResolvedPlayer(PlayerKey.nickname(nickname), nickname, null));
        when(identityService.findOrMigrate(any(), anyString()))
                .thenReturn(Optional.empty());
        when(messages.getSuccessfullyAdded()).thenReturn("Player %nickname% successfully added!");

        addCommand.execute(issuer, new String[]{nickname});

        verify(service).create(nickname);
        verify(issuer).sendMessage("Player newPlayer successfully added!");
    }

    @Test
    public void executeNicknameAndTimeAddsPlayerWithTime() {
        String nickname = "timedPlayer";
        String timeArgument = "1d 2h";
        when(identityResolver.resolveByNickname(nickname))
                .thenReturn(new ResolvedPlayer(PlayerKey.nickname(nickname), nickname, null));
        when(identityService.findOrMigrate(any(), anyString()))
                .thenReturn(Optional.empty());
        Duration duration = Duration.ofHours(25);
        when(convertor.getTime(timeArgument)).thenReturn(duration);
        when(convertor.getTimeLine(duration)).thenReturn("1 day 1 hour");
        when(messages.getSuccessfullyAddedForTime()).thenReturn("Player %nickname% added for %time%!");

        addCommand.execute(issuer, new String[]{nickname, timeArgument});

        ArgumentCaptor<Instant> timeCaptor = ArgumentCaptor.forClass(Instant.class);
        verify(service).create(eq(nickname), timeCaptor.capture());
        verify(issuer).sendMessage("Player timedPlayer added for 1 day 1 hour!");

        assertTrue(timeCaptor.getValue().isAfter(Instant.now().minus(duration)));
    }

    @Test
    public void executeMultipleArgumentsForTimeConcatenatesAndConvertsTime() {
        String nickname = "concatPlayer";
        String[] args = {"concatPlayer", "1d", "2h"};

        Instant fixedNow = Instant.now();
        Duration duration = Duration.ofHours(26);
        Instant expectedTime = fixedNow.plus(duration);

        when(identityResolver.resolveByNickname(nickname))
                .thenReturn(new ResolvedPlayer(PlayerKey.nickname(nickname), nickname, null));
        when(identityService.findOrMigrate(any(), anyString()))
                .thenReturn(Optional.empty());
        when(convertor.getTime("1d 2h")).thenReturn(duration);
        when(convertor.getTimeLine(duration)).thenReturn("1 day 2 hours");
        when(messages.getSuccessfullyAddedForTime()).thenReturn("Player %nickname% added for %time%!");

        addCommand.execute(issuer, args);

        ArgumentCaptor<Instant> instantCaptor = ArgumentCaptor.forClass(Instant.class);
        verify(service).create(eq(nickname), instantCaptor.capture());

        Instant actualTime = instantCaptor.getValue();
        long allowedDifference = 100L;

        assertTrue(Math.abs(actualTime.toEpochMilli() - expectedTime.toEpochMilli()) < allowedDifference);
        verify(issuer).sendMessage("Player concatPlayer added for 1 day 2 hours!");
    }

    @Test
    public void getTabulateNoArgsReturnsIssuerNickname() {
        when(issuer.getNickname()).thenReturn("issuerNickname");

        Set<String> tabulate = addCommand.getTabulate(issuer, new String[]{});

        assertEquals(Set.of("issuerNickname"), tabulate);
    }

    @Test
    public void getTabulateWithArgsReturnsRandomTime() {
        when(random.getRandomOneTime()).thenReturn("1h");
        when(random.getTimes()).thenReturn(Set.of("1h", "2h", "3h"));

        Set<String> tabulate = addCommand.getTabulate(issuer, new String[]{"someArg"});

        assertEquals(Set.of("1h", "2h", "3h"), tabulate);
    }

    @Test
    public void executePlayerWithoutTimeSendsSuccessMessage() {
        String nickname = "noTimePlayer";
        when(identityResolver.resolveByNickname(nickname))
                .thenReturn(new ResolvedPlayer(PlayerKey.nickname(nickname), nickname, null));
        when(identityService.findOrMigrate(any(), anyString()))
                .thenReturn(Optional.empty());
        when(messages.getSuccessfullyAdded()).thenReturn("Player %nickname% successfully added!");

        addCommand.execute(issuer, new String[]{nickname});

        verify(service).create(nickname);
        verify(issuer).sendMessage("Player noTimePlayer successfully added!");
    }

    @Test
    public void executePlayerWithTimeSendsFormattedSuccessMessage() {
        String nickname = "timePlayer";
        String timeArgument = "3d";

        Instant fixedNow = Instant.now();
        Duration duration = Duration.ofDays(3);
        Instant expectedTime = fixedNow.plus(duration);

        when(identityResolver.resolveByNickname(nickname))
                .thenReturn(new ResolvedPlayer(PlayerKey.nickname(nickname), nickname, null));
        when(identityService.findOrMigrate(any(), anyString()))
                .thenReturn(Optional.empty());
        when(convertor.getTime(timeArgument)).thenReturn(duration);
        when(convertor.getTimeLine(duration)).thenReturn("3 days");
        when(messages.getSuccessfullyAddedForTime()).thenReturn("Player %nickname% added for %time%!");

        addCommand.execute(issuer, new String[]{nickname, timeArgument});

        ArgumentCaptor<Instant> instantCaptor = ArgumentCaptor.forClass(Instant.class);
        verify(service).create(eq(nickname), instantCaptor.capture());

        Instant actualTime = instantCaptor.getValue();
        assertEquals(expectedTime.getEpochSecond(), actualTime.getEpochSecond());

        verify(issuer).sendMessage("Player timePlayer added for 3 days!");
    }
}
