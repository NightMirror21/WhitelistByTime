package ru.nightmirror.wlbytime.impl.command;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;
import ru.nightmirror.wlbytime.interfaces.WhitelistByTime;
import ru.nightmirror.wlbytime.interfaces.command.wrappers.TabCompleter;
import ru.nightmirror.wlbytime.interfaces.command.wrappers.WrappedCommandSender;
import ru.nightmirror.wlbytime.interfaces.database.PlayerDao;
import ru.nightmirror.wlbytime.models.PlayerData;
import ru.nightmirror.wlbytime.time.TimeUnitsConvertorSettings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TabCompleterImpl implements TabCompleter {
    private static final String PERM_ADD = "whitelistbytime.add";
    private static final String PERM_SWITCHFREEZE = "whitelistbytime.switchfreeze";
    private static final String PERM_TURN = "whitelistbytime.turn";
    private static final String PERM_REMOVE = "whitelistbytime.remove";
    private static final String PERM_CHECK = "whitelistbytime.check";
    private static final String PERM_CHECKME = "whitelistbytime.checkme";
    private static final String PERM_RELOAD = "whitelistbytime.reload";
    private static final String PERM_GETALL = "whitelistbytime.getall";
    private static final String PERM_TIME = "whitelistbytime.time";

    PlayerDao playerDao;
    TimeUnitsConvertorSettings timeSettings;
    WhitelistByTime plugin;

    @Override
    public List<String> onTabComplete(@NotNull WrappedCommandSender commandSender, @NotNull String command, String[] args) {
        List<String> suggestions = new ArrayList<>();

        switch (args.length) {
            case 1:
                handleFirstArgument(commandSender, suggestions);
                break;
            case 2:
                handleSecondArgument(commandSender, args, suggestions);
                break;
            case 3:
                handleThirdArgument(commandSender, args, suggestions);
                break;
            case 4:
                handleFourthArgument(commandSender, args, suggestions);
                break;
        }
        return suggestions;
    }

    private void handleFirstArgument(WrappedCommandSender commandSender, List<String> suggestions) {
        if (commandSender.hasPermission(PERM_ADD)) suggestions.add("add");
        if (commandSender.hasPermission(PERM_SWITCHFREEZE)) suggestions.add("switchfreeze");
        if (commandSender.hasPermission(PERM_TURN)) {
            suggestions.add(plugin.isWhitelistEnabled() ? "off" : "on");
        }
        if (commandSender.hasPermission(PERM_REMOVE)) suggestions.add("remove");
        if (commandSender.hasPermission(PERM_CHECK)) suggestions.add("check");
        if (commandSender.hasPermission(PERM_CHECKME)) suggestions.add("checkme");
        if (commandSender.hasPermission(PERM_RELOAD)) suggestions.add("reload");
        if (commandSender.hasPermission(PERM_GETALL)) suggestions.add("getall");
        if (commandSender.hasPermission(PERM_TIME)) suggestions.add("time");
    }

    private void handleSecondArgument(WrappedCommandSender commandSender, String[] args, List<String> suggestions) {
        List<PlayerData> inWhitelist = playerDao.getPlayersCached();
        List<String> notInWhitelist = new ArrayList<>();

        if (!plugin.isWhitelistEnabled()) {
            for (String nickname : commandSender.getAllPlayerNicknamesOnServer()) {
                if (playerDao.getPlayerCached(nickname).isEmpty()) {
                    notInWhitelist.add(nickname);
                }
            }
        }

        switch (args[0]) {
            case "time":
                if (commandSender.hasPermission(PERM_TIME)) {
                    suggestions.addAll(Arrays.asList("set", "add", "remove"));
                }
                break;
            case "add":
                if (commandSender.hasPermission(PERM_ADD)) {
                    suggestions.addAll(notInWhitelist);
                }
                break;
            case "remove":
                if (commandSender.hasPermission(PERM_REMOVE)) {
                    suggestions.addAll(inWhitelist.stream().map(PlayerData::getNickname).toList());
                }
                break;
            case "check":
            case "switchfreeze":
                if (commandSender.hasPermission(PERM_CHECK)) {
                    suggestions.addAll(inWhitelist.stream().map(PlayerData::getNickname).toList());
                    suggestions.addAll(notInWhitelist);
                }
                break;
        }
    }

    private void handleThirdArgument(WrappedCommandSender commandSender, String[] args, List<String> suggestions) {
        if (commandSender.hasPermission(PERM_ADD) && "add".equals(args[0])) {
            suggestions.add("1" + timeSettings.getFirstMonthOrDefault());
            suggestions.add("1" + timeSettings.getFirstWeekOrDefault());
            suggestions.add("1" + timeSettings.getFirstDayOrDefault());
            suggestions.add("12" + timeSettings.getFirstHourOrDefault());
        }
        if (commandSender.hasPermission(PERM_TIME)) {
            if ("set".equals(args[1]) || "add".equals(args[1]) || "remove".equals(args[1])) {
                suggestions.addAll(playerDao.getPlayersCached().stream().map(PlayerData::getNickname).toList());
            }
        }
    }

    private void handleFourthArgument(WrappedCommandSender commandSender, String[] args, List<String> suggestions) {
        if (commandSender.hasPermission(PERM_TIME)) {
            if ("set".equals(args[1]) || "add".equals(args[1]) || "remove".equals(args[1])) {
                suggestions.add("1" + timeSettings.getFirstMonthOrDefault());
                suggestions.add("1" + timeSettings.getFirstWeekOrDefault());
                suggestions.add("1" + timeSettings.getFirstDayOrDefault());
                suggestions.add("12" + timeSettings.getFirstHourOrDefault());
            }
        }
    }
}