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

import java.util.Optional;
import java.util.Set;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Slf4j
public class RemoveCommand implements Command {

    CommandsConfig commandsConfig;
    MessagesConfig messages;
    EntryService service;
    PlayerIdentityResolver identityResolver;
    EntryIdentityService identityService;

    @Override
    public String getPermission() {
        return commandsConfig.getRemovePermission();
    }

    @Override
    public String getName() {
        return "remove";
    }

    @Override
    public void execute(CommandIssuer issuer, String[] args) {
        if (args.length != 1) {
            issuer.sendMessage(messages.getIncorrectArguments());
            log.info("RemoveCommand: invalid args length {}", args.length);
            return;
        }

        String nickname = args[0];
        ResolvedPlayer resolved = identityResolver.resolveByNickname(nickname);
        Optional<EntryImpl> entry = identityService.findOrMigrate(resolved, nickname);
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
