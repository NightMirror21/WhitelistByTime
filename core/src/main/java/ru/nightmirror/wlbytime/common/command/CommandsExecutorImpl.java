package ru.nightmirror.wlbytime.common.command;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import ru.nightmirror.wlbytime.common.config.configs.MessagesConfig;
import ru.nightmirror.wlbytime.common.covertors.time.TimeConvertor;
import ru.nightmirror.wlbytime.common.database.misc.PlayerData;
import ru.nightmirror.wlbytime.interfaces.WhitelistByTime;
import ru.nightmirror.wlbytime.interfaces.command.CommandsExecutor;
import ru.nightmirror.wlbytime.interfaces.command.wrappers.WrappedCommandSender;
import ru.nightmirror.wlbytime.interfaces.database.PlayerAccessor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CommandsExecutorImpl implements CommandsExecutor {

    /*
    Why don't I say I'll redo it in the future, and you'll turn a blind eye?
    Seriously, this config system is really dumb.
     */

    PlayerAccessor playerAccessor;
    WhitelistByTime whitelistByTime;
    TimeConvertor timeConvertor;
    MessagesConfig messages;

    public CommandsExecutorImpl(PlayerAccessor playerAccessor, WhitelistByTime whitelistByTime, TimeConvertor timeConvertor) {
        this.playerAccessor = playerAccessor;
        this.whitelistByTime = whitelistByTime;
        messages = whitelistByTime.getMessages();
        this.timeConvertor = timeConvertor;
    }

    @Override
    public void reload(WrappedCommandSender sender, String[] strings) {
        if (!(sender.hasPermission("whitelistbytime.reload"))) {
            sender.sendMessage(messages.notPermission);
            return;
        }

        whitelistByTime.reload();
        sender.sendMessage(messages.pluginReloaded);
    }

    @Override
    public void help(WrappedCommandSender sender, String[] strings) {
        if (!(sender.hasPermission("whitelistbytime.help"))) {
            sender.sendMessage(messages.notPermission);
            return;
        }
        for (String line : messages.help) {
            sender.sendMessage(line);
        }
    }

    @Override
    public void getAll(WrappedCommandSender sender, String[] strings) {
        if (!(sender.hasPermission("whitelistbytime.getall"))) {
            sender.sendMessage(messages.notPermission);
            return;
        }

        playerAccessor.getPlayers()
                .thenApply(list -> list.stream().sorted(Comparator.comparingLong(PlayerData::getUntil).reversed()).toList())
                .thenAccept(players -> {

                    if (players.size() == 0) {
                        sender.sendMessage(messages.listEmpty);
                        return;
                    }

                    int page = 1;
                    if (strings.length > 1) {
                        try {
                            page = Integer.parseInt(strings[1]);
                        } catch (NumberFormatException ignored) {
                            // ignored
                        }
                    }

                    int displayOnPage = 5;
                    int maxPage = players.size() % displayOnPage != 0 ? players.size() / displayOnPage + 1 : players.size() / displayOnPage;

                    if (page > maxPage) {
                        sender.sendMessage(messages.pageNotExists.replaceAll("%page%", String.valueOf(page)));
                        return;
                    }

                    List<PlayerData> toDisplay = new ArrayList<>();

                    for (int i = (page - 1) * displayOnPage; i < Math.min(page * displayOnPage, players.size()); i++) {
                        toDisplay.add(players.get(i));
                    }

                    sender.sendMessage(messages.listTitle);

                    toDisplay.forEach(player -> {
                        String time;

                        if (player.getUntil() == -1L) {
                            time = messages.forever;
                        } else {
                            time = timeConvertor.getTimeLine(player.getUntil() - System.currentTimeMillis());
                        }

                        sender.sendMessage(messages.listPlayer
                                .replaceAll("%player%", player.getNickname())
                                .replaceAll("%time%", time.trim()));

                    });

                    if (maxPage > 1) {
                        sender.sendMessage(messages.listPageableCommands
                                .replaceAll("%current-page%", String.valueOf(page))
                                .replaceAll("%max-page%", String.valueOf(maxPage))

                        );
                    }
                });
    }

    @Override
    public void remove(WrappedCommandSender sender, String[] strings) {
        if (!(sender.hasPermission("whitelistbytime.remove"))) {
            sender.sendMessage(messages.notPermission);
            return;
        }

        String removeNickname = strings[1];
        playerAccessor.delete(removeNickname).thenAccept(deleted -> {
            if (deleted) {
                sender.sendMessage(messages.playerRemovedFromWhitelist
                        .replaceAll("%player%", removeNickname));
            } else {
                sender.sendMessage(messages.playerNotInWhitelist
                        .replaceAll("%player%", removeNickname));
            }
        });
    }

    @Override
    public void check(WrappedCommandSender sender, String[] strings) {
        if (!(sender.hasPermission("whitelistbytime.check"))) {
            sender.sendMessage(messages.notPermission);
            return;
        }

        String checkNickname = strings[1];
        playerAccessor.getPlayer(checkNickname).thenAccept(playerOptional -> playerOptional.ifPresentOrElse(player -> {
            if (player.getUntil() == -1) {
                sender.sendMessage(messages.stillInWhitelist
                        .replaceAll("%player%", checkNickname));
            } else {
                String time = timeConvertor.getTimeLine(player.getUntil() - System.currentTimeMillis());
                sender.sendMessage(messages.stillInWhitelistForTime
                        .replaceAll("%player%", checkNickname)
                        .replaceAll("%time%", time));
            }
        }, () -> sender.sendMessage(messages.playerNotInWhitelist
                .replaceAll("%player%", checkNickname))));
    }

    @Override
    public void checkme(WrappedCommandSender sender) {
        if (!(sender.hasPermission("whitelistbytime.checkme"))) {
            sender.sendMessage(messages.notPermission);
            return;
        }

        playerAccessor.getPlayer(sender.getNickname()).thenAccept(playerOptional -> playerOptional.ifPresentOrElse(player -> {
            if (player.getUntil() == -1) {
                sender.sendMessage(messages.stillInWhitelist);
            } else {
                String time = timeConvertor.getTimeLine(player.getUntil() - System.currentTimeMillis());
                sender.sendMessage(messages.stillInWhitelistForTime
                        .replaceAll("%time%", time));
            }
        }, () -> sender.sendMessage(messages.playerNotInWhitelist
                .replaceAll("%player%", sender.getNickname()))));
    }

    @Override
    public void add(WrappedCommandSender sender, String[] strings) {
        if (!(sender.hasPermission("whitelistbytime.add"))) {
            sender.sendMessage(messages.notPermission);
            return;
        }

        String addNickname = strings[1];
        playerAccessor.getPlayer(addNickname).thenAccept(playerOptional -> playerOptional.ifPresentOrElse(player -> sender.sendMessage(messages.playerAlreadyInWhitelist
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
            playerAccessor.createOrUpdate(new PlayerData(addNickname, until)).thenRun(() -> {
                if (finalUntil == -1L) {
                    sender.sendMessage(messages.successfullyAdded
                            .replaceAll("%player%", addNickname));
                } else {
                    sender.sendMessage(messages.successfullyAddedForTime
                            .replaceAll("%player%", addNickname)
                            .replaceAll("%time%", timeConvertor.getTimeLine(finalUntil - System.currentTimeMillis() + 1000L)));
                }
            });
        }));
    }

    @Override
    public void time(WrappedCommandSender sender, String[] strings) {
        if (!(sender.hasPermission("whitelistbytime.time"))) {
            sender.sendMessage(messages.notPermission);
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
                    playerAccessor.createOrUpdate(player).thenRun(() -> sender.sendMessage(messages.setTime
                            .replaceAll("%time%", timeConvertor.getTimeLine(until - System.currentTimeMillis() + 1000L))
                            .replaceAll("%player%", nickname)));
                }, () -> {
                    PlayerData player = new PlayerData(nickname, until);
                    player.setUntil(until);
                    playerAccessor.createOrUpdate(player).thenRun(() -> sender.sendMessage(messages.successfullyAddedForTime
                            .replaceAll("%time%", timeConvertor.getTimeLine(until - System.currentTimeMillis() + 1000L))
                            .replaceAll("%player%", nickname)));
                });
                case "add" -> playerOptional.ifPresentOrElse(player -> {
                    player.setUntil(player.getUntil() + (until - System.currentTimeMillis()));
                    playerAccessor.createOrUpdate(player).thenRun(() -> sender.sendMessage(messages.addTime
                            .replaceAll("%time%", timeConvertor.getTimeLine(until - System.currentTimeMillis() + 1000L))
                            .replaceAll("%player%", nickname)));
                }, () -> {
                    PlayerData player = new PlayerData(nickname, until);
                    playerAccessor.createOrUpdate(player).thenRun(() -> sender.sendMessage(messages.successfullyAddedForTime
                            .replaceAll("%time%", timeConvertor.getTimeLine(until - System.currentTimeMillis() + 1000L))
                            .replaceAll("%player%", nickname)));
                });
                case "remove" -> playerOptional.ifPresentOrElse(player -> {
                    if ((player.getUntil() - (until - System.currentTimeMillis())) > System.currentTimeMillis()) {
                        player.setUntil(player.getUntil() - (until - System.currentTimeMillis()));
                        playerAccessor.createOrUpdate(player).thenRun(() -> sender.sendMessage(messages.removeTime
                                .replaceAll("%time%", timeConvertor.getTimeLine(until - System.currentTimeMillis() + 1000L))
                                .replaceAll("%player%", nickname)));
                    } else {
                        playerAccessor.delete(player).thenRun(() -> sender.sendMessage(messages.playerRemovedFromWhitelist
                                .replaceAll("%time%", timeConvertor.getTimeLine(until - System.currentTimeMillis() + 1000L))
                                .replaceAll("%player%", nickname)));
                    }
                }, () -> sender.sendMessage(messages.playerNotInWhitelist
                        .replaceAll("%time%", timeConvertor.getTimeLine(until - System.currentTimeMillis()))
                        .replaceAll("%player%", nickname)));
            }
        });
    }

    @Override
    public void turn(WrappedCommandSender sender, String[] strings) {
        if (!(sender.hasPermission("whitelistbytime.turn"))) {
            sender.sendMessage(messages.notPermission);
            return;
        }

        if (strings[0].equalsIgnoreCase("on")) {
            if (whitelistByTime.isWhitelistEnabled()) {
                sender.sendMessage(messages.whitelistAlreadyEnabled);
            } else {
                sender.sendMessage(messages.whitelistEnabled);
                whitelistByTime.setWhitelistEnabled(true);
            }
        } else {
            if (!whitelistByTime.isWhitelistEnabled()) {
                sender.sendMessage(messages.whitelistAlreadyDisabled);
            } else {
                sender.sendMessage(messages.whitelistDisabled);
                whitelistByTime.setWhitelistEnabled(false);
            }
        }
    }

    @Override
    public void execute(WrappedCommandSender sender, String[] strings) {
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
