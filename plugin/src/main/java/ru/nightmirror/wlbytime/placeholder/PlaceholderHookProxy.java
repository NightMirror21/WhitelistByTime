package ru.nightmirror.wlbytime.placeholder;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.nightmirror.wlbytime.interfaces.parser.PlaceholderParser;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class PlaceholderHookProxy extends PlaceholderExpansion {

    PlaceholderParser placeholderParser;
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
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null) {
            return placeholderParser.getEmpty();
        }

        return placeholderParser.parse(player.getName(), params);
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) {
            return placeholderParser.getEmpty();
        }

        return placeholderParser.parse(player.getName(), params);
    }
}
