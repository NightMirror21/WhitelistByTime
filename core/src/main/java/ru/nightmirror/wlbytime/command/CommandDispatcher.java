package ru.nightmirror.wlbytime.command;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import ru.nightmirror.wlbytime.config.configs.MessagesConfig;
import ru.nightmirror.wlbytime.interfaces.command.Command;
import ru.nightmirror.wlbytime.interfaces.command.CommandIssuer;

import java.util.Set;
import java.util.stream.Collectors;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class CommandDispatcher {

    MessagesConfig messagesConfig;
    Set<Command> commands;

    public void dispatchExecute(CommandIssuer issuer, String commandName, String[] args) {
        for (Command command : commands) {
            if (!command.getName().equalsIgnoreCase(commandName)) {
                continue;
            }

            if (!issuer.hasPermission(command.getPermission())) {
                issuer.sendMessage(messagesConfig.getNotPermission());
            } else {
                command.execute(issuer, args);
            }
            return;
        }
    }

    public Set<String> dispatchTabulate(CommandIssuer issuer, String commandName, String[] args) {
        for (Command command : commands) {
            if (!command.getName().equalsIgnoreCase(commandName)) {
                continue;
            }

            if (issuer.hasPermission(command.getPermission())) {
                return command.getTabulate(issuer, args);
            } else {
                return Set.of();
            }
        }
        return Set.of();
    }

    public Set<String> getCommands() {
        return commands.stream()
                .map(Command::getName)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
    }
}
