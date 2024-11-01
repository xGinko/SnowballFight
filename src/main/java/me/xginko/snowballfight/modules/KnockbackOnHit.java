package me.xginko.snowballfight.modules;

import com.cryptomorin.xseries.XEntityType;
import me.xginko.snowballfight.SnowballConfig;
import me.xginko.snowballfight.SnowballFight;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.util.Vector;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class KnockbackOnHit implements SnowballModule, Listener {

    private final Set<EntityType> configuredTypes;
    private final Vector vectorModifier;
    private final double multiplier;
    private final boolean modifyVector, onlyForSpecificEntities, asBlacklist, onlyPlayers;

    protected KnockbackOnHit() {
        shouldEnable();
        SnowballConfig config = SnowballFight.config();
        config.master().addComment("settings.knockback", "Modify knockback values on snowball hit.");
        this.onlyPlayers = config.getBoolean("settings.knockback.only-thrown-by-player", true,
                "If enabled will only work if the snowball was thrown by a player.");
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
        this.configuredTypes = config.getList("settings.knockback.specific-entity-types", Collections.singletonList("PLAYER"),
                "Please use correct enums from: https://jd.papermc.io/paper/1.20/org/bukkit/entity/EntityType.html")
                .stream()
                .map(configuredType -> {
                    try {
                        return EntityType.valueOf(configuredType);
                    } catch (IllegalArgumentException e) {
                        SnowballFight.logger().warn("(Knockback) Configured entity type '{}' not recognized. " +
                                "Please use correct values from: https://jd.papermc.io/paper/1.20/org/bukkit/entity/EntityType.html", configuredType);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(EntityType.class)));
    }

    @Override
    public boolean shouldEnable() {
        return SnowballFight.config().getBoolean("settings.knockback.enable", true);
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
        if (event.getEntityType() != XEntityType.SNOWBALL.get()) return;
        final Entity hitEntity = event.getHitEntity();
        if (hitEntity == null) return;
        if (onlyForSpecificEntities && (asBlacklist == configuredTypes.contains(hitEntity.getType()))) return;

        final Projectile snowball = event.getEntity();
        if (onlyPlayers && !(snowball.getShooter() instanceof Player)) return;

        if (SnowballFight.isServerFolia()) {
            SnowballFight.getScheduler().entitySpecificScheduler(hitEntity).run(() -> {
                if (modifyVector) hitEntity.setVelocity(snowball.getVelocity().multiply(multiplier).add(vectorModifier));
                else hitEntity.setVelocity(snowball.getVelocity().multiply(multiplier));
            }, null);
        } else {
            if (modifyVector) hitEntity.setVelocity(snowball.getVelocity().multiply(multiplier).add(vectorModifier));
            else hitEntity.setVelocity(snowball.getVelocity().multiply(multiplier));
        }
    }
}