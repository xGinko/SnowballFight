package me.xginko.snowballfight.events;

import org.bukkit.entity.Snowball;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public abstract class SnowballEvent extends Event {

    private static final @NotNull HandlerList handlers = new HandlerList();

    private final Snowball snowball;

    public SnowballEvent(boolean isAsync, Snowball snowball) {
        super(isAsync);
        this.snowball = snowball;
    }

    public SnowballEvent(Snowball snowball) {
        this.snowball = snowball;
    }

    public Snowball getSnowball() {
        return snowball;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
