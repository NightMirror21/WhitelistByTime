package ru.nightmirror.wlbytime.command.commands;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import ru.nightmirror.wlbytime.config.configs.MessagesConfig;
import ru.nightmirror.wlbytime.entry.Entry;
import ru.nightmirror.wlbytime.interfaces.command.Command;
import ru.nightmirror.wlbytime.interfaces.command.CommandIssuer;
import ru.nightmirror.wlbytime.interfaces.finder.EntryFinder;
import ru.nightmirror.wlbytime.interfaces.services.EntryTimeService;
import ru.nightmirror.wlbytime.time.TimeConvertor;
import ru.nightmirror.wlbytime.time.TimeRandom;

import java.util.Optional;
import java.util.Set;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class TimeCommand implements Command {

    static Set<String> OPERATIONS = Set.of("add", "remove", "set");

    MessagesConfig messages;
    EntryFinder finder;
    TimeConvertor convertor;
    TimeRandom timeRandom;
    EntryTimeService timeService;

    @Override
    public String getPermission() {
        return "wlbytime.time";
    }

    @Override
    public String getName() {
        return "time";
    }

    @Override
    public void execute(CommandIssuer issuer, String[] args) {
        if (args.length < 3) {
            issuer.sendMessage(messages.getIncorrectArguments());
            return;
        }

        String operation = args[0].toLowerCase();
        String nickname = args[1];
        StringBuilder timeArgument = new StringBuilder();
        for (int i = 2; i < args.length; i++) {
            timeArgument.append(args[i]);
        }

        Optional<Entry> entry = finder.find(nickname);
        if (entry.isEmpty()) {
            issuer.sendMessage(messages.getPlayerNotInWhitelist()
                    .replaceAll("%nickname%", nickname));
            return;
        }

        if (!OPERATIONS.contains(operation)) {
            issuer.sendMessage(messages.getIncorrectArguments());
            return;
        }

        long timeInMillis = convertor.getTimeMs(timeArgument.toString());
        if (timeInMillis <= 0) {
            issuer.sendMessage(messages.getTimeIsIncorrect());
            return;
        }
        String timeAsString = convertor.getTimeLine(timeInMillis);

        if (operation.equals("add")) {
            if (timeService.canAdd(entry.get(), timeInMillis)) {
                timeService.add(entry.get(), timeInMillis);
                issuer.sendMessage(messages.getAddTime()
                        .replaceAll("%nickname%", nickname)
                        .replaceAll("%time%", timeAsString));
            } else {
                issuer.sendMessage(messages.getCantAddTime());
            }
        } else if (operation.equals("remove")) {
            if (timeService.canRemove(entry.get(), timeInMillis)) {
                timeService.remove(entry.get(), timeInMillis);
                issuer.sendMessage(messages.getRemoveTime()
                        .replaceAll("%nickname%", nickname)
                        .replaceAll("%time%", timeAsString));
            } else {
                issuer.sendMessage(messages.getCantRemoveTime());
            }
        } else if (operation.equals("set")) {
            timeService.set(entry.get(), timeInMillis);
            issuer.sendMessage(messages.getSetTime()
                    .replaceAll("%nickname%", nickname)
                    .replaceAll("%time%", timeAsString));
        }
    }

    @Override
    public Set<String> getTabulate(CommandIssuer issuer, String[] args) {
        if (args.length == 0) {
            return OPERATIONS;
        }

        if (!OPERATIONS.contains(args[0].toLowerCase())) {
            return Set.of();
        }

        if (args.length == 1) {
            return Set.of(issuer.getNickname());
        }

        return Set.of(timeRandom.getRandomOneTime());
    }
}
