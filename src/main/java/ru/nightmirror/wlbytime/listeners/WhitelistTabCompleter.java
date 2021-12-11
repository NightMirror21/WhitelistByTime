package ru.nightmirror.wlbytime.listeners;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import ru.nightmirror.wlbytime.main.Config;
import ru.nightmirror.wlbytime.main.SQLite;

import java.util.ArrayList;
import java.util.List;

public class WhitelistTabCompleter implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        List<String> args = new ArrayList<>();

        SQLite sqLite = SQLite.getInstance();

        if (strings.length == 1) {
            if (commandSender.hasPermission("whitelistbytime.add")) args.add("add");
            if (commandSender.hasPermission("whitelistbytime.remove")) args.add("remove");
            if (commandSender.hasPermission("whitelistbytime.check")) args.add("check");
            if (commandSender.hasPermission("whitelistbytime.reload")) args.add("reload");
            if (commandSender.hasPermission("whitelistbytime.getall")) args.add("getall");
        } else if (strings.length == 2) {
            List<String> notInWhitelist = new ArrayList<>();
            List<String> inWhitelist = sqLite.getAll();

            for (Player player : commandSender.getServer().getOnlinePlayers()) {
                if (!sqLite.checkPlayer(player.getName())) notInWhitelist.add(player.getName());
            }

            switch (strings[0]) {
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
            if (commandSender.hasPermission("whitelistbytime.add") && strings[1].equals("add")) {
                Config config = Config.getInstance();

                // :D
                args.add("1"+config.getLine("month"));
                args.add("1"+config.getLine("week"));
                args.add("3"+config.getLine("day"));
                args.add("1"+config.getLine("day"));
                args.add("12"+config.getLine("hour"));
                args.add("8"+config.getLine("hour"));
                args.add("1"+config.getLine("hour"));
                args.add("30"+config.getLine("minute"));
                args.add("15"+config.getLine("minute"));
            }
        }

        return args;
    }
}
