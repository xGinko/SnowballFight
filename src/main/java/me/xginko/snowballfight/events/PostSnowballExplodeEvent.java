package me.xginko.snowballfight.events;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Snowball;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PostSnowballExplodeEvent extends Event {

    private static final @NotNull HandlerList handlers = new HandlerList();

    private final @NotNull Snowball snowball;
    private final @Nullable Entity hitEntity;
    private final @NotNull Location explodeLocation;
    private final float explosionPower;
    private final boolean setFire, breakBlocks, hasExploded;

    public PostSnowballExplodeEvent(
            final @NotNull Snowball snowball,
            final @Nullable Entity hitEntity,
            final @NotNull Location explodeLocation,
            final float explosionPower,
            final boolean setFire,
            final boolean breakBlocks,
            final boolean isAsync
    ) {
        super(isAsync);
        this.snowball = snowball;
        this.hitEntity = hitEntity;
        this.explodeLocation = explodeLocation;
        this.explosionPower = explosionPower;
        this.setFire = setFire;
        this.breakBlocks = breakBlocks;
        this.hasExploded = explodeLocation.getWorld().createExplosion(
                snowball.getShooter() instanceof LivingEntity livingThrower ? livingThrower : snowball, // Set explode source for tracking
                explodeLocation,
                explosionPower,
                setFire,
                breakBlocks
        );
    }

    public @NotNull Snowball getSnowball() {
        return snowball;
    }
    public @Nullable Entity getHitEntity() {
        return hitEntity;
    }
    public @NotNull Location getExplodeLocation() {
        return explodeLocation;
    }
    public float getExplosionPower() {
        return explosionPower;
    }
    public boolean hasSetFire() {
        return setFire;
    }
    public boolean hasBrokenBlocks() {
        return breakBlocks;
    }
    public boolean hasExploded() {
        return hasExploded;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }
    public static @NotNull HandlerList getHandlerList() {
        return handlers;
    }
}
