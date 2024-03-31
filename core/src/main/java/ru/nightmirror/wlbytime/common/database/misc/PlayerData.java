package ru.nightmirror.wlbytime.common.database.misc;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@Data
public class PlayerData {
    @Nullable Long id;
    @NotNull String nickname;
    @NotNull Long until;

    public PlayerData(@NotNull String nickname, @NotNull Long until) {
        this.nickname = nickname;
        this.until = until;
    }


    @Override
    public String toString() {
        return "WLPlayer{" +
                "id=" + id +
                ", nickname='" + nickname + '\'' +
                ", until=" + until +
                '}';
    }
}
