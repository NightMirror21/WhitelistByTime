package ru.nightmirror.wlbytime.common.database.misc;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@DatabaseTable(tableName = "wlbytime_players")
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@Data
@NoArgsConstructor
public class WLPlayerTable {

    public static final String TABLE_NAME = "wlbytime_players";
    public static final String ID_COLUMN = "id";
    public static final String NICKNAME_COLUMN = "nickname";
    public static final String UNTIL_COLUMN = "until";

    @DatabaseField(generatedId = true, columnName = ID_COLUMN)
    Long id;

    @DatabaseField(columnName = NICKNAME_COLUMN)
    String nickname;

    @DatabaseField(columnName = UNTIL_COLUMN)
    Long until;

    @Override
    public String toString() {
        return "WLPlayerTable{" +
                "id=" + id +
                ", nickname='" + nickname + '\'' +
                ", until=" + until +
                '}';
    }
}
