package ru.nightmirror.wlbytime.shared.api.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerAddedToWhitelistEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();
    private final String nickname;
    private final long until;
    private boolean isCancelled;

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public PlayerAddedToWhitelistEvent(String nickname, long until) {
        this.nickname = nickname;
        this.until = until;
        isCancelled = false;
    }

    public String getNickname() {
        return nickname;
    }

    public long getUntil() {
        return until;
    }

    public boolean isCancelled() {
        return this.isCancelled;
    }

    public void setCancelled(boolean isCancelled) {
        this.isCancelled = isCancelled;
    }
}
