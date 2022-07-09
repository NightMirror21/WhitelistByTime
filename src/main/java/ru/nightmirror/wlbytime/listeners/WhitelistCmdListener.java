package ru.nightmirror.wlbytime.listeners;

import lombok.RequiredArgsConstructor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.plugin.Plugin;
import ru.nightmirror.wlbytime.config.ConfigUtils;
import ru.nightmirror.wlbytime.convertors.ColorsConvertor;
import ru.nightmirror.wlbytime.convertors.TimeConvertor;
import ru.nightmirror.wlbytime.database.IDatabase;

import java.util.List;

@RequiredArgsConstructor
public class WhitelistCmdListener implements Listener {

    private final IDatabase database;
    private final Plugin plugin;

    @EventHandler
    private void onPlayerWhitelistCommand(PlayerCommandPreprocessEvent event) {
        String cmd = (event.getMessage() + " ").split(" ")[0];

        if (cmd.equals("/whitelist") || cmd.equals("whitelist") || cmd.equals("/wl") || cmd.equals("wl")) {
            event.setCancelled(true);

            final Player sender = event.getPlayer();

            final String[] strings = event.getMessage()
                    .replaceAll("/whitelist", " ")
                    .replaceAll("/wl", " ")
                    .replaceAll("whitelist", " ")
                    .replaceAll("wl", " ")
                    .trim()
                    .split(" ");

            // Help (..me, please)
            if (strings.length == 0 || strings[0].equals("")) {
                sendHelp(sender, strings);

                return;
            }

            // Add
            if (strings.length > 1 && strings[0].equals("add")) {
                if (sender.hasPermission("whitelistbytime.add")) {
                    addToWhitelist(sender, strings);
                } else {
                    sender.sendMessage(ColorsConvertor.convert(plugin.getConfig().getString("minecraft-commands.not-permission")));
                }

                return;
            }

            // Remove
            if (strings.length > 1 && strings[0].equals("remove")) {
                if (sender.hasPermission("whitelistbytime.remove")) {
                    removeFromWhitelist(sender, strings);
                } else {
                    sender.sendMessage(ColorsConvertor.convert(plugin.getConfig().getString("not-permission")));
                }

                return;
            }

            // Check
            if (strings.length > 1 && strings[0].equals("check")) {
                if (sender.hasPermission("whitelistbytime.check")) {
                    checkInWhitelist(sender, strings);
                } else {
                    sender.sendMessage(ColorsConvertor.convert(plugin.getConfig().getString("minecraft-commands.not-permission")));
                }

                return;
            }

            // Reload
            if (strings[0].equals("reload")) {
                if (sender.hasPermission("whitelistbytime.reload")) {
                    reload(sender, strings);
                } else {
                    sender.sendMessage(ColorsConvertor.convert(plugin.getConfig().getString("minecraft-commands.not-permission")));
                }

                return;
            }

            // Get all
            if (strings[0].equals("getall")) {
                if (sender.hasPermission("whitelistbytime.getall")) {
                    getAll(sender, strings);
                } else {
                    sender.sendMessage(ColorsConvertor.convert(plugin.getConfig().getString("minecraft-commands.not-permission")));
                }
            }
        }
    }

    @EventHandler
    public void onConsoleWhitelistCommand(ServerCommandEvent event) {
        String cmd = (event.getCommand() + " ").split(" ")[0];
        if (cmd.equals("/whitelist") || cmd.equals("whitelist")) {
            event.setCancelled(true);

            final CommandSender sender = event.getSender();

            final String[] strings = event.getCommand()
                    .replaceAll("/whitelist", " ")
                    .replaceAll("whitelist", " ")
                    .trim()
                    .split(" ");

            // Help (..me, please)
            if (strings.length == 0 || strings[0].equals("")) {
                sendHelp(sender, strings);
                return;
            }

            // Add
            if (strings.length > 1 && strings[0].equals("add")) {
                addToWhitelist(sender, strings);
                return;
            }

            // Remove
            if (strings.length > 1 && strings[0].equals("remove")) {
                removeFromWhitelist(sender, strings);
                return;
            }

            // Check
            if (strings.length > 1 && strings[0].equals("check")) {
                checkInWhitelist(sender, strings);
                return;
            }

            // Reload
            if (strings[0].equals("reload")) {
                reload(sender, strings);
                return;
            }

            // Get all
            if (strings[0].equals("getall")) {
                getAll(sender, strings);
            }
        }
    }

