package ru.nightmirror.wlbytime.common.listeners.command;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import ru.nightmirror.wlbytime.common.listeners.command.wrappers.WrapperCommandSenderImpl;
import ru.nightmirror.wlbytime.interfaces.command.wrappers.TabCompleter;

import java.util.List;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class WhitelistTabCompleterExecutor implements org.bukkit.command.TabCompleter {

    TabCompleter tabCompleter;

    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, String[] strings) {
        return tabCompleter.onTabComplete(new WrapperCommandSenderImpl(commandSender), s, strings);
    }
}
