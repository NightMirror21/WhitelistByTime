package ru.nightmirror.wlbytime.entry;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.sql.Timestamp;

@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class LastJoin {
    long entryId;
    Timestamp lastJoinTime;

    public LastJoin(long entryId) {
        this.entryId = entryId;
        lastJoinTime = new Timestamp(System.currentTimeMillis());
    }
}