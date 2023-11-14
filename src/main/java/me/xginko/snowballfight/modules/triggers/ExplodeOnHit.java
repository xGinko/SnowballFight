package me.xginko.snowballfight.modules.triggers;

import io.papermc.paper.threadedregions.scheduler.RegionScheduler;
import me.xginko.snowballfight.SnowballConfig;
import me.xginko.snowballfight.SnowballFight;
import me.xginko.snowballfight.events.PostSnowballExplodeEvent;
import me.xginko.snowballfight.events.PreSnowballExplodeEvent;
import me.xginko.snowballfight.modules.SnowballModule;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;

import java.util.HashSet;
import java.util.List;

public class ExplodeOnHit implements SnowballModule, Listener {

    private final SnowballFight plugin;
    private final RegionScheduler regionScheduler;
    private final HashSet<EntityType> typesThatExplode = new HashSet<>();
    private final boolean onlyForEntities, onlyForSpecificEntities, asBlacklist;

    public ExplodeOnHit() {
        this.plugin = SnowballFight.getInstance();
        this.regionScheduler = plugin.getServer().getRegionScheduler();
        SnowballConfig config = SnowballFight.getConfiguration();
        this.onlyForEntities = config.getBoolean("explosion-triggers.on-snowball-hit.only-for-entities", true);
        this.onlyForSpecificEntities = config.getBoolean("explosion-triggers.on-snowball-hit.only-for-specific-entities", false);
        this.asBlacklist = config.getBoolean("explosion-triggers.on-snowball-hit.use-as-blacklist", false);
        config.getList("explosion-triggers.on-snowball-hit.specific-hit-types",
                List.of(EntityType.PLAYER.name())
        ).forEach(configuredType -> {
            try {
                EntityType type = EntityType.valueOf(configuredType);
                this.typesThatExplode.add(type);
            } catch (IllegalArgumentException e) {
                SnowballFight.getLog().warning("Configured entity type '"+configuredType+"' not recognized. " +
                        "Please use correct values from: https://jd.papermc.io/paper/1.20/org/bukkit/entity/EntityType.html");
            }
        });
    }

    @Override
    public boolean shouldEnable() {
        return SnowballFight.getConfiguration().getBoolean("explosion-triggers.on-snowball-hit.enable", true);
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
    private void onProjectileHit(ProjectileHitEvent event) {
        if (!event.getEntityType().equals(EntityType.SNOWBALL)) return;
        Entity hitEntity = event.getHitEntity();
        if (onlyForEntities) {
            if (hitEntity == null) return;
            if (onlyForSpecificEntities && (asBlacklist == typesThatExplode.contains(event.getHitEntity().getType()))) return;
        }

        PreSnowballExplodeEvent preSnowballExplodeEvent = new PreSnowballExplodeEvent(
                (Snowball) event.getEntity(),
                hitEntity,
                event.getHitBlock() != null ? event.getHitBlock().getLocation().toCenterLocation() : event.getEntity().getLocation()
        );

        if (!preSnowballExplodeEvent.callEvent()) return;
        final Entity hit = preSnowballExplodeEvent.getHitEntity();

        if (hit != null) {
            hit.getScheduler().run(plugin, snobol -> {
                new PostSnowballExplodeEvent(
                        preSnowballExplodeEvent.getSnowball(),
                        hit,
                        preSnowballExplodeEvent.getExplodeLocation(),
                        preSnowballExplodeEvent.getExplosionPower(),
                        preSnowballExplodeEvent.willSetFire(),
                        preSnowballExplodeEvent.willBreakBlocks()
                ).callEvent();
            }, null);
        } else {
            final Location explodeLoc = preSnowballExplodeEvent.getExplodeLocation();
            regionScheduler.run(plugin, explodeLoc, snobol -> {
                new PostSnowballExplodeEvent(
                        preSnowballExplodeEvent.getSnowball(),
                        null,
                        explodeLoc,
                        preSnowballExplodeEvent.getExplosionPower(),
                        preSnowballExplodeEvent.willSetFire(),
                        preSnowballExplodeEvent.willBreakBlocks()
                ).callEvent();
            });
        }
    }
}