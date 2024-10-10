package ru.nightmirror.wlbytime.interfaces.command;

import org.jetbrains.annotations.NotNull;
import ru.nightmirror.wlbytime.interfaces.command.wrappers.WrappedCommandSender;

import java.util.List;

public interface TabCompleter {
    List<String> onTabComplete(@NotNull WrappedCommandSender commandSender, @NotNull String s, String[] strings);
}
