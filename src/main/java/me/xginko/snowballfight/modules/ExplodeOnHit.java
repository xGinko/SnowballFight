package me.xginko.snowballfight.modules;

import com.tcoded.folialib.FoliaLib;
import com.tcoded.folialib.impl.ServerImplementation;
import me.xginko.snowballfight.SnowballConfig;
import me.xginko.snowballfight.SnowballFight;
import me.xginko.snowballfight.events.PostSnowballExplodeEvent;
import me.xginko.snowballfight.events.PreSnowballExplodeEvent;
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

    private final ServerImplementation scheduler;
    private final HashSet<EntityType> configuredTypes = new HashSet<>();
    private final float explosionPower;
    private final boolean explosionSetFire, explosionBreakBlocks, onlyForEntities, onlyForSpecificEntities, asBlacklist, isFolia;

    protected ExplodeOnHit() {
        shouldEnable();
        FoliaLib foliaLib = SnowballFight.getFoliaLib();
        this.isFolia = foliaLib.isFolia();
        this.scheduler = isFolia ? foliaLib.getImpl() : null;
        SnowballConfig config = SnowballFight.getConfiguration();
        config.master().addComment("settings.explosions","\nMake snowballs explode when hitting something.");
        this.explosionPower = config.getFloat("settings.explosions.power", 2.0F,
                "TNT has a power of 4.0.");
        this.explosionSetFire = config.getBoolean("settings.explosions.set-fire", false,
                "Enable explosion fire like with respawn anchors.");
        this.explosionBreakBlocks = config.getBoolean("settings.explosions.break-blocks", true,
                "Enable destruction of nearby blocks.");
        this.onlyForEntities = config.getBoolean("settings.explosions.only-for-entities", false,
                "Enable if you only want explosions to happen when snowballs hit an entity.");
        this.onlyForSpecificEntities = config.getBoolean("settings.explosions.only-for-specific-entities", false, """
                When enabled, snowballs will only explode for the configured entity types below.\s
                Needs only-for-entities to be set to true.""");
        this.asBlacklist = config.getBoolean("settings.explosions.use-list-as-blacklist", false, """
                Setting this and only-for-specific-entities to true will mean there won't be an explosion\s
                when one of the configured entities are hit by a snowball.""");
        config.getList("settings.explosions.specific-entity-types",
                List.of(EntityType.PLAYER.name(), EntityType.WITHER.name()),
                "Please use correct enums from: https://jd.papermc.io/paper/1.20/org/bukkit/entity/EntityType.html"
        ).forEach(configuredType -> {
            try {
                EntityType type = EntityType.valueOf(configuredType);
                this.configuredTypes.add(type);
            } catch (IllegalArgumentException e) {
                SnowballFight.getLog().warning("(Explosions) Configured entity type '"+configuredType+"' not recognized. " +
                        "Please use correct values from: https://jd.papermc.io/paper/1.20/org/bukkit/entity/EntityType.html");
            }
        });
    }

    @Override
    public boolean shouldEnable() {
        return SnowballFight.getConfiguration().getBoolean("settings.explosions.enable", true);
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
    private void onSnowballHit(ProjectileHitEvent event) {
        if (!event.getEntityType().equals(EntityType.SNOWBALL)) return;
        final Entity hitEntity = event.getHitEntity();
        if (onlyForEntities) {
            if (hitEntity == null) return;
            if (onlyForSpecificEntities && (asBlacklist == configuredTypes.contains(hitEntity.getType()))) return;
        }

        PreSnowballExplodeEvent preSnowballExplodeEvent = new PreSnowballExplodeEvent(
                (Snowball) event.getEntity(),
                hitEntity,
                event.getHitBlock() != null ? event.getHitBlock().getLocation().toCenterLocation() : event.getEntity().getLocation(),
                explosionPower,
                explosionSetFire,
                explosionBreakBlocks,
                event.isAsynchronous()
        );

        if (!preSnowballExplodeEvent.callEvent()) return;

        if (isFolia) {
            final Location explodeLoc = preSnowballExplodeEvent.getExplodeLocation();
            scheduler.runAtLocation(explodeLoc, snobol -> {
                new PostSnowballExplodeEvent(
                        preSnowballExplodeEvent.getSnowball(),
                        preSnowballExplodeEvent.getHitEntity(),
                        explodeLoc,
                        preSnowballExplodeEvent.getExplosionPower(),
                        preSnowballExplodeEvent.willSetFire(),
                        preSnowballExplodeEvent.willBreakBlocks(),
                        event.isAsynchronous()
                ).callEvent();
            });
        } else {
            new PostSnowballExplodeEvent(
                    preSnowballExplodeEvent.getSnowball(),
                    preSnowballExplodeEvent.getHitEntity(),
                    preSnowballExplodeEvent.getExplodeLocation(),
                    preSnowballExplodeEvent.getExplosionPower(),
                    preSnowballExplodeEvent.willSetFire(),
                    preSnowballExplodeEvent.willBreakBlocks(),
                    event.isAsynchronous()
            ).callEvent();
        }
    }
}