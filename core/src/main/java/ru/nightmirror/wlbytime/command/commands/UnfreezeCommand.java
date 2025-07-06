package ru.nightmirror.wlbytime.command.commands;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import ru.nightmirror.wlbytime.config.configs.CommandsConfig;
import ru.nightmirror.wlbytime.config.configs.MessagesConfig;
import ru.nightmirror.wlbytime.entry.EntryImpl;
import ru.nightmirror.wlbytime.interfaces.command.Command;
import ru.nightmirror.wlbytime.interfaces.command.CommandIssuer;
import ru.nightmirror.wlbytime.interfaces.finder.EntryFinder;
import ru.nightmirror.wlbytime.interfaces.services.EntryService;

import java.util.Optional;
import java.util.Set;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class UnfreezeCommand implements Command {

    CommandsConfig commandsConfig;
    MessagesConfig messages;
    EntryFinder finder;
    EntryService service;

    @Override
    public String getPermission() {
        return commandsConfig.getUnfreezePermission();
    }

    @Override
    public String getName() {
        return "unfreeze";
    }

    @Override
    public void execute(CommandIssuer issuer, String[] args) {
        if (args.length < 1) {
            issuer.sendMessage(messages.getIncorrectArguments());
            return;
        }

        String nickname = args[0];
        Optional<EntryImpl> entryOpt = finder.find(nickname);

        if (entryOpt.isEmpty()) {
            issuer.sendMessage(
                    messages.getPlayerNotInWhitelist().replace("%nickname%", nickname));
            return;
        }

        EntryImpl entry = entryOpt.get();

        if (!entry.isFrozen()) {
            issuer.sendMessage(
                    messages.getPlayerNotFrozen().replace("%nickname%", nickname));
            return;
        }

        if (entry.isFreezeInactive()) {
            issuer.sendMessage(
                    messages.getPlayerFreezeExpired().replace("%nickname%", nickname));
            return;
        }

        service.unfreeze(entry);
        issuer.sendMessage(
                messages.getPlayerUnfrozen().replace("%nickname%", nickname));
    }

    @Override
    public Set<String> getTabulate(CommandIssuer issuer, String[] args) {
        return args.length == 0 ? Set.of(issuer.getNickname()) : Set.of();
    }
}
