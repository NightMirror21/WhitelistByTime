package ru.nightmirror.wlbytime.command.commands;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class OnCommand implements Command {

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
        return "on";
    }

    @Override
    public void execute(CommandIssuer issuer, String[] args) {
        if (args.length > 0) {
            issuer.sendMessage(messages.getIncorrectArguments());
            log.info("OnCommand: invalid args length {} from {}", args.length, issuer.getNickname());
            return;
        }

        if (settings.isWhitelistEnabled()) {
            issuer.sendMessage(messages.getWhitelistAlreadyEnabled());
            log.info("OnCommand: whitelist already enabled (by {})", issuer.getNickname());
            return;
        }

        settings.setWhitelistEnabled(true);
        settings.save(settingsPath);
        issuer.sendMessage(messages.getWhitelistEnabled());
        log.info("OnCommand: whitelist enabled by {}", issuer.getNickname());
    }

    @Override
    public Set<String> getTabulate(CommandIssuer issuer, String[] args) {
        return Set.of();
    }
}
