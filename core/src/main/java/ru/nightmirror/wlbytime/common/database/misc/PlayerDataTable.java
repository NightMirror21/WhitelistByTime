package ru.nightmirror.wlbytime.common.database.misc;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.*;
import lombok.experimental.FieldDefaults;

@DatabaseTable(tableName = "wlbytime_players")
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@Data
@NoArgsConstructor
@ToString
public class PlayerDataTable {

    public static final String TABLE_NAME = "wlbytime_players";
    public static final String ID_COLUMN = "id";
    public static final String NICKNAME_COLUMN = "nickname";
    public static final String UNTIL_COLUMN = "until";
    public static final String FROZEN_AT_COLUMN = "frozen_at";

    @DatabaseField(generatedId = true, columnName = ID_COLUMN)
    Long id;

    @DatabaseField(columnName = NICKNAME_COLUMN)
    String nickname;

    @DatabaseField(columnName = UNTIL_COLUMN)
    Long until;

    @DatabaseField(columnName = FROZEN_AT_COLUMN)
    Long frozenAt;
}
