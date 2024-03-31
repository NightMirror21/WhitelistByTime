package ru.nightmirror.wlbytime.interfaces.command;

import ru.nightmirror.wlbytime.interfaces.command.wrappers.WrappedCommandSender;

public interface CommandsExecutor {
    void reload(WrappedCommandSender sender, String[] strings);
    void help(WrappedCommandSender sender, String[] strings);
    void getAll(WrappedCommandSender sender, String[] strings);
    void remove(WrappedCommandSender sender, String[] strings);
    void check(WrappedCommandSender sender, String[] strings);
    void checkme(WrappedCommandSender sender);
    void add(WrappedCommandSender sender, String[] strings);
    void time(WrappedCommandSender sender, String[] strings);
    void turn(WrappedCommandSender sender, String[] strings);

    void execute(WrappedCommandSender sender, String[] strings);
}
