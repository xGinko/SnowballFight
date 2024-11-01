package me.xginko.snowballfight.modules;

import com.cryptomorin.xseries.XEntityType;
import com.destroystokyo.paper.event.player.PlayerLaunchProjectileEvent;
import com.github.benmanes.caffeine.cache.Caffeine;
import me.xginko.snowballfight.SnowballConfig;
import me.xginko.snowballfight.SnowballFight;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.projectiles.BlockProjectileSource;
import org.bukkit.projectiles.ProjectileSource;

import java.time.Duration;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;

public class ThrowCoolDown implements SnowballModule, Listener {

    private final Set<UUID> player_cooldowns, entity_cooldowns;
    private final Set<Location> block_cooldowns;
    private final boolean blockCooldownEnabled, entityCooldownEnabled;

    protected ThrowCoolDown() {
        shouldEnable();
        SnowballConfig config = SnowballFight.config();
        config.master().addComment("settings.cooldown",
                "Configure a cooldown delay between throwing snowballs for players.");
        this.player_cooldowns = Collections.newSetFromMap(Caffeine.newBuilder().expireAfterWrite(Duration.ofMillis(
                Math.max(1, config.getInt("settings.cooldown.players.delay-in-ticks", 10) * 50L)
        )).<UUID, Boolean>build().asMap());
        this.entityCooldownEnabled = config.getBoolean("settings.cooldown.entities.enable", false);
        this.entity_cooldowns = Collections.newSetFromMap(Caffeine.newBuilder().expireAfterWrite(Duration.ofMillis(
                Math.max(1, config.getInt("settings.cooldown.entities.delay-in-ticks", 10) * 50L)
        )).<UUID, Boolean>build().asMap());
        this.blockCooldownEnabled = config.getBoolean("settings.cooldown.blocks.enable", false);
        this.block_cooldowns = Collections.newSetFromMap(Caffeine.newBuilder().expireAfterWrite(Duration.ofMillis(
                Math.max(1, config.getInt("settings.cooldown.blocks.delay-in-ticks", 20) * 50L)
        )).<Location, Boolean>build().asMap());
    }

    @Override
    public boolean shouldEnable() {
        return SnowballFight.config().getBoolean("settings.cooldown.enable", false);
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

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void onPlayerLaunchSnowball(PlayerLaunchProjectileEvent event) {
        if (event.getProjectile().getType() != XEntityType.SNOWBALL.get()) return;

        if (player_cooldowns.contains(event.getPlayer().getUniqueId())) {
            event.setShouldConsume(false);
            event.setCancelled(true);
        } else {
            player_cooldowns.add(event.getPlayer().getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void onEntityLaunch(ProjectileLaunchEvent event) {
        if (event.getEntityType() != XEntityType.SNOWBALL.get()) return;

        final ProjectileSource shooter = event.getEntity().getShooter();

        if (entityCooldownEnabled && shooter instanceof LivingEntity) {
            LivingEntity livingShooter = (LivingEntity) shooter;
            if (livingShooter.getType().equals(EntityType.PLAYER)) return; // Players in a different event due to item consumption.
            if (entity_cooldowns.contains(livingShooter.getUniqueId())) event.setCancelled(true);
            else entity_cooldowns.add(livingShooter.getUniqueId());
            return;
        }

        if (blockCooldownEnabled && shooter instanceof BlockProjectileSource) {
            BlockProjectileSource blockShooter = (BlockProjectileSource) shooter;
            final Location blockLocation = blockShooter.getBlock().getLocation();
            if (block_cooldowns.contains(blockLocation)) event.setCancelled(true);
            else block_cooldowns.add(blockLocation);
        }
    }
}