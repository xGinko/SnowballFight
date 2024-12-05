package me.xginko.snowballfight.modules;

import com.cryptomorin.xseries.XEntityType;
import me.xginko.snowballfight.SnowballFight;
import me.xginko.snowballfight.events.PostSnowballExplodeEvent;
import me.xginko.snowballfight.events.PreSnowballExplodeEvent;
import me.xginko.snowballfight.utils.Util;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class ExplodeOnHit extends SnowballModule implements Listener {

    private final Set<EntityType> configuredTypes;
    private final float explosionPower;
    private final boolean explosionSetFire, explosionBreakBlocks, onlyForEntities, onlyForSpecificEntities, asBlacklist,
            onlyPlayers;

    public ExplodeOnHit() {
        super("settings.explosions", true,
                "\nMake snowballs explode when hitting something.");
        this.onlyPlayers = config.getBoolean(configPath + ".only-thrown-by-player", true,
                "If enabled will only work if the snowball was thrown by a player.");
        this.explosionPower = config.getFloat(configPath + ".power", 2.0F,
                "TNT has a power of 4.0.");
        this.explosionSetFire = config.getBoolean(configPath + ".set-fire", false,
                "Enable explosion fire like with respawn anchors.");
        this.explosionBreakBlocks = config.getBoolean(configPath + ".break-blocks", true,
                "Enable destruction of nearby blocks.");
        this.onlyForEntities = config.getBoolean(configPath + ".only-for-entities", false,
                "Enable if you only want explosions to happen when snowballs hit an entity.");
        this.onlyForSpecificEntities = config.getBoolean(configPath + ".only-for-specific-entities", false,
                "When enabled, snowballs will only explode for the configured entity types below.\n" +
                "Needs only-for-entities to be set to true.");
        this.asBlacklist = config.getBoolean(configPath + ".use-list-as-blacklist", false,
                "Setting this and only-for-specific-entities to true will mean there won't be an explosion \n" +
                "when one of the configured entities are hit by a snowball.");
        this.configuredTypes = config.getList(configPath + ".specific-entity-types", Arrays.asList("PLAYER", "WITHER"),
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

        if (onlyForEntities) {
            if (event.getHitEntity() == null) return;
            if (onlyForSpecificEntities && (asBlacklist == configuredTypes.contains(event.getHitEntity().getType()))) return;
        }

        if (onlyPlayers && !(event.getEntity().getShooter() instanceof Player)) return;

        PreSnowballExplodeEvent preSnowballExplodeEvent = new PreSnowballExplodeEvent(
                (Snowball) event.getEntity(),
                event.getHitEntity(),
                event.getEntity().getLocation(),
                explosionPower,
                explosionSetFire,
                explosionBreakBlocks,
                event.isAsynchronous()
        );

        if (Util.isChunkUnsafe(
                preSnowballExplodeEvent.getExplodeLocation().getBlockX() >> 4,
                preSnowballExplodeEvent.getExplodeLocation().getBlockZ() >> 4)) {
            preSnowballExplodeEvent.setCancelled(true);
        }

        plugin.getServer().getPluginManager().callEvent(preSnowballExplodeEvent);

        if (preSnowballExplodeEvent.isCancelled()) {
            return;
        }

        PostSnowballExplodeEvent postSnowballExplodeEvent = new PostSnowballExplodeEvent(
                preSnowballExplodeEvent.getSnowball(),
                preSnowballExplodeEvent.getHitEntity(),
                preSnowballExplodeEvent.getExplodeLocation(),
                preSnowballExplodeEvent.getExplosionPower(),
                preSnowballExplodeEvent.willSetFire(),
                preSnowballExplodeEvent.willBreakBlocks(),
                createExplosion(preSnowballExplodeEvent),
                event.isAsynchronous()
        );

        plugin.getServer().getPluginManager().callEvent(postSnowballExplodeEvent);
    }

    private boolean createExplosion(PreSnowballExplodeEvent preSnowballExplodeEvent) {
        if (SnowballFight.isServerPaper()) {
            return preSnowballExplodeEvent.getExplodeLocation().getWorld().createExplosion(
                    // Set explode source for damage tracking without getting blocked by the MOB_GRIEFING gamerule
                    preSnowballExplodeEvent.getSnowball().getShooter() instanceof LivingEntity ?
                            (LivingEntity) preSnowballExplodeEvent.getSnowball().getShooter() : preSnowballExplodeEvent.getSnowball(),
                    preSnowballExplodeEvent.getExplodeLocation(),
                    preSnowballExplodeEvent.getExplosionPower(),
                    preSnowballExplodeEvent.willSetFire(),
                    preSnowballExplodeEvent.willBreakBlocks()
            );
        } else {
            return preSnowballExplodeEvent.getExplodeLocation().getWorld().createExplosion(
                    preSnowballExplodeEvent.getExplodeLocation(),
                    preSnowballExplodeEvent.getExplosionPower(),
                    preSnowballExplodeEvent.willSetFire(),
                    preSnowballExplodeEvent.willBreakBlocks()
            );
        }
    }
}