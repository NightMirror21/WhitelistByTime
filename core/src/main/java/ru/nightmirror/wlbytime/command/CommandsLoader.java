package ru.nightmirror.wlbytime.command;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.Unmodifiable;
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

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CommandsLoader {

    Reloadable reloadable;
    CommandsConfig commandsConfig;
    MessagesConfig messages;
    SettingsConfig settings;
    Path settingsPath;
    EntryFinder entryFinder;
    PlayerIdentityResolver identityResolver;
    EntryIdentityService identityService;
    TimeConvertor convertor;
    EntryService entryService;
    TimeRandom random;
    EntryTimeService entryTimeService;

    public @Unmodifiable Set<Command> load() {
        return Set.of(
                new AddCommand(commandsConfig, messages, convertor, entryService, random, identityResolver, identityService),
                new CheckCommand(commandsConfig, messages, convertor, identityResolver, identityService),
                new CheckMeCommand(commandsConfig, messages, convertor, identityResolver, identityService),
                new FreezeCommand(commandsConfig, messages, convertor, random, entryService, identityResolver, identityService),
                new GetAllCommand(commandsConfig, messages, entryService, convertor),
                new OffCommand(commandsConfig, messages, settings, settingsPath),
                new OnCommand(commandsConfig, messages, settings, settingsPath),
                new RemoveCommand(commandsConfig, messages, entryService, identityResolver, identityService),
                new StatusCommand(commandsConfig, messages, settings),
                new TimeCommand(commandsConfig, messages, convertor, random, entryService, entryTimeService, identityResolver, identityService),
                new ReloadCommand(messages, commandsConfig, reloadable),
                new UnfreezeCommand(commandsConfig, messages, entryFinder, entryService)
        );
    }
}
