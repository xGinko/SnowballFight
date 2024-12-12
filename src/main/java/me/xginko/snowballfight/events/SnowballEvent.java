package me.xginko.snowballfight.events;

import org.bukkit.entity.Snowball;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityEvent;
import org.jetbrains.annotations.NotNull;

public abstract class SnowballEvent extends EntityEvent {

    private static final @NotNull HandlerList handlers = new HandlerList();

    public SnowballEvent(@NotNull Snowball snowball) {
        super(snowball);
    }

    public @NotNull Snowball getSnowball() {
        return (Snowball) entity;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static @NotNull HandlerList getHandlerList() {
        return handlers;
    }
}
