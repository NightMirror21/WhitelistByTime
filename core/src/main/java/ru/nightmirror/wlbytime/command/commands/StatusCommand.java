package ru.nightmirror.wlbytime.command.commands;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import ru.nightmirror.wlbytime.config.configs.CommandsConfig;
import ru.nightmirror.wlbytime.config.configs.MessagesConfig;
import ru.nightmirror.wlbytime.config.configs.SettingsConfig;
import ru.nightmirror.wlbytime.interfaces.command.Command;
import ru.nightmirror.wlbytime.interfaces.command.CommandIssuer;

import java.util.Set;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class StatusCommand implements Command {

    CommandsConfig commandsConfig;
    MessagesConfig messages;
    SettingsConfig settings;

    @Override
    public String getPermission() {
        return commandsConfig.getTogglePermission();
    }

    @Override
    public String getName() {
        return "status";
    }

    @Override
    public void execute(CommandIssuer issuer, String[] args) {
        if (args.length > 0) {
            issuer.sendMessage(messages.getIncorrectArguments());
            return;
        }

        if (settings.isWhitelistEnabled()) {
            issuer.sendMessage(messages.getWhitelistStatusEnabled());
        } else {
            issuer.sendMessage(messages.getWhitelistStatusDisabled());
        }
    }

    @Override
    public Set<String> getTabulate(CommandIssuer issuer, String[] args) {
        return Set.of();
    }
}
