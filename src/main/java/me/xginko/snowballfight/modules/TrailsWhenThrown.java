package me.xginko.snowballfight.modules;

import com.cryptomorin.xseries.particles.XParticle;
import com.destroystokyo.paper.ParticleBuilder;
import me.xginko.snowballfight.SnowballFight;
import me.xginko.snowballfight.WrappedSnowball;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import space.arim.morepaperlib.scheduling.ScheduledTask;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class TrailsWhenThrown extends SnowballModule implements Listener {

    private final long maxTrailTaskAliveTime, initialDelay, period;
    private final int particlesPerTick;
    private final boolean onlyPlayers;

    private Map<UUID, ScheduledTask> particleTracker;

    protected TrailsWhenThrown() {
        super("settings.trails", true,
                "\nSpawn colored particle trails when a snowball is launched.");
        this.onlyPlayers = config.getBoolean(configPath + ".only-thrown-by-player", true,
                "If enabled will only work if the snowball was thrown by a player.");
        this.particlesPerTick = config.getInt(configPath + ".particles-per-tick", 10,
                "How many particles to spawn per tick. Recommended to leave low.");
        this.maxTrailTaskAliveTime = TimeUnit.SECONDS.toMillis(config.getInt(configPath + ".max-trail-task-alive-time-seconds", 20,
                "How many seconds until the trails will no longer be generated on the same snowball to save resources."));
        this.initialDelay = Math.max(1, config.getInt(configPath + ".initial-delay-ticks", 3,
                "Time in ticks after throwing snowball until trails should begin to generate." +
                "Recommended: At least 2 ticks delay so the particles don't obstruct the players view."));
        this.period = Math.max(1, config.getInt(configPath + ".repeat-delay-ticks", 1,
                "How often per tick a particle should be spawned.\n" +
                "Recommended: 1 tick delay to get the best looking trails."));
    }

    @Override
    public void enable() {
        particleTracker = new ConcurrentHashMap<>();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void disable() {
        HandlerList.unregisterAll(this);
        if (particleTracker != null) {
            particleTracker.forEach(((uuid, scheduledTask) -> scheduledTask.cancel()));
            particleTracker.clear();
            particleTracker = null;
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (!event.getEntityType().equals(EntityType.SNOWBALL)) return;
        if (onlyPlayers && !(event.getEntity().getShooter() instanceof Player)) return;

        final Snowball snowball = (Snowball) event.getEntity();
        final WrappedSnowball wrappedSnowball = SnowballFight.snowballs().get(snowball.getUniqueId(), k -> new WrappedSnowball(snowball));

        // According to console errors, only redstone particles can be colored
        ParticleBuilder primary = new ParticleBuilder(XParticle.DUST.get())
                .color(wrappedSnowball.getPrimaryColor())
                .count(particlesPerTick);
        ParticleBuilder secondary = new ParticleBuilder(XParticle.DUST.get())
                .color(wrappedSnowball.getSecondaryColor())
                .count(particlesPerTick);

        final long stopTimeMillis = System.currentTimeMillis() + maxTrailTaskAliveTime;

        this.particleTracker.put(
                snowball.getUniqueId(),
                SnowballFight.scheduling().entitySpecificScheduler(snowball).runAtFixedRate(() -> {
                    // Get new current location on each run
                    final Location snowballLoc = snowball.getLocation();
                    // Spawn particles using preconfigured ParticleBuilders
                    primary.location(snowballLoc).spawn();
                    secondary.location(snowballLoc).spawn();
                    // Stop the task because by itself it would keep running until server restart.
                    if (snowball.isDead() || System.currentTimeMillis() > stopTimeMillis) {
                        ScheduledTask trails = particleTracker.get(snowball.getUniqueId());
                        if (trails != null) trails.cancel();
                        particleTracker.remove(snowball.getUniqueId());
                    }
                }, null, initialDelay, period)
        );
    }
}