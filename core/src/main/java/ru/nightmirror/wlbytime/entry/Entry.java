package ru.nightmirror.wlbytime.entry;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;

import java.sql.Timestamp;

@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
@EqualsAndHashCode(of = {"id", "nickname"})
public class Entry {
    long id;
    String nickname;
    @Builder.Default
    Expiration expiration = null;
    @Builder.Default
    Freezing freezing = null;
    @Builder.Default
    LastJoin lastJoin = null;

    public boolean isForever() {
        return expiration == null;
    }

    public boolean isActive() {
        if (isForever()) {
            return true;
        }

        if (expiration.isNotExpired()) {
            return true;
        }

        if (isFreezeActive()) {
            return expiration.isNotExpired(freezing.getDurationOfFreeze());
        } else {
            return false;
        }
    }

    public boolean isInactive() {
        return !isActive();
    }

    public void setForever() {
        expiration = null;
    }

    public void setExpiration(@NotNull Timestamp timestamp) {
        expiration = new Expiration(id, timestamp);
    }

    public boolean isFrozen() {
        return freezing != null;
    }

    public boolean isNotFrozen() {
        return !isFrozen();
    }

    public boolean isFreezeActive() {
        return isFrozen() && freezing.isFrozen();
    }

    public boolean isFreezeInactive() {
        return isFrozen() && !freezing.isFrozen();
    }

    public void freeze(long time) {
        if (time <= 0) {
            throw new IllegalArgumentException("Time for freeze must be positive and not zero.");
        }

        if (isFrozen()) {
            throw new IllegalStateException("Entry is already frozen.");
        }

        if (isForever()) {
            throw new IllegalStateException("Can't freeze forever entry.");
        }

        freezing = new Freezing(id, time);
    }

    public void unfreeze() {
        if (isNotFrozen()) {
            throw new IllegalStateException("Entry is not frozen.");
        }

        long previousExpirationTime = expiration.getExpirationTime().getTime();
        long durationOfFreeze = freezing.getDurationOfFreeze();
        expiration = new Expiration(id, new Timestamp(previousExpirationTime + durationOfFreeze));

        freezing = null;
    }

    public void updateLastJoin() {
        lastJoin = new LastJoin(id);
    }

    public boolean isJoined() {
        return lastJoin != null;
    }
    
    public long getLeftActiveTime() {
        long offset = 0L;
        
        if (isFreezeActive()) {
            offset = freezing.getDurationOfFreeze();
        }
        
        if (isForever()) {
            throw new IllegalStateException("Can't get left expiration time cause entry is forever");
        }
        
        return offset + expiration.getExpirationTime().getTime() - System.currentTimeMillis();
    }

    public long getLeftFreezeTime() {
        if (isFreezeInactive()) {
            throw new IllegalStateException("Can't get left freeze time cause entry is not frozen");
        }
        return freezing.getDurationOfFreeze();
    }
}