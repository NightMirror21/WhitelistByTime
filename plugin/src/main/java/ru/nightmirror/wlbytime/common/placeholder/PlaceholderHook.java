package ru.nightmirror.wlbytime.common.placeholder;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.nightmirror.wlbytime.config.configs.PlaceholdersConfig;
import ru.nightmirror.wlbytime.entry.WhitelistEntry;
import ru.nightmirror.wlbytime.interfaces.WhitelistByTime;
import ru.nightmirror.wlbytime.interfaces.finder.WhitelistEntryFinder;
import ru.nightmirror.wlbytime.time.TimeConvertor;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class PlaceholderHook extends PlaceholderExpansion {
    private static final String EMPTY = "";
    private static final String IN_WHITELIST_PARAM = "in_whitelist";
    private static final String TIME_LEFT_PARAM = "time_left";

    WhitelistEntryFinder finder;
    TimeConvertor timeConvertor;
    PlaceholdersConfig config;
    String version;

    @Override
    public @NotNull String getIdentifier() {
        return WhitelistByTime.getPAPIIdentifier();
    }

    @Override
    public @NotNull String getAuthor() {
        return "NightMirror";
    }

    @Override
    public @NotNull String getVersion() {
        return version;
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) {
            return EMPTY;
        }

        WhitelistEntry entry = finder.find(player.getName()).orElse(null);
        if (entry == null) {
            return config.getInWhitelistFalse();
        }

        return switch (params.toLowerCase()) {
            case IN_WHITELIST_PARAM -> handleInWhitelistParam(entry);
            case TIME_LEFT_PARAM -> handleTimeLeftParam(entry);
            default -> EMPTY;
        };
    }

    private String handleInWhitelistParam(WhitelistEntry entry) {
        if (entry.isFrozen()) {
            return config.getFrozen();
        } else if (entry.isActive()) {
            return config.getInWhitelistTrue();
        } else {
            return config.getInWhitelistFalse();
        }
    }

    private String handleTimeLeftParam(WhitelistEntry entry) {
        long remainingTime;
        String output;

        if (entry.isFrozen()) {
            remainingTime = entry.getRemainingTimeOfFreeze();
            output = config.getTimeLeftWithFreeze();
        } else if (entry.isActive()) {
            remainingTime = entry.getRemainingTime();
            output = config.getTimeLeft();
        } else {
            return EMPTY;
        }

        String time = timeConvertor.getTimeLine(remainingTime);
        return output.replace("%time%", time);
    }
}
