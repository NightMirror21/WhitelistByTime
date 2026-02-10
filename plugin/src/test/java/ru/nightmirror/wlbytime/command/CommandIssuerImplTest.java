package ru.nightmirror.wlbytime.command;

import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CommandIssuerImplTest {

    @Test
    public void consoleHasPermissionAlwaysTrue() {
        ConsoleCommandSender sender = mock(ConsoleCommandSender.class);
        CommandIssuerImpl issuer = new CommandIssuerImpl(sender);

        assertTrue(issuer.hasPermission("any.permission"));
    }

    @Test
    public void playerUuidPresentWhenSenderIsPlayer() {
        Player player = mock(Player.class);
        UUID uuid = UUID.randomUUID();
        when(player.getUniqueId()).thenReturn(uuid);
        CommandIssuerImpl issuer = new CommandIssuerImpl(player);

        Optional<UUID> result = issuer.getUuid();

        assertEquals(Optional.of(uuid), result);
    }

    @Test
    public void sendMessageConvertsAndSendsComponent() {
        CommandSender sender = mock(CommandSender.class);
        CommandIssuerImpl issuer = new CommandIssuerImpl(sender);

        issuer.sendMessage("test");

        verify(sender).sendMessage(Component.text("test"));
    }
}
