package me.xginko.snowballfight.modules;

import com.cryptomorin.xseries.XEntityType;
import com.cryptomorin.xseries.XMaterial;
import me.xginko.snowballfight.SnowballConfig;
import me.xginko.snowballfight.SnowballFight;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Snow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
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

public class SnowOnHit implements SnowballModule, Listener {

    private final Set<EntityType> configuredTypes;
    private final int snowPatchRadius;
    private final boolean formIce, addSnowLayer, replaceFullLayer, onlyForEntities, onlyForSpecificEntities, asBlacklist;
    private boolean powderSnowEnabled, onlyPlayers;

    protected SnowOnHit() {
        shouldEnable();
        SnowballConfig config = SnowballFight.config();
        config.master().addComment("settings.snow",
                "\nCovers the hit block in snow.");
        this.onlyPlayers = config.getBoolean("settings.snow.only-thrown-by-player", true,
                "If enabled will only work if the snowball was thrown by a player.");
        this.snowPatchRadius = config.getInt("settings.snow.size", 2,
                "How big the snow patch should be that the snowball leaves as block radius.");
        this.formIce = config.getBoolean("settings.snow.form-ice", true,
                "Turns water to ice when hit.");
        this.addSnowLayer = config.getBoolean("settings.snow.stack-snow-layer.enable", true,
                "Adds snow on top of existing snow layers.");
        this.replaceFullLayer = config.getBoolean("settings.snow.stack-snow-layer.full-layers.turn-to-blocks", false,
                "Recommended to leave off if you want the snow layers to be able to melt away.");
        this.powderSnowEnabled = config.getBoolean("settings.snow.stack-snow-layer.full-layers.use-powder-snow", XMaterial.POWDER_SNOW.isSupported(),
                "Of course only works if your minecraft version has powder snow.");
        if (powderSnowEnabled && !XMaterial.POWDER_SNOW.isSupported()) {
            powderSnowEnabled = false;
            SnowballFight.logger().warn("(Snow) Your server version does not support powder snow. Using regular snow.");
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
                        SnowballFight.logger().warn("(Snow) Configured entity type '{}' not recognized. " +
                                "Please use correct values from: https://jd.papermc.io/paper/1.20/org/bukkit/entity/EntityType.html", configuredType);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(EntityType.class)));
    }

    @Override
    public boolean shouldEnable() {
        return SnowballFight.config().getBoolean("settings.snow.enable", true);
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
        if (event.getEntityType() != XEntityType.SNOWBALL.get()) return;

        final Entity hitEntity = event.getHitEntity();
        if (onlyForEntities) {
            if (hitEntity == null) return;
            if (onlyForSpecificEntities && (asBlacklist == configuredTypes.contains(hitEntity.getType()))) return;
        }

        if (onlyPlayers && !(event.getEntity().getShooter() instanceof Player)) return;

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
        SnowballFight.getScheduler().regionSpecificScheduler(hitLoc).runDelayed(() -> {
            World world = hitLoc.getWorld();
            int centerX = hitLoc.getBlockX();
            int centerY = hitLoc.getBlockY();
            int centerZ = hitLoc.getBlockZ();

            for (int x = centerX - snowPatchRadius; x <= centerX + snowPatchRadius; x++) {
                for (int z = centerZ - snowPatchRadius; z <= centerZ + snowPatchRadius; z++) {
                    for (int y = Math.max(world.getMinHeight(), centerY - snowPatchRadius); y <= centerY + snowPatchRadius; y++) {
                        if (y > world.getMaxHeight()) break;

                        Block iterativeBlock = world.getBlockAt(x, y, z);

                        // Gives us that nice round shape
                        if (iterativeBlock.getLocation().distance(hitLoc) >= snowPatchRadius) continue;

                        Material iterativeType = iterativeBlock.getType();

                        if (iterativeType.isAir()) {
                            if (iterativeBlock.getRelative(BlockFace.DOWN).isSolid()) {
                                iterativeBlock.setType(XMaterial.SNOW.parseMaterial(), true);
                            }
                            continue;
                        }

                        if (addSnowLayer && iterativeType == XMaterial.SNOW.parseMaterial()) {
                            Snow snow = (Snow) iterativeBlock.getBlockData();
                            final int layers = snow.getLayers();
                            if (replaceFullLayer) {
                                if (layers < snow.getMaximumLayers() - 1) {
                                    snow.setLayers(layers + 1);
                                    iterativeBlock.setBlockData(snow);
                                } else {
                                    // If only one or no more layers left to add, turn into snow block.
                                    iterativeBlock.setType(powderSnowEnabled ? XMaterial.POWDER_SNOW.parseMaterial() : XMaterial.SNOW_BLOCK.parseMaterial(), true);
                                }
                            } else {
                                if (layers < snow.getMaximumLayers()) {
                                    snow.setLayers(layers + 1);
                                    iterativeBlock.setBlockData(snow);
                                }
                            }
                            continue;
                        }

                        if (formIce && iterativeType.equals(XMaterial.WATER.parseMaterial())) {
                            iterativeBlock.setType(XMaterial.ICE.parseMaterial(), true);
                        }
                    }
                }
            }
        }, 1L);
    }
}