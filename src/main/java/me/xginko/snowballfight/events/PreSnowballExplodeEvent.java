package me.xginko.snowballfight.events;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Snowball;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PreSnowballExplodeEvent extends Event implements Cancellable {

    private static final @NotNull HandlerList handlers = new HandlerList();
    private boolean isCancelled;

    private @NotNull Snowball snowball;
    private @Nullable Entity hitEntity;
    private @NotNull Location explodeLocation;
    private float explosionPower;
    private boolean setFire, breakBlocks;

    public PreSnowballExplodeEvent(
            @NotNull Snowball snowball,
            @Nullable Entity hitEntity,
            @NotNull Location explodeLocation,
            float explosionPower,
            boolean explosionSetFire,
            boolean explosionBreakBlocks,
            final boolean isAsync
    ) {
        super(isAsync);
        this.isCancelled = false;
        this.snowball = snowball;
        this.hitEntity = hitEntity;
        this.explodeLocation = explodeLocation;
        this.explosionPower = explosionPower;
        this.setFire = explosionSetFire;
        this.breakBlocks = explosionBreakBlocks;
    }

    public @NotNull Snowball getSnowball() {
        return snowball;
    }
    public void setSnowball(@NotNull Snowball snowball) {
        this.snowball = snowball;
    }
    public @Nullable Entity getHitEntity() {
        return hitEntity;
    }
    public void setHitEntity(@Nullable Entity hitEntity) {
        this.hitEntity = hitEntity;
    }
    public @NotNull Location getExplodeLocation() {
        return explodeLocation;
    }
    public void setExplodeLocation(@NotNull Location explodeLocation) {
        this.explodeLocation = explodeLocation;
    }
    public float getExplosionPower() {
        return explosionPower;
    }
    public void setExplosionPower(float explosionPower) {
        this.explosionPower = explosionPower;
    }
    public boolean willSetFire() {
        return setFire;
    }
    public void setFire(boolean setFire) {
        this.setFire = setFire;
    }
    public boolean willBreakBlocks() {
        return breakBlocks;
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
