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

import java.util.Optional;
import java.util.Set;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class RemoveCommand implements Command {

    MessagesConfig messages;
    EntryFinder finder;
    EntryService service;

    @Override
    public String getPermission() {
        return "wlbytime.remove";
    }

    @Override
    public String getName() {
        return "remove";
    }

    @Override
    public void execute(CommandIssuer issuer, String[] args) {
        if (args.length != 1) {
            issuer.sendMessage(messages.getIncorrectArguments());
            return;
        }

        String nickname = args[0];
        Optional<Entry> entry = finder.find(nickname);
        if (entry.isPresent()) {
            service.remove(entry.get());
            issuer.sendMessage(messages.getPlayerRemovedFromWhitelist()
                    .replace("%nickname%", nickname));
        } else {
            issuer.sendMessage(messages.getPlayerNotInWhitelist()
                    .replace("%nickname%", nickname));
        }
    }

    @Override
    public Set<String> getTabulate(CommandIssuer issuer, String[] args) {
        if (args.length == 0) {
            return Set.of(issuer.getNickname());
        } else {
            return Set.of();
        }
    }
}
