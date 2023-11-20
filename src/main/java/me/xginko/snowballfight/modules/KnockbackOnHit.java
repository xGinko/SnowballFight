package me.xginko.snowballfight.modules;

import com.tcoded.folialib.FoliaLib;
import com.tcoded.folialib.impl.ServerImplementation;
import me.xginko.snowballfight.SnowballConfig;
import me.xginko.snowballfight.SnowballFight;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.List;

public class KnockbackOnHit implements SnowballModule, Listener {

    private final ServerImplementation scheduler;
    private final HashSet<EntityType> configuredTypes = new HashSet<>();
    private final Vector vectorModifier;
    private final double multiplier;
    private final boolean isFolia, modifyVector, onlyForSpecificEntities, asBlacklist;

    protected KnockbackOnHit() {
        shouldEnable();
        FoliaLib foliaLib = SnowballFight.getFoliaLib();
        this.isFolia = foliaLib.isFolia();
        this.scheduler = isFolia ? foliaLib.getImpl() : null;
        SnowballConfig config = SnowballFight.getConfiguration();
        config.master().addComment("settings.knockback", "Modify knockback values on snowball hit.");
        this.multiplier = config.getDouble("settings.knockback.multiplier", 1.2,
                "The multiplier for the knockback of the snowball.");
        this.modifyVector = config.getBoolean("settings.knockback.vector-modifier.enable", true);
        this.vectorModifier = new Vector(
                config.getDouble("settings.knockback.vector-modifier.x", 0.0),
                config.getDouble("settings.knockback.vector-modifier.y", 0.6),
                config.getDouble("settings.knockback.vector-modifier.z", 0.0)
        );
        this.onlyForSpecificEntities = config.getBoolean("settings.knockback.only-for-specific-entities", false,
                "When enabled, only configured entities will be knocked back by snowballs.");
        this.asBlacklist = config.getBoolean("settings.knockback.use-list-as-blacklist", false,
                "All entities except the ones on this list will be knocked back by snowballs if set to true.");
        config.getList("settings.knockback.specific-entity-types",
                List.of(EntityType.PLAYER.name()),
                "Please use correct enums from: https://jd.papermc.io/paper/1.20/org/bukkit/entity/EntityType.html"
        ).forEach(configuredType -> {
            try {
                EntityType type = EntityType.valueOf(configuredType);
                this.configuredTypes.add(type);
            } catch (IllegalArgumentException e) {
                SnowballFight.getLog().warning("(Knockback) Configured entity type '"+configuredType+"' not recognized. " +
                        "Please use correct values from: https://jd.papermc.io/paper/1.20/org/bukkit/entity/EntityType.html");
            }
        });
    }

    @Override
    public boolean shouldEnable() {
        return SnowballFight.getConfiguration().getBoolean("settings.knockback.enable", true);
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

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void onSnowballHit(ProjectileHitEvent event) {
        if (!event.getEntityType().equals(EntityType.SNOWBALL)) return;
        final Projectile snowball = event.getEntity();
        if (onlyForSpecificEntities && (asBlacklist == configuredTypes.contains(snowball.getType()))) return;
        final Entity hitEntity = event.getHitEntity();
        if (hitEntity == null) return;

        if (isFolia) {
            scheduler.runAtEntity(hitEntity, knockback -> hitEntity.setVelocity(modifyVector ? snowball.getVelocity().multiply(multiplier).add(vectorModifier) : snowball.getVelocity().multiply(multiplier)));
        } else {
            hitEntity.setVelocity(modifyVector ? snowball.getVelocity().multiply(multiplier).add(vectorModifier) : snowball.getVelocity().multiply(multiplier));
        }
    }
}