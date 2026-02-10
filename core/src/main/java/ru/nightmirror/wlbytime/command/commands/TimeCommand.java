package ru.nightmirror.wlbytime.command.commands;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.experimental.FieldDefaults;
import ru.nightmirror.wlbytime.config.configs.CommandsConfig;
import ru.nightmirror.wlbytime.config.configs.MessagesConfig;
import ru.nightmirror.wlbytime.entry.EntryImpl;
import ru.nightmirror.wlbytime.identity.ResolvedPlayer;
import ru.nightmirror.wlbytime.interfaces.command.Command;
import ru.nightmirror.wlbytime.interfaces.command.CommandIssuer;
import ru.nightmirror.wlbytime.interfaces.identity.PlayerIdentityResolver;
import ru.nightmirror.wlbytime.interfaces.services.EntryIdentityService;
import ru.nightmirror.wlbytime.interfaces.services.EntryService;
import ru.nightmirror.wlbytime.interfaces.services.EntryTimeService;
import ru.nightmirror.wlbytime.time.TimeConvertor;
import ru.nightmirror.wlbytime.time.TimeRandom;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Slf4j
public class TimeCommand implements Command {

    static Set<String> OPERATIONS = Set.of("add", "remove", "set");

    CommandsConfig commandsConfig;
    MessagesConfig messages;
    TimeConvertor convertor;
    TimeRandom timeRandom;
    EntryService entryService;
    EntryTimeService timeService;
    PlayerIdentityResolver identityResolver;
    EntryIdentityService identityService;

    @Override
    public Set<String> getPermissions() {
        return commandsConfig.getTimePermission();
    }

    @Override
    public String getName() {
        return "time";
    }

    @Override
    public void execute(CommandIssuer issuer, String[] args) {
        if (!areArgsValid(args, issuer)) return;

        String operation = args[0].toLowerCase();
        if (!OPERATIONS.contains(operation)) {
            issuer.sendMessage(messages.getIncorrectArguments());
            return;
        }

        String nickname = args[1];
        String timeArgument = concatenateArgs(args);
        Duration duration = convertor.getTime(timeArgument);
        if (duration.isNegative() || duration.isZero()) {
            issuer.sendMessage(messages.getTimeIsIncorrect());
            log.info("TimeCommand: invalid time '{}'", timeArgument);
            return;
        }

        String timeAsString = convertor.getTimeLine(duration);

        ResolvedPlayer resolved = identityResolver.resolveByNickname(nickname);
        Optional<EntryImpl> entry = identityService.findOrMigrate(resolved, nickname);
        processOperation(issuer, entry, operation, nickname, duration, timeAsString, resolved);
    }

    private boolean areArgsValid(String[] args, CommandIssuer issuer) {
        if (args.length < 3) {
            issuer.sendMessage(messages.getIncorrectArguments());
            log.info("TimeCommand: insufficient args");
            return false;
        }
        return true;
    }

    private String concatenateArgs(String[] args) {
        StringBuilder timeArgument = new StringBuilder();
        for (int i = 2; i < args.length; i++) {
            timeArgument.append(args[i]).append(" ");
        }
        return timeArgument.toString().trim();
    }

    private void processOperation(CommandIssuer issuer, Optional<EntryImpl> entry, String operation, String nickname, Duration duration, String timeAsString, ResolvedPlayer resolved) {
        if (entry.isEmpty()) {
            if (!operation.equals("add")) {
                issuer.sendMessage(messages.getPlayerNotInWhitelist().replace("%nickname%", nickname));
                return;
            }

            if (resolved.uuid() != null) {
                entryService.create(resolved.nickname(), resolved.uuid().toString(), Instant.now().plus(duration));
            } else {
                entryService.create(nickname, Instant.now().plus(duration));
            }
            issuer.sendMessage(messages.getSuccessfullyAddedForTime()
                    .replace("%nickname%", nickname)
                    .replace("%time%", timeAsString));
            return;
        }

        switch (operation) {
            case "add" -> {
                if (entry.get().isForever()) {
                    issuer.sendMessage(messages.getCantAddTimeCausePlayerIsForever());
                } else if (timeService.canAdd(entry.get(), duration)) {
                    timeService.add(entry.get(), duration);
                    issuer.sendMessage(messages.getAddTime().replace("%nickname%", nickname).replace("%time%", timeAsString));
                } else {
                    issuer.sendMessage(messages.getCantAddTime());
                }
            }
            case "remove" -> {
                if (entry.get().isForever()) {
                    issuer.sendMessage(messages.getCantRemoveTimeCausePlayerIsForever());
                } else if (timeService.canRemove(entry.get(), duration)) {
                    timeService.remove(entry.get(), duration);
                    issuer.sendMessage(messages.getRemoveTime().replace("%nickname%", nickname).replace("%time%", timeAsString));
                } else {
                    issuer.sendMessage(messages.getCantRemoveTime());
                }
            }
            case "set" -> {
                timeService.set(entry.get(), Instant.now().plus(duration));
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

        return timeRandom.getTimes();
    }
}
