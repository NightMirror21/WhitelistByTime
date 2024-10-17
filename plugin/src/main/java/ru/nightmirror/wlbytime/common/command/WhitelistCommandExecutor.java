package ru.nightmirror.wlbytime.common.command;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import ru.nightmirror.wlbytime.common.command.wrappers.WrapperCommandSenderImpl;
import ru.nightmirror.wlbytime.interfaces.command.CommandsExecutor;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WhitelistCommandExecutor implements CommandExecutor {

    CommandsExecutor executor;

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, String[] strings) {
        executor.execute(new WrapperCommandSenderImpl(sender), strings);
        return true;
    }
}
