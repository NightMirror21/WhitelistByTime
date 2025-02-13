package ru.nightmirror.wlbytime.interfaces.entry;

import java.time.Duration;

public interface EntryView {
    boolean isForever();

    boolean isActive();

    default boolean isInactive() {
        return !isActive();
    }

    boolean isFrozen();

    default boolean isNotFrozen() {
        return !isFrozen();
    }

    boolean isFreezeActive();

    boolean isFreezeInactive();

    boolean isJoined();

    Duration getLeftActiveDuration();

    Duration getLeftFreezeDuration();
}
