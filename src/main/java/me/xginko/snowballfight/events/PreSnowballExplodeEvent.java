package me.xginko.snowballfight.events;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Snowball;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PreSnowballExplodeEvent extends SnowballExplodeEvent implements Cancellable {

    private static final @NotNull HandlerList handlers = new HandlerList();
    private boolean isCancelled;

    public PreSnowballExplodeEvent(@NotNull Snowball snowball, @Nullable Entity hitEntity,
                                   @NotNull Location location, float power, boolean fire, boolean breakBlocks) {
        super(snowball, hitEntity, location, power, fire, breakBlocks);
        this.isCancelled = false;
        this.hitEntity = hitEntity;
    }

    public void setHitEntity(@Nullable Entity hitEntity) {
        this.hitEntity = hitEntity;
    }

    public void setLocation(@NotNull Location location) {
        this.location = location;
    }

    public void setPower(float explosionPower) {
        this.explosionPower = explosionPower;
    }

    public void setFire(boolean setFire) {
        this.setFire = setFire;
    }

    public void setBreakBlocks(boolean breakBlocks) {
        this.breakBlocks = breakBlocks;
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