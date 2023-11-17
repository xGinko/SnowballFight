package me.xginko.snowballfight.events;

import me.xginko.snowballfight.SnowballFight;
import me.xginko.snowballfight.models.WrappedSnowball;
import org.bukkit.entity.Snowball;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.jetbrains.annotations.NotNull;

public class SnowballLaunchEvent extends ProjectileLaunchEvent implements Cancellable {

    private static final @NotNull HandlerList handlers = new HandlerList();
    private boolean isCancelled;

    private @NotNull WrappedSnowball wrappedSnowball;

    public SnowballLaunchEvent(@NotNull Snowball snowball, boolean isCancelled) {
        this(snowball, SnowballFight.getCache().getOrAdd(snowball), isCancelled);
    }

    public SnowballLaunchEvent(@NotNull Snowball snowball, @NotNull WrappedSnowball wrappedSnowball, boolean isCancelled) {
        super(snowball);
        this.wrappedSnowball = wrappedSnowball;
        this.isCancelled = isCancelled;
    }

    public @NotNull WrappedSnowball getWrappedSnowball() {
        return wrappedSnowball;
    }
    public void setWrappedSnowball(@NotNull WrappedSnowball wrappedSnowball) {
        this.wrappedSnowball = wrappedSnowball;
    }

    @Override
    public void setCancelled(boolean cancel) {
        isCancelled = cancel;
    }
    @Override
    public boolean isCancelled() {
        return isCancelled;
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
