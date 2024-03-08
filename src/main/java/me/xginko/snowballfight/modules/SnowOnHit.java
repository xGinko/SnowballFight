package me.xginko.snowballfight.modules;

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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.stream.Collectors;

public class SnowOnHit implements SnowballModule, Listener {

    private final ServerImplementation scheduler;
    private final HashSet<EntityType> configuredTypes;
    private final Material powderedSnow;
    private final int snowPatchRadius;
    private final boolean formIce, addSnowLayer, replaceFullLayer, onlyForEntities, onlyForSpecificEntities, asBlacklist;
    private boolean powderSnowEnabled;

    protected SnowOnHit() {
        shouldEnable();
        this.scheduler = SnowballFight.getFoliaLib().getImpl();
        SnowballConfig config = SnowballFight.getConfiguration();
        config.master().addComment("settings.snow",
                "\nCovers the hit block in snow.");
        this.snowPatchRadius = config.getInt("settings.snow.size", 2,
                "How big the snow patch should be that the snowball leaves as block radius.");
        this.formIce = config.getBoolean("settings.snow.form-ice", true,
                "Turns water to ice when hit.");
        this.addSnowLayer = config.getBoolean("settings.snow.stack-snow-layer.enable", true,
                "Adds snow on top of existing snow layers.");
        this.replaceFullLayer = config.getBoolean("settings.snow.stack-snow-layer.full-layers.turn-to-blocks", false,
                "Recommended to leave off if you want the snow layers to be able to melt away.");
        this.powderedSnow = Material.matchMaterial("POWDER_SNOW");
        this.powderSnowEnabled = config.getBoolean("settings.snow.stack-snow-layer.full-layers.use-powder-snow", powderedSnow != null,
                "Of course only works if your minecraft version has powder snow.");
        if (powderSnowEnabled && powderedSnow == null) {
            powderSnowEnabled = false;
            SnowballFight.getLog().warn("(Snow) Your server version does not support powder snow. Using regular snow.");
            config.master().set("settings.snow.stack-snow-layer.full-layers.use-powder-snow", false);
        }
        this.onlyForEntities = config.getBoolean("settings.snow.only-for-entities", false,
                "Enable if you only want snow to spread when snowballs hit an entity.");
        this.onlyForSpecificEntities = config.getBoolean("settings.snow.only-for-specific-entities", false, 
                "When enabled, snowballs will only spread snow for the configured entity types below.\n" +
                "Needs only-for-entities to be set to true.");
        this.asBlacklist = config.getBoolean("settings.snow.use-list-as-blacklist", false, 
                "Setting this and only-for-specific-entities to true will mean there won't be snow spreading\n" +
                "when one of the configured entities are hit by a snowball.");
        this.configuredTypes = config.getList("settings.snow.specific-entity-types", Arrays.asList("PLAYER", "WITHER"),
                "Please use correct enums from: https://jd.papermc.io/paper/1.20/org/bukkit/entity/EntityType.html")
                .stream()
                .map(configuredType -> {
                    try {
                        return EntityType.valueOf(configuredType);
                    } catch (IllegalArgumentException e) {
                        SnowballFight.getLog().warn("(Snow) Configured entity type '"+configuredType+"' not recognized. " +
                                "Please use correct values from: https://jd.papermc.io/paper/1.20/org/bukkit/entity/EntityType.html");
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(HashSet::new));
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
            coverWithSnowAt(hitBlock);
            return;
        }

        if (hitEntity != null) {
            coverWithSnowAt(hitEntity.getLocation().getBlock());
        }
    }

    private void coverWithSnowAt(Block startBlock) {
        final Location hitLoc = startBlock.getLocation().toCenterLocation();
        scheduler.runAtLocationLater(hitLoc, snowDown -> {
            for (int x = -snowPatchRadius; x <= snowPatchRadius; x++) {
                for (int z = -snowPatchRadius; z <= snowPatchRadius; z++) {
                    for (int y = -snowPatchRadius; y <= snowPatchRadius; y++) {
                        Block iterativeBlock = startBlock.getRelative(x, y, z);
                        // Gives us that nice round shape
                        if (iterativeBlock.getLocation().distance(hitLoc) >= snowPatchRadius) continue;

                        Material iterativeType = iterativeBlock.getType();

                        if (iterativeType.isAir()) {
                            if (iterativeBlock.getRelative(BlockFace.DOWN).getType().isSolid()) {
                                iterativeBlock.setType(Material.SNOW, true);
                            }
                            continue;
                        }

                        if (addSnowLayer && iterativeType.equals(Material.SNOW)) {
                            Snow snow = (Snow) iterativeBlock.getBlockData();
                            final int layers = snow.getLayers();
                            if (replaceFullLayer) {
                                if (layers < snow.getMaximumLayers() - 1) {
                                    snow.setLayers(layers + 1);
                                    iterativeBlock.setBlockData(snow);
                                } else {
                                    // If only one or no more layers left to add, turn into snow block.
                                    iterativeBlock.setType(powderSnowEnabled ? powderedSnow : Material.SNOW_BLOCK, true);
                                }
                            } else {
                                if (layers < snow.getMaximumLayers()) {
                                    snow.setLayers(layers + 1);
                                    iterativeBlock.setBlockData(snow);
                                }
                            }
                            continue;
                        }

                        if (formIce && iterativeType.equals(Material.WATER)) {
                            iterativeBlock.setType(Material.ICE, true);
                        }
                    }
                }
            }
        }, 1L);
    }
}