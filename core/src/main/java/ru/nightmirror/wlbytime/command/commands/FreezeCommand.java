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
import ru.nightmirror.wlbytime.time.TimeConvertor;
import ru.nightmirror.wlbytime.time.TimeRandom;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Slf4j
public class FreezeCommand implements Command {

    CommandsConfig commandsConfig;
    MessagesConfig messages;
    TimeConvertor convertor;
    TimeRandom timeRandom;
    EntryService service;
    PlayerIdentityResolver identityResolver;
    EntryIdentityService identityService;

    @Override
    public Set<String> getPermissions() {
        return commandsConfig.getFreezePermission();
    }

    @Override
    public String getName() {
        return "freeze";
    }

    @Override
    public void execute(CommandIssuer issuer, String[] args) {
        if (args.length < 2) {
            issuer.sendMessage(messages.getIncorrectArguments());
            log.info("FreezeCommand: insufficient args");
            return;
        }

        String nickname = args[0];
        String timeString = concatenateArgs(args);

        ResolvedPlayer resolved = identityResolver.resolveByNickname(nickname);
        Optional<EntryImpl> entry = identityService.findOrMigrate(resolved, nickname);
        if (entry.isEmpty()) {
            issuer.sendMessage(messages.getPlayerNotInWhitelist().replace("%nickname%", nickname));
            return;
        }

        EntryImpl userEntry = entry.get();
        if (!userEntry.isActive()) {
            issuer.sendMessage(messages.getPlayerExpired().replace("%nickname%", nickname));
            return;
        }

        if (userEntry.isForever()) {
            issuer.sendMessage(messages.getCantFreezeCausePlayerIsForever().replace("%nickname%", nickname));
            return;
        }

        if (userEntry.isFreezeActive()) {
            issuer.sendMessage(messages.getPlayerAlreadyFrozen().replace("%nickname%", nickname));
        } else {
            Duration duration = convertor.getTime(timeString);
            if (duration.isNegative() || duration.isZero()) {
                issuer.sendMessage(messages.getTimeIsIncorrect());
                log.info("FreezeCommand: invalid time '{}'", timeString);
                return;
            }
            freezePlayer(issuer, userEntry, duration, nickname);
        }
    }

    private void freezePlayer(CommandIssuer issuer, EntryImpl userEntry, Duration duration, String nickname) {
        service.freeze(userEntry, duration);
        String timeAsString = convertor.getTimeLine(duration);
        issuer.sendMessage(messages.getPlayerFrozen()
                .replace("%nickname%", nickname)
                .replace("%time%", timeAsString));
    }

    private String concatenateArgs(String[] args) {
        StringBuilder timeArgument = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            timeArgument.append(args[i]);
        }
        return timeArgument.toString();
    }

    @Override
    public Set<String> getTabulate(CommandIssuer issuer, String[] args) {
        if (args.length == 0) {
            return Set.of(issuer.getNickname());
        }

        return timeRandom.getTimes();
    }
}
