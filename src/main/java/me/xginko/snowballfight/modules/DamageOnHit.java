package me.xginko.snowballfight.modules;

import com.cryptomorin.xseries.XEntityType;
import me.xginko.snowballfight.SnowballFight;
import me.xginko.snowballfight.utils.Util;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class DamageOnHit extends SnowballModule implements Listener {

    private final Set<EntityType> configuredTypes;
    private final double damage;
    private final boolean onlyForSpecificEntities, asBlacklist, onlyPlayers;

    public DamageOnHit() {
        super("settings.damage", false,
                "\nEnable snowballs dealing damage when they hit an entity.");
        this.damage = config.getDouble(configPath + ".damage", 3.0,
                "Configure the damage that entities take from getting hit by a snowball.");
        this.onlyPlayers = config.getBoolean(configPath + ".only-thrown-by-player", true,
                "If enabled will only work if the snowball was thrown by a player.");
        this.onlyForSpecificEntities = config.getBoolean(configPath + ".only-for-specific-entities", false,
                "When enabled, only configured entities will take extra damage when hit by a snowball.");
        this.asBlacklist = config.getBoolean(configPath + ".use-list-as-blacklist", false,
                "All entities except the ones on this list will take damage when hit by a snowball if set to true.");
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

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onProjectileHit(ProjectileHitEvent event) {
        if (event.getEntityType() != XEntityType.SNOWBALL.get()) return;
        if (!Util.isLivingEntity(event.getHitEntity())) return;
        if (onlyPlayers && !(event.getEntity().getShooter() instanceof Player)) return;

        final LivingEntity livingEntity = (LivingEntity) event.getHitEntity();
        if (onlyForSpecificEntities && (asBlacklist == configuredTypes.contains(livingEntity.getType()))) return;

        if (SnowballFight.isServerFolia()) {
            SnowballFight.scheduling().entitySpecificScheduler(livingEntity)
                    .run(() -> livingEntity.damage(damage, event.getEntity()), null);
        } else {
            livingEntity.damage(damage, event.getEntity());
        }
    }
}