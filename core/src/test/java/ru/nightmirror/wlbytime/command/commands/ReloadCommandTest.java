package ru.nightmirror.wlbytime.command.commands;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.nightmirror.wlbytime.config.configs.CommandsConfig;
import ru.nightmirror.wlbytime.config.configs.MessagesConfig;
import ru.nightmirror.wlbytime.interfaces.command.CommandIssuer;
import ru.nightmirror.wlbytime.interfaces.plugin.Reloadable;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ReloadCommandTest {

    private CommandsConfig commandsConfig;
    private MessagesConfig messages;
    private Reloadable reloadable;
    private ReloadCommand reloadCommand;
    private CommandIssuer issuer;

    @BeforeEach
    public void setUp() {
        commandsConfig = mock(CommandsConfig.class);
        messages = mock(MessagesConfig.class);
        reloadable = mock(Reloadable.class);
        issuer = mock(CommandIssuer.class);

        reloadCommand = new ReloadCommand(messages, commandsConfig, reloadable);
    }

    @Test
    public void getPermissionReturnsCorrectPermission() {
        when(commandsConfig.getReloadPermission()).thenReturn("wlbytime.reload");
        assertEquals("wlbytime.reload", reloadCommand.getPermission());
    }

    @Test
    public void getNameReturnsCorrectName() {
        assertEquals("reload", reloadCommand.getName());
    }

    @Test
    public void executeSuccessfulReloadCallsReloadAndSendsSuccessMessage() {
        when(messages.getPluginSuccessfullyReloaded()).thenReturn("Successfully reloaded!");

        reloadCommand.execute(issuer, new String[]{});

        verify(reloadable).reload();
        verify(issuer).sendMessage("Successfully reloaded!");
    }

    @Test
    public void executeReloadThrowsExceptionSendsErrorMessage() {
        when(messages.getPluginReloadedWithErrors()).thenReturn("Reloaded with errors!");
        doThrow(new RuntimeException("Test exception")).when(reloadable).reload();

        reloadCommand.execute(issuer, new String[]{});

        verify(reloadable).reload();
        verify(issuer).sendMessage("Reloaded with errors!");
    }

    @Test
    public void getTabulateAlwaysReturnsEmptySet() {
        Set<String> tabulate = reloadCommand.getTabulate(issuer, new String[]{});
        assertTrue(tabulate.isEmpty());
    }
}
