package ru.nightmirror.wlbytime.listeners;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;
import ru.nightmirror.wlbytime.main.Config;
import ru.nightmirror.wlbytime.main.Database;
import ru.nightmirror.wlbytime.main.Util;
import ru.nightmirror.wlbytime.main.WhitelistByTime;

import java.util.ArrayList;
import java.util.List;

public class WhitelistCmdListener implements Listener {

    private WhitelistByTime plugin;

    public WhitelistCmdListener(WhitelistByTime plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerWhitelistCommand(PlayerCommandPreprocessEvent event) {
        String cmd = (event.getMessage() + " ").split(" ")[0];
        if (cmd.equals("/whitelist") || cmd.equals("whitelist")) {
            event.setCancelled(true);

            final Player sender = event.getPlayer();
            final Config config = Config.getInstance();
            final Database database = Database.getInstance();

            final String[] strings = event.getMessage()
                    .replaceAll("/whitelist", " ")
                    .replaceAll("whitelist", " ")
                    .trim()
                    .split(" ");

            // Help (..me, please)
            if (strings.length == 0 || strings[0].equals("")) {
                for (String line : config.getList("help")) {
                    sender.sendMessage(line);
                }

                return;
            }
            // Add
            if (strings.length > 1 && strings[0].equals("add")) {
                if (sender.hasPermission("whitelistbytime.add")) {
                    String addNickname = strings[1];

                    if (!database.checkPlayer(addNickname)) {
                        List<String> timeArguments = new ArrayList<>();

                        long current = System.currentTimeMillis()+1000L;
                        long until = current;

                        if (strings.length > 2) {
                            for (int i = 2; i < strings.length; i++) {
                                until += Util.getTimeMs(strings[i]);
                            }
                        }

                        if (until == current) until = -1L;

                        database.addPlayer(addNickname, until);

                        if (until == -1L) {
                            sender.sendMessage(config.getLine("successfully-added")
                                    .replaceAll("%player%", addNickname));
                        } else {
                            sender.sendMessage(config.getLine("successfully-added-time")
                                    .replaceAll("%player%", addNickname)
                                    .replaceAll("%time%", Util.getTimeLine(until - System.currentTimeMillis())));
                        }
                    } else {
                        sender.sendMessage(config.getLine("player-already-in-whitelist")
                                .replaceAll("%player%", addNickname));
                    }
                } else {
                    sender.sendMessage(config.getLine("not-permission"));
                }
            }

            // Remove
            if (strings.length > 1 && strings[0].equals("remove")) {
                if (sender.hasPermission("whitelistbytime.remove")) {
                    String removeNickname = strings[1];

                    if (database.checkPlayer(removeNickname)) {
                        database.removePlayer(removeNickname);

                        sender.sendMessage(config.getLine("player-removed-from-whitelist")
                                    .replaceAll("%player%", removeNickname));
                    } else {
                        sender.sendMessage(config.getLine("player-not-in-whitelist")
                                .replaceAll("%player%", removeNickname));
                    }
                } else {
                    sender.sendMessage(config.getLine("not-permission"));
                }
            }

            // Check
            if (strings.length > 1 && strings[0].equals("check")) {
                if (sender.hasPermission("whitelistbytime.check")) {
                    String checkNickname = strings[1];

                    if (database.checkPlayer(checkNickname)) {

                        long until = database.getUntil(checkNickname);

                        if (until == -1L) {
                            sender.sendMessage(config.getLine("still-in-whitelist")
                                    .replaceAll("%player%", checkNickname));
                        } else {
                            String time = Util.getTimeLine((until - System.currentTimeMillis()));

                            sender.sendMessage(config.getLine("still-in-whitelist-time")
                                    .replaceAll("%player%", checkNickname)
                                    .replaceAll("%time%", time));
                        }

                    } else {
                        sender.sendMessage(config.getLine("player-not-in-whitelist")
                                .replaceAll("%player%", checkNickname));
                    }
                } else {
                    sender.sendMessage(config.getLine("not-permission"));
                }
            }

            // Reload
            if (strings[0].equals("reload")) {
                if (sender.hasPermission("whitelistbytime.reload")) {
                    database.init(plugin);
                    config.checkConfig(plugin);

                    sender.sendMessage(config.getLine("plugin-reloaded"));
                } else {
                    sender.sendMessage(config.getLine("not-permission"));
                }
            }

            // Get all
            if (strings[0].equals("getall")) {
                if (sender.hasPermission("whitelistbytime.getall")) {
                    List<String> list = database.getAll();
                    if (list != null && !list.isEmpty()) {
                        sender.sendMessage(config.getLine("list-title"));

                        for (String nickname : list) {
                            String time;
                            long util = database.getUntil(nickname);

                            if (util == -1L) {
                                time = "forever";
                            } else {
                                time = Util.getTimeLine(util - System.currentTimeMillis());
                            }

                            sender.sendMessage(config.getLine("list-player")
                                    .replaceAll("%player%", nickname)
                                    .replaceAll("%time%", time.trim()));
                        }
                    } else {
                        sender.sendMessage(config.getLine("list-empty"));
                    }
                } else {
                    sender.sendMessage(config.getLine("not-permission"));
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
            final Config config = Config.getInstance();
            final Database database = Database.getInstance();

            final String[] strings = event.getCommand()
                    .replaceAll("/whitelist", " ")
                    .replaceAll("whitelist", " ")
                    .trim()
                    .split(" ");

            // Help (..me, please)
            if (strings.length == 0 || strings[0].equals("")) {
                for (String line : config.getList("help")) {
                    sender.sendMessage(line);
                }

                return;
            }
            // Add
            if (strings.length > 1 && strings[0].equals("add")) {
                String addNickname = strings[1];

                if (!database.checkPlayer(addNickname)) {
                    List<String> timeArguments = new ArrayList<>();

                    long current = System.currentTimeMillis()+1000L;
                    long until = current;

                    if (strings.length > 2) {
                        for (int i = 2; i < strings.length; i++) {
                            until += Util.getTimeMs(strings[i]);
                        }
                    }

                    if (until == current) until = -1L;

                    database.addPlayer(addNickname, until);

                    if (until == -1L) {
                        sender.sendMessage(config.getLine("successfully-added")
                                .replaceAll("%player%", addNickname));
                    } else {
                        sender.sendMessage(config.getLine("successfully-added-time")
                                .replaceAll("%player%", addNickname)
                                .replaceAll("%time%", Util.getTimeLine(until - System.currentTimeMillis())));
                    }
                } else {
                    sender.sendMessage(config.getLine("player-already-in-whitelist")
                            .replaceAll("%player%", addNickname));
                }
            }

            // Remove
            if (strings.length > 1 && strings[0].equals("remove")) {
                String removeNickname = strings[1];

                if (database.checkPlayer(removeNickname)) {
                    database.removePlayer(removeNickname);

                    sender.sendMessage(config.getLine("player-removed-from-whitelist")
                            .replaceAll("%player%", removeNickname));
                } else {
                    sender.sendMessage(config.getLine("player-not-in-whitelist")
                            .replaceAll("%player%", removeNickname));
                }
            }

            // Check
            if (strings.length > 1 && strings[0].equals("check")) {
                String checkNickname = strings[1];

                if (database.checkPlayer(checkNickname)) {

                    long until = database.getUntil(checkNickname);

                    if (until == -1L) {
                        sender.sendMessage(config.getLine("still-in-whitelist")
                                .replaceAll("%player%", checkNickname));
                    } else {
                        String time = Util.getTimeLine((until - System.currentTimeMillis()));

                        sender.sendMessage(config.getLine("still-in-whitelist-time")
                                .replaceAll("%player%", checkNickname)
                                .replaceAll("%time%", time));
                    }

                } else {
                    sender.sendMessage(config.getLine("player-not-in-whitelist")
                            .replaceAll("%player%", checkNickname));
                }
            }

            // Reload
            if (strings[0].equals("reload")) {
                database.init(plugin);
                config.checkConfig(plugin);

                sender.sendMessage(config.getLine("plugin-reloaded"));
            }

            // Get all
            if (strings[0].equals("getall")) {
                List<String> list = database.getAll();
                if (list != null && !list.isEmpty()) {
                    sender.sendMessage(config.getLine("list-title"));

                    for (String nickname : list) {
                        String time;
                        long util = database.getUntil(nickname);

                        if (util == -1L) {
                            time = "forever";
                        } else {
                            time = Util.getTimeLine(util - System.currentTimeMillis());
                        }

                        sender.sendMessage(config.getLine("list-player")
                                .replaceAll("%player%", nickname)
                                .replaceAll("%time%", time.trim()));
                    }
                } else {
                    sender.sendMessage(config.getLine("list-empty"));
                }
            }
        }
    }
}
