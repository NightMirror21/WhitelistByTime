package ru.nightmirror.wlbytime.common.listeners.command;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import ru.nightmirror.wlbytime.common.listeners.command.wrappers.WrapperCommandSenderImpl;
import ru.nightmirror.wlbytime.interfaces.command.CommandsExecutor;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WhitelistCommandExecutor implements CommandExecutor {

    CommandsExecutor executor;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] strings) {
        executor.execute(new WrapperCommandSenderImpl(sender), strings);
        return true;
    }
}
