package me.xginko.snowballfight.modules;

import com.tcoded.folialib.FoliaLib;
import com.tcoded.folialib.impl.ServerImplementation;
import me.xginko.snowballfight.SnowballConfig;
import me.xginko.snowballfight.SnowballFight;
import me.xginko.snowballfight.events.SnowballHitEvent;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import java.util.HashSet;
import java.util.List;
import java.util.Random;

public class LightningOnHit implements SnowballModule, Listener {

    private final ServerImplementation scheduler;
    private final HashSet<EntityType> configuredTypes = new HashSet<>();
    private final double probability;
    private final int strikeAmount, flashCount;
    private final boolean isFolia, dealDamage, onlyForEntities, onlyForSpecificEntities, asBlacklist;

    protected LightningOnHit() {
        shouldEnable();
        FoliaLib foliaLib = SnowballFight.getFoliaLib();
        this.isFolia = foliaLib.isFolia();
        this.scheduler = isFolia ? foliaLib.getImpl() : null;
        SnowballConfig config = SnowballFight.getConfiguration();
        config.master().addComment("settings.lightning",
                "\nStrike a lightning when a snowball hits something.");
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
        this.onlyForSpecificEntities = config.getBoolean("settings.lightning.only-for-specific-entities", false, """
                When enabled, only entities in this list will be struck by lightning when hit by a snowball.\s
                Needs only-for-entities to be set to true.""");
        this.asBlacklist = config.getBoolean("settings.lightning.use-list-as-blacklist", false,
                "All entities except the ones on this list will get struck by lightning if set to true.");
        config.getList("settings.lightning.specific-entity-types",
                List.of(EntityType.PLAYER.name()),
                "Please use correct enums from: https://jd.papermc.io/paper/1.20/org/bukkit/entity/EntityType.html"
        ).forEach(configuredType -> {
            try {
                EntityType type = EntityType.valueOf(configuredType);
                this.configuredTypes.add(type);
            } catch (IllegalArgumentException e) {
                SnowballFight.getLog().warning("(Lightning) Configured entity type '"+configuredType+"' not recognized. " +
                        "Please use correct values from: https://jd.papermc.io/paper/1.20/org/bukkit/entity/EntityType.html");
            }
        });
    }

    @Override
    public boolean shouldEnable() {
        return SnowballFight.getConfiguration().getBoolean("settings.lightning.enable", false);
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

    @EventHandler(priority = EventPriority.LOW)
    private void onSnowballHit(SnowballHitEvent event) {
        if (probability < 1.0 && new Random().nextDouble() > probability) return;

        final Entity hitEntity = event.getHitEntity();
        if (onlyForEntities) {
            if (hitEntity == null) return;
            if (onlyForSpecificEntities && (asBlacklist == configuredTypes.contains(event.getHitEntity().getType()))) return;
        }

        if (hitEntity != null) {
            if (isFolia) scheduler.runAtEntity(hitEntity, strike -> strikeLightning(hitEntity.getLocation()));
            else strikeLightning(hitEntity.getLocation());
            return;
        }

        final Block hitBlock = event.getHitBlock();
        if (hitBlock != null) {
            final Location hitBlockLoc = hitBlock.getLocation();
            if (isFolia) scheduler.runAtLocation(hitBlockLoc, strike -> strikeLightning(hitBlockLoc));
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