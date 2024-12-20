package me.xginko.snowballfight.utils;

import me.xginko.snowballfight.SnowballFight;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;
import java.util.Map;
import java.util.Random;

public final class Util {

    public static final Random RANDOM;
    public static final TextColor SNOWY_WHITE, SNOWY_BLUE, SNOWY_DARK_BLUE, SNOWY_RED;
    public static final Style SNOWY_WHITE_BOLD;

    static {
        RANDOM = new Random();
        SNOWY_WHITE = TextColor.fromHexString("#E8EBF0");
        SNOWY_BLUE = TextColor.fromHexString("#9BDBFF");
        SNOWY_DARK_BLUE = TextColor.fromHexString("#407794");
        SNOWY_RED = TextColor.fromHexString("#ff9ba6");
        SNOWY_WHITE_BOLD = Style.style().color(SNOWY_WHITE).decorate(TextDecoration.BOLD).build();
    }

    public static @NotNull Location toCenterLocation(@NotNull Location location) {
        Location centered = location.clone();
        centered.setX(centered.getBlockX() + 0.5);
        centered.setY(centered.getBlockY() + 0.5);
        centered.setZ(centered.getBlockZ() + 0.5);
        return location;
    }

    private static final Map<EntityType, Boolean> IS_LIVING_CACHE = new EnumMap<>(EntityType.class);
    public static boolean isLivingEntity(Entity entity) {
        return entity != null && IS_LIVING_CACHE.computeIfAbsent(entity.getType(), entityType -> entity instanceof LivingEntity);
    }

    private static final boolean GET_MIN_WORLD_HEIGHT_AVAILABLE = hasMethod(World.class, "getMinHeight");
    public static int getMinWorldHeight(World world) {
        return GET_MIN_WORLD_HEIGHT_AVAILABLE ? world.getMinHeight() : 0;
    }

    public static void sendMessage(CommandSender sender, Component message) {
        SnowballFight.audiences().sender(sender).sendMessage(message);
    }

    // If any chunk coord is outside 30 million blocks, paper will warn about dangerous chunk retrieval
    public static boolean isChunkUnsafe(int chunkX, int chunkZ) {
        return chunkX > 1875000 || chunkZ > 1875000 || chunkX < -1875000 || chunkZ < -1875000;
    }

    public static @NotNull Color colorFromHexString(String hexString) throws NumberFormatException {
        return Color.fromRGB(Integer.parseInt(hexString.replace("#", ""), 16));
    }

    public static double square(double d) {
        return d * d;
    }

    public static double square(double x, double y, double z) {
        return square(x) + square(y) + square(z);
    }

    public static boolean hasClass(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static boolean hasMethod(Class<?> holderClass, String methodName, Class<?>... parameterClasses) {
        try {
            holderClass.getMethod(methodName, parameterClasses);
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }
}
