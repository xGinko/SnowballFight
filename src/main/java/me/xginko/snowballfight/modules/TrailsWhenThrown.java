package me.xginko.snowballfight.modules;

import com.destroystokyo.paper.ParticleBuilder;
import com.tcoded.folialib.impl.ServerImplementation;
import com.tcoded.folialib.wrapper.task.WrappedTask;
import me.xginko.snowballfight.SnowballConfig;
import me.xginko.snowballfight.SnowballFight;
import me.xginko.snowballfight.events.SnowballLaunchEvent;
import me.xginko.snowballfight.models.WrappedSnowball;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.UUID;

public class TrailsWhenThrown implements SnowballModule, Listener {

    private final ServerImplementation scheduler;
    private final HashMap<UUID, WrappedTask> particleTrails = new HashMap<>();
    private final int particlesPerTick;
    private final long maxTrailTaskAliveTime;

    protected TrailsWhenThrown() {
        shouldEnable();
        this.scheduler = SnowballFight.getFoliaLib().getImpl();
        SnowballConfig config = SnowballFight.getConfiguration();
        this.particlesPerTick = config.getInt("settings.trails.particles-per-tick", 10,
                "How many particles to spawn per tick. Recommended to leave low.");
        this.maxTrailTaskAliveTime = config.getInt("settings.trails.max-trail-task-alive-time-seconds", 30,
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
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    private void onPostSnowballExplode(SnowballLaunchEvent event) {
        final WrappedSnowball wrappedSnowball = event.getWrappedSnowball();
        final Snowball snowball = wrappedSnowball.snowball();

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
                    final Location snowballLoc = snowball.getLocation();
                    primary.location(snowballLoc).spawn();
                    secondary.location(snowballLoc).spawn();
                    if (snowball.isDead() || System.currentTimeMillis() > stopTimeMillis) {
                        // Stop the task because by itself it will loop until server restart.
                        WrappedTask trails = particleTrails.get(snowballUUID);
                        if (trails != null) trails.cancel();
                        particleTrails.remove(snowballUUID);
                    }
                }, 1L, 1L)
        );
    }
}