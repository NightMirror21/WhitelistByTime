package ru.nightmirror.wlbytime.command;

import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.junit.jupiter.api.Test;
import ru.nightmirror.wlbytime.config.configs.MessagesConfig;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CommandProxyTest {

    @Test
    public void onCommandWithoutArgsSendsHelp() {
        CommandSender sender = mock(CommandSender.class);
        Command command = mock(Command.class);
        MessagesConfig messages = mock(MessagesConfig.class);
        CommandDispatcher dispatcher = mock(CommandDispatcher.class);
        when(messages.getHelp()).thenReturn(List.of("line1", "line2"));

        CommandProxy proxy = new CommandProxy(messages, dispatcher);
        proxy.onCommand(sender, command, "wl", new String[]{});

        verify(sender).sendMessage(Component.text("line1"));
        verify(sender).sendMessage(Component.text("line2"));
    }

    @Test
    public void onTabCompleteUsesDispatcherCommandsWhenNoArgs() {
        CommandSender sender = mock(CommandSender.class);
        Command command = mock(Command.class);
        MessagesConfig messages = mock(MessagesConfig.class);
        CommandDispatcher dispatcher = mock(CommandDispatcher.class);
        when(dispatcher.getCommands()).thenReturn(Set.of("add", "remove"));

        CommandProxy proxy = new CommandProxy(messages, dispatcher);
        List<String> result = proxy.onTabComplete(sender, command, "wl", new String[]{});

        assertEquals(2, result.size());
        assertTrue(result.containsAll(List.of("add", "remove")));
    }
}
