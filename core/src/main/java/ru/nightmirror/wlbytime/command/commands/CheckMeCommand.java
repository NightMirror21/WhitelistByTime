package ru.nightmirror.wlbytime.command.commands;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import ru.nightmirror.wlbytime.config.configs.MessagesConfig;
import ru.nightmirror.wlbytime.entry.EntryImpl;
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
        Optional<EntryImpl> entry = finder.find(issuer.getNickname());
        if (entry.isEmpty() || entry.get().isInactive()) {
            issuer.sendMessage(messages.getCheckMeNotInWhitelist());
        } else {
            if (entry.get().isForever()) {
                sendForeverMessage(issuer);
            } else if (entry.get().isFreezeActive()) {
                sendFrozenMessage(issuer, entry.get());
            } else {
                sendWhitelistForTimeMessage(issuer, entry.get());
            }
        }
    }

    private void sendForeverMessage(CommandIssuer issuer) {
        issuer.sendMessage(messages.getCheckMeStillInWhitelistForever());
    }

    private void sendFrozenMessage(CommandIssuer issuer, EntryImpl entry) {
        long leftOfFreeze = entry.getLeftFreezeTime();
        String timeAsString = convertor.getTimeLine(leftOfFreeze);
        issuer.sendMessage(messages.getCheckMeFrozen().replace("%time%", timeAsString));
    }

    private void sendWhitelistForTimeMessage(CommandIssuer issuer, EntryImpl entry) {
        long leftOfTime = entry.getLeftActiveTime();
        String timeAsString = convertor.getTimeLine(leftOfTime);
        issuer.sendMessage(messages.getCheckMeStillInWhitelistForTime().replace("%time%", timeAsString));
    }

    @Override
    public Set<String> getTabulate(CommandIssuer issuer, String[] args) {
        return Set.of();
    }
}
