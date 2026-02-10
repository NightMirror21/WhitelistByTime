package ru.nightmirror.wlbytime.command;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import ru.nightmirror.wlbytime.config.configs.MessagesConfig;
import ru.nightmirror.wlbytime.interfaces.command.Command;
import ru.nightmirror.wlbytime.interfaces.command.CommandIssuer;

import java.util.Set;
import java.util.stream.Collectors;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Slf4j
public class CommandDispatcher {

    MessagesConfig messagesConfig;
    Set<Command> commands;

    public void dispatchExecute(CommandIssuer issuer, String commandName, String[] args) {
        for (Command command : commands) {
            if (!command.getName().equalsIgnoreCase(commandName)) {
                continue;
            }

            if (!hasAnyPermission(issuer, command.getPermissions())) {
                log.debug("Permission denied for command={} issuer={}", commandName, issuer.getNickname());
                issuer.sendMessage(messagesConfig.getNotPermission());
            } else {
                command.execute(issuer, args);
            }
            return;
        }
        log.debug("Command not found: {}", commandName);
    }

    public Set<String> dispatchTabulate(CommandIssuer issuer, String commandName, String[] args) {
        for (Command command : commands) {
            if (!command.getName().equalsIgnoreCase(commandName)) {
                continue;
            }

            if (hasAnyPermission(issuer, command.getPermissions())) {
                return command.getTabulate(issuer, args);
            } else {
                log.debug("Permission denied for tab completion command={} issuer={}", commandName, issuer.getNickname());
                return Set.of();
            }
        }
        log.debug("Tab completion command not found: {}", commandName);
        return Set.of();
    }

    private boolean hasAnyPermission(CommandIssuer issuer, Set<String> permissions) {
        if (permissions == null || permissions.isEmpty()) {
            return true;
        }
        for (String permission : permissions) {
            if (permission == null || permission.isBlank()) {
                continue;
            }
            if (issuer.hasPermission(permission)) {
                return true;
            }
        }
        return false;
    }

    public Set<String> getCommands() {
        return commands.stream()
                .map(Command::getName)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
    }
}
