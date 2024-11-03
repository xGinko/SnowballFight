package me.xginko.snowballfight.modules;

import com.cryptomorin.xseries.XEntityType;
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

public class KnockbackOnHit extends SnowballModule implements Listener {

    private final Set<EntityType> configuredTypes;
    private final Vector vectorModifier;
    private final double multiplier;
    private final boolean modifyVector, onlyForSpecificEntities, asBlacklist, onlyPlayers;

    protected KnockbackOnHit() {
        super("settings.knockback", true,
                "\nModify knockback values on snowball hit.");
        this.onlyPlayers = config.getBoolean(configPath + ".only-thrown-by-player", true,
                "If enabled will only work if the snowball was thrown by a player.");
        this.multiplier = config.getDouble(configPath + ".multiplier", 1.2,
                "The multiplier for the knockback of the snowball.");
        this.modifyVector = config.getBoolean(configPath + ".vector-modifier.enable", true);
        this.vectorModifier = new Vector(
                config.getDouble(configPath + ".vector-modifier.x", 0.0),
                config.getDouble(configPath + ".vector-modifier.y", 0.6),
                config.getDouble(configPath + ".vector-modifier.z", 0.0)
        );
        this.onlyForSpecificEntities = config.getBoolean(configPath + ".only-for-specific-entities", false,
                "When enabled, only configured entities will be knocked back by snowballs.");
        this.asBlacklist = config.getBoolean(configPath + ".use-list-as-blacklist", false,
                "All entities except the ones on this list will be knocked back by snowballs if set to true.");
        this.configuredTypes = config.getList(configPath + ".specific-entity-types", Collections.singletonList("PLAYER"),
                "Please use correct enums from: https://jd.papermc.io/paper/1.20/org/bukkit/entity/EntityType.html")
                .stream()
                .map(configuredType -> {
                    try {
                        return EntityType.valueOf(configuredType);
                    } catch (IllegalArgumentException e) {
                        warn("EntityType '" + configuredType + "' not recognized. " +
                                "Please use correct values from: https://jd.papermc.io/paper/1.20/org/bukkit/entity/EntityType.html");
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(EntityType.class)));
    }

    @Override
    public void enable() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void disable() {
        HandlerList.unregisterAll(this);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void onProjectileHit(ProjectileHitEvent event) {
        if (event.getEntityType() != XEntityType.SNOWBALL.get()) return;
        final Entity hitEntity = event.getHitEntity();
        if (hitEntity == null) return;
        if (onlyForSpecificEntities && (asBlacklist == configuredTypes.contains(hitEntity.getType()))) return;

        final Projectile snowball = event.getEntity();
        if (onlyPlayers && !(snowball.getShooter() instanceof Player)) return;

        if (SnowballFight.isServerFolia()) {
            SnowballFight.scheduling().entitySpecificScheduler(hitEntity).run(() -> {
                if (modifyVector) hitEntity.setVelocity(snowball.getVelocity().multiply(multiplier).add(vectorModifier));
                else hitEntity.setVelocity(snowball.getVelocity().multiply(multiplier));
            }, null);
        } else {
            if (modifyVector) hitEntity.setVelocity(snowball.getVelocity().multiply(multiplier).add(vectorModifier));
            else hitEntity.setVelocity(snowball.getVelocity().multiply(multiplier));
        }
    }
}