package me.xginko.snowballfight.modules;

import com.cryptomorin.xseries.XEntityType;
import com.cryptomorin.xseries.particles.XParticle;
import com.destroystokyo.paper.ParticleBuilder;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;
import com.github.benmanes.caffeine.cache.RemovalListener;
import me.xginko.snowballfight.SnowballFight;
import me.xginko.snowballfight.WrappedSnowball;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import space.arim.morepaperlib.scheduling.ScheduledTask;

import java.time.Duration;
import java.util.UUID;

public class TrailsWhenThrown extends SnowballModule implements RemovalListener<UUID, ScheduledTask>, Listener {

    private final long maxTrailSeconds, initialDelayTicks, periodTicks;
    private final int particlesPerTick;
    private final boolean onlyPlayers;

    private Cache<UUID, ScheduledTask> particleTracker;

    protected TrailsWhenThrown() {
        super("settings.trails", true,
                "\nSpawn colored particle trails when a snowball is launched.");
        this.onlyPlayers = config.getBoolean(configPath + ".only-thrown-by-player", true,
                "If enabled will only work if the snowball was thrown by a player.");
        this.maxTrailSeconds = Math.max(1, config.getInt(configPath + ".trail-duration-seconds", 20));
        this.particlesPerTick = Math.max(1, config.getInt(configPath + ".particles-per-tick", 10,
                "How many particles to spawn per tick. Recommended to leave low."));
        this.initialDelayTicks = Math.max(1, config.getInt(configPath + ".initial-delay-ticks", 3,
                "Time in ticks after throwing snowball until trails should begin to generate." +
                "Recommended: At least 2 ticks delay so the particles don't obstruct the players view."));
        this.periodTicks = Math.max(1, config.getInt(configPath + ".repeat-delay-ticks", 1,
                "How often per tick a particle should be spawned.\n" +
                "Recommended: 1 tick delay to get the best looking trails."));
    }

    @Override
    public void enable() {
        particleTracker = Caffeine.newBuilder().expireAfterWrite(Duration.ofSeconds(maxTrailSeconds)).evictionListener(this).build();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void disable() {
        HandlerList.unregisterAll(this);
        if (particleTracker != null) {
            particleTracker.invalidateAll();
            particleTracker.cleanUp();
            particleTracker = null;
        }
    }

    @Override
    public void onRemoval(@Nullable UUID uuid, @Nullable ScheduledTask scheduledTask, @NonNull RemovalCause cause) {
        if (scheduledTask != null) // Shouldn't happen since we aren't using weakKeys but just in case
            scheduledTask.cancel();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (event.getEntityType() != XEntityType.SNOWBALL.get()) return;
        if (onlyPlayers && !(event.getEntity().getShooter() instanceof Player)) return;

        final Snowball snowball = (Snowball) event.getEntity();
        if (particleTracker.getIfPresent(snowball.getUniqueId()) != null) return;

        final WrappedSnowball wrappedSnowball = SnowballFight.snowballs().get(snowball);

        ParticleBuilder primary = new ParticleBuilder(XParticle.DUST.get()) // Only redstone particles can be colored
                .color(wrappedSnowball.getPrimaryColor())
                .count(particlesPerTick);
        ParticleBuilder secondary = new ParticleBuilder(XParticle.DUST.get())
                .color(wrappedSnowball.getSecondaryColor())
                .count(particlesPerTick);

        @Nullable ScheduledTask particleTask = SnowballFight.scheduling().entitySpecificScheduler(snowball).runAtFixedRate(() -> {
            if (snowball.isDead()) { // Stop the task because it would keep running until server restart on bukkit scheduler.
                ScheduledTask task = particleTracker.getIfPresent(snowball.getUniqueId());
                if (task != null) task.cancel();
                return;
            }

            // Get new current location on each run
            final Location snowballLoc = snowball.getLocation();
            // Spawn particles using preconfigured ParticleBuilders
            primary.location(snowballLoc).spawn();
            secondary.location(snowballLoc).spawn();
        }, null, initialDelayTicks, periodTicks);

        if (particleTask != null) {
            this.particleTracker.put(snowball.getUniqueId(), particleTask);
        }
    }
}