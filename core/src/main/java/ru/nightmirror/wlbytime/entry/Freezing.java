package ru.nightmirror.wlbytime.entry;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.sql.Timestamp;

@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class Freezing {
    long entryId;
    Timestamp startTime;
    Timestamp endTime;

    public Freezing(long entryId, long time) {
        this.entryId = entryId;
        startTime = new Timestamp(System.currentTimeMillis());
        endTime = new Timestamp(System.currentTimeMillis() + time);
    }

    public boolean isFrozen() {
        return endTime.after(new Timestamp(System.currentTimeMillis()));
    }

    public long getTimeOfFreeze() {
        return endTime.getTime() - startTime.getTime();
    }
}
