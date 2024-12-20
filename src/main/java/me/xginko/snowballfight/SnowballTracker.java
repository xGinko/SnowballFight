package me.xginko.snowballfight;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import me.xginko.snowballfight.utils.Disableable;
import org.bukkit.entity.Snowball;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.UUID;

public final class SnowballTracker implements Disableable {

    private final @NotNull Cache<UUID, WrappedSnowball> snowballCache;

    SnowballTracker(@NotNull Duration duration) {
        snowballCache = Caffeine.newBuilder().expireAfterWrite(duration).build();
    }

    @Override
    public void disable() {
        snowballCache.invalidateAll();
        snowballCache.cleanUp();
    }

    public WrappedSnowball get(@NotNull Snowball snowball) {
        return snowballCache.get(snowball.getUniqueId(), uuid -> new WrappedSnowball(snowball));
    }
}
