package ru.nightmirror.wlbytime.command;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.nightmirror.wlbytime.command.commands.*;
import ru.nightmirror.wlbytime.config.configs.CommandsConfig;
import ru.nightmirror.wlbytime.config.configs.MessagesConfig;
import ru.nightmirror.wlbytime.config.configs.SettingsConfig;
import ru.nightmirror.wlbytime.interfaces.command.Command;
import ru.nightmirror.wlbytime.interfaces.finder.EntryFinder;
import ru.nightmirror.wlbytime.interfaces.identity.PlayerIdentityResolver;
import ru.nightmirror.wlbytime.interfaces.plugin.Reloadable;
import ru.nightmirror.wlbytime.interfaces.services.EntryIdentityService;
import ru.nightmirror.wlbytime.interfaces.services.EntryService;
import ru.nightmirror.wlbytime.interfaces.services.EntryTimeService;
import ru.nightmirror.wlbytime.time.TimeConvertor;
import ru.nightmirror.wlbytime.time.TimeRandom;

import java.nio.file.Path;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class CommandsLoaderTest {

    private CommandsConfig commandsConfig;
    private MessagesConfig messages;
    private SettingsConfig settings;
    private Path settingsPath;
    private EntryFinder entryFinder;
    private PlayerIdentityResolver identityResolver;
    private EntryIdentityService identityService;
    private TimeConvertor convertor;
    private EntryService entryService;
    private TimeRandom random;
    private EntryTimeService entryTimeService;
    private Reloadable reloadable;

    private CommandsLoader commandsLoader;

    @BeforeEach
    public void setUp() {
        commandsConfig = mock(CommandsConfig.class);
        messages = mock(MessagesConfig.class);
        settings = mock(SettingsConfig.class);
        settingsPath = Path.of("settings.yml");
        entryFinder = mock(EntryFinder.class);
        identityResolver = mock(PlayerIdentityResolver.class);
        identityService = mock(EntryIdentityService.class);
        convertor = mock(TimeConvertor.class);
        entryService = mock(EntryService.class);
        random = mock(TimeRandom.class);
        entryTimeService = mock(EntryTimeService.class);
        reloadable = mock(Reloadable.class);

        commandsLoader = new CommandsLoader(
                reloadable,
                commandsConfig,
                messages,
                settings,
                settingsPath,
                entryFinder,
                identityResolver,
                identityService,
                convertor,
                entryService,
                random,
                entryTimeService
        );
    }

    @Test
    public void shouldLoadAllCommands() {
        Set<Command> commands = commandsLoader.load();

        assertThat(commands).hasSize(12);
        assertThat(commands).hasAtLeastOneElementOfType(AddCommand.class);
        assertThat(commands).hasAtLeastOneElementOfType(CheckCommand.class);
        assertThat(commands).hasAtLeastOneElementOfType(CheckMeCommand.class);
        assertThat(commands).hasAtLeastOneElementOfType(FreezeCommand.class);
        assertThat(commands).hasAtLeastOneElementOfType(UnfreezeCommand.class);
        assertThat(commands).hasAtLeastOneElementOfType(GetAllCommand.class);
        assertThat(commands).hasAtLeastOneElementOfType(OffCommand.class);
        assertThat(commands).hasAtLeastOneElementOfType(OnCommand.class);
        assertThat(commands).hasAtLeastOneElementOfType(RemoveCommand.class);
        assertThat(commands).hasAtLeastOneElementOfType(StatusCommand.class);
        assertThat(commands).hasAtLeastOneElementOfType(TimeCommand.class);
        assertThat(commands).hasAtLeastOneElementOfType(ReloadCommand.class);
    }
}
