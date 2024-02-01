package ru.nightmirror.wlbytime.interfaces.command.wrappers;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface ITabCompleter {

    List<String> onTabComplete(@NotNull IWrappedCommandSender commandSender, @NotNull String s, String[] strings);

}
