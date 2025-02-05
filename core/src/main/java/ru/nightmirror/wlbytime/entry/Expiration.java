package ru.nightmirror.wlbytime.entry;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@Getter
@Builder
public class Expiration {
    final long entryId;
    Instant expirationTime;

    public boolean canAdd(long milliseconds) {
        if (milliseconds <= 0) {
            return false;
        }

        Instant newTime = expirationTime.plusMillis(milliseconds);
        return newTime.isAfter(Instant.now());
    }

    public void add(long milliseconds) {
        if (milliseconds <= 0) {
            throw new IllegalArgumentException("Milliseconds cannot be negative or zero.");
        }
        Instant newTime = expirationTime.plusMillis(milliseconds);
        if (newTime.isBefore(Instant.now())) {
            throw new IllegalArgumentException("Cannot set expiration time to the past.");
        }
        expirationTime = newTime;
    }

    public boolean canRemove(long milliseconds) {
        if (milliseconds <= 0) {
            return false;
        }
        Instant newTime = expirationTime.minusMillis(milliseconds);
        return newTime.isAfter(Instant.now());
    }

    public void remove(long milliseconds) {
        if (milliseconds <= 0) {
            throw new IllegalArgumentException("Milliseconds cannot be negative or zero.");
        }
        Instant newTime = expirationTime.minusMillis(milliseconds);
        if (newTime.isBefore(Instant.now())) {
            throw new IllegalArgumentException("Cannot set expiration time to the past.");
        }
        expirationTime = newTime;
    }

    public boolean canSet(long milliseconds) {
        if (milliseconds <= 0) {
            return false;
        }
        return Instant.ofEpochMilli(milliseconds).isAfter(Instant.now());
    }

    public void set(long milliseconds) {
        if (milliseconds <= 0) {
            throw new IllegalArgumentException("Milliseconds cannot be negative or zero.");
        }
        Instant newTime = Instant.ofEpochMilli(milliseconds);
        if (newTime.isBefore(Instant.now())) {
            throw new IllegalArgumentException("Milliseconds must be in the future");
        }
        expirationTime = newTime;
    }

    public boolean isExpired() {
        return expirationTime.isBefore(Instant.now());
    }

    public boolean isExpired(long offset) {
        return expirationTime.isBefore(Instant.now().plusMillis(offset));
    }

    public boolean isNotExpired() {
        return !isExpired();
    }

    public boolean isNotExpired(long offset) {
        return !isExpired(offset);
    }
}
