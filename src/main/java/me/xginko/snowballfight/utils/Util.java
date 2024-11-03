package me.xginko.snowballfight.utils;

import me.xginko.snowballfight.SnowballFight;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Color;
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

    private static final Map<EntityType, Boolean> IS_LIVING_CACHE = new EnumMap<>(EntityType.class);
    public static boolean isLivingEntity(Entity entity) {
        return entity != null && IS_LIVING_CACHE.computeIfAbsent(entity.getType(), entityType -> entity instanceof LivingEntity);
    }

    public static void sendMessage(CommandSender sender, Component message) {
        SnowballFight.audiences().sender(sender).sendMessage(message);
    }

    public static @NotNull Color colorFromHexString(String hexString) throws NumberFormatException {
        return Color.fromRGB(Integer.parseInt(hexString.replace("#", ""), 16));
    }

    public static boolean hasClass(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
