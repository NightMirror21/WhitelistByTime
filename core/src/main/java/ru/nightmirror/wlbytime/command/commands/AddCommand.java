package ru.nightmirror.wlbytime.command.commands;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import ru.nightmirror.wlbytime.config.configs.MessagesConfig;
import ru.nightmirror.wlbytime.interfaces.command.Command;
import ru.nightmirror.wlbytime.interfaces.command.CommandIssuer;
import ru.nightmirror.wlbytime.interfaces.finder.EntryFinder;
import ru.nightmirror.wlbytime.interfaces.services.EntryService;
import ru.nightmirror.wlbytime.time.TimeConvertor;
import ru.nightmirror.wlbytime.time.TimeRandom;

import java.util.Set;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class AddCommand implements Command {

    MessagesConfig messages;
    EntryFinder finder;
    TimeConvertor convertor;
    EntryService service;
    TimeRandom random;

    @Override
    public String getPermission() {
        return "wlbytime.add";
    }

    @Override
    public String getName() {
        return "add";
    }

    @Override
    public void execute(CommandIssuer issuer, String[] args) {
        if (args.length < 1) {
            issuer.sendMessage(messages.getIncorrectArguments());
            return;
        }

        String nickname = args[0];
        if (isPlayerInWhitelist(nickname, issuer)) {
            return;
        }

        if (args.length == 1) {
            addPlayerWithoutTime(nickname, issuer);
        } else {
            addPlayerWithTime(args, nickname, issuer);
        }
    }

    private boolean isPlayerInWhitelist(String nickname, CommandIssuer issuer) {
        if (finder.find(nickname).isPresent()) {
            String message = messages.getPlayerAlreadyInWhitelist()
                    .replace("%nickname%", nickname);
            issuer.sendMessage(message);
            return true;
        }
        return false;
    }

    private void addPlayerWithoutTime(String nickname, CommandIssuer issuer) {
        service.create(nickname);
        String message = messages.getSuccessfullyAdded()
                .replace("%nickname%", nickname);
        issuer.sendMessage(message);
    }

    private void addPlayerWithTime(String[] args, String nickname, CommandIssuer issuer) {
        StringBuilder timeArgument = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            timeArgument.append(args[i]);
        }

        long timeInMillis = convertor.getTimeMs(timeArgument.toString());
        service.create(nickname, timeInMillis + System.currentTimeMillis());

        String timeAsString = convertor.getTimeLine(timeInMillis);
        String message = messages.getSuccessfullyAddedForTime()
                .replace("%nickname%", nickname)
                .replace("%time%", timeAsString);
        issuer.sendMessage(message);
    }

    @Override
    public Set<String> getTabulate(CommandIssuer issuer, String[] args) {
        if (args.length == 0) {
            return Set.of(issuer.getNickname());
        } else {
            return Set.of(random.getRandomOneTime());
        }
    }
}
