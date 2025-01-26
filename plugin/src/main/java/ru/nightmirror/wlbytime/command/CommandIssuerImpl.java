package ru.nightmirror.wlbytime.command;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import ru.nightmirror.wlbytime.interfaces.command.CommandIssuer;
import ru.nightmirror.wlbytime.utils.ColorsUtils;

import java.util.Optional;
import java.util.UUID;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class CommandIssuerImpl implements CommandIssuer {

    CommandSender commandSender;

    @Override
    public boolean isConsole() {
        return commandSender instanceof ConsoleCommandSender;
    }

    @Override
    public boolean isPlayer() {
        return commandSender instanceof Player;
    }

    @Override
    public String getNickname() {
        return commandSender.getName();
    }

    @Override
    public Optional<UUID> getUuid() {
        return commandSender instanceof Player player ? Optional.of(player.getUniqueId()) : Optional.empty();
    }

    @Override
    public void sendMessage(String message) {
        commandSender.sendMessage(ColorsUtils.convertMessage(message));
    }

    @Override
    public boolean hasPermission(String permission) {
        return isConsole() || commandSender.hasPermission(permission);
    }
}
