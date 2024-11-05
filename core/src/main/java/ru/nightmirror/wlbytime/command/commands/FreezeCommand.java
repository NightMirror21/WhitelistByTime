package ru.nightmirror.wlbytime.command.commands;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import ru.nightmirror.wlbytime.config.configs.MessagesConfig;
import ru.nightmirror.wlbytime.entry.Entry;
import ru.nightmirror.wlbytime.interfaces.command.Command;
import ru.nightmirror.wlbytime.interfaces.command.CommandIssuer;
import ru.nightmirror.wlbytime.interfaces.finder.EntryFinder;
import ru.nightmirror.wlbytime.interfaces.services.EntryService;
import ru.nightmirror.wlbytime.time.TimeConvertor;
import ru.nightmirror.wlbytime.time.TimeRandom;

import java.util.Optional;
import java.util.Set;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class FreezeCommand implements Command {

    MessagesConfig messages;
    EntryFinder finder;
    TimeConvertor convertor;
    TimeRandom timeRandom;
    EntryService service;

    @Override
    public String getPermission() {
        return "wlbytime.freeze";
    }

    @Override
    public String getName() {
        return "freeze";
    }

    @Override
    public void execute(CommandIssuer issuer, String[] args) {
        if (args.length < 2) {
            sendIncorrectArgumentsMessage(issuer);
            return;
        }

        String nickname = args[0];
        String timeString = concatenateArgs(args);

        Optional<Entry> entry = finder.find(nickname);
        if (entry.isEmpty()) {
            sendPlayerNotInWhitelistMessage(issuer, nickname);
            return;
        }

        long timeInMillis = convertor.getTimeMs(timeString);
        if (timeInMillis <= 0) {
            sendTimeIsIncorrectMessage(issuer);
            return;
        }

        Entry userEntry = entry.get();
        if (!userEntry.isActive()) {
            sendPlayerExpiredMessage(issuer, nickname);
            return;
        }

        if (userEntry.isFreezeActive()) {
            sendPlayerAlreadyFrozenMessage(issuer, nickname);
        } else {
            freezePlayer(issuer, userEntry, timeInMillis, nickname);
        }
    }

    private void sendIncorrectArgumentsMessage(CommandIssuer issuer) {
        issuer.sendMessage(messages.getIncorrectArguments());
    }

    private void sendPlayerNotInWhitelistMessage(CommandIssuer issuer, String nickname) {
        issuer.sendMessage(messages.getPlayerNotInWhitelist().replaceAll("%nickname%", nickname));
    }

    private void sendTimeIsIncorrectMessage(CommandIssuer issuer) {
        issuer.sendMessage(messages.getTimeIsIncorrect());
    }

    private void sendPlayerExpiredMessage(CommandIssuer issuer, String nickname) {
        issuer.sendMessage(messages.getPlayerExpired().replaceAll("%nickname%", nickname));
    }

    private void sendPlayerAlreadyFrozenMessage(CommandIssuer issuer, String nickname) {
        issuer.sendMessage(messages.getPlayerAlreadyFrozen().replaceAll("%nickname%", nickname));
    }

    private void freezePlayer(CommandIssuer issuer, Entry userEntry, long timeInMillis, String nickname) {
        service.freeze(userEntry, timeInMillis);
        String timeAsString = convertor.getTimeLine(timeInMillis);
        issuer.sendMessage(messages.getPlayerFrozen()
                .replaceAll("%nickname%", nickname)
                .replaceAll("%time%", timeAsString));
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

        return Set.of(timeRandom.getRandomOneTime());
    }
}
