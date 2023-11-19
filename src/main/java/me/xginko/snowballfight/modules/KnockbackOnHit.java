package me.xginko.snowballfight.modules;

import com.destroystokyo.paper.event.entity.EntityKnockbackByEntityEvent;
import me.xginko.snowballfight.SnowballConfig;
import me.xginko.snowballfight.SnowballFight;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.List;

public class KnockbackOnHit implements SnowballModule, Listener {

    private final HashSet<EntityType> configuredTypes = new HashSet<>();
    private final Vector accVectorModifier;
    private final boolean onlyForSpecificEntities, asBlacklist;

    protected KnockbackOnHit() {
        shouldEnable();
        SnowballConfig config = SnowballFight.getConfiguration();
        config.master().addComment("settings.knockback", "Modify knockback values on snowball hit.");
        this.accVectorModifier = new Vector(
                config.getDouble("settings.knockback.vector-modifier.x", 0.0),
                config.getDouble("settings.knockback.vector-modifier.y", 3.0),
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
    private void onKnockbackBySnowball(EntityKnockbackByEntityEvent event) {
        if (!event.getHitBy().getType().equals(EntityType.SNOWBALL)) return;
        if (onlyForSpecificEntities && (asBlacklist == configuredTypes.contains(event.getEntity().getType()))) return;

        event.getAcceleration().add(accVectorModifier);
    }
}