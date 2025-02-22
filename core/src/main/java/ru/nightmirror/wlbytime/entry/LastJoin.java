package ru.nightmirror.wlbytime.entry;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class LastJoin {
    long entryId;
    Instant lastJoinTime;

    public LastJoin(long entryId) {
        this.entryId = entryId;
        lastJoinTime = Instant.now();
    }
}