package ru.nightmirror.wlbytime.command;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.Unmodifiable;
import ru.nightmirror.wlbytime.command.commands.*;
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

    MessagesConfig messages;
    EntryFinder finder;
    TimeConvertor convertor;
    EntryService entryService;
    TimeRandom random;
    EntryTimeService entryTimeService;

    public @Unmodifiable Set<Command> load() {
        return Set.of(
                new AddCommand(messages, finder, convertor, entryService, random),
                new CheckCommand(messages, finder, convertor),
                new CheckMeCommand(messages, finder, convertor),
                new FreezeCommand(messages, finder, convertor, random, entryService),
                new GetAllCommand(messages, entryService, convertor),
                new RemoveCommand(messages, finder, entryService),
                new TimeCommand(messages, finder, convertor, random, entryTimeService)
        );
    }
}
