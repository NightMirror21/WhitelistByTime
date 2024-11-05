package ru.nightmirror.wlbytime.placeholder;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.nightmirror.wlbytime.config.configs.PlaceholdersConfig;
import ru.nightmirror.wlbytime.entry.Entry;
import ru.nightmirror.wlbytime.interfaces.finder.EntryFinder;
import ru.nightmirror.wlbytime.time.TimeConvertor;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class PlaceholderHook extends PlaceholderExpansion {
    private static final String EMPTY = "";
    private static final String IN_WHITELIST_PARAM = "in_whitelist";
    private static final String TIME_LEFT_PARAM = "time_left";

    EntryFinder finder;
    TimeConvertor timeConvertor;
    PlaceholdersConfig config;
    String version;

    @Override
    public @NotNull String getIdentifier() {
        return "wlbytime";
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

        Entry entry = finder.find(player.getName()).orElse(null);
        if (entry == null) {
            return config.getInWhitelistFalse();
        }

        return switch (params.toLowerCase()) {
            case IN_WHITELIST_PARAM -> handleInWhitelistParam(entry);
            case TIME_LEFT_PARAM -> handleTimeLeftParam(entry);
            default -> EMPTY;
        };
    }

    private String handleInWhitelistParam(Entry entry) {
        if (entry.isCurrentlyFrozen()) {
            return config.getFrozen();
        } else if (entry.isCurrentlyActive()) {
            return config.getInWhitelistTrue();
        } else {
            return config.getInWhitelistFalse();
        }
    }

    private String handleTimeLeftParam(Entry entry) {
        long remainingTime;
        String output;

        if (entry.isCurrentlyFrozen()) {
            remainingTime = entry.getRemainingFreezeTime();
            output = config.getTimeLeftWithFreeze();
        } else if (entry.isCurrentlyActive()) {
            remainingTime = entry.getRemainingActiveTime();
            output = config.getTimeLeft();
        } else {
            return EMPTY;
        }

        String time = timeConvertor.getTimeLine(remainingTime);
        return output.replace("%time%", time);
    }
}
