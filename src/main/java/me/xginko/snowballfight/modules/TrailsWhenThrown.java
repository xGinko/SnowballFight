package me.xginko.snowballfight.modules;

import com.destroystokyo.paper.ParticleBuilder;
import com.tcoded.folialib.impl.ServerImplementation;
import com.tcoded.folialib.wrapper.task.WrappedTask;
import me.xginko.snowballfight.SnowballCache;
import me.xginko.snowballfight.SnowballConfig;
import me.xginko.snowballfight.SnowballFight;
import me.xginko.snowballfight.models.WrappedSnowball;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;

import java.util.HashMap;
import java.util.UUID;

public class TrailsWhenThrown implements SnowballModule, Listener {

    private final ServerImplementation scheduler;
    private final SnowballCache snowballCache;
    private final HashMap<UUID, WrappedTask> particleTrails = new HashMap<>();
    private final int particlesPerTick;
    private final long maxTrailTaskAliveTime;

    protected TrailsWhenThrown() {
        shouldEnable();
        this.scheduler = SnowballFight.getFoliaLib().getImpl();
        this.snowballCache = SnowballFight.getCache();
        SnowballConfig config = SnowballFight.getConfiguration();
        config.master().addComment("settings.trails", "\nSpawn colored particle trails when a snowball is launched.");
        this.particlesPerTick = config.getInt("settings.trails.particles-per-tick", 10,
                "How many particles to spawn per tick. Recommended to leave low.");
        this.maxTrailTaskAliveTime = config.getInt("settings.trails.max-trail-task-alive-time-seconds", 20,
                "How many seconds until the trails will no longer be generated on the same snowball to save resources.") * 1000L;
    }

    @Override
    public boolean shouldEnable() {
        return SnowballFight.getConfiguration().getBoolean("settings.trails.enable", true);
    }

    @Override
    public void enable() {
        SnowballFight plugin = SnowballFight.getInstance();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void disable() {
        HandlerList.unregisterAll(this);
        this.particleTrails.values().forEach(WrappedTask::cancel);
        this.particleTrails.clear();
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    private void onSnowballLaunch(ProjectileLaunchEvent event) {
        if (!event.getEntityType().equals(EntityType.SNOWBALL)) return;

        final Snowball snowball = (Snowball) event.getEntity();
        final WrappedSnowball wrappedSnowball = snowballCache.getOrAdd(snowball);

        // According to console errors, only redstone particles can be colored
        ParticleBuilder primary = new ParticleBuilder(Particle.REDSTONE)
                .color(wrappedSnowball.getPrimaryColor())
                .count(particlesPerTick);
        ParticleBuilder secondary = new ParticleBuilder(Particle.REDSTONE)
                .color(wrappedSnowball.getSecondaryColor())
                .count(particlesPerTick);

        final UUID snowballUUID = snowball.getUniqueId();
        final long stopTimeMillis = System.currentTimeMillis() + maxTrailTaskAliveTime;

        this.particleTrails.put(
                snowballUUID,
                scheduler.runAtEntityTimer(snowball, () -> {
                    // Get new current location on each run
                    final Location snowballLoc = snowball.getLocation();
                    // Spawn particles using preconfigured ParticleBuilders
                    primary.location(snowballLoc).spawn();
                    secondary.location(snowballLoc).spawn();
                    // Stop the task because by itself it would keep running until server restart.
                    if (snowball.isDead() || System.currentTimeMillis() > stopTimeMillis) {
                        WrappedTask trails = particleTrails.get(snowballUUID);
                        if (trails != null) trails.cancel();
                        particleTrails.remove(snowballUUID);
                    }
                }, 1L, 1L) // 1 Tick start delay because folia, 1 Tick execute delay to get the best looking trails
        );
    }
}