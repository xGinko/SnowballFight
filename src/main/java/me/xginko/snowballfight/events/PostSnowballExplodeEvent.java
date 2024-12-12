package me.xginko.snowballfight.events;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Snowball;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PostSnowballExplodeEvent extends SnowballExplodeEvent {

    private static final @NotNull HandlerList handlers = new HandlerList();

    private final boolean hasExploded;

    public PostSnowballExplodeEvent(@NotNull Snowball snowball, @Nullable Entity hitEntity,
                                    @NotNull Location location, float power, boolean fire, boolean brokeBlocks, boolean hasExploded) {
        super(snowball, hitEntity, location, power, fire, brokeBlocks);
        this.hasExploded = hasExploded;
    }

    public boolean hasExploded() {
        return hasExploded;
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