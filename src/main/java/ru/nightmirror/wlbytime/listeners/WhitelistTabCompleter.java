package ru.nightmirror.wlbytime.listeners;

import lombok.RequiredArgsConstructor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import ru.nightmirror.wlbytime.database.IDatabase;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class WhitelistTabCompleter implements TabCompleter {

    private final IDatabase database;
    private final Plugin plugin;

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        List<String> args = new ArrayList<>();

        if (strings.length == 1) {
            if (commandSender.hasPermission("whitelistbytime.add")) args.add("add");
            if (commandSender.hasPermission("whitelistbytime.remove")) args.add("remove");
            if (commandSender.hasPermission("whitelistbytime.check")) args.add("check");
            if (commandSender.hasPermission("whitelistbytime.reload")) args.add("reload");
            if (commandSender.hasPermission("whitelistbytime.getall")) args.add("getall");
        } else if (strings.length == 2) {
            List<String> notInWhitelist = new ArrayList<>();
            List<String> inWhitelist = database.getAll();

            for (Player player : commandSender.getServer().getOnlinePlayers()) {
                if (!database.checkPlayer(player.getName())) notInWhitelist.add(player.getName());
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
                // :D
                args.add("1"+plugin.getConfig().getString("month"));
                args.add("1"+plugin.getConfig().getString("week"));
                args.add("3"+plugin.getConfig().getString("day"));
                args.add("1"+plugin.getConfig().getString("day"));
                args.add("12"+plugin.getConfig().getString("hour"));
                args.add("8"+plugin.getConfig().getString("hour"));
                args.add("1"+plugin.getConfig().getString("hour"));
                args.add("30"+plugin.getConfig().getString("minute"));
                args.add("15"+plugin.getConfig().getString("minute"));
            }
        }

        return args;
    }
}
