package me.xginko.snowballfight.modules;

import com.tcoded.folialib.FoliaLib;
import com.tcoded.folialib.impl.ServerImplementation;
import me.xginko.snowballfight.SnowballConfig;
import me.xginko.snowballfight.SnowballFight;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Snow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;

import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class SnowOnHit implements SnowballModule, Listener {

    private final ServerImplementation scheduler;
    private final HashSet<EntityType> configuredTypes = new HashSet<>();
    private final int radius;
    private final boolean isFolia, formIce, addSnowLayer, onlyForEntities, onlyForSpecificEntities, asBlacklist;

    protected SnowOnHit() {
        shouldEnable();
        FoliaLib foliaLib = SnowballFight.getFoliaLib();
        this.isFolia = foliaLib.isFolia();
        this.scheduler = isFolia ? foliaLib.getImpl() : null;
        SnowballConfig config = SnowballFight.getConfiguration();
        config.master().addComment("settings.snow",
                "\nCovers the hit block in snow.");
        this.addSnowLayer = config.getBoolean("settings.snow.add-to-existing-layer", true,
                "Adds snow to existing snow layers.");
        this.formIce = config.getBoolean("settings.snow.form-ice", true,
                "Turns water to ice when hit.");
        this.radius = config.getInt("settings.snow.size", 2,
                "How big the snow patch should be that the snowball leaves as block radius.");
        this.onlyForEntities = config.getBoolean("settings.snow.only-for-entities", false,
                "Enable if you only want explosions to happen when snowballs hit an entity.");
        this.onlyForSpecificEntities = config.getBoolean("settings.snow.only-for-specific-entities", false, """
                When enabled, snowballs will only explode for the configured entity types below.\s
                Needs only-for-entities to be set to true.""");
        this.asBlacklist = config.getBoolean("settings.snow.use-list-as-blacklist", false, """
                Setting this and only-for-specific-entities to true will mean there won't be an explosion\s
                when one of the configured entities are hit by a snowball.""");
        config.getList("settings.snow.specific-entity-types",
                List.of(EntityType.PLAYER.name(), EntityType.WITHER.name()),
                "Please use correct enums from: https://jd.papermc.io/paper/1.20/org/bukkit/entity/EntityType.html"
        ).forEach(configuredType -> {
            try {
                EntityType type = EntityType.valueOf(configuredType);
                this.configuredTypes.add(type);
            } catch (IllegalArgumentException e) {
                SnowballFight.getLog().warning("(Snow) Configured entity type '"+configuredType+"' not recognized. " +
                        "Please use correct values from: https://jd.papermc.io/paper/1.20/org/bukkit/entity/EntityType.html");
            }
        });
    }

    @Override
    public boolean shouldEnable() {
        return SnowballFight.getConfiguration().getBoolean("settings.snow.enable", true);
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

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    private void onSnowballHit(ProjectileHitEvent event) {
        if (!event.getEntityType().equals(EntityType.SNOWBALL)) return;

        final Entity hitEntity = event.getHitEntity();
        if (onlyForEntities) {
            if (hitEntity == null) return;
            if (onlyForSpecificEntities && (asBlacklist == configuredTypes.contains(hitEntity.getType()))) return;
        }

        final Block hitBlock = event.getHitBlock();
        if (hitBlock != null) {
            // Assume snow position is this block +=y
            snow(hitBlock.getRelative(BlockFace.UP));
            return;
        }

        if (hitEntity != null) {
            // Assume snow position is this.blockPos.+=y
            snow(hitEntity.getLocation().toBlockLocation().add(0,1,0).getBlock());
        }
    }

    private void snow(Block startBlock) {
        final Location startLoc = startBlock.getLocation();
        scheduler.runAtLocationLater(startLoc, snowDown -> {
            for (int x = -2-radius; x <= radius+2; x++) {
                for (int z = -2-radius; z <= radius+2; z++) {
                    for (int y = -2-radius; y < radius+2; y++) {
                        Block iterativeBlock = startBlock.getRelative(x, y, z);
                        // Gives us that nice circular shape
                        if (iterativeBlock.getLocation().distance(startLoc) > radius) continue;

                        Material iterativeType = iterativeBlock.getType();

                        if (iterativeType.isAir()) {
                            if (iterativeBlock.getRelative(BlockFace.DOWN).getType().isSolid()) {
                                iterativeBlock.setType(Material.SNOW);
                            }
                            continue;
                        }

                        if (formIce && iterativeType.equals(Material.WATER)) {
                            iterativeBlock.setType(Material.ICE);
                            continue;
                        }

                        if (addSnowLayer) {
                            if (iterativeType.equals(Material.SNOW)) {
                                Snow snow = (Snow) iterativeBlock.getBlockData();
                                final int layers = snow.getLayers();
                                if (layers < snow.getMaximumLayers()) {
                                    snow.setLayers(layers + 1);
                                    iterativeBlock.setBlockData(snow);
                                }
                                continue;
                            }

                            if (iterativeType.equals(Material.SNOW_BLOCK)) {
                                Block oneUp = iterativeBlock.getRelative(BlockFace.UP);
                                if (oneUp.getType().isAir()) {
                                    oneUp.setType(Material.SNOW);
                                }
                            }
                        }
                    }
                }
            }
        }, 50L, TimeUnit.MILLISECONDS);
    }
}