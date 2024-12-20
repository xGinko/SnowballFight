package me.xginko.snowballfight.modules;

import com.cryptomorin.xseries.XEntityType;
import com.cryptomorin.xseries.XMaterial;
import me.xginko.snowballfight.utils.SnowLayerHelper;
import me.xginko.snowballfight.utils.Util;
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

    private final Set<EntityType> configuredTypes;
    private final int snowPatchRadius;
    private final boolean formIce, addSnowLayer, replaceFullLayer, onlyForEntities, onlyForSpecificEntities, asBlacklist, onlyPlayers;
    private boolean powderSnowEnabled;

    protected SnowOnHit() {
        super("settings.snow", true,
                "\nCovers the hit block in snow.");
        this.onlyPlayers = config.getBoolean(configPath + ".only-thrown-by-player", true,
                "If enabled will only work if the snowball was thrown by a player.");
        this.snowPatchRadius = config.getInt(configPath + ".size", 3,
                "How big the snow patch should be that the snowball leaves as block radius.");
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
        }

        else if (event.getHitEntity() != null) {
            coverWithSnowAt(event.getHitEntity().getLocation().getBlock());
        }
    }

    private void coverWithSnowAt(Block centerBlock) {
        final int chunkX = centerBlock.getX() >> 4;
        final int chunkZ = centerBlock.getZ() >> 4;

        if (Util.isChunkUnsafe(chunkX, chunkZ)) { // Ignore if chunk way out of bounds
            return;
        }

        scheduling.regionSpecificScheduler(centerBlock.getWorld(), chunkX, chunkZ).runDelayed(() -> {
            if (snowPatchRadius == 1) {
                setSnowOrIce(centerBlock.getRelative(BlockFace.UP));
                return;
            }

            for (int x = centerBlock.getX() - snowPatchRadius; x <= centerBlock.getX() + snowPatchRadius; x++) {
                for (int z = centerBlock.getZ() - snowPatchRadius; z <= centerBlock.getZ() + snowPatchRadius; z++) {
                    for (int y = Math.max(Util.getMinWorldHeight(centerBlock.getWorld()), centerBlock.getY() - snowPatchRadius); y <= centerBlock.getY() + snowPatchRadius; y++) {
                        if (y > centerBlock.getWorld().getMaxHeight()) break;

                        if (Util.square(
                                centerBlock.getX() - x,
                                centerBlock.getY() - y,
                                centerBlock.getZ() - z) < Util.square(snowPatchRadius) - (snowPatchRadius == 2 ? 1 : 3)) {
                            setSnowOrIce(centerBlock.getWorld().getBlockAt(x, y, z));
                        }
                    }
                }
            }
        }, 1L);
    }

    private void setSnowOrIce(final Block block) {
        if (block.getType() == XMaterial.AIR.parseMaterial() || block.getType() == XMaterial.CAVE_AIR.parseMaterial()) {
            if (block.getRelative(BlockFace.DOWN).getType().isSolid()) {
                block.setType(XMaterial.SNOW.parseMaterial(), true);
            }
            return;
        }

        if (SnowLayerHelper.CAN_MODIFY_SNOW && addSnowLayer && block.getType() == XMaterial.SNOW.parseMaterial()) {
            SnowLayerHelper.addLayer(block, replaceFullLayer, powderSnowEnabled);
            return;
        }

        if (formIce && block.getType() == XMaterial.WATER.parseMaterial()) {
            block.setType(XMaterial.ICE.parseMaterial(), true);
        }
    }
}