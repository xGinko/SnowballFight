package me.xginko.snowballfight;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import me.xginko.snowballfight.models.WrappedSnowball;
import org.bukkit.Bukkit;
import org.bukkit.entity.Snowball;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;

public final class SnowballCache {

    private final @NotNull Cache<UUID, WrappedSnowball> cache;

    SnowballCache(long expireAfterWriteSeconds) {
        this.cache = Caffeine.newBuilder().expireAfterWrite(Duration.ofSeconds(expireAfterWriteSeconds)).build();
    }

    public @NotNull ConcurrentMap<UUID, WrappedSnowball> cacheMap() {
        return this.cache.asMap();
    }

    public @Nullable WrappedSnowball get(@NotNull UUID uuid) {
        WrappedSnowball wrappedSnowball = this.cache.getIfPresent(uuid);
        return wrappedSnowball == null && Bukkit.getEntity(uuid) instanceof Snowball snowball ? this.add(snowball) : wrappedSnowball;
    }

    public @NotNull WrappedSnowball getOrAdd(@NotNull Snowball snowball) {
        WrappedSnowball WrappedSnowball = this.cache.getIfPresent(snowball.getUniqueId());
        return WrappedSnowball == null ? this.add(new WrappedSnowball(snowball)) : this.add(WrappedSnowball);
    }

    public @NotNull WrappedSnowball add(@NotNull WrappedSnowball snowball) {
        this.cache.put(snowball.snowball().getUniqueId(), snowball);
        return snowball;
    }

    public @NotNull WrappedSnowball add(@NotNull Snowball snowball) {
        return this.add(new WrappedSnowball(snowball));
    }

    public boolean contains(@NotNull UUID uuid) {
        return this.cache.getIfPresent(uuid) != null;
    }

    public boolean contains(@NotNull WrappedSnowball snowball) {
        return this.contains(snowball.snowball().getUniqueId());
    }

    public boolean contains(@NotNull Snowball snowball) {
        return this.contains(snowball.getUniqueId());
    }
}