package me.xginko.snowballfight.modules;

import com.destroystokyo.paper.event.entity.EntityKnockbackByEntityEvent;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.tcoded.folialib.FoliaLib;
import com.tcoded.folialib.impl.ServerImplementation;
import me.xginko.snowballfight.SnowballCache;
import me.xginko.snowballfight.SnowballConfig;
import me.xginko.snowballfight.SnowballFight;
import me.xginko.snowballfight.models.WrappedSnowball;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.meta.FireworkMeta;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class FireworkOnHit implements SnowballModule, Listener {

    private final ServerImplementation scheduler;
    private final SnowballCache snowballCache;
    private final Cache<UUID, Boolean> snowballFireworks;
    private final List<FireworkEffect.Type> effectTypes;
    private final HashSet<EntityType> configuredTypes;
    private final boolean isFolia, dealDamage, dealKnockback, flicker, trail, onlyForEntities, onlyForSpecificEntities, asBlacklist;

    protected FireworkOnHit() {
        shouldEnable();
        FoliaLib foliaLib = SnowballFight.getFoliaLib();
        this.isFolia = foliaLib.isFolia();
        this.scheduler = isFolia ? foliaLib.getImpl() : null;
        this.snowballCache = SnowballFight.getCache();
        this.snowballFireworks = Caffeine.newBuilder().expireAfterWrite(2, TimeUnit.SECONDS).build();
        SnowballConfig config = SnowballFight.getConfiguration();
        config.master().addComment("settings.fireworks",
                "\nDetonate a firework when a snowball hits something for a cool effect.");
        this.dealDamage = config.getBoolean("settings.fireworks.deal-damage", false,
                "Should firework effects deal damage like regular fireworks?");
        this.dealKnockback = config.getBoolean("settings.fireworks.deal-knockback", false,
                "Should firework effects deal knockback like regular fireworks?");
        this.trail = config.getBoolean("settings.fireworks.trail", true,
                "Whether the firework particles should leave trails.");
        this.flicker = config.getBoolean("settings.fireworks.flicker", false,
                "Whether the firework particles should flicker.");
        this.effectTypes = config.getList("settings.fireworks.types", Arrays.asList("BURST", "BALL"), 
                        "FireworkEffect Types you wish to use. Has to be a valid enum from:\n" +
                        "https://jd.papermc.io/paper/1.20/org/bukkit/FireworkEffect.Type.html")
                .stream()
                .map(effect -> {
                    try {
                        return FireworkEffect.Type.valueOf(effect);
                    } catch (IllegalArgumentException e) {
                        SnowballFight.getLog().warn("FireworkEffect Type '"+effect+"' not recognized. " +
                                "Please use valid enums from: https://jd.papermc.io/paper/1.20/org/bukkit/FireworkEffect.Type.html");
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (effectTypes.isEmpty()) {
            effectTypes.add(FireworkEffect.Type.BURST);
        }
        this.onlyForEntities = config.getBoolean("settings.fireworks.only-for-entities", false,
                "Enable if you only want explosions to happen when a snowball hits an entity.");
        this.onlyForSpecificEntities = config.getBoolean("settings.fireworks.only-for-specific-entities", false,
                "When enabled, snowballs will only explode for the configured entity types below.\n" +
                "Needs only-for-entities to be set to true.");
        this.asBlacklist = config.getBoolean("settings.fireworks.use-list-as-blacklist", false,
                "Setting this and only-for-specific-entities to true will mean there will only be a firework effect\n" +
                "if the hit entity is NOT on this list.");
        this.configuredTypes = config.getList("settings.fireworks.specific-entity-types", Collections.singletonList("PLAYER"),
                "Please use correct enums from: https://jd.papermc.io/paper/1.20/org/bukkit/entity/EntityType.html")
                .stream()
                .map(configuredType -> {
                    try {
                        return EntityType.valueOf(configuredType);
                    } catch (IllegalArgumentException e) {
                        SnowballFight.getLog().warn("(Fireworks) Configured entity type '"+configuredType+"' not recognized. " +
                                "Please use correct values from: https://jd.papermc.io/paper/1.20/org/bukkit/entity/EntityType.html");
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(HashSet::new));
    }

    @Override
    public boolean shouldEnable() {
        return SnowballFight.getConfiguration().getBoolean("settings.fireworks.enable", true);
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
    private void onSnowballHit(ProjectileHitEvent event) {
        if (!event.getEntityType().equals(EntityType.SNOWBALL)) return;
        final Entity hitEntity = event.getHitEntity();
        if (onlyForEntities) {
            if (hitEntity == null) return;
            if (onlyForSpecificEntities && (asBlacklist == configuredTypes.contains(hitEntity.getType()))) return;
        }

        if (hitEntity != null) {
            if (isFolia) scheduler.runAtEntity(hitEntity, firework -> detonateFirework(hitEntity.getLocation(), (Snowball) event.getEntity()));
            else detonateFirework(hitEntity.getLocation(), (Snowball) event.getEntity());
            return;
        }

        final Block hitBlock = event.getHitBlock();

        if (hitBlock != null) {
            final BlockFace hitFace = event.getHitBlockFace();
            final Location fireworkLoc = hitFace != null ? hitBlock.getRelative(hitFace).getLocation().toCenterLocation() : hitBlock.getLocation().toCenterLocation();
            if (isFolia) scheduler.runAtLocation(fireworkLoc, firework -> detonateFirework(fireworkLoc, (Snowball) event.getEntity()));
            else detonateFirework(hitBlock.getLocation(), (Snowball) event.getEntity());
        }
    }

    private void detonateFirework(final Location explosionLoc, final Snowball snowball) {
        Firework firework = explosionLoc.getWorld().spawn(explosionLoc, Firework.class);
        if (!dealDamage || !dealKnockback)
            this.snowballFireworks.put(firework.getUniqueId(), true); // store uuid to cancel damage by fireworks
        FireworkMeta meta = firework.getFireworkMeta();
        meta.clearEffects();
        WrappedSnowball wrappedSnowball = snowballCache.getOrAdd(snowball);
        meta.addEffect(FireworkEffect.builder()
                .withColor(wrappedSnowball.getPrimaryColor(), wrappedSnowball.getSecondaryColor())
                .with(effectTypes.get(SnowballFight.getRandom().nextInt(effectTypes.size())))
                .flicker(flicker)
                .trail(trail)
                .build());
        firework.setFireworkMeta(meta);
        firework.setShooter(snowball.getShooter()); // Copy over shooter for damage tracking
        firework.detonate();
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void onFireworkExplode(EntityDamageByEntityEvent event) {
        if (!dealDamage && this.snowballFireworks.getIfPresent(event.getDamager().getUniqueId()) != null) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    private void onKnockback(EntityKnockbackByEntityEvent event) {
        if (!dealKnockback && this.snowballFireworks.getIfPresent(event.getHitBy().getUniqueId()) != null) {
            event.setCancelled(true);
        }
    }
}