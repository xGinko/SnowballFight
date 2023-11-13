package me.xginko.snowballfight.modules.triggers;

import io.papermc.paper.threadedregions.scheduler.RegionScheduler;
import me.xginko.snowballfight.SnowballConfig;
import me.xginko.snowballfight.SnowballFight;
import me.xginko.snowballfight.modules.SnowballModule;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;

import java.util.HashSet;
import java.util.List;

public class ExplodeOnEntityHit implements SnowballModule, Listener {

    private final SnowballFight plugin;
    private final RegionScheduler regionScheduler;
    private final HashSet<EntityType> typesThatExplode = new HashSet<>();
    private final boolean onlySpecificTypes, asBlacklist;

    public ExplodeOnEntityHit() {
        this.plugin = SnowballFight.getInstance();
        this.regionScheduler = plugin.getServer().getRegionScheduler();
        SnowballConfig config = SnowballFight.getConfiguration();
        this.onlySpecificTypes = config.getBoolean("explosion-triggers.snowball-hits-entity.only-for-specific-types", false);
        this.asBlacklist = config.getBoolean("explosion-triggers.snowball-hits-entity.use-as-blacklist", false);
        config.getList("explosion-triggers.snowball-hits-entity.specific-hit-types",
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
        return SnowballFight.getConfiguration().getBoolean("explosion-triggers.snowball-hits-entity.enable", true);
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
        if (!event.getEntity().getType().equals(EntityType.SNOWBALL) || event.getHitEntity() == null) return;
        if (onlySpecificTypes && (asBlacklist == typesThatExplode.contains(event.getHitEntity().getType()))) return;


    }
}
