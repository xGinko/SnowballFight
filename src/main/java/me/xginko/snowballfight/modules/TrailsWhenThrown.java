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

    public TrailsWhenThrown() {
        shouldEnable();
        this.scheduler = SnowballFight.getFoliaLib().getImpl();
        SnowballConfig config = SnowballFight.getConfiguration();
        this.particlesPerTick = config.getInt("settings.trajectory-trails.particles-per-tick", 10);
    }

    @Override
    public boolean shouldEnable() {
        return SnowballFight.getConfiguration().getBoolean("settings.trajectory-trails.enable", true);
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

        this.particleTrails.put(
                snowballUUID,
                scheduler.runAtEntityTimer(snowball, () -> {
                    final Location snowballLoc = snowball.getLocation();
                    primary.location(snowballLoc).spawn();
                    secondary.location(snowballLoc).spawn();
                    if (snowball.isDead()) { // End the things we begin.
                        WrappedTask trails = particleTrails.get(snowballUUID);
                        if (trails != null) trails.cancel();
                        particleTrails.remove(snowballUUID);
                    }
                }, 2L, 1L)
        );
    }
}