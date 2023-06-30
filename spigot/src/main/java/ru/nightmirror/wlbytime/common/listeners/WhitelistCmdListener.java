package ru.nightmirror.wlbytime.common.listeners;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;
import ru.nightmirror.wlbytime.interfaces.command.ICommandsExecutor;

import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WhitelistCmdListener implements Listener {

    static List<String> ALIASES = Arrays.asList("/whitelist", "whitelist", "/wl", "wl");
    ICommandsExecutor executor;

    @EventHandler
    private void onPlayerWhitelistCommand(PlayerCommandPreprocessEvent event) {
        final String cmd = (event.getMessage() + " ").split(" ")[0];

        if (ALIASES.contains(cmd)) {
            event.setCancelled(true);

            final Player sender = event.getPlayer();
            final String[] strings = event.getMessage()
                    .replaceAll("/whitelist", " ")
                    .replaceAll("/wl", " ")
                    .replaceAll("whitelist", " ")
                    .replaceAll("wl", " ")
                    .trim()
                    .split(" ");

            executor.execute(sender, strings);
        }
    }

    @EventHandler
    private void onConsoleWhitelistCommand(ServerCommandEvent event) {
        final String cmd = (event.getCommand() + " ").split(" ")[0];

        if (ALIASES.contains(cmd)) {
            event.setCancelled(true);

            final CommandSender sender = event.getSender();
            final String[] strings = event.getCommand()
                    .replaceAll("/whitelist", " ")
                    .replaceAll("/wl", " ")
                    .replaceAll("whitelist", " ")
                    .replaceAll("wl", " ")
                    .trim()
                    .split(" ");

            executor.execute(sender, strings);
        }
    }
}
