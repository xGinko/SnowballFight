package me.xginko.snowballfight.utils;

import me.xginko.snowballfight.SnowballFight;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

import java.util.EnumMap;
import java.util.Map;

public final class Util {

    private static final Map<EntityType, Boolean> IS_LIVING_CACHE = new EnumMap<>(EntityType.class);
    public static boolean isLivingEntity(Entity entity) {
        if (entity == null) return false;
        return IS_LIVING_CACHE.computeIfAbsent(entity.getType(), entityType -> entity instanceof LivingEntity);
    }

    public static void sendMessage(CommandSender sender, Component message) {
        SnowballFight.getAudiences().sender(sender).sendMessage(message);
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
