package ru.nightmirror.wlbytime.interfaces.command.wrappers;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface TabCompleter {
    List<String> onTabComplete(@NotNull WrappedCommandSender commandSender, @NotNull String s, String[] strings);
}
