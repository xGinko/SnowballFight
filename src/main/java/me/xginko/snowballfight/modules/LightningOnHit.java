package me.xginko.snowballfight.modules;

import me.xginko.snowballfight.SnowballConfig;
import me.xginko.snowballfight.SnowballFight;
import org.bukkit.Location;
import org.bukkit.World;
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

public class LightningOnHit implements SnowballModule, Listener {

    private final Set<EntityType> configuredTypes;
    private final double probability;
    private final int strikeAmount, flashCount;
    private final boolean dealDamage, onlyForEntities, onlyForSpecificEntities, asBlacklist, onlyPlayers;

    protected LightningOnHit() {
        shouldEnable();
        SnowballConfig config = SnowballFight.config();
        config.master().addComment("settings.lightning",
                "\nStrike a lightning when a snowball hits something.");
        this.onlyPlayers = config.getBoolean("settings.lightning.only-thrown-by-player", true,
                "If enabled will only work if the snowball was thrown by a player.");
        this.dealDamage = config.getBoolean("settings.lightning.deal-damage", true,
                "Whether the lightning strike should deal damage.");
        this.strikeAmount = config.getInt("settings.lightning.strike-count", 2,
                "Amount of times to strike on hit.");
        this.flashCount = config.getInt("settings.lightning.flash-count", 2,
                "Amount of times to flash after strike.");
        this.probability = config.getDouble("settings.lightning.chance", 0.1,
                "Percentage as double: 100% = 1.0");
        this.onlyForEntities = config.getBoolean("settings.lightning.only-for-entities", false,
                "Enable if you only want explosions to happen when snowballs hit an entity.");
        this.onlyForSpecificEntities = config.getBoolean("settings.lightning.only-for-specific-entities", false,
                "When enabled, only entities in this list will be struck by lightning when hit by a snowball.\n" +
                "Needs only-for-entities to be set to true.");
        this.asBlacklist = config.getBoolean("settings.lightning.use-list-as-blacklist", false,
                "All entities except the ones on this list will get struck by lightning if set to true.");
        this.configuredTypes = config.getList("settings.lightning.specific-entity-types", Collections.singletonList("PLAYER"),
                "Please use correct enums from: https://jd.papermc.io/paper/1.20/org/bukkit/entity/EntityType.html")
                .stream()
                .map(configuredType -> {
                    try {
                        return EntityType.valueOf(configuredType);
                    } catch (IllegalArgumentException e) {
                        SnowballFight.logger().warn("(Lightning) Configured entity type '{}' not recognized. " +
                                "Please use correct values from: https://jd.papermc.io/paper/1.20/org/bukkit/entity/EntityType.html", configuredType);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(EntityType.class)));
    }

    @Override
    public boolean shouldEnable() {
        return SnowballFight.config().getBoolean("settings.lightning.enable", false);
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

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onSnowballHit(ProjectileHitEvent event) {
        if (!event.getEntityType().equals(EntityType.SNOWBALL)) return;
        if (probability < 1.0 && SnowballFight.getRandom().nextDouble() > probability) return;

        final Entity hitEntity = event.getHitEntity();
        if (onlyForEntities) {
            if (hitEntity == null) return;
            if (onlyForSpecificEntities && (asBlacklist == configuredTypes.contains(hitEntity.getType()))) return;
        }

        if (onlyPlayers && !(event.getEntity().getShooter() instanceof Player)) return;

        if (hitEntity != null) {
            if (SnowballFight.isServerFolia()) SnowballFight.getScheduler().runAtEntity(hitEntity, strike -> strikeLightning(hitEntity.getLocation()));
            else strikeLightning(hitEntity.getLocation());
            return;
        }

        final Block hitBlock = event.getHitBlock();
        if (hitBlock != null) {
            final Location hitBlockLoc = hitBlock.getLocation();
            if (SnowballFight.isServerFolia()) SnowballFight.getScheduler().runAtLocation(hitBlockLoc, strike -> strikeLightning(hitBlockLoc));
            else strikeLightning(hitBlockLoc);
        }
    }

    private void strikeLightning(final Location strikeLoc) {
        final World world = strikeLoc.getWorld();
        for (int i = 0; i < strikeAmount; i++) {
            (dealDamage ? world.strikeLightning(strikeLoc) : world.strikeLightningEffect(strikeLoc)).setFlashCount(flashCount);
        }
    }
}