package ru.nightmirror.wlbytime.command.commands;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import ru.nightmirror.wlbytime.config.configs.CommandsConfig;
import ru.nightmirror.wlbytime.config.configs.MessagesConfig;
import ru.nightmirror.wlbytime.config.configs.SettingsConfig;
import ru.nightmirror.wlbytime.interfaces.command.Command;
import ru.nightmirror.wlbytime.interfaces.command.CommandIssuer;

import java.nio.file.Path;
import java.util.Set;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class OffCommand implements Command {

    CommandsConfig commandsConfig;
    MessagesConfig messages;
    SettingsConfig settings;
    Path settingsPath;

    @Override
    public String getPermission() {
        return commandsConfig.getTogglePermission();
    }

    @Override
    public String getName() {
        return "off";
    }

    @Override
    public void execute(CommandIssuer issuer, String[] args) {
        if (args.length > 0) {
            issuer.sendMessage(messages.getIncorrectArguments());
            return;
        }

        if (!settings.isWhitelistEnabled()) {
            issuer.sendMessage(messages.getWhitelistAlreadyDisabled());
            return;
        }

        settings.setWhitelistEnabled(false);
        settings.save(settingsPath);
        issuer.sendMessage(messages.getWhitelistDisabled());
    }

    @Override
    public Set<String> getTabulate(CommandIssuer issuer, String[] args) {
        return Set.of();
    }
}
