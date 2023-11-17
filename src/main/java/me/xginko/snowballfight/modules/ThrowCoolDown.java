package me.xginko.snowballfight.modules;

import com.destroystokyo.paper.event.player.PlayerLaunchProjectileEvent;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import me.xginko.snowballfight.SnowballConfig;
import me.xginko.snowballfight.SnowballFight;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import java.time.Duration;
import java.util.UUID;

public class ThrowCoolDown implements SnowballModule, Listener {

    private final Cache<UUID, Boolean> cooldowns;

    protected ThrowCoolDown() {
        shouldEnable();
        SnowballConfig config = SnowballFight.getConfiguration();
        config.master().addComment("settings.cooldown",
                "Configure a cooldown delay between throwing snowballs for players.");
        this.cooldowns = Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofMillis(config.getInt("settings.cooldown.delay-in-ticks", 10) * 50L))
                .build();
    }

    @Override
    public boolean shouldEnable() {
        return SnowballFight.getConfiguration().getBoolean("settings.cooldown.enable", false);
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

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    private void onPlayerLaunchSnowball(PlayerLaunchProjectileEvent event) {
        if (!event.getProjectile().getType().equals(EntityType.SNOWBALL)) return;

        final UUID playerUniqueId = event.getPlayer().getUniqueId();

        if (cooldowns.getIfPresent(playerUniqueId) != null) {
            event.setShouldConsume(false);
            event.setCancelled(true);
        } else {
            cooldowns.put(playerUniqueId, true);
        }
    }
}