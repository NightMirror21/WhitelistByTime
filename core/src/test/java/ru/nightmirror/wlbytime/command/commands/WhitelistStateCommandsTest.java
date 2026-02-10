package ru.nightmirror.wlbytime.command.commands;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.nightmirror.wlbytime.config.configs.CommandsConfig;
import ru.nightmirror.wlbytime.config.configs.MessagesConfig;
import ru.nightmirror.wlbytime.config.configs.SettingsConfig;
import ru.nightmirror.wlbytime.interfaces.command.CommandIssuer;

import java.nio.file.Path;
import java.util.Set;

import static org.mockito.Mockito.*;

public class WhitelistStateCommandsTest {

    private CommandsConfig commandsConfig;
    private MessagesConfig messages;
    private SettingsConfig settings;
    private CommandIssuer issuer;
    private Path settingsPath;

    private OnCommand onCommand;
    private OffCommand offCommand;
    private StatusCommand statusCommand;

    @BeforeEach
    public void setUp() {
        commandsConfig = mock(CommandsConfig.class);
        messages = mock(MessagesConfig.class);
        settings = mock(SettingsConfig.class);
        issuer = mock(CommandIssuer.class);
        settingsPath = Path.of("settings.yml");

        when(commandsConfig.getTogglePermission()).thenReturn(Set.of("whitelistbytime.toggle", "wlbytime.toggle"));
        when(messages.getIncorrectArguments()).thenReturn("Incorrect arguments.");
        when(messages.getWhitelistEnabled()).thenReturn("Whitelist enabled");
        when(messages.getWhitelistDisabled()).thenReturn("Whitelist disabled");
        when(messages.getWhitelistAlreadyEnabled()).thenReturn("Whitelist already enabled");
        when(messages.getWhitelistAlreadyDisabled()).thenReturn("Whitelist already disabled");
        when(messages.getWhitelistStatusEnabled()).thenReturn("Whitelist is enabled");
        when(messages.getWhitelistStatusDisabled()).thenReturn("Whitelist is disabled");

        onCommand = new OnCommand(commandsConfig, messages, settings, settingsPath);
        offCommand = new OffCommand(commandsConfig, messages, settings, settingsPath);
        statusCommand = new StatusCommand(commandsConfig, messages, settings);
    }

    @Test
    public void onCommandEnablesWhitelistAndSaves() {
        when(settings.isWhitelistEnabled()).thenReturn(false);

        onCommand.execute(issuer, new String[]{});

        verify(settings).setWhitelistEnabled(true);
        verify(settings).save(settingsPath);
        verify(issuer).sendMessage("Whitelist enabled");
    }

    @Test
    public void onCommandAlreadyEnabledSendsMessage() {
        when(settings.isWhitelistEnabled()).thenReturn(true);

        onCommand.execute(issuer, new String[]{});

        verify(issuer).sendMessage("Whitelist already enabled");
        verify(settings, never()).save(any(Path.class));
    }

    @Test
    public void offCommandDisablesWhitelistAndSaves() {
        when(settings.isWhitelistEnabled()).thenReturn(true);

        offCommand.execute(issuer, new String[]{});

        verify(settings).setWhitelistEnabled(false);
        verify(settings).save(settingsPath);
        verify(issuer).sendMessage("Whitelist disabled");
    }

    @Test
    public void offCommandAlreadyDisabledSendsMessage() {
        when(settings.isWhitelistEnabled()).thenReturn(false);

        offCommand.execute(issuer, new String[]{});

        verify(issuer).sendMessage("Whitelist already disabled");
        verify(settings, never()).save(any(Path.class));
    }

    @Test
    public void statusCommandSendsEnabledMessage() {
        when(settings.isWhitelistEnabled()).thenReturn(true);

        statusCommand.execute(issuer, new String[]{});

        verify(issuer).sendMessage("Whitelist is enabled");
    }

    @Test
    public void statusCommandSendsDisabledMessage() {
        when(settings.isWhitelistEnabled()).thenReturn(false);

        statusCommand.execute(issuer, new String[]{});

        verify(issuer).sendMessage("Whitelist is disabled");
    }
}
