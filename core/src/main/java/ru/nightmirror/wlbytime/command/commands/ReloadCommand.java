package ru.nightmirror.wlbytime.command.commands;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import ru.nightmirror.wlbytime.config.configs.CommandsConfig;
import ru.nightmirror.wlbytime.config.configs.MessagesConfig;
import ru.nightmirror.wlbytime.interfaces.command.Command;
import ru.nightmirror.wlbytime.interfaces.command.CommandIssuer;
import ru.nightmirror.wlbytime.interfaces.plugin.Reloadable;

import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class ReloadCommand implements Command {

    private static final Logger LOGGER = Logger.getLogger(ReloadCommand.class.getSimpleName());

    MessagesConfig messagesConfig;
    CommandsConfig commandsConfig;
    Reloadable reloadable;

    @Override
    public String getPermission() {
        return commandsConfig.getReloadPermission();
    }

    @Override
    public String getName() {
        return "reload";
    }

    @Override
    public void execute(CommandIssuer issuer, String[] args) {
        try {
            reloadable.reload();
            issuer.sendMessage(messagesConfig.getPluginSuccessfullyReloaded());
        } catch (Exception exception) {
            LOGGER.log(Level.SEVERE, "Error while reloading plugin", exception);
            issuer.sendMessage(messagesConfig.getPluginReloadedWithErrors());
        }
    }

    @Override
    public Set<String> getTabulate(CommandIssuer issuer, String[] args) {
        return Set.of();
    }
}
