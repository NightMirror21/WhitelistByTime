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
public class CheckMeCommand implements Command {

    MessagesConfig messages;
    EntryFinder finder;
    TimeConvertor convertor;

    @Override
    public String getPermission() {
        return "wlbytime.checkme";
    }

    @Override
    public String getName() {
        return "checkme";
    }

    @Override
    public void execute(CommandIssuer issuer, String[] args) {
        Optional<Entry> entry = finder.find(issuer.getNickname());
        if (entry.isEmpty() || entry.get().isExpiredConsideringFreeze()) {
            issuer.sendMessage(messages.getCheckMeNotInWhitelist());
        } else {
            if (entry.get().hasNoExpiration()) {
                sendForeverMessage(issuer);
            } else if (entry.get().isCurrentlyFrozen()) {
                sendFrozenMessage(issuer, entry.get());
            } else {
                sendWhitelistForTimeMessage(issuer, entry.get());
            }
        }
    }

    private void sendForeverMessage(CommandIssuer issuer) {
        issuer.sendMessage(messages.getCheckMeStillInWhitelistForever());
    }

    private void sendFrozenMessage(CommandIssuer issuer, Entry entry) {
        long leftOfFreeze = entry.getRemainingFreezeTime();
        String timeAsString = convertor.getTimeLine(leftOfFreeze);
        issuer.sendMessage(messages.getCheckMeFrozen().replaceAll("%time%", timeAsString));
    }

    private void sendWhitelistForTimeMessage(CommandIssuer issuer, Entry entry) {
        long leftOfTime = entry.getRemainingActiveTime();
        String timeAsString = convertor.getTimeLine(leftOfTime);
        issuer.sendMessage(messages.getCheckMeStillInWhitelistForTime().replaceAll("%time%", timeAsString));
    }

    @Override
    public Set<String> getTabulate(CommandIssuer issuer, String[] args) {
        return Set.of();
    }
}
