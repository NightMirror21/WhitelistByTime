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
import java.time.Instant;
import java.util.Optional;
import java.util.Set;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Slf4j
public class AddCommand implements Command {

    CommandsConfig commandsConfig;
    MessagesConfig messages;
    TimeConvertor convertor;
    EntryService service;
    TimeRandom random;
    PlayerIdentityResolver identityResolver;
    EntryIdentityService identityService;

    @Override
    public String getPermission() {
        return commandsConfig.getAddPermission();
    }

    @Override
    public String getName() {
        return "add";
    }

    @Override
    public void execute(CommandIssuer issuer, String[] args) {
        if (args.length < 1) {
            issuer.sendMessage(messages.getIncorrectArguments());
            log.info("AddCommand: insufficient args from {}", issuer.getNickname());
            return;
        }

        String nickname = args[0];
        ResolvedPlayer resolved = identityResolver.resolveByNickname(nickname);
        if (isPlayerInWhitelist(resolved, nickname, issuer)) {
            return;
        }

        if (args.length == 1) {
            addPlayerWithoutTime(resolved, issuer);
        } else {
            addPlayerWithTime(args, resolved, issuer);
        }
    }

    private boolean isPlayerInWhitelist(ResolvedPlayer resolved, String nickname, CommandIssuer issuer) {
        Optional<EntryImpl> entry = identityService.findOrMigrate(resolved, nickname);
        if (entry.isPresent()) {
            String message = messages.getPlayerAlreadyInWhitelist()
                    .replace("%nickname%", nickname);
            issuer.sendMessage(message);
            return true;
        }
        return false;
    }

    private void addPlayerWithoutTime(ResolvedPlayer resolved, CommandIssuer issuer) {
        if (resolved.uuid() != null) {
            service.create(resolved.nickname(), resolved.uuid().toString());
        } else {
            service.create(resolved.nickname());
        }
        String message = messages.getSuccessfullyAdded()
                .replace("%nickname%", resolved.nickname());
        issuer.sendMessage(message);
    }

    private void addPlayerWithTime(String[] args, ResolvedPlayer resolved, CommandIssuer issuer) {
        StringBuilder timeArgument = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            timeArgument.append(args[i]).append(" ");
        }

        Duration duration = convertor.getTime(timeArgument.toString().trim());
        if (duration.isNegative() || duration.isZero()) {
            issuer.sendMessage(messages.getTimeIsIncorrect());
            log.info("AddCommand: invalid time '{}' for {}", timeArgument, resolved.nickname());
            return;
        }
        Instant until = Instant.now().plus(duration);
        if (resolved.uuid() != null) {
            service.create(resolved.nickname(), resolved.uuid().toString(), until);
        } else {
            service.create(resolved.nickname(), until);
        }

        String timeAsString = convertor.getTimeLine(duration);
        String message = messages.getSuccessfullyAddedForTime()
                .replace("%nickname%", resolved.nickname())
                .replace("%time%", timeAsString);
        issuer.sendMessage(message);
    }

    @Override
    public Set<String> getTabulate(CommandIssuer issuer, String[] args) {
        if (args.length == 0) {
            return Set.of(issuer.getNickname());
        } else {
            return random.getTimes();
        }
    }
}
