package me.xginko.snowballfight.utils;

import com.cryptomorin.xseries.XMaterial;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Snow;

public class SnowLayerHelper {

    public static final boolean CAN_MODIFY_SNOW = Util.hasClass("org.bukkit.block.data.type.Snow");

    public static void addLayer(Block block, boolean replaceFullLayer, boolean usePowderSnow) {
        Snow snow = (Snow) block.getBlockData();

        if (replaceFullLayer) {
            if (snow.getLayers() < snow.getMaximumLayers() - 1) {
                snow.setLayers(snow.getLayers() + 1);
                block.setBlockData(snow);
            } else {
                // If only one or no more layers left to add, turn into snow block.
                block.setType(usePowderSnow ? XMaterial.POWDER_SNOW.parseMaterial() : XMaterial.SNOW_BLOCK.parseMaterial(), true);
            }
            return;
        }

        if (snow.getLayers() < snow.getMaximumLayers()) {
            snow.setLayers(snow.getLayers() + 1);
            block.setBlockData(snow);
        }
    }
}
