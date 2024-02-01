package ru.nightmirror.wlbytime.common.listeners.command;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import ru.nightmirror.wlbytime.common.listeners.command.wrappers.WrapperCommandSender;
import ru.nightmirror.wlbytime.interfaces.command.ICommandsExecutor;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WhitelistCommandExecutor implements CommandExecutor {

    ICommandsExecutor executor;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] strings) {
        executor.execute(new WrapperCommandSender(sender), strings);
        return true;
    }
}
