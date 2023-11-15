package me.xginko.snowballfight.modules.effects;

import com.destroystokyo.paper.ParticleBuilder;
import com.tcoded.folialib.FoliaLib;
import com.tcoded.folialib.impl.ServerImplementation;
import com.tcoded.folialib.wrapper.task.WrappedTask;
import me.xginko.snowballfight.SnowballConfig;
import me.xginko.snowballfight.SnowballFight;
import me.xginko.snowballfight.modules.SnowballModule;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;

import java.util.*;

public class TrailsWhenThrown implements SnowballModule, Listener {

    private final ServerImplementation scheduler;
    private final HashMap<UUID, WrappedTask> trailTasks = new HashMap<>();
    private final List<ParticleBuilder> particleBuilders = new ArrayList<>();
    private final boolean isFolia;

    public TrailsWhenThrown() {
        shouldEnable();
        FoliaLib foliaLib = SnowballFight.getFoliaLib();
        this.isFolia = foliaLib.isFolia();
        this.scheduler = isFolia ? foliaLib.getImpl() : null;
        SnowballConfig config = SnowballFight.getConfiguration();
        final int particles_per_tick = config.getInt("trails.particle-count-per-tick", 10);
        config.getList("trails.colors", List.of(
                "<color:#EEB64B>",   // Yellow
                "<color:#FC9460>",   // Orange
                "<color:#E54264>",   // Red
                "<color:#A92F5F>",   // Dark Red
                "<color:#442261>",   // Purple
                "<color:#283D5E>",   // Dark Blue
                "<color:#327F51>",   // Green
                "<color:#64A47F>",   // Light Green
                "<color:#C3C48A>"    // Light Yellow-Green
        ), "Uses MiniMessage formatting.").forEach(hexString -> {
            final TextColor textColor = MiniMessage.miniMessage().deserialize(hexString).color();
            if (textColor == null) {
                SnowballFight.getLog().warning("Hex color string '"+hexString+"' is not formatted correctly. Use it like this: <color:#E54264>");
            } else {
                this.particleBuilders.add(new ParticleBuilder(Particle.REDSTONE)
                        .color(Color.fromRGB(textColor.red(), textColor.green(), textColor.blue()))
                        .count(particles_per_tick));
            }
        });
    }

    @Override
    public boolean shouldEnable() {
        return SnowballFight.getConfiguration().getBoolean("trails.enable", true) && !this.particleBuilders.isEmpty();
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

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    private void onPostSnowballExplode(ProjectileLaunchEvent event) {
        if (!event.getEntityType().equals(EntityType.SNOWBALL)) return;

        final ParticleBuilder colorA = this.particleBuilders.get(new Random().nextInt(particleBuilders.size()));
        ParticleBuilder rollingColorB = colorA;
        int tries = 0;
        while (rollingColorB.equals(colorA)) {
            if (tries > 100) break;
            rollingColorB = this.particleBuilders.get(new Random().nextInt(particleBuilders.size()));
            tries++;
        }
        final ParticleBuilder colorB = rollingColorB;
        final Projectile snowball = event.getEntity();
        final UUID snowballUUID = snowball.getUniqueId();

        this.trailTasks.put(snowballUUID, scheduler.runAtEntityTimer(snowball, () -> {
            final Location snowballLoc = snowball.getLocation();
            colorA.location(snowballLoc).spawn();
            colorB.location(snowballLoc).spawn();
            if (snowball.isDead()) {
                WrappedTask trails = trailTasks.get(snowballUUID);
                if (trails != null) trails.cancel();
                trailTasks.remove(snowballUUID);
            }
        }, 2L, 1L));
    }
}