package ru.nightmirror.wlbytime.entry;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;
import ru.nightmirror.wlbytime.interfaces.entry.Entry;

import java.sql.Timestamp;

@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
@Setter
@EqualsAndHashCode(of = {"id", "nickname"})
public class EntryImpl implements Entry {
    long id;
    String nickname;
    @Builder.Default
    Expiration expiration = null;
    @Builder.Default
    Freezing freezing = null;
    @Builder.Default
    LastJoin lastJoin = null;

    @Override
    public boolean isForever() {
        return expiration == null;
    }

    @Override
    public boolean isActive() {
        if (isForever()) {
            return true;
        }

        if (expiration.isNotExpired()) {
            return true;
        }

        if (isFreezeActive()) {
            return expiration.isNotExpired(freezing.getLeftTime());
        } else {
            return false;
        }
    }

    @Override
    public void setForever() {
        expiration = null;
    }

    @Override
    public void setExpiration(@NotNull Timestamp timestamp) {
        expiration = new Expiration(id, timestamp.toInstant());
    }

    @Override
    public boolean isFrozen() {
        return freezing != null;
    }

    @Override
    public boolean isFreezeActive() {
        return isFrozen() && freezing.isFrozen();
    }

    @Override
    public boolean isFreezeInactive() {
        return isFrozen() && !freezing.isFrozen();
    }

    @Override
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

    @Override
    public void unfreeze() {
        if (isNotFrozen()) {
            throw new IllegalStateException("Entry is not frozen.");
        }

        long previousExpirationTime = expiration.getExpirationTime().toEpochMilli();
        long durationOfFreeze = freezing.getDurationOfFreeze();
        expiration = new Expiration(id, new Timestamp(previousExpirationTime + durationOfFreeze).toInstant());

        freezing = null;
    }

    @Override
    public void updateLastJoin() {
        lastJoin = new LastJoin(id);
    }

    @Override
    public boolean isJoined() {
        return lastJoin != null;
    }

    @Override
    public long getLeftActiveTime() {
        long offset = 0L;
        
        if (isFreezeActive()) {
            offset = freezing.getLeftTime();
        }
        
        if (isForever()) {
            throw new IllegalStateException("Can't get left expiration time cause entry is forever");
        }
        
        return offset + expiration.getExpirationTime().toEpochMilli() - System.currentTimeMillis();
    }

    @Override
    public long getLeftFreezeTime() {
        if (isFreezeInactive()) {
            throw new IllegalStateException("Can't get left freeze time cause entry is not frozen");
        }
        return freezing.getLeftTime();
    }
}