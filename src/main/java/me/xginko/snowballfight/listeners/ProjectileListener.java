package me.xginko.snowballfight.listeners;

import me.xginko.snowballfight.SnowballCache;
import me.xginko.snowballfight.SnowballFight;
import me.xginko.snowballfight.events.SnowballHitEvent;
import me.xginko.snowballfight.events.SnowballLaunchEvent;
import me.xginko.snowballfight.modules.SnowballModule;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;

public class ProjectileListener implements SnowballModule, Listener {

    private final SnowballCache cache;

    public ProjectileListener() {
        this.cache = SnowballFight.getCache();
    }

    @Override
    public boolean shouldEnable() {
        return true;
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

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (!event.getEntityType().equals(EntityType.SNOWBALL)) return;
        final Snowball snowball = (Snowball) event.getEntity();
        if (!new SnowballLaunchEvent(snowball, cache.getOrAdd(snowball), event.isCancelled()).callEvent()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onProjectileHit(ProjectileHitEvent event) {
        if (!event.getEntityType().equals(EntityType.SNOWBALL)) return;
        final Snowball snowball = (Snowball) event.getEntity();
        if (!new SnowballHitEvent(snowball, cache.getOrAdd(snowball), event.getHitEntity(),
                event.getHitBlock(), event.getHitBlockFace(), event.isCancelled()).callEvent()) {
            event.setCancelled(true);
        }
    }
}