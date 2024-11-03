package me.xginko.snowballfight.modules;

import com.cryptomorin.xseries.XEntityType;
import me.xginko.snowballfight.SnowballFight;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
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

public class LightningOnHit extends SnowballModule implements Listener {

    private final Set<EntityType> configuredTypes;
    private final double probability;
    private final int strikeAmount, flashCount;
    private final boolean dealDamage, onlyForEntities, onlyForSpecificEntities, asBlacklist, onlyPlayers;

    protected LightningOnHit() {
        super("settings.lightning", false,
                "\nStrike a lightning when a snowball hits something.");
        this.onlyPlayers = config.getBoolean(configPath + ".only-thrown-by-player", true,
                "If enabled will only work if the snowball was thrown by a player.");
        this.dealDamage = config.getBoolean(configPath + ".deal-damage", true,
                "Whether the lightning strike should deal damage.");
        this.strikeAmount = config.getInt(configPath + ".strike-count", 2,
                "Amount of times to strike on hit.");
        this.flashCount = config.getInt(configPath + ".flash-count", 2,
                "Amount of times to flash after strike.");
        this.probability = config.getDouble(configPath + ".chance", 0.1,
                "Percentage as double: 100% = 1.0");
        this.onlyForEntities = config.getBoolean(configPath + ".only-for-entities", false,
                "Enable if you only want explosions to happen when snowballs hit an entity.");
        this.onlyForSpecificEntities = config.getBoolean(configPath + ".only-for-specific-entities", false,
                "When enabled, only entities in this list will be struck by lightning when hit by a snowball.\n" +
                "Needs only-for-entities to be set to true.");
        this.asBlacklist = config.getBoolean(configPath + ".use-list-as-blacklist", false,
                "All entities except the ones on this list will get struck by lightning if set to true.");
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
        if (probability < 1.0 && SnowballFight.getRandom().nextDouble() > probability) return;

        final Entity hitEntity = event.getHitEntity();
        if (onlyForEntities) {
            if (hitEntity == null) return;
            if (onlyForSpecificEntities && (asBlacklist == configuredTypes.contains(hitEntity.getType()))) return;
        }

        if (onlyPlayers && !(event.getEntity().getShooter() instanceof Player)) return;

        if (hitEntity != null) {
            if (SnowballFight.isServerFolia()) SnowballFight.scheduling().entitySpecificScheduler(hitEntity)
                    .run(() -> strikeLightning(hitEntity.getLocation()), null);
            else strikeLightning(hitEntity.getLocation());
            return;
        }

        final Block hitBlock = event.getHitBlock();
        if (hitBlock != null) {
            final Location hitBlockLoc = hitBlock.getLocation();
            if (SnowballFight.isServerFolia()) SnowballFight.scheduling().regionSpecificScheduler(hitBlockLoc)
                    .run(() -> strikeLightning(hitBlockLoc));
            else strikeLightning(hitBlockLoc);
        }
    }

    private void strikeLightning(final Location strikeLoc) {
        for (int i = 0; i < strikeAmount; i++) {
            (dealDamage ? strikeLoc.getWorld().strikeLightning(strikeLoc) : strikeLoc.getWorld().strikeLightningEffect(strikeLoc))
                    .setFlashCount(flashCount);
        }
    }
}