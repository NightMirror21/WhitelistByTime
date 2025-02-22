package ru.nightmirror.wlbytime.command;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.nightmirror.wlbytime.config.configs.MessagesConfig;
import ru.nightmirror.wlbytime.utils.ColorsUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class CommandProxy implements TabExecutor {

    MessagesConfig messagesConfig;
    CommandDispatcher commandDispatcher;

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (args.length == 0) {
            ColorsUtils.convert(messagesConfig.getHelp()).forEach(sender::sendMessage);
        } else {
            String commandName = args[0];
            String[] commandArgs = Arrays.copyOfRange(args, 1, args.length);
            commandDispatcher.dispatchExecute(new CommandIssuerImpl(sender), commandName, commandArgs);
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (args.length <= 1) {
            return new ArrayList<>(commandDispatcher.getCommands());
        } else {
            String commandName = args[0];
            String[] commandArgs = Arrays.copyOfRange(args, 1, args.length - 1);
            return new ArrayList<>(commandDispatcher.dispatchTabulate(new CommandIssuerImpl(sender), commandName, commandArgs));
        }
    }
}
