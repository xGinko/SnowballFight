package me.xginko.snowballfight.modules;

import com.cryptomorin.xseries.XEntityType;
import com.cryptomorin.xseries.XMaterial;
import me.xginko.snowballfight.SnowballFight;
import me.xginko.snowballfight.utils.Util;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
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

public class SnowOnHit extends SnowballModule implements Listener {
    private static final boolean HAS_SNOW_DATA = Util.hasClass("org.bukkit.block.data.type.Snow");

    private final Set<EntityType> configuredTypes;
    private final double snowPatchRadiusSquared;
    private final int snowPatchRadius;
    private final boolean formIce, addSnowLayer, replaceFullLayer, onlyForEntities, onlyForSpecificEntities, asBlacklist, onlyPlayers;
    private boolean powderSnowEnabled;

    protected SnowOnHit() {
        super("settings.snow", true,
                "\nCovers the hit block in snow.");
        this.onlyPlayers = config.getBoolean(configPath + ".only-thrown-by-player", true,
                "If enabled will only work if the snowball was thrown by a player.");
        this.snowPatchRadius = config.getInt(configPath + ".size", 2,
                "How big the snow patch should be that the snowball leaves as block radius.");
        this.snowPatchRadiusSquared = snowPatchRadius * snowPatchRadius;
        this.formIce = config.getBoolean(configPath + ".form-ice", true,
                "Turns water to ice when hit.");
        this.addSnowLayer = config.getBoolean(configPath + ".stack-snow-layer.enable", true,
                "Adds snow on top of existing snow layers.");
        this.replaceFullLayer = config.getBoolean(configPath + ".stack-snow-layer.full-layers.turn-to-blocks", false,
                "Recommended to leave off if you want the snow layers to be able to melt away.");
        this.powderSnowEnabled = config.getBoolean(configPath + ".stack-snow-layer.full-layers.use-powder-snow", XMaterial.POWDER_SNOW.isSupported(),
                "Of course only works if your minecraft version has powder snow.");
        if (powderSnowEnabled && !XMaterial.POWDER_SNOW.isSupported()) {
            powderSnowEnabled = false;
            warn("Your server version does not support powder snow. Using regular snow.");
            config.master().set(configPath + ".stack-snow-layer.full-layers.use-powder-snow", false);
        }
        this.onlyForEntities = config.getBoolean(configPath + ".only-for-entities", false,
                "Enable if you only want snow to spread when snowballs hit an entity.");
        this.onlyForSpecificEntities = config.getBoolean(configPath + ".only-for-specific-entities", false,
                "When enabled, snowballs will only spread snow for the configured entity types below.\n" +
                "Needs only-for-entities to be set to true.");
        this.asBlacklist = config.getBoolean(configPath + ".use-list-as-blacklist", false,
                "Setting this and only-for-specific-entities to true will mean there won't be snow spreading\n" +
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

        if (event.getHitBlock() != null) {
            coverWithSnowAt(event.getHitBlock());
            return;
        }

        if (event.getHitEntity() != null) {
            coverWithSnowAt(event.getHitEntity().getLocation().getBlock());
        }
    }

    private void coverWithSnowAt(Block startBlock) {
        final Location hitLoc = Util.toCenterLocation(startBlock.getLocation());

        final int chunkX = hitLoc.getBlockX() >> 4;
        final int chunkZ = hitLoc.getBlockZ() >> 4;

        if (Util.isChunkUnsafe(chunkX, chunkZ)) { // Ignore if chunk way out of bounds
            return;
        }

        SnowballFight.scheduling().regionSpecificScheduler(startBlock.getWorld(), chunkX, chunkZ).runDelayed(() -> {
            World world = hitLoc.getWorld();
            int centerX = hitLoc.getBlockX();
            int centerY = hitLoc.getBlockY();
            int centerZ = hitLoc.getBlockZ();

            for (int x = centerX - snowPatchRadius; x <= centerX + snowPatchRadius; x++) {
                for (int z = centerZ - snowPatchRadius; z <= centerZ + snowPatchRadius; z++) {
                    for (int y = Math.max(Util.getMinWorldHeight(world), centerY - snowPatchRadius); y <= centerY + snowPatchRadius; y++) {
                        if (y > world.getMaxHeight()) break;

                        Block iterativeBlock = world.getBlockAt(x, y, z);

                        // Gives us that round shape
                        if (iterativeBlock.getLocation().distanceSquared(hitLoc) >= snowPatchRadiusSquared) continue;

                        Material iterativeType = iterativeBlock.getType();

                        if (iterativeType == XMaterial.AIR.parseMaterial() || iterativeType == XMaterial.CAVE_AIR.parseMaterial()) {
                            if (iterativeBlock.getRelative(BlockFace.DOWN).getType().isSolid()) {
                                iterativeBlock.setType(XMaterial.SNOW.parseMaterial(), true);
                            }
                            continue;
                        }

                        if (addSnowLayer && HAS_SNOW_DATA && iterativeType == XMaterial.SNOW.parseMaterial()) {
                            org.bukkit.block.data.type.Snow snowData = (org.bukkit.block.data.type.Snow) iterativeBlock.getBlockData();
                            final int layers = snowData.getLayers();
                            if (replaceFullLayer) {
                                if (layers < snowData.getMaximumLayers() - 1) {
                                    snowData.setLayers(layers + 1);
                                    iterativeBlock.setBlockData(snowData);
                                } else {
                                    // If only one or no more layers left to add, turn into snow block.
                                    iterativeBlock.setType(powderSnowEnabled ? XMaterial.POWDER_SNOW.parseMaterial() : XMaterial.SNOW_BLOCK.parseMaterial(), true);
                                }
                            } else {
                                if (layers < snowData.getMaximumLayers()) {
                                    snowData.setLayers(layers + 1);
                                    iterativeBlock.setBlockData(snowData);
                                }
                            }
                            continue;
                        }

                        if (formIce && iterativeType == XMaterial.WATER.parseMaterial()) {
                            iterativeBlock.setType(XMaterial.ICE.parseMaterial(), true);
                        }
                    }
                }
            }
        }, 1L);
    }
}