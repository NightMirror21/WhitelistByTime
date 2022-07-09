package ru.nightmirror.wlbytime.executors;

import lombok.RequiredArgsConstructor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.Plugin;
import ru.nightmirror.wlbytime.config.ConfigUtils;
import ru.nightmirror.wlbytime.convertors.ColorsConvertor;
import ru.nightmirror.wlbytime.convertors.TimeConvertor;
import ru.nightmirror.wlbytime.database.IDatabase;

import java.util.List;

@RequiredArgsConstructor
public class CommandsExecutor implements ICommandsExecutor {

    private final IDatabase database;
    private final Plugin plugin;

    @Override
    public void reload(CommandSender sender, String[] strings) {
        if (!(sender instanceof ConsoleCommandSender || sender.hasPermission("whitelistbytime.reload"))) {
            sender.sendMessage(ColorsConvertor.convert(plugin.getConfig().getString("minecraft-commands.not-permission")));
            return;
        }

        ConfigUtils.checkConfig(plugin);
        database.reload();
        sender.sendMessage(ColorsConvertor.convert(plugin.getConfig().getString("minecraft-commands.plugin-reloaded")));
    }

    @Override
    public void help(CommandSender sender, String[] strings) {
        for (String line : ColorsConvertor.convert(plugin.getConfig().getStringList("minecraft-commands.help"))) {
            sender.sendMessage(line);
        }
    }

    @Override
    public void getAll(CommandSender sender, String[] strings) {
        if (!(sender instanceof ConsoleCommandSender || sender.hasPermission("whitelistbytime.getall"))) {
            sender.sendMessage(ColorsConvertor.convert(plugin.getConfig().getString("minecraft-commands.not-permission")));
            return;
        }

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

    @Override
    public void remove(CommandSender sender, String[] strings) {
        if (!(sender instanceof ConsoleCommandSender || sender.hasPermission("whitelistbytime.remove"))) {
            sender.sendMessage(ColorsConvertor.convert(plugin.getConfig().getString("minecraft-commands.not-permission")));
            return;
        }

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

    @Override
    public void check(CommandSender sender, String[] strings) {
        if (!(sender instanceof ConsoleCommandSender || sender.hasPermission("whitelistbytime.check"))) {
            sender.sendMessage(ColorsConvertor.convert(plugin.getConfig().getString("minecraft-commands.not-permission")));
            return;
        }

        String checkNickname = strings[1];

        if (database.checkPlayer(checkNickname)) {

            long until = database.getUntil(checkNickname);

            if (until == -1L) {
                sender.sendMessage(ColorsConvertor.convert(plugin.getConfig().getString("minecraft-commands.still-in-whitelist"))
                        .replaceAll("%player%", checkNickname));
            } else {
                String time = TimeConvertor.getTimeLine(plugin, (until - System.currentTimeMillis()));

                sender.sendMessage(ColorsConvertor.convert(plugin.getConfig().getString("minecraft-commands.still-in-whitelist-for-time"))
                        .replaceAll("%player%", checkNickname)
                        .replaceAll("%time%", time));
            }

        } else {
            sender.sendMessage(ColorsConvertor.convert(plugin.getConfig().getString("minecraft-commands.player-not-in-whitelist"))
                    .replaceAll("%player%", checkNickname));
        }
    }

    @Override
    public void add(CommandSender sender, String[] strings) {
        if (!(sender instanceof ConsoleCommandSender || sender.hasPermission("whitelistbytime.add"))) {
            sender.sendMessage(ColorsConvertor.convert(plugin.getConfig().getString("minecraft-commands.not-permission")));
            return;
        }

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
            sender.sendMessage(ColorsConvertor.convert(plugin.getConfig().getString("minecraft-commands.successfully-added-for-time"))
                    .replaceAll("%player%", addNickname)
                    .replaceAll("%time%", TimeConvertor.getTimeLine(plugin, until - System.currentTimeMillis())));
        }
    }

    @Override
    public void time(CommandSender sender, String[] strings) {
        String nickname = strings[2];
        boolean playerExists = database.checkPlayer(nickname);

        long current = System.currentTimeMillis()+1000L;
        long until = current;

        for (int i = 3; i < strings.length; i++) {
            until += TimeConvertor.getTimeMs(plugin, strings[i]);
        }

        switch (strings[1]) {
            case "set":
                if (playerExists) {
                    sender.sendMessage(ColorsConvertor.convert(plugin.getConfig().getString("minecraft-commands.set-time"))
                            .replaceAll("%time%", TimeConvertor.getTimeLine(plugin, until-System.currentTimeMillis()))
                            .replaceAll("%player%", nickname));
                    database.setUntil(nickname, until);
                } else {
                    sender.sendMessage(ColorsConvertor.convert(plugin.getConfig().getString("minecraft-commands.successfully-added-for-time"))
                            .replaceAll("%time%", TimeConvertor.getTimeLine(plugin, until-System.currentTimeMillis()-1000L))
                            .replaceAll("%player%", nickname));
                    database.addPlayer(nickname, until);
                }
                break;
            case "add":
                if (playerExists) {
                    sender.sendMessage(ColorsConvertor.convert(plugin.getConfig().getString("minecraft-commands.add-time"))
                            .replaceAll("%time%", TimeConvertor.getTimeLine(plugin, until-System.currentTimeMillis()-1000L))
                            .replaceAll("%player%", nickname));
                    database.setUntil(nickname, (database.getUntil(nickname) + (until-System.currentTimeMillis())));
                } else {
                    sender.sendMessage(ColorsConvertor.convert(plugin.getConfig().getString("minecraft-commands.successfully-added-for-time"))
                            .replaceAll("%time%", TimeConvertor.getTimeLine(plugin, until-System.currentTimeMillis()))
                            .replaceAll("%player%", nickname));
                    database.addPlayer(nickname, until);
                }
                break;
            case "remove":
                if (playerExists) {
                    if ((database.getUntil(nickname) - (until - System.currentTimeMillis())) > System.currentTimeMillis()) {
                        sender.sendMessage(ColorsConvertor.convert(plugin.getConfig().getString("minecraft-commands.remove-time"))
                                .replaceAll("%time%", TimeConvertor.getTimeLine(plugin, until - System.currentTimeMillis()))
                                .replaceAll("%player%", nickname));
                        database.setUntil(nickname, (database.getUntil(nickname) - (until - System.currentTimeMillis())));
                    } else {
                        sender.sendMessage(ColorsConvertor.convert(plugin.getConfig().getString("minecraft-commands.player-removed-from-whitelist"))
                                .replaceAll("%time%", TimeConvertor.getTimeLine(plugin, until - System.currentTimeMillis()))
                                .replaceAll("%player%", nickname));
                        database.removePlayer(nickname);
                    }
                } else {
                    sender.sendMessage(ColorsConvertor.convert(plugin.getConfig().getString("minecraft-commands.player-not-in-whitelist"))
                            .replaceAll("%time%", TimeConvertor.getTimeLine(plugin, until-System.currentTimeMillis()))
                            .replaceAll("%player%", nickname));
                }
                break;
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
        } else if (strings.length > 1 && strings[0].equals("check")) {
            check(sender, strings);
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
