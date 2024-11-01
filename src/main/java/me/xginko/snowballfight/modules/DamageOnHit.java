package me.xginko.snowballfight.modules;

import com.cryptomorin.xseries.XEntityType;
import me.xginko.snowballfight.SnowballConfig;
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

public class DamageOnHit implements SnowballModule, Listener {

    private final Set<EntityType> configuredTypes;
    private final double damage;
    private final boolean onlyForSpecificEntities, asBlacklist, onlyPlayers;

    protected DamageOnHit() {
        shouldEnable();
        SnowballConfig config = SnowballFight.config();
        config.master().addComment("settings.damage", "\nEnable snowballs dealing damage when they hit an entity.");
        this.damage = config.getDouble("settings.damage.damage", 3.0,
                "Configure the damage that entities take from getting hit by a snowball.");
        this.onlyPlayers = config.getBoolean("settings.damage.only-thrown-by-player", true,
                "If enabled will only work if the snowball was thrown by a player.");
        this.onlyForSpecificEntities = config.getBoolean("settings.damage.only-for-specific-entities", false,
                "When enabled, only configured entities will take extra damage when hit by a snowball.");
        this.asBlacklist = config.getBoolean("settings.damage.use-list-as-blacklist", false,
                "All entities except the ones on this list will take damage when hit by a snowball if set to true.");
        this.configuredTypes = config.getList("settings.damage.specific-entity-types", Collections.singletonList("PLAYER"),
                "Please use correct enums from: https://jd.papermc.io/paper/1.20/org/bukkit/entity/EntityType.html")
                .stream()
                .map(configuredType -> {
                    try {
                        return EntityType.valueOf(configuredType);
                    } catch (IllegalArgumentException e) {
                        SnowballFight.logger().warn("(Damage) Configured entity type '{}' not recognized. " +
                                "Please use correct values from: https://jd.papermc.io/paper/1.20/org/bukkit/entity/EntityType.html", configuredType);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(EntityType.class)));
    }

    @Override
    public boolean shouldEnable() {
        return SnowballFight.config().getBoolean("settings.damage.enable", false);
    }

    @Override
    public void enable() {
        SnowballFight plugin = SnowballFight.getInstance();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void disable() {
        HandlerList.unregisterAll();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onSnowballHit(ProjectileHitEvent event) {
        if (event.getEntityType() != XEntityType.SNOWBALL.get()) return;
        if (!Util.isLivingEntity(event.getHitEntity())) return;
        if (onlyPlayers && !(event.getEntity().getShooter() instanceof Player)) return;

        final LivingEntity livingEntity = (LivingEntity) event.getHitEntity();
        if (onlyForSpecificEntities && (asBlacklist == configuredTypes.contains(livingEntity.getType()))) return;

        if (SnowballFight.isServerFolia()) {
            SnowballFight.getScheduler().entitySpecificScheduler(livingEntity)
                    .run(() -> livingEntity.damage(damage, event.getEntity()), null);
        } else {
            livingEntity.damage(damage, event.getEntity());
        }
    }
}