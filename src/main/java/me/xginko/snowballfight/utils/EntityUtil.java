package me.xginko.snowballfight.utils;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

import java.util.EnumMap;
import java.util.Map;

public final class EntityUtil {

    private static final Map<EntityType, Boolean> IS_LIVING_CACHE = new EnumMap<>(EntityType.class);
    public static boolean isLivingEntity(Entity entity) {
        if (entity == null) return false;
        return IS_LIVING_CACHE.computeIfAbsent(entity.getType(), entityType -> entity instanceof LivingEntity);
    }
}
