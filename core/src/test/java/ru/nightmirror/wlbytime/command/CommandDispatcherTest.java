package ru.nightmirror.wlbytime.command;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.nightmirror.wlbytime.config.configs.MessagesConfig;
import ru.nightmirror.wlbytime.interfaces.command.Command;
import ru.nightmirror.wlbytime.interfaces.command.CommandIssuer;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class CommandDispatcherTest {

    private CommandDispatcher commandDispatcher;
    private MessagesConfig messagesConfig;
    private CommandIssuer issuer;
    private Command command;

    @BeforeEach
    public void setUp() {
        messagesConfig = mock(MessagesConfig.class);
        issuer = mock(CommandIssuer.class);
        command = mock(Command.class);

        when(messagesConfig.getNotPermission()).thenReturn("You do not have permission!");

        Set<Command> commands = Set.of(command);
        commandDispatcher = new CommandDispatcher(messagesConfig, commands);
    }

    @Test
    public void dispatchExecuteWhenCommandExistsAndIssuerHasPermissionExecutesCommand() {
        String commandName = "test";
        String[] args = {"arg1", "arg2"};
        when(command.getName()).thenReturn(commandName);
        when(command.getPermissions()).thenReturn(Set.of("permission.test", "permission.alt"));
        when(issuer.hasPermission("permission.test")).thenReturn(false);
        when(issuer.hasPermission("permission.alt")).thenReturn(true);

        commandDispatcher.dispatchExecute(issuer, commandName, args);

        verify(command).execute(issuer, args);
        verify(issuer, never()).sendMessage("You do not have permission!");
    }

    @Test
    public void dispatchExecuteWhenCommandExistsAndIssuerLacksPermissionSendsNoPermissionMessage() {
        String commandName = "test";
        String[] args = {"arg1", "arg2"};
        when(command.getName()).thenReturn(commandName);
        when(command.getPermissions()).thenReturn(Set.of("permission.test", "permission.alt"));
        when(issuer.hasPermission("permission.test")).thenReturn(false);
        when(issuer.hasPermission("permission.alt")).thenReturn(false);

        commandDispatcher.dispatchExecute(issuer, commandName, args);

        verify(issuer).sendMessage("You do not have permission!");
        verify(command, never()).execute(issuer, args);
    }

    @Test
    public void dispatchExecuteWhenCommandDoesNotExistDoesNothing() {
        String commandName = "nonexistent";
        String[] args = {"arg1", "arg2"};
        when(command.getName()).thenReturn("test");

        commandDispatcher.dispatchExecute(issuer, commandName, args);

        verify(command, never()).execute(issuer, args);
        verify(issuer, never()).sendMessage("You do not have permission!");
    }

    @Test
    public void dispatchTabulateWhenCommandExistsAndIssuerHasPermissionReturnsTabulatedResults() {
        String commandName = "test";
        String[] args = {"arg1", "arg2"};
        Set<String> expectedTabulatedResults = Set.of("tab1", "tab2");
        when(command.getName()).thenReturn(commandName);
        when(command.getPermissions()).thenReturn(Set.of("permission.test", "permission.alt"));
        when(issuer.hasPermission("permission.test")).thenReturn(false);
        when(issuer.hasPermission("permission.alt")).thenReturn(true);
        when(command.getTabulate(issuer, args)).thenReturn(expectedTabulatedResults);

        Set<String> result = commandDispatcher.dispatchTabulate(issuer, commandName, args);

        assertEquals(expectedTabulatedResults, result);
        verify(command).getTabulate(issuer, args);
    }

    @Test
    public void dispatchTabulateWhenCommandExistsAndIssuerLacksPermissionReturnsEmptySet() {
        String commandName = "test";
        String[] args = {"arg1", "arg2"};
        when(command.getName()).thenReturn(commandName);
        when(command.getPermissions()).thenReturn(Set.of("permission.test", "permission.alt"));
        when(issuer.hasPermission("permission.test")).thenReturn(false);
        when(issuer.hasPermission("permission.alt")).thenReturn(false);

        Set<String> result = commandDispatcher.dispatchTabulate(issuer, commandName, args);

        assertTrue(result.isEmpty());
        verify(command, never()).getTabulate(issuer, args);
    }

    @Test
    public void dispatchTabulateWhenCommandDoesNotExistReturnsEmptySet() {
        String commandName = "nonexistent";
        String[] args = {"arg1", "arg2"};
        when(command.getName()).thenReturn("test");

        Set<String> result = commandDispatcher.dispatchTabulate(issuer, commandName, args);

        assertTrue(result.isEmpty());
        verify(command, never()).getTabulate(issuer, args);
    }

    @Test
    public void getCommandsReturnsSetOfCommandNamesInLowerCase() {
        when(command.getName()).thenReturn("TestCommand");

        Set<String> result = commandDispatcher.getCommands();

        assertEquals(Set.of("testcommand"), result);
    }

    @Test
    public void getCommandsWhenNoCommandsReturnsEmptySet() {
        commandDispatcher = new CommandDispatcher(messagesConfig, Set.of());

        Set<String> result = commandDispatcher.getCommands();

        assertTrue(result.isEmpty());
    }
}
