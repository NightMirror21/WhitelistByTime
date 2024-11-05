package ru.nightmirror.wlbytime.entry;

import lombok.*;
import lombok.experimental.FieldDefaults;

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
            return expiration.isNotExpired(freezing.getTimeOfFreeze());
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

    public void setExpiration(Timestamp timestamp) {
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
        if (isFrozen()) {
            throw new IllegalStateException("Entry is already frozen.");
        }

        freezing = new Freezing(id, time);
    }

    public void unfreeze() {
        if (isNotFrozen()) {
            throw new IllegalStateException("Entry is not frozen.");
        }

        expiration = new Expiration(id, new Timestamp(System.currentTimeMillis() + freezing.getTimeOfFreeze()));

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
            offset = freezing.getTimeOfFreeze();
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
        return freezing.getTimeOfFreeze() - System.currentTimeMillis();
    }
}