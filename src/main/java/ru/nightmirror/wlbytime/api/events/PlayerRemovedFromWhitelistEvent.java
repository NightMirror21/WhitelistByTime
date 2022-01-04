package ru.nightmirror.wlbytime.api.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerRemovedFromWhitelistEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();
    private final String nickname;
    private boolean isCancelled;

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public PlayerRemovedFromWhitelistEvent(String nickname) {
        this.nickname = nickname;
        isCancelled = false;
    }

    public String getNickname() {
        return nickname;
    }

    public boolean isCancelled() {
        return this.isCancelled;
    }

    public void setCancelled(boolean isCancelled) {
        this.isCancelled = isCancelled;
    }
}
