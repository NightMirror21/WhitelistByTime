package ru.nightmirror.wlbytime.interfaces.command;

import ru.nightmirror.wlbytime.interfaces.command.wrappers.IWrappedCommandSender;

public interface ICommandsExecutor {
    void reload(IWrappedCommandSender sender, String[] strings);
    void help(IWrappedCommandSender sender, String[] strings);
    void getAll(IWrappedCommandSender sender, String[] strings);
    void remove(IWrappedCommandSender sender, String[] strings);
    void check(IWrappedCommandSender sender, String[] strings);
    void checkme(IWrappedCommandSender sender);
    void add(IWrappedCommandSender sender, String[] strings);
    void time(IWrappedCommandSender sender, String[] strings);
    void turn(IWrappedCommandSender sender, String[] strings);

    void execute(IWrappedCommandSender sender, String[] strings);
}
