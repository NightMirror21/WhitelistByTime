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
import ru.nightmirror.wlbytime.time.TimeConvertor;

import java.util.Optional;
import java.util.Set;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Slf4j
public class CheckMeCommand implements Command {

    CommandsConfig commandsConfig;
    MessagesConfig messages;
    TimeConvertor convertor;
    PlayerIdentityResolver identityResolver;
    EntryIdentityService identityService;

    @Override
    public Set<String> getPermissions() {
        return commandsConfig.getCheckMePermission();
    }

    @Override
    public String getName() {
        return "checkme";
    }

    @Override
    public void execute(CommandIssuer issuer, String[] args) {
        ResolvedPlayer resolved = identityResolver.resolveByIssuer(issuer);
        Optional<EntryImpl> entry = identityService.findOrMigrate(resolved, issuer.getNickname());
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
        String timeAsString = convertor.getTimeLine(entry.getLeftFreezeDuration());
        issuer.sendMessage(messages.getCheckMeFrozen().replace("%time%", timeAsString));
    }

    private void sendWhitelistForTimeMessage(CommandIssuer issuer, EntryImpl entry) {
        String timeAsString = convertor.getTimeLine(entry.getLeftActiveDuration());
        issuer.sendMessage(messages.getCheckMeStillInWhitelistForTime().replace("%time%", timeAsString));
    }

    @Override
    public Set<String> getTabulate(CommandIssuer issuer, String[] args) {
        return Set.of();
    }
}
