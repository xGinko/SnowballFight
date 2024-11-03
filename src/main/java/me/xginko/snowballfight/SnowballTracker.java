package me.xginko.snowballfight;

import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import me.xginko.snowballfight.utils.Disableable;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.UUID;

public final class SnowballTracker implements Disableable, Listener {

    private final @NotNull Cache<UUID, WrappedSnowball> snowballCache;

    SnowballTracker(@NotNull SnowballFight plugin, @NotNull Duration duration) {
        this.snowballCache = Caffeine.newBuilder().expireAfterWrite(duration).build();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void disable() {
        HandlerList.unregisterAll(this);
        snowballCache.invalidateAll();
        snowballCache.cleanUp();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private void onEntityRemoveFromWorld(EntityRemoveFromWorldEvent event) {
        snowballCache.invalidate(event.getEntity().getUniqueId());
    }

    public WrappedSnowball get(@NotNull Snowball snowball) {
        return snowballCache.get(snowball.getUniqueId(), uuid -> new WrappedSnowball(snowball));
    }

    public boolean contains(@NotNull UUID uuid) {
        return snowballCache.getIfPresent(uuid) != null;
    }
}
