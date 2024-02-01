package ru.nightmirror.wlbytime.common.command;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import ru.nightmirror.wlbytime.common.command.wrappers.WrapperCommandSender;
import ru.nightmirror.wlbytime.interfaces.command.wrappers.ITabCompleter;

import java.util.List;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class WhitelistTabCompleterExecutor implements TabCompleter {

    ITabCompleter tabCompleter;

    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, String[] strings) {
        return tabCompleter.onTabComplete(new WrapperCommandSender(commandSender), s, strings);
    }
}
