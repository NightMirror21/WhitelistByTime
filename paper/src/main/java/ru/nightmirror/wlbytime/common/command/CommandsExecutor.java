package ru.nightmirror.wlbytime.common.command;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import ru.nightmirror.wlbytime.common.config.ConfigsContainer;
import ru.nightmirror.wlbytime.common.config.configs.MessagesConfigForPaperFamily;
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
    IWhitelist<MessagesConfigForPaperFamily> whitelist;
    TimeConvertor timeConvertor;
    ConfigsContainer<MessagesConfigForPaperFamily> config;

    public CommandsExecutor(PlayerAccessor playerAccessor, IWhitelist<MessagesConfigForPaperFamily> whitelist, TimeConvertor timeConvertor) {
        this.playerAccessor = playerAccessor;
        this.whitelist = whitelist;

        this.timeConvertor = timeConvertor;
    }

    @Override
    public void reload(CommandSender sender, String[] strings) {
        if (!(sender instanceof ConsoleCommandSender || sender.hasPermission("whitelistbytime.reload"))) {
            sender.sendMessage(ColorsConvertor.convert(config.getString("minecraft-commands.not-permission", "<red>You do not have permission!")));
            return;
        }

        whitelist.reload();
        sender.sendMessage(ColorsConvertor.convert(config.getString("minecraft-commands.plugin-reloaded", "<gold>Plugin reloaded!")));
    }

    @Override
    public void help(CommandSender sender, String[] strings) {
        if (!(sender instanceof ConsoleCommandSender || sender.hasPermission("whitelistbytime.help"))) {
            sender.sendMessage(ColorsConvertor.convert(config.getString("minecraft-commands.not-permission", "<red>You do not have permission!")));
            return;
        }
        ColorsConvertor.convert(config.getStringList("minecraft-commands.help")).forEach(sender::sendMessage);
    }

    @Override
    public void getAll(CommandSender sender, String[] strings) {
        if (!(sender instanceof ConsoleCommandSender || sender.hasPermission("whitelistbytime.getall"))) {
            sender.sendMessage(ColorsConvertor.convert(config.getString("minecraft-commands.not-permission", "<red>You do not have permission!")));
            return;
        }

        playerAccessor.getPlayers().thenAccept(players -> {
            if (players.size() == 0) {
                sender.sendMessage(ColorsConvertor.convert(config.getString("minecraft-commands.list-empty", "<green>Whitelist is empty")));
                return;
            }

            players.forEach(player -> {
                sender.sendMessage(ColorsConvertor.convert(config.getString("minecraft-commands.list-title", "<green>> Whitelist:")));
                Component time;

                if (player.getUntil() == -1L) {
                    time = ColorsConvertor.convert(config.getString("minecraft-commands.forever", "forever"));
                } else {
                    time = ColorsConvertor.convert(timeConvertor.getTimeLine(player.getUntil() - System.currentTimeMillis()));
                }

                sender.sendMessage(ColorsConvertor.convert(config.getString("minecraft-commands.list-player", "<green>|</green> %player% <gray>[%time%]"))
                                .replaceText(builder -> builder.match("%player%").replacement(player.getNickname()))
                                .replaceText(builder -> builder.match("%time%").replacement(time)));
            });
        });
    }

    @Override
    public void remove(CommandSender sender, String[] strings) {
        if (!(sender instanceof ConsoleCommandSender || sender.hasPermission("whitelistbytime.remove"))) {
            sender.sendMessage(ColorsConvertor.convert(config.getString("minecraft-commands.not-permission", "<red>You do not have permission!")));
            return;
        }

        String removeNickname = strings[1];
        playerAccessor.delete(removeNickname).thenAccept(deleted -> {
            if (deleted) {
                sender.sendMessage(ColorsConvertor.convert(config.getString("minecraft-commands.player-removed-from-whitelist", "<yellow>%player% successfully removed from whitelist"))
                        .replaceText(builder -> builder.match("%player%").replacement(removeNickname)));
            } else {
                sender.sendMessage(ColorsConvertor.convert(config.getString("minecraft-commands.player-not-in-whitelist", "<yellow>%player% not in whitelist"))
                        .replaceText(builder -> builder.match("%player%").replacement(removeNickname)));
            }
        });
    }

    @Override
    public void check(CommandSender sender, String[] strings) {
        if (!(sender instanceof ConsoleCommandSender || sender.hasPermission("whitelistbytime.check"))) {
            sender.sendMessage(ColorsConvertor.convert(config.getString("minecraft-commands.not-permission", "<red>You do not have permission!")));
            return;
        }

        String checkNickname = strings[1];
        playerAccessor.getPlayer(checkNickname).thenAccept(playerOptional -> playerOptional.ifPresentOrElse(player -> {
            if (player.getUntil() == -1) {
                sender.sendMessage(ColorsConvertor.convert(config.getString("minecraft-commands.still-in-whitelist", "<green>%player% will be in whitelist forever"))
                        .replaceText(builder -> builder.match("%player%").replacement(checkNickname)));
            } else {
                String time = timeConvertor.getTimeLine(player.getUntil() - System.currentTimeMillis());
                sender.sendMessage(ColorsConvertor.convert(config.getString("minecraft-commands.still-in-whitelist-for-time", "<green>%player% will be in whitelist still %time%"))
                        .replaceText(builder -> builder.match("%player%").replacement(checkNickname))
                        .replaceText(builder -> builder.match("%time%").replacement(time)));
            }
        }, () -> sender.sendMessage(ColorsConvertor.convert(config.getString("minecraft-commands.player-not-in-whitelist", "<yellow>%player% not in whitelist"))
                .replaceText(builder -> builder.match("%player%").replacement(checkNickname)))));
    }

    @Override
    public void checkme(CommandSender sender) {
        if (!(sender instanceof ConsoleCommandSender || sender.hasPermission("whitelistbytime.checkme"))) {
            sender.sendMessage(ColorsConvertor.convert(config.getString("minecraft-commands.not-permission", "<red>You do not have permission!")));
            return;
        }

        playerAccessor.getPlayer(sender.getName()).thenAccept(playerOptional -> playerOptional.ifPresentOrElse(player -> {
            if (player.getUntil() == -1) {
                sender.sendMessage(ColorsConvertor.convert(config.getString("minecraft-commands.checkme-still-in-whitelist", "&fYou are permanently whitelisted")));
            } else {
                String time = timeConvertor.getTimeLine(player.getUntil() - System.currentTimeMillis());
                sender.sendMessage(ColorsConvertor.convert(config.getString("minecraft-commands.checkme-still-in-whitelist-for-time", "&fYou will remain on the whitelist for <green>%time%"))
                        .replaceText(builder -> builder.match("%time%").replacement(time)));
            }
        }, () -> sender.sendMessage(ColorsConvertor.convert(config.getString("minecraft-commands.player-not-in-whitelist", "<yellow>%player% not in whitelist"))
                .replaceText(builder -> builder.match("%player%").replacement(sender.getName())))));
    }

    @Override
    public void add(CommandSender sender, String[] strings) {
        if (!(sender instanceof ConsoleCommandSender || sender.hasPermission("whitelistbytime.add"))) {
            sender.sendMessage(ColorsConvertor.convert(config.getString("minecraft-commands.not-permission", "<red>You do not have permission!")));
            return;
        }

        String addNickname = strings[1];
        playerAccessor.getPlayer(addNickname).thenAccept(playerOptional -> playerOptional.ifPresentOrElse(player -> sender.sendMessage(ColorsConvertor.convert(config.getString("minecraft-commands.player-already-in-whitelist", "<yellow>%player% already in whitelist"))
                .replaceText(builder -> builder.match("%player%").replacement(addNickname))), () -> {
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
                    sender.sendMessage(ColorsConvertor.convert(config.getString("minecraft-commands.successfully-added", "<green>%player% <white>added to whitelist forever"))
                            .replaceText(builder -> builder.match("%player%").replacement(addNickname)));
                } else {
                    sender.sendMessage(ColorsConvertor.convert(config.getString("minecraft-commands.successfully-added-for-time", "<green>%player% <white>added to whitelist for <green>%time%"))
                            .replaceText(builder -> builder.match("%player%").replacement(addNickname))
                            .replaceText(builder -> builder.match("%time%").replacement(timeConvertor.getTimeLine(finalUntil - System.currentTimeMillis() + 1000L))));
                }
            });
        }));
    }

    @Override
    public void time(CommandSender sender, String[] strings) {
        if (!(sender instanceof ConsoleCommandSender || sender.hasPermission("whitelistbytime.time"))) {
            sender.sendMessage(ColorsConvertor.convert(config.getString("minecraft-commands.not-permission", "<red>You do not have permission!")));
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
                    playerAccessor.createOrUpdate(player).thenRun(() -> sender.sendMessage(ColorsConvertor.convert(config.getString("minecraft-commands.set-time", "Now <green>%player% &fwill be in whitelist for <green>%time%"))
                            .replaceText(builder -> builder.match("%player%").replacement(nickname))
                            .replaceText(builder -> builder.match("%time%").replacement(timeConvertor.getTimeLine(until - System.currentTimeMillis() + 1000L)))));
                }, () -> {
                    WLPlayer player = new WLPlayer(nickname, until);
                    player.setUntil(until);
                    playerAccessor.createOrUpdate(player).thenRun(() -> sender.sendMessage(ColorsConvertor.convert(config.getString("minecraft-commands.successfully-added-for-time", "<green>%player% added to whitelist for %time%"))
                            .replaceText(builder -> builder.match("%player%").replacement(nickname))
                            .replaceText(builder -> builder.match("%time%").replacement(timeConvertor.getTimeLine(until - System.currentTimeMillis() + 1000L)))));
                });
                case "add" -> playerOptional.ifPresentOrElse(player -> {
                    player.setUntil(player.getUntil() + (until - System.currentTimeMillis()));
                    playerAccessor.createOrUpdate(player).thenRun(() -> sender.sendMessage(ColorsConvertor.convert(config.getString("minecraft-commands.add-time", "Added <green>%time% &fto <green>%player%"))
                            .replaceText(builder -> builder.match("%player%").replacement(nickname))
                            .replaceText(builder -> builder.match("%time%").replacement(timeConvertor.getTimeLine(until - System.currentTimeMillis() + 1000L)))));
                }, () -> {
                    WLPlayer player = new WLPlayer(nickname, until);
                    playerAccessor.createOrUpdate(player).thenRun(() -> sender.sendMessage(ColorsConvertor.convert(config.getString("minecraft-commands.successfully-added-for-time", "<green>%player% added to whitelist for %time%"))
                            .replaceText(builder -> builder.match("%player%").replacement(nickname))
                            .replaceText(builder -> builder.match("%time%").replacement(timeConvertor.getTimeLine(until - System.currentTimeMillis() + 1000L)))));
                });
                case "remove" -> playerOptional.ifPresentOrElse(player -> {
                    if ((player.getUntil() - (until - System.currentTimeMillis())) > System.currentTimeMillis()) {
                        player.setUntil(player.getUntil() - (until - System.currentTimeMillis()));
                        playerAccessor.createOrUpdate(player).thenRun(() -> sender.sendMessage(ColorsConvertor.convert(config.getString("minecraft-commands.remove-time", "Removed <green>%time% &ffrom <green>%player%"))
                                .replaceText(builder -> builder.match("%player%").replacement(nickname))
                                .replaceText(builder -> builder.match("%time%").replacement(timeConvertor.getTimeLine(until - System.currentTimeMillis() + 1000L)))));
                    } else {
                        playerAccessor.delete(player).thenRun(() -> sender.sendMessage(ColorsConvertor.convert(config.getString("minecraft-commands.player-removed-from-whitelist", "<yellow>%player% successfully removed from whitelist"))
                                .replaceText(builder -> builder.match("%player%").replacement(nickname))
                                .replaceText(builder -> builder.match("%time%").replacement(timeConvertor.getTimeLine(until - System.currentTimeMillis() + 1000L)))));
                    }
                }, () -> sender.sendMessage(ColorsConvertor.convert(config.getString("minecraft-commands.player-not-in-whitelist", "<yellow>%player% not in whitelist"))
                        .replaceText(builder -> builder.match("%player%").replacement(nickname))
                        .replaceText(builder -> builder.match("%time%").replacement(timeConvertor.getTimeLine(until - System.currentTimeMillis() + 1000L)))));
            }
        });
    }

    @Override
    public void turn(CommandSender sender, String[] strings) {
        if (!(sender instanceof ConsoleCommandSender || sender.hasPermission("whitelistbytime.turn"))) {
            sender.sendMessage(ColorsConvertor.convert(config.getString("minecraft-commands.not-permission", "<red>You do not have permission!")));
            return;
        }

        if (strings[0].equalsIgnoreCase("on")) {
            if (whitelist.isWhitelistEnabled()) {
                sender.sendMessage(ColorsConvertor.convert(config.getString("minecraft-commands.whitelist-already-enabled", "<green>WhitelistByTime already enabled")));
            } else {
                sender.sendMessage(ColorsConvertor.convert(config.getString("minecraft-commands.whitelist-enabled", "<green>WhitelistByTime enabled")));
                whitelist.setWhitelistEnabled(true);
            }
        } else {
            if (!whitelist.isWhitelistEnabled()) {
                sender.sendMessage(ColorsConvertor.convert(config.getString("minecraft-commands.whitelist-already-disabled", "<green>WhitelistByTime already disabled")));
            } else {
                sender.sendMessage(ColorsConvertor.convert(config.getString("minecraft-commands.whitelist-disabled", "<green>WhitelistByTime disabled")));
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
