package ru.nightmirror.wlbytime.interfaces.entry;

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

    long getLeftActiveTime();

    long getLeftFreezeTime();
}
