package ru.nightmirror.wlbytime.entry;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.time.Duration;
import java.time.Instant;

@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@Getter
@Builder
public class Expiration {
    final long entryId;
    Instant expirationTime;

    public boolean canAdd(Duration duration) {
        Instant newTime = expirationTime.plus(duration);
        return newTime.isAfter(Instant.now());
    }

    public void add(Duration duration) {
        Instant newTime = expirationTime.plus(duration);
        if (newTime.isBefore(Instant.now())) {
            throw new IllegalArgumentException("Cannot set expiration time to the past.");
        }
        expirationTime = newTime;
    }

    public boolean canRemove(Duration duration) {
        Instant newTime = expirationTime.minus(duration);
        return newTime.isAfter(Instant.now());
    }

    public void remove(Duration duration) {
        Instant newTime = expirationTime.minus(duration);
        if (newTime.isBefore(Instant.now())) {
            throw new IllegalArgumentException("Cannot set expiration time to the past.");
        }
        expirationTime = newTime;
    }

    public void set(Instant newTime) {
        if (newTime.isBefore(Instant.now())) {
            throw new IllegalArgumentException("Milliseconds must be in the future");
        }
        expirationTime = newTime;
    }

    public boolean isExpired() {
        return expirationTime.isBefore(Instant.now());
    }

    public boolean isExpired(Duration offset) {
        return expirationTime.plus(offset).isBefore(Instant.now());
    }

    public boolean isNotExpired() {
        return !isExpired();
    }

    public boolean isNotExpired(Duration offset) {
        return !isExpired(offset);
    }
}
