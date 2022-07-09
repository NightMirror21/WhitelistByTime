package ru.nightmirror.wlbytime.executors.minecraft;

import lombok.RequiredArgsConstructor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import ru.nightmirror.wlbytime.executors.ICommandsExecutor;

@RequiredArgsConstructor
public class WhitelistCommandExecutor implements CommandExecutor {

    private final ICommandsExecutor executor;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] strings) {
        doCommand(sender, strings, executor);
        return true;
    }

    public static void doCommand(CommandSender sender, String[] strings, ICommandsExecutor executor) {
        if (strings.length == 0 || strings[0].equals("")) {
            executor.help(sender, strings);
        } else if (strings.length > 1 && strings[0].equals("add")) {
            executor.add(sender, strings);
        } else if (strings.length > 1 && strings[0].equals("remove")) {
            executor.remove(sender, strings);
        } else if (strings.length > 1 && strings[0].equals("check")) {
            executor.check(sender, strings);
        } else if (strings[0].equals("reload")) {
            executor.reload(sender, strings);
        } else if (strings[0].equals("getall")) {
            executor.getAll(sender, strings);
        }
    }
}