    private void addToWhitelist(CommandSender sender, String[] strings) {
        String addNickname = strings[1];

        if (database.checkPlayer(addNickname)) {
            sender.sendMessage(ColorsConvertor.convert(plugin.getConfig().getString("minecraft-commands.player-already-in-whitelist"))
                    .replaceAll("%player%", addNickname));
            return;
        }


        long current = System.currentTimeMillis()+1000L;
        long until = current;

        if (strings.length > 2) {
            for (int i = 2; i < strings.length; i++) {
                until += TimeConvertor.getTimeMs(plugin, strings[i]);
            }
        }

        if (until == current) until = -1L;

        database.addPlayer(addNickname, until);

        if (until == -1L) {
            sender.sendMessage(ColorsConvertor.convert(plugin.getConfig().getString("minecraft-commands.successfully-added"))
                    .replaceAll("%player%", addNickname));
        } else {
            sender.sendMessage(ColorsConvertor.convert(plugin.getConfig().getString("minecraft-commands.successfully-added-time"))
                    .replaceAll("%player%", addNickname)
                    .replaceAll("%time%", TimeConvertor.getTimeLine(plugin, until - System.currentTimeMillis())));
        }
    }

    private void checkInWhitelist(CommandSender sender, String[] strings) {
        String checkNickname = strings[1];

        if (database.checkPlayer(checkNickname)) {

            long until = database.getUntil(checkNickname);

            if (until == -1L) {
                sender.sendMessage(ColorsConvertor.convert(plugin.getConfig().getString("minecraft-commands.still-in-whitelist"))
                        .replaceAll("%player%", checkNickname));
            } else {
                String time = TimeConvertor.getTimeLine(plugin, (until - System.currentTimeMillis()));

                sender.sendMessage(ColorsConvertor.convert(plugin.getConfig().getString("minecraft-commands.still-in-whitelist-time"))
                        .replaceAll("%player%", checkNickname)
                        .replaceAll("%time%", time));
            }

        } else {
            sender.sendMessage(ColorsConvertor.convert(plugin.getConfig().getString("minecraft-commands.player-not-in-whitelist"))
                    .replaceAll("%player%", checkNickname));
        }
    }

    private void removeFromWhitelist(CommandSender sender, String[] strings) {
        String removeNickname = strings[1];

        if (database.checkPlayer(removeNickname)) {
            database.removePlayer(removeNickname);

            sender.sendMessage(ColorsConvertor.convert(plugin.getConfig().getString("minecraft-commands.player-removed-from-whitelist"))
                    .replaceAll("%player%", removeNickname));
        } else {
            sender.sendMessage(ColorsConvertor.convert(plugin.getConfig().getString("minecraft-commands.player-not-in-whitelist"))
                    .replaceAll("%player%", removeNickname));
        }
    }

    private void getAll(CommandSender sender, String[] strings) {
        List<String> list = database.getAll();
        if (list != null && !list.isEmpty()) {
            sender.sendMessage(ColorsConvertor.convert(plugin.getConfig().getString("minecraft-commands.list-title")));

            for (String nickname : list) {
                String time;
                long util = database.getUntil(nickname);

                if (util == -1L) {
                    time = ColorsConvertor.convert(plugin.getConfig().getString("minecraft-commands.forever"));
                } else {
                    time = TimeConvertor.getTimeLine(plugin, util - System.currentTimeMillis());
                }

                sender.sendMessage(ColorsConvertor.convert(plugin.getConfig().getString("minecraft-commands.list-player"))
                        .replaceAll("%player%", nickname)
                        .replaceAll("%time%", time.trim()));
            }
        } else {
            sender.sendMessage(ColorsConvertor.convert(plugin.getConfig().getString("minecraft-commands.list-empty")));
        }
    }

    private void sendHelp(CommandSender sender, String[] strings) {
        for (String line : ColorsConvertor.convert(plugin.getConfig().getStringList("minecraft-commands.help"))) {
            sender.sendMessage(line);
        }
    }

    private void reload(CommandSender sender, String[] strings) {
        ConfigUtils.checkConfig(plugin);
        database.reload();

        sender.sendMessage(ColorsConvertor.convert(plugin.getConfig().getString("minecraft-commands.plugin-reloaded")));
    }
}
