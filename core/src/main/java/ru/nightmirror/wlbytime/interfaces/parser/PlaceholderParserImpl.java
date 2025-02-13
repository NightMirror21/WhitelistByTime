package ru.nightmirror.wlbytime.interfaces.parser;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import ru.nightmirror.wlbytime.config.configs.PlaceholdersConfig;
import ru.nightmirror.wlbytime.entry.EntryImpl;
import ru.nightmirror.wlbytime.impl.parser.PlaceholderParser;
import ru.nightmirror.wlbytime.interfaces.finder.EntryFinder;
import ru.nightmirror.wlbytime.time.TimeConvertor;

import java.time.Duration;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PlaceholderParserImpl implements PlaceholderParser {

    private static final String EMPTY = "";
    private static final String IN_WHITELIST_PARAM = "in_whitelist";
    private static final String TIME_LEFT_PARAM = "time_left";

    EntryFinder finder;
    TimeConvertor timeConvertor;
    PlaceholdersConfig config;

    @Override
    public String parse(String playerNickname, String params) {
        EntryImpl entry = finder.find(playerNickname).orElse(null);
        if (entry == null) {
            return config.getInWhitelistFalse();
        }

        return switch (params.toLowerCase()) {
            case IN_WHITELIST_PARAM -> handleInWhitelistParam(entry);
            case TIME_LEFT_PARAM -> handleTimeLeftParam(entry);
            default -> EMPTY;
        };
    }

    @Override
    public String getEmpty() {
        return EMPTY;
    }

    private String handleInWhitelistParam(EntryImpl entry) {
        if (entry.isFreezeActive()) {
            return config.getFrozen();
        } else if (entry.isActive()) {
            return config.getInWhitelistTrue();
        } else {
            return config.getInWhitelistFalse();
        }
    }

    private String handleTimeLeftParam(EntryImpl entry) {
        Duration remainingTime;
        String output;

        if (entry.isFreezeActive()) {
            remainingTime = entry.getLeftFreezeDuration();
            output = config.getTimeLeftWithFreeze();
        } else if (!entry.isForever() && entry.isActive()) {
            remainingTime = entry.getLeftActiveDuration();
            output = config.getTimeLeft();
        } else {
            return EMPTY;
        }

        String time = timeConvertor.getTimeLine(remainingTime);
        return output.replace("%time%", time);
    }
}
