package ru.nightmirror.wlbytime.command;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.Unmodifiable;
import ru.nightmirror.wlbytime.command.commands.*;
import ru.nightmirror.wlbytime.config.configs.CommandsConfig;
import ru.nightmirror.wlbytime.config.configs.MessagesConfig;
import ru.nightmirror.wlbytime.interfaces.command.Command;
import ru.nightmirror.wlbytime.interfaces.finder.EntryFinder;
import ru.nightmirror.wlbytime.interfaces.services.EntryService;
import ru.nightmirror.wlbytime.interfaces.services.EntryTimeService;
import ru.nightmirror.wlbytime.time.TimeConvertor;
import ru.nightmirror.wlbytime.time.TimeRandom;

import java.util.Set;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CommandsLoader {

    CommandsConfig commandsConfig;
    MessagesConfig messages;
    EntryFinder finder;
    TimeConvertor convertor;
    EntryService entryService;
    TimeRandom random;
    EntryTimeService entryTimeService;

    public @Unmodifiable Set<Command> load() {
        return Set.of(
                new AddCommand(commandsConfig, messages, finder, convertor, entryService, random),
                new CheckCommand(commandsConfig, messages, finder, convertor),
                new CheckMeCommand(commandsConfig, messages, finder, convertor),
                new FreezeCommand(commandsConfig, messages, finder, convertor, random, entryService),
                new GetAllCommand(commandsConfig, messages, entryService, convertor),
                new RemoveCommand(commandsConfig, messages, finder, entryService),
                new TimeCommand(commandsConfig, messages, finder, convertor, random, entryService, entryTimeService)
        );
    }
}
