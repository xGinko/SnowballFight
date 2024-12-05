package me.xginko.snowballfight.modules;

import com.cryptomorin.xseries.XEntityType;
import com.cryptomorin.xseries.particles.XParticle;
import me.xginko.snowballfight.SnowballFight;
import me.xginko.snowballfight.WrappedSnowball;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
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
    private final float particleSize;
    private final int particlesPerTick;
    private final boolean onlyPlayers;

    private static Map<UUID, ScheduledTask> particleTasks;

    protected TrailsWhenThrown() {
        super("settings.trails", true,
                "\nSpawn colored particle trails when a snowball is launched.");
        this.onlyPlayers = config.getBoolean(configPath + ".only-thrown-by-player", true,
                "If enabled will only work if the snowball was thrown by a player.");
        this.trailDurationMillis = TimeUnit.SECONDS.toMillis(
                Math.max(1, config.getInt(configPath + ".trail-duration-seconds", 20)));
        this.particlesPerTick = Math.max(1, config.getInt(configPath + ".particles-per-tick", 10,
                "How many particles to spawn per tick. Recommended to leave low."));
        this.particleSize = config.getFloat(configPath + ".particle-size", 1.0F);
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
        if (particleTasks.containsKey(event.getEntity().getUniqueId())) return;

        @Nullable ScheduledTask particleTask = SnowballFight.scheduling().entitySpecificScheduler(event.getEntity())
                .runAtFixedRate(
                        new SnowballTrailTask(event.getEntity(), trailDurationMillis, particlesPerTick, particleSize),
                        null,
                        initialDelayTicks,
                        periodTicks
                );

        if (particleTask != null) { // Task is null if the entity was removed by the time particles were scheduled
            particleTasks.put(event.getEntity().getUniqueId(), particleTask);
        }
    }

    private static class SnowballTrailTask implements Runnable {

        private final Projectile snowball;
        private final Particle.DustOptions[] dustOptions;
        private final long expireMillis;
        private final int amount;

        private SnowballTrailTask(Projectile snowball, long durationMillis, int amount, float size) {
            this.snowball = snowball;
            this.amount = amount;
            final WrappedSnowball wrappedSnowball = SnowballFight.snowballTracker().get((Snowball) snowball);
            this.dustOptions = new Particle.DustOptions[] {
                    new Particle.DustOptions(wrappedSnowball.getPrimaryColor(), size),
                    new Particle.DustOptions(wrappedSnowball.getSecondaryColor(), size)
            };
            this.expireMillis = System.currentTimeMillis() + durationMillis;
        }

        @Override
        public void run() {
            if (snowball.isDead() || System.currentTimeMillis() >= expireMillis) {
                particleTasks.remove(snowball.getUniqueId()).cancel();
                return;
            }

            for (Particle.DustOptions dustOption : dustOptions) {
                try {
                    snowball.getWorld().spawnParticle(XParticle.DUST.get(), snowball.getLocation(), amount, dustOption);
                } catch (Throwable t) {
                    particleTasks.remove(snowball.getUniqueId()).cancel();
                    SnowballFight.logger().warn("Trail task ended with an exception - {}", t.getLocalizedMessage());
                }
            }
        }
    }
}