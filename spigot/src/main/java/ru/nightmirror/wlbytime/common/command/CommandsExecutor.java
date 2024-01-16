package ru.nightmirror.wlbytime.common.command;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import ru.nightmirror.wlbytime.common.config.configs.MessagesConfig;
import ru.nightmirror.wlbytime.common.convertor.ColorsConvertor;
import ru.nightmirror.wlbytime.common.covertors.time.TimeConvertor;
import ru.nightmirror.wlbytime.common.database.misc.WLPlayer;
import ru.nightmirror.wlbytime.interfaces.IWhitelist;
import ru.nightmirror.wlbytime.interfaces.command.ICommandsExecutor;
import ru.nightmirror.wlbytime.interfaces.database.PlayerAccessor;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CommandsExecutor implements ICommandsExecutor {

    /*
    Why don't I say I'll redo it in the future, and you'll turn a blind eye?
    Seriously, this config system is really dumb.
     */

    PlayerAccessor playerAccessor;
    IWhitelist whitelist;
    TimeConvertor timeConvertor;
    MessagesConfig messages;

    public CommandsExecutor(PlayerAccessor playerAccessor, IWhitelist whitelist, TimeConvertor timeConvertor) {
        this.playerAccessor = playerAccessor;
        this.whitelist = whitelist;
        messages = whitelist.getMessages();
        this.timeConvertor = timeConvertor;
    }

    @Override
    public void reload(CommandSender sender, String[] strings) {
        if (!(sender instanceof ConsoleCommandSender || sender.hasPermission("whitelistbytime.reload"))) {
            sender.sendMessage(ColorsConvertor.convert(messages.notPermission));
            return;
        }

        whitelist.reload();
        sender.sendMessage(ColorsConvertor.convert(messages.pluginReloaded));
    }

    @Override
    public void help(CommandSender sender, String[] strings) {
        if (!(sender instanceof ConsoleCommandSender || sender.hasPermission("whitelistbytime.help"))) {
            sender.sendMessage(ColorsConvertor.convert(messages.notPermission));
            return;
        }
        for (String line : ColorsConvertor.convert(messages.help)) {
            sender.sendMessage(line);
        }
    }

    @Override
    public void getAll(CommandSender sender, String[] strings) {
        if (!(sender instanceof ConsoleCommandSender || sender.hasPermission("whitelistbytime.getall"))) {
            sender.sendMessage(ColorsConvertor.convert(messages.notPermission));
            return;
        }

        playerAccessor.getPlayers().thenAccept(players -> {
            if (players.size() == 0) {
                sender.sendMessage(ColorsConvertor.convert(messages.listEmpty));
                return;
            }

            players.forEach(player -> {
                sender.sendMessage(ColorsConvertor.convert(messages.listTitle));
                String time;

                if (player.getUntil() == -1L) {
                    time = ColorsConvertor.convert(messages.forever);
                } else {
                    time = timeConvertor.getTimeLine(player.getUntil() - System.currentTimeMillis());
                }

                sender.sendMessage(ColorsConvertor.convert(messages.listPlayer)
                        .replaceAll("%player%", player.getNickname())
                        .replaceAll("%time%", time.trim()));
            });
        });
    }

    @Override
    public void remove(CommandSender sender, String[] strings) {
        if (!(sender instanceof ConsoleCommandSender || sender.hasPermission("whitelistbytime.remove"))) {
            sender.sendMessage(ColorsConvertor.convert(messages.notPermission));
            return;
        }

        String removeNickname = strings[1];
        playerAccessor.delete(removeNickname).thenAccept(deleted -> {
            if (deleted) {
                sender.sendMessage(ColorsConvertor.convert(messages.playerRemovedFromWhitelist)
                        .replaceAll("%player%", removeNickname));
            } else {
                sender.sendMessage(ColorsConvertor.convert(messages.playerNotInWhitelist)
                        .replaceAll("%player%", removeNickname));
            }
        });
    }

    @Override
    public void check(CommandSender sender, String[] strings) {
        if (!(sender instanceof ConsoleCommandSender || sender.hasPermission("whitelistbytime.check"))) {
            sender.sendMessage(ColorsConvertor.convert(messages.notPermission));
            return;
        }

        String checkNickname = strings[1];
        playerAccessor.getPlayer(checkNickname).thenAccept(playerOptional -> playerOptional.ifPresentOrElse(player -> {
            if (player.getUntil() == -1) {
                sender.sendMessage(ColorsConvertor.convert(messages.stillInWhitelist)
                        .replaceAll("%player%", checkNickname));
            } else {
                String time = timeConvertor.getTimeLine(player.getUntil() - System.currentTimeMillis());
                sender.sendMessage(ColorsConvertor.convert(messages.stillInWhitelistForTime)
                        .replaceAll("%player%", checkNickname)
                        .replaceAll("%time%", time));
            }
        }, () -> sender.sendMessage(ColorsConvertor.convert(messages.playerNotInWhitelist)
                .replaceAll("%player%", checkNickname))));
    }

    @Override
    public void checkme(CommandSender sender) {
        if (!(sender instanceof ConsoleCommandSender || sender.hasPermission("whitelistbytime.checkme"))) {
            sender.sendMessage(ColorsConvertor.convert(messages.notPermission));
            return;
        }

        playerAccessor.getPlayer(sender.getName()).thenAccept(playerOptional -> playerOptional.ifPresentOrElse(player -> {
            if (player.getUntil() == -1) {
                sender.sendMessage(ColorsConvertor.convert(messages.stillInWhitelist));
            } else {
                String time = timeConvertor.getTimeLine(player.getUntil() - System.currentTimeMillis());
                sender.sendMessage(ColorsConvertor.convert(messages.stillInWhitelistForTime)
                        .replaceAll("%time%", time));
            }
        }, () -> sender.sendMessage(ColorsConvertor.convert(messages.playerNotInWhitelist)
                .replaceAll("%player%", sender.getName()))));
    }

    @Override
    public void add(CommandSender sender, String[] strings) {
        if (!(sender instanceof ConsoleCommandSender || sender.hasPermission("whitelistbytime.add"))) {
            sender.sendMessage(ColorsConvertor.convert(messages.notPermission));
            return;
        }

        String addNickname = strings[1];
        playerAccessor.getPlayer(addNickname).thenAccept(playerOptional -> playerOptional.ifPresentOrElse(player -> sender.sendMessage(ColorsConvertor.convert(messages.playerAlreadyInWhitelist)
                .replaceAll("%player%", addNickname)), () -> {
            long current = System.currentTimeMillis();
            long until = current;

            if (strings.length > 2) {
                for (int i = 2; i < strings.length; i++) {
                    until += timeConvertor.getTimeMs(strings[i]);
                }
            }

            if (until == current) until = -1L;
            long finalUntil = until;
            playerAccessor.createOrUpdate(new WLPlayer(addNickname, until)).thenRun(() -> {
                if (finalUntil == -1L) {
                    sender.sendMessage(ColorsConvertor.convert(messages.successfullyAdded)
                            .replaceAll("%player%", addNickname));
                } else {
                    sender.sendMessage(ColorsConvertor.convert(messages.successfullyAddedForTime)
                            .replaceAll("%player%", addNickname)
                            .replaceAll("%time%", timeConvertor.getTimeLine(finalUntil - System.currentTimeMillis() + 1000L)));
                }
            });
        }));
    }

    @Override
    public void time(CommandSender sender, String[] strings) {
        if (!(sender instanceof ConsoleCommandSender || sender.hasPermission("whitelistbytime.time"))) {
            sender.sendMessage(ColorsConvertor.convert(messages.notPermission));
            return;
        }

        String nickname = strings[2];
        playerAccessor.getPlayer(nickname).thenAccept(playerOptional -> {
            long tempUntil = System.currentTimeMillis();
            for (int i = 3; i < strings.length; i++) tempUntil += timeConvertor.getTimeMs(strings[i]);
            long until = tempUntil;
            switch (strings[1]) {
                case "set" -> playerOptional.ifPresentOrElse(player -> {
                    player.setUntil(until);
                    playerAccessor.createOrUpdate(player).thenRun(() -> sender.sendMessage(ColorsConvertor.convert(messages.setTime)
                            .replaceAll("%time%", timeConvertor.getTimeLine(until - System.currentTimeMillis() + 1000L))
                            .replaceAll("%player%", nickname)));
                }, () -> {
                    WLPlayer player = new WLPlayer(nickname, until);
                    player.setUntil(until);
                    playerAccessor.createOrUpdate(player).thenRun(() -> sender.sendMessage(ColorsConvertor.convert(messages.successfullyAddedForTime)
                            .replaceAll("%time%", timeConvertor.getTimeLine(until - System.currentTimeMillis() + 1000L))
                            .replaceAll("%player%", nickname)));
                });
                case "add" -> playerOptional.ifPresentOrElse(player -> {
                    player.setUntil(player.getUntil() + (until - System.currentTimeMillis()));
                    playerAccessor.createOrUpdate(player).thenRun(() -> sender.sendMessage(ColorsConvertor.convert(messages.addTime)
                            .replaceAll("%time%", timeConvertor.getTimeLine(until - System.currentTimeMillis() + 1000L))
                            .replaceAll("%player%", nickname)));
                }, () -> {
                    WLPlayer player = new WLPlayer(nickname, until);
                    playerAccessor.createOrUpdate(player).thenRun(() -> sender.sendMessage(ColorsConvertor.convert(messages.successfullyAddedForTime)
                            .replaceAll("%time%", timeConvertor.getTimeLine(until - System.currentTimeMillis() + 1000L))
                            .replaceAll("%player%", nickname)));
                });
                case "remove" -> playerOptional.ifPresentOrElse(player -> {
                    if ((player.getUntil() - (until - System.currentTimeMillis())) > System.currentTimeMillis()) {
                        player.setUntil(player.getUntil() - (until - System.currentTimeMillis()));
                        playerAccessor.createOrUpdate(player).thenRun(() -> sender.sendMessage(ColorsConvertor.convert(messages.removeTime)
                                .replaceAll("%time%", timeConvertor.getTimeLine(until - System.currentTimeMillis() + 1000L))
                                .replaceAll("%player%", nickname)));
                    } else {
                        playerAccessor.delete(player).thenRun(() -> sender.sendMessage(ColorsConvertor.convert(messages.playerRemovedFromWhitelist)
                                .replaceAll("%time%", timeConvertor.getTimeLine(until - System.currentTimeMillis()  + 1000L))
                                .replaceAll("%player%", nickname)));
                    }
                }, () -> sender.sendMessage(ColorsConvertor.convert(messages.playerNotInWhitelist)
                        .replaceAll("%time%", timeConvertor.getTimeLine(until - System.currentTimeMillis()))
                        .replaceAll("%player%", nickname)));
            }
        });
    }

    @Override
    public void turn(CommandSender sender, String[] strings) {
        if (!(sender instanceof ConsoleCommandSender || sender.hasPermission("whitelistbytime.turn"))) {
            sender.sendMessage(ColorsConvertor.convert(messages.notPermission));
            return;
        }

        if (strings[0].equalsIgnoreCase("on")) {
            if (whitelist.isWhitelistEnabled()) {
                sender.sendMessage(ColorsConvertor.convert(messages.whitelistAlreadyEnabled));
            } else {
                sender.sendMessage(ColorsConvertor.convert(messages.whitelistEnabled));
                whitelist.setWhitelistEnabled(true);
            }
        } else {
            if (!whitelist.isWhitelistEnabled()) {
                sender.sendMessage(ColorsConvertor.convert(messages.whitelistAlreadyDisabled));
            } else {
                sender.sendMessage(ColorsConvertor.convert(messages.whitelistDisabled));
                whitelist.setWhitelistEnabled(false);
            }
        }
    }

    @Override
    public void execute(CommandSender sender, String[] strings) {
        if (strings.length == 0 || strings[0].equals("")) {
            help(sender, strings);
        } else if (strings.length > 1 && strings[0].equals("add")) {
            add(sender, strings);
        } else if (strings.length > 1 && strings[0].equals("remove")) {
            remove(sender, strings);
        } else if (strings[0].equals("on") || strings[0].equals("off")) {
            turn(sender, strings);
        } else if (strings.length > 1 && strings[0].equals("check")) {
            check(sender, strings);
        } else if (strings.length == 1 && strings[0].equals("checkme")) {
            checkme(sender);
        } else if (strings[0].equals("reload")) {
            reload(sender, strings);
        } else if (strings.length > 3 && strings[0].equals("time")) {
            time(sender, strings);
        } else if (strings[0].equals("getall")) {
            getAll(sender, strings);
        } else {
            help(sender, strings);
        }
    }
}
