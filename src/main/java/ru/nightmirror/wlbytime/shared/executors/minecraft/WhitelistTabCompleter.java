package ru.nightmirror.wlbytime.shared.executors.minecraft;

import lombok.RequiredArgsConstructor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import ru.nightmirror.wlbytime.interfaces.database.IDatabase;
import ru.nightmirror.wlbytime.shared.WhitelistByTime;
import ru.nightmirror.wlbytime.shared.common.Checker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class WhitelistTabCompleter implements TabCompleter {

    private final IDatabase database;
    private final WhitelistByTime plugin;

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        List<String> args = new ArrayList<>();

        if (strings.length == 1) {
            if (commandSender.hasPermission("whitelistbytime.add")) args.add("add");
            if (commandSender.hasPermission("whitelistbytime.turn")) {
                if (plugin.isWhitelistEnabled()) {
                    args.add("off");
                } else {
                    args.add("on");
                }
            }
            if (commandSender.hasPermission("whitelistbytime.remove")) args.add("remove");
            if (commandSender.hasPermission("whitelistbytime.check")) args.add("check");
            if (commandSender.hasPermission("whitelistbytime.checkme")) args.add("checkme");
            if (commandSender.hasPermission("whitelistbytime.reload")) args.add("reload");
            if (commandSender.hasPermission("whitelistbytime.getall")) args.add("getall");
            if (commandSender.hasPermission("whitelistbytime.time")) args.add("time");
        } else if (strings.length == 2) {
            List<String> notInWhitelist = new ArrayList<>();
            List<String> inWhitelist = getWL();


            if(!plugin.isWhitelistEnabled()) {
                for (Player player : commandSender.getServer().getOnlinePlayers()) {
                    if (!database.checkPlayer(player.getName())) notInWhitelist.add(player.getName());
                }
            }

            switch (strings[0]) {
                case "time":
                    if (commandSender.hasPermission("whitelistbytime.time")) {
                        args.addAll(Arrays.asList("set", "add", "remove"));
                    }
                    break;
                case "add":
                    if (commandSender.hasPermission("whitelistbytime.add")) {
                        args.addAll(notInWhitelist);
                    }
                    break;
                case "remove":
                    if (commandSender.hasPermission("whitelistbytime.remove")) {
                        args.addAll(inWhitelist);
                    }
                    break;
                case "check":
                    if (commandSender.hasPermission("whitelistbytime.check")) {
                        args.addAll(inWhitelist);
                        args.addAll(notInWhitelist);
                    }
                    break;
            }
        } else if (strings.length == 3) {
            if (commandSender.hasPermission("whitelistbytime.add") && strings[0].equals("add")) {
                args.add("1"+plugin.getConfig().getString("time-units.month"));
                args.add("1"+plugin.getConfig().getString("time-units.week"));
                args.add("1"+plugin.getConfig().getString("time-units.day"));
                args.add("12"+plugin.getConfig().getString("time-units.hour"));
            }

            if (commandSender.hasPermission("whitelistbytime.time")) {
                if (strings[1].equals("set") || strings[1].equals("add") || strings[1].equals("remove")) {
                    args.addAll(getWL());
                }
            }
        } else if (strings.length == 4) {
            if (commandSender.hasPermission("whitelistbytime.time")) {
                if (strings[1].equals("set") || strings[1].equals("add") || strings[1].equals("remove")) {
                    args.add("1"+plugin.getConfig().getString("time-units.month"));
                    args.add("1"+plugin.getConfig().getString("time-units.week"));
                    args.add("1"+plugin.getConfig().getString("time-units.day"));
                    args.add("12"+plugin.getConfig().getString("time-units.hour"));
                }
            }
        }

        return args;
    }

    private List<String> getWL() {
        Map<String, Long> all;

        synchronized (Checker.players) {
            all = Checker.players;
        }

        List<String> inWhitelist = new ArrayList<>();

        for (Map.Entry<String, Long> playerEntry: all.entrySet()) {
            inWhitelist.add(playerEntry.getKey());
        }

        return inWhitelist;
    }
}
