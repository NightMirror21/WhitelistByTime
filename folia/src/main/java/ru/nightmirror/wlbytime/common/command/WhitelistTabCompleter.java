package ru.nightmirror.wlbytime.common.command;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.nightmirror.wlbytime.common.database.misc.WLPlayer;
import ru.nightmirror.wlbytime.interfaces.IWhitelist;
import ru.nightmirror.wlbytime.interfaces.database.PlayerAccessor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WhitelistTabCompleter implements TabCompleter {

    PlayerAccessor playerAccessor;
    IWhitelist plugin;

    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, String[] strings) {
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
            List<WLPlayer> inWhitelist = playerAccessor.getPlayersCached();

            if (!plugin.isWhitelistEnabled()) {
                for (Player player : commandSender.getServer().getOnlinePlayers()) {
                    if (playerAccessor.getPlayerCached(player.getName()).isEmpty()) notInWhitelist.add(player.getName());
                }
            }

            switch (strings[0]) {
                case "time" -> {
                    if (commandSender.hasPermission("whitelistbytime.time")) {
                        args.addAll(Arrays.asList("set", "add", "remove"));
                    }
                }
                case "add" -> {
                    if (commandSender.hasPermission("whitelistbytime.add")) {
                        args.addAll(notInWhitelist);
                    }
                }
                case "remove" -> {
                    if (commandSender.hasPermission("whitelistbytime.remove")) {
                        args.addAll(inWhitelist.stream().map(WLPlayer::getNickname).toList());
                    }
                }
                case "check" -> {
                    if (commandSender.hasPermission("whitelistbytime.check")) {
                        args.addAll(inWhitelist.stream().map(WLPlayer::getNickname).toList());
                        args.addAll(notInWhitelist);
                    }
                }
            }
        } else if (strings.length == 3) {
            if (commandSender.hasPermission("whitelistbytime.add") && strings[0].equals("add")) {
                args.add("1" + plugin.getPluginConfig().timeUnitsMonth.get(0));
                args.add("1" + plugin.getPluginConfig().timeUnitsWeek.get(0));
                args.add("1" + plugin.getPluginConfig().timeUnitsDay.get(0));
                args.add("12" + plugin.getPluginConfig().timeUnitsHour.get(0));
            }

            if (commandSender.hasPermission("whitelistbytime.time")) {
                if (strings[1].equals("set") || strings[1].equals("add") || strings[1].equals("remove")) {
                    args.addAll(playerAccessor.getPlayersCached().stream().map(WLPlayer::getNickname).toList());
                }
            }
        } else if (strings.length == 4) {
            if (commandSender.hasPermission("whitelistbytime.time")) {
                if (strings[1].equals("set") || strings[1].equals("add") || strings[1].equals("remove")) {
                    args.add("1" + plugin.getPluginConfig().timeUnitsMonth.get(0));
                    args.add("1" + plugin.getPluginConfig().timeUnitsWeek.get(0));
                    args.add("1" + plugin.getPluginConfig().timeUnitsDay.get(0));
                    args.add("12" + plugin.getPluginConfig().timeUnitsHour.get(0));
                }
            }
        }

        return args;
    }
}
