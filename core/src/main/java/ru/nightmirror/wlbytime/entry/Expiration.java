package ru.nightmirror.wlbytime.entry;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.sql.Timestamp;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Getter
@Builder
public class Expiration {
    long entryId;
    Timestamp expirationTime;

    public boolean isExpired() {
        return expirationTime.before(new Timestamp(System.currentTimeMillis()));
    }

    public boolean isExpired(long offset) {
        return expirationTime.before(new Timestamp(System.currentTimeMillis() + offset));
    }

    public boolean isNotExpired() {
        return !isExpired();
    }

    public boolean isNotExpired(long offset) {
        return !isExpired(offset);
    }
}
