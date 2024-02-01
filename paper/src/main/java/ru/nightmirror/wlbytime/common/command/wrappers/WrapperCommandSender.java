package ru.nightmirror.wlbytime.common.command.wrappers;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.nightmirror.wlbytime.common.convertor.ColorsConvertor;
import ru.nightmirror.wlbytime.interfaces.command.wrappers.IWrappedCommandSender;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WrapperCommandSender implements IWrappedCommandSender {

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
    public @NotNull String getNickname() {
        return commandSender.getName();
    }

    @Override
    public @Nullable UUID getUuid() {
        return (commandSender instanceof Player player) ? player.getUniqueId() : null;
    }

    @Override
    public void sendMessage(@NotNull String message) {
        commandSender.sendMessage(ColorsConvertor.convert(message));
    }

    @Override
    public boolean hasPermission(@NotNull String permission) {
        return isConsole() || commandSender.hasPermission(permission);
    }

    @Override
    public List<String> getAllPlayerNicknamesOnServer() {
        return commandSender.getServer().getOnlinePlayers()
                .stream()
                .map(Player::getName)
                .toList();
    }
}
