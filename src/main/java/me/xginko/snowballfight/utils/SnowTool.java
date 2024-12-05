package me.xginko.snowballfight.utils;

import com.cryptomorin.xseries.XMaterial;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Snow;

public class SnowTool {

    public static final boolean CAN_MODIFY_SNOW = Util.hasClass("org.bukkit.block.data.type.Snow");

    public static void increaseSnowLayer(Block block, boolean replaceFullLayer, boolean powderSnowEnabled) {
        Snow snowData = (Snow) block.getBlockData();
        final int layers = snowData.getLayers();
        if (replaceFullLayer) {
            if (layers < snowData.getMaximumLayers() - 1) {
                snowData.setLayers(layers + 1);
                block.setBlockData(snowData);
            } else {
                // If only one or no more layers left to add, turn into snow block.
                block.setType(powderSnowEnabled ? XMaterial.POWDER_SNOW.parseMaterial() : XMaterial.SNOW_BLOCK.parseMaterial(), true);
            }
        } else {
            if (layers < snowData.getMaximumLayers()) {
                snowData.setLayers(layers + 1);
                block.setBlockData(snowData);
            }
        }
    }
}
