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
        if (!areArgsValid(args, issuer)) return;

        String operation = args[0].toLowerCase();
        String nickname = args[1];
        String timeArgument = concatenateArgs(args);

        Optional<Entry> entry = finder.find(nickname);
        if (entry.isEmpty()) {
            issuer.sendMessage(messages.getPlayerNotInWhitelist().replace("%nickname%", nickname));
            return;
        }
        if (!OPERATIONS.contains(operation)) {
            issuer.sendMessage(messages.getIncorrectArguments());
            return;
        }

        long timeInMillis = convertor.getTimeMs(timeArgument);
        if (timeInMillis <= 0) {
            issuer.sendMessage(messages.getTimeIsIncorrect());
            return;
        }

        String timeAsString = convertor.getTimeLine(timeInMillis);
        processOperation(issuer, entry.get(), operation, nickname, timeInMillis, timeAsString);
    }

    private boolean areArgsValid(String[] args, CommandIssuer issuer) {
        if (args.length < 3) {
            issuer.sendMessage(messages.getIncorrectArguments());
            return false;
        }
        return true;
    }

    private String concatenateArgs(String[] args) {
        StringBuilder timeArgument = new StringBuilder();
        for (int i = 2; i < args.length; i++) {
            timeArgument.append(args[i]);
        }
        return timeArgument.toString();
    }

    private void processOperation(CommandIssuer issuer, Entry entry, String operation, String nickname, long timeInMillis, String timeAsString) {
        switch (operation) {
            case "add" -> {
                if (entry.isForever()) {
                    issuer.sendMessage(messages.getCantAddTimeCausePlayerIsForever());
                } else if (timeService.canAdd(entry, timeInMillis)) {
                    timeService.add(entry, timeInMillis);
                    issuer.sendMessage(messages.getAddTime().replace("%nickname%", nickname).replace("%time%", timeAsString));
                } else {
                    issuer.sendMessage(messages.getCantAddTime());
                }
            }
            case "remove" -> {
                if (entry.isForever()) {
                    issuer.sendMessage(messages.getCantRemoveTimeCausePlayerIsForever());
                } else if (timeService.canRemove(entry, timeInMillis)) {
                    timeService.remove(entry, timeInMillis);
                    issuer.sendMessage(messages.getRemoveTime().replace("%nickname%", nickname).replace("%time%", timeAsString));
                } else {
                    issuer.sendMessage(messages.getCantRemoveTime());
                }
            }
            case "set" -> {
                timeService.set(entry, System.currentTimeMillis() + timeInMillis);
                issuer.sendMessage(messages.getSetTime().replace("%nickname%", nickname).replace("%time%", timeAsString));
            }
            default -> issuer.sendMessage(messages.getIncorrectArguments());
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
