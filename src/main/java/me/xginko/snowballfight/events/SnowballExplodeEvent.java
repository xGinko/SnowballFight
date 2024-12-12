package me.xginko.snowballfight.events;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Snowball;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class SnowballExplodeEvent extends SnowballEvent {

    private static final @NotNull HandlerList handlers = new HandlerList();

    protected @Nullable Entity hitEntity;
    protected @NotNull Location location;
    protected float explosionPower;
    protected boolean setFire, breakBlocks;

    public SnowballExplodeEvent(Snowball snowball, @Nullable Entity hitEntity,
                                @NotNull Location location, float explosionPower, boolean setFire, boolean breakBlocks) {
        super(snowball);
        this.hitEntity = hitEntity;
        this.location = location;
        this.explosionPower = explosionPower;
        this.setFire = setFire;
        this.breakBlocks = breakBlocks;
    }

    public @Nullable Entity getHitEntity() {
        return hitEntity;
    }

    public @NotNull Location getLocation() {
        return location;
    }

    public float getPower() {
        return explosionPower;
    }

    public boolean getFire() {
        return setFire;
    }

    public boolean getBreakBlocks() {
        return breakBlocks;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static @NotNull HandlerList getHandlerList() {
        return handlers;
    }
}
