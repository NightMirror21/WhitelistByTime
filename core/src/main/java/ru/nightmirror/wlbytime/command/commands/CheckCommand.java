package ru.nightmirror.wlbytime.command.commands;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import ru.nightmirror.wlbytime.config.configs.MessagesConfig;
import ru.nightmirror.wlbytime.entry.Entry;
import ru.nightmirror.wlbytime.interfaces.command.Command;
import ru.nightmirror.wlbytime.interfaces.command.CommandIssuer;
import ru.nightmirror.wlbytime.interfaces.finder.EntryFinder;
import ru.nightmirror.wlbytime.time.TimeConvertor;

import java.util.Optional;
import java.util.Set;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class CheckCommand implements Command {

    MessagesConfig messages;
    EntryFinder finder;
    TimeConvertor convertor;

    @Override
    public String getPermission() {
        return "wlbytime.check";
    }

    @Override
    public String getName() {
        return "check";
    }

    @Override
    public void execute(CommandIssuer issuer, String[] args) {
        if (args.length < 1) {
            issuer.sendMessage(messages.getIncorrectArguments());
            return;
        }

        String nickname = args[0];
        Optional<Entry> entry = finder.find(nickname);
        if (entry.isPresent()) {
            if (entry.get().isFreezeActive()) {
                long timeInMillis = entry.get().getLeftFreezeTime();
                String timeAsString = convertor.getTimeLine(timeInMillis);
                issuer.sendMessage(messages.getPlayerFrozen()
                        .replace("%nickname%", nickname)
                        .replace("%time%", timeAsString));
            } else if (entry.get().isActive()) {
                if (entry.get().isForever()) {
                    issuer.sendMessage(messages.getCheckStillInWhitelist().replace("%nickname%", nickname));
                } else {
                    long timeInMillis = entry.get().getLeftActiveTime();
                    String timeAsString = convertor.getTimeLine(timeInMillis);
                    issuer.sendMessage(messages.getCheckStillInWhitelistForTime()
                            .replace("%nickname%", nickname)
                            .replace("%time%", timeAsString));
                }
            } else {
                issuer.sendMessage(messages.getPlayerExpired().replace("%nickname%", nickname));
            }
        } else {
            issuer.sendMessage(messages.getPlayerNotInWhitelist().replace("%nickname%", nickname));
        }

    }


    @Override
    public Set<String> getTabulate(CommandIssuer issuer, String[] args) {
        if (args.length == 0) {
            return Set.of(issuer.getNickname());
        }
        return Set.of();
    }
}
