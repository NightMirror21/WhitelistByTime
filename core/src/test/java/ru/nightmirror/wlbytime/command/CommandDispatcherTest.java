package ru.nightmirror.wlbytime.command;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.nightmirror.wlbytime.interfaces.command.Command;
import ru.nightmirror.wlbytime.interfaces.command.CommandIssuer;

import java.util.Set;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class CommandDispatcherTest {

    private CommandDispatcher commandDispatcher;
    private Consumer<CommandIssuer> noPermissionSender;
    private CommandIssuer issuer;
    private Command command;

    @SuppressWarnings("unchecked")
    @BeforeEach
    public void setUp() {
        noPermissionSender = mock(Consumer.class);
        command = mock(Command.class);
        issuer = mock(CommandIssuer.class);
        Set<Command> commands = Set.of(command);
        commandDispatcher = new CommandDispatcher(noPermissionSender, commands);
    }

    @Test
    public void testDispatchExecute_WhenCommandExists_AndIssuerHasPermission_ShouldExecuteCommand() {
        String commandName = "test";
        String[] args = {"arg1", "arg2"};
        when(command.getName()).thenReturn(commandName);
        when(command.getPermission()).thenReturn("permission.test");
        when(issuer.hasPermission("permission.test")).thenReturn(true);

        commandDispatcher.dispatchExecute(issuer, commandName, args);

        verify(command).execute(issuer, args);
        verify(noPermissionSender, never()).accept(issuer);
    }

    @Test
    public void testDispatchExecute_WhenCommandExists_AndIssuerLacksPermission_ShouldSendNoPermissionMessage() {
        String commandName = "test";
        String[] args = {"arg1", "arg2"};
        when(command.getName()).thenReturn(commandName);
        when(command.getPermission()).thenReturn("permission.test");
        when(issuer.hasPermission("permission.test")).thenReturn(false);

        commandDispatcher.dispatchExecute(issuer, commandName, args);

        verify(noPermissionSender).accept(issuer);
        verify(command, never()).execute(issuer, args);
    }

    @Test
    public void testDispatchExecute_WhenCommandDoesNotExist_ShouldDoNothing() {
        String commandName = "nonexistent";
        String[] args = {"arg1", "arg2"};
        when(command.getName()).thenReturn("test");

        commandDispatcher.dispatchExecute(issuer, commandName, args);

        verify(command, never()).execute(issuer, args);
        verify(noPermissionSender, never()).accept(issuer);
    }

    @Test
    public void testDispatchTabulate_WhenCommandExists_AndIssuerHasPermission_ShouldReturnTabulatedResults() {
        String commandName = "test";
        String[] args = {"arg1", "arg2"};
        Set<String> expectedTabulatedResults = Set.of("tab1", "tab2");
        when(command.getName()).thenReturn(commandName);
        when(command.getPermission()).thenReturn("permission.test");
        when(issuer.hasPermission("permission.test")).thenReturn(true);
        when(command.getTabulate(issuer, args)).thenReturn(expectedTabulatedResults);

        Set<String> result = commandDispatcher.dispatchTabulate(issuer, commandName, args);

        assertEquals(expectedTabulatedResults, result);
        verify(command).getTabulate(issuer, args);
    }

    @Test
    public void testDispatchTabulate_WhenCommandExists_AndIssuerLacksPermission_ShouldReturnEmptySet() {
        String commandName = "test";
        String[] args = {"arg1", "arg2"};
        when(command.getName()).thenReturn(commandName);
        when(command.getPermission()).thenReturn("permission.test");
        when(issuer.hasPermission("permission.test")).thenReturn(false);

        Set<String> result = commandDispatcher.dispatchTabulate(issuer, commandName, args);

        assertTrue(result.isEmpty());
        verify(command, never()).getTabulate(issuer, args);
    }

    @Test
    public void testDispatchTabulate_WhenCommandDoesNotExist_ShouldReturnEmptySet() {
        String commandName = "nonexistent";
        String[] args = {"arg1", "arg2"};
        when(command.getName()).thenReturn("test");

        Set<String> result = commandDispatcher.dispatchTabulate(issuer, commandName, args);

        assertTrue(result.isEmpty());
        verify(command, never()).getTabulate(issuer, args);
    }

    @Test
    public void testGetCommands_ShouldReturnSetOfCommandNamesInLowerCase() {
        when(command.getName()).thenReturn("TestCommand");

        Set<String> result = commandDispatcher.getCommands();

        assertEquals(Set.of("testcommand"), result);
    }

    @Test
    public void testGetCommands_WhenNoCommands_ShouldReturnEmptySet() {
        commandDispatcher = new CommandDispatcher(noPermissionSender, Set.of());

        Set<String> result = commandDispatcher.getCommands();

        assertTrue(result.isEmpty());
    }
}
