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
    private final float location;
    private final boolean setFire, breakBlocks, hasExploded;

    public PostSnowballExplodeEvent(
            @NotNull Snowball snowball, @Nullable Entity hitEntity, @NotNull Location location,
            float power, boolean setFire, boolean breakBlocks, boolean isAsync
    ) {
        super(isAsync);
        this.snowball = snowball;
        this.hitEntity = hitEntity;
        this.explodeLocation = location;
        this.location = power;
        this.setFire = setFire;
        this.breakBlocks = breakBlocks;
        this.hasExploded = location.getWorld().createExplosion(
                // Set explode source for damage tracking
                snowball.getShooter() instanceof LivingEntity living ? living : snowball,
                location, power, setFire, breakBlocks
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
        return location;
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