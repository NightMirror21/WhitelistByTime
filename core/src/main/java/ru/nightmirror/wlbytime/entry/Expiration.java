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

    public boolean canAdd(long milliseconds) {
        if (milliseconds <= 0) {
            return false;
        }

        long newTime = expirationTime.getTime() + milliseconds;
        return newTime > System.currentTimeMillis();
    }
    
    public void add(long milliseconds) {
        if (milliseconds <= 0) {
            throw new IllegalArgumentException("Milliseconds cannot be negative or zero.");
        }
        long newTime = expirationTime.getTime() + milliseconds;
        if (newTime < System.currentTimeMillis()) {
            throw new IllegalArgumentException("Cannot set expiration time to the past.");
        }
        expirationTime.setTime(expirationTime.getTime() + milliseconds);
    }

    public boolean canRemove(long milliseconds) {
        if (milliseconds <= 0) {
            return false;
        }

        long newTime = expirationTime.getTime() - milliseconds;
        return newTime > System.currentTimeMillis();
    }
    
    public void remove(long milliseconds) {
        if (milliseconds <= 0) {
            throw new IllegalArgumentException("Milliseconds cannot be negative or zero.");
        }
        long newTime = expirationTime.getTime() - milliseconds;
        if (newTime < System.currentTimeMillis()) {
            throw new IllegalArgumentException("Cannot set expiration time to the past.");
        }
        expirationTime.setTime(newTime);
    }

    public boolean canSet(long milliseconds) {
        if (milliseconds <= 0) {
            return false;
        }
        return milliseconds < System.currentTimeMillis();
    }

    public void set(long milliseconds) {
        if (milliseconds <= 0) {
            throw new IllegalArgumentException("Milliseconds cannot be negative or zero.");
        }
        if (milliseconds <= System.currentTimeMillis()) {
            throw new IllegalArgumentException("Milliseconds must be in the future");
        }
        expirationTime.setTime(milliseconds);
    }

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
