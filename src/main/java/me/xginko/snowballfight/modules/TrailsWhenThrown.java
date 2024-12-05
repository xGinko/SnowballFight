package me.xginko.snowballfight.modules;

import com.cryptomorin.xseries.XEntityType;
import com.cryptomorin.xseries.particles.XParticle;
import me.xginko.snowballfight.SnowballFight;
import me.xginko.snowballfight.WrappedSnowball;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.checkerframework.checker.nullness.qual.Nullable;
import space.arim.morepaperlib.scheduling.ScheduledTask;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class TrailsWhenThrown extends SnowballModule implements Listener {

    private final long trailDurationMillis, initialDelayTicks, periodTicks;
    private final int particlesPerTick;
    private final boolean onlyPlayers;

    private Map<UUID, ScheduledTask> particleTasks;

    protected TrailsWhenThrown() {
        super("settings.trails", true,
                "\nSpawn colored particle trails when a snowball is launched.");
        this.onlyPlayers = config.getBoolean(configPath + ".only-thrown-by-player", true,
                "If enabled will only work if the snowball was thrown by a player.");
        this.trailDurationMillis = TimeUnit.SECONDS.toMillis(
                Math.max(1, config.getInt(configPath + ".trail-duration-seconds", 20)));
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
        particleTasks = new ConcurrentHashMap<>();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void disable() {
        HandlerList.unregisterAll(this);
        if (particleTasks != null) {
            particleTasks.forEach(((uuid, scheduledTask) -> scheduledTask.cancel()));
            particleTasks.clear();
            particleTasks = null;
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (event.getEntityType() != XEntityType.SNOWBALL.get()) return;
        if (onlyPlayers && !(event.getEntity().getShooter() instanceof Player)) return;

        final Snowball snowball = (Snowball) event.getEntity();
        if (particleTasks.containsKey(snowball.getUniqueId())) return;

        final WrappedSnowball wrappedSnowball = SnowballFight.snowballTracker().get(snowball);
        final long expireTimeMillis = System.currentTimeMillis() + trailDurationMillis;

        @Nullable ScheduledTask particleTask = SnowballFight.scheduling().entitySpecificScheduler(snowball).runAtFixedRate(() -> {
            if (snowball.isDead() || System.currentTimeMillis() >= expireTimeMillis) {
                particleTasks.remove(snowball.getUniqueId()).cancel();
            } else {
                spawnTrailParticle(snowball.getLocation(), wrappedSnowball.getPrimaryColor());
                spawnTrailParticle(snowball.getLocation(), wrappedSnowball.getSecondaryColor());
            }
        }, null, initialDelayTicks, periodTicks);

        if (particleTask != null) { // Task is null if the entity was removed by the time particles were scheduled
            particleTasks.put(snowball.getUniqueId(), particleTask);
        }
    }

    private void spawnTrailParticle(Location location, Color color) {
        location.getWorld().spawnParticle( // Use spigot available method for compatibility
                XParticle.DUST.get(), // Only redstone dust can be colored (XParticle for compatibility across versions)
                null, // receivers = null means it will be visible for everyone
                null, // source = null because the source is not a player
                location.getX(), location.getY(), location.getZ(),
                particlesPerTick,
                0, 0, 0, // No location offset needed, we want the exact location of the snowball
                1, // No extras needed so we set it to default (1)
                color, // data = color only works on redstone dust
                true // Always force
        );
    }
}