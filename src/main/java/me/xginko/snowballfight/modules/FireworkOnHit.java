package me.xginko.snowballfight.modules;

import com.cryptomorin.xseries.XEntityType;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.collect.ImmutableList;
import me.xginko.snowballfight.SnowballFight;
import me.xginko.snowballfight.WrappedSnowball;
import me.xginko.snowballfight.utils.Util;
import org.bukkit.FireworkEffect;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.projectiles.ProjectileSource;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class FireworkOnHit extends SnowballModule implements Listener {

    private final static boolean CAN_SET_SHOOTER = Util.hasMethod(Firework.class, "setShooter", ProjectileSource.class);

    private final List<FireworkEffect.Type> effectTypes;
    private final Set<EntityType> configuredTypes;
    private final boolean dealDamage, dealKnockback, flicker, trail, onlyForEntities, onlyForSpecificEntities,
            asBlacklist, onlyPlayers;

    private Set<UUID> effectFireworks;
    private Listener damageListener, knockBackListener;

    protected FireworkOnHit() {
        super("settings.fireworks", true,
                "\nDetonate a firework when a snowball hits something for a cool effect.");
        this.onlyPlayers = config.getBoolean(configPath + ".only-thrown-by-player", true,
                "If enabled will only work if the snowball was thrown by a player.");
        this.dealDamage = config.getBoolean(configPath + ".deal-damage", false,
                "Should firework effects deal damage like regular fireworks?");
        this.dealKnockback = config.getBoolean(configPath + ".deal-knockback", !SnowballFight.isServerPaper(),
                "Should firework effects deal knockback like regular fireworks?\n" +
                        "Note: This setting has no effect on non-paper servers.");
        this.trail = config.getBoolean(configPath + ".trail", true,
                "Whether the firework particles should leave trails.");
        this.flicker = config.getBoolean(configPath + ".flicker", false,
                "Whether the firework particles should flicker.");
        this.effectTypes = config.getList(configPath + ".types", Arrays.asList("BURST", "BALL"),
                        "FireworkEffect Types you wish to use. Has to be a valid enum from:\n" +
                        "https://jd.papermc.io/paper/1.20/org/bukkit/FireworkEffect.Type.html")
                .stream()
                .map(effect -> {
                    try {
                        return FireworkEffect.Type.valueOf(effect);
                    } catch (IllegalArgumentException e) {
                        warn("FireworkEffectType '" + effect + "' not recognized. Please use valid enums from:" +
                                " https://jd.papermc.io/paper/1.20/org/bukkit/FireworkEffect.Type.html");
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.collectingAndThen(Collectors.toList(), list -> {
                    if (list.isEmpty()) {
                        list.add(FireworkEffect.Type.BURST);
                    }
                    return ImmutableList.copyOf(list);
                }));
        this.onlyForEntities = config.getBoolean(configPath + ".only-for-entities", false,
                "Enable if you only want explosions to happen when a snowball hits an entity.");
        this.onlyForSpecificEntities = config.getBoolean(configPath + ".only-for-specific-entities", false,
                "When enabled, snowballs will only explode for the configured entity types below.\n" +
                "Needs only-for-entities to be set to true.");
        this.asBlacklist = config.getBoolean(configPath + ".use-list-as-blacklist", false,
                "Setting this and only-for-specific-entities to true will mean there will only be a firework effect\n" +
                "if the hit entity is NOT on this list.");
        this.configuredTypes = config.getList(configPath + ".specific-entity-types", Collections.singletonList("PLAYER"),
                "Please use correct enums from: https://jd.papermc.io/paper/1.20/org/bukkit/entity/EntityType.html")
                .stream()
                .map(configuredType -> {
                    try {
                        return EntityType.valueOf(configuredType);
                    } catch (IllegalArgumentException e) {
                        warn("EntityType '" + configuredType + "' not recognized. " +
                                "Please use correct values from: https://jd.papermc.io/paper/1.20/org/bukkit/entity/EntityType.html");
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(EntityType.class)));
    }

    @Override
    public void enable() {
        if (!dealDamage || !dealKnockback) {
            effectFireworks = Collections.newSetFromMap(Caffeine.newBuilder()
                    .expireAfterWrite(Duration.ofSeconds(2)).<UUID, Boolean>build().asMap());
            if (!dealDamage) {
                damageListener = new DamageListener(effectFireworks);
                plugin.getServer().getPluginManager().registerEvents(damageListener, plugin);
            }
            if (!dealKnockback && SnowballFight.isServerPaper()) {
                knockBackListener = new KnockBackListener(effectFireworks);
                plugin.getServer().getPluginManager().registerEvents(knockBackListener, plugin);
            }
        }
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void disable() {
        HandlerList.unregisterAll(this);
        if (damageListener != null) {
            HandlerList.unregisterAll(damageListener);
            damageListener = null;
        }
        if (knockBackListener != null) {
            HandlerList.unregisterAll(knockBackListener);
            knockBackListener = null;
        }
        if (effectFireworks != null) {
            effectFireworks.clear();
            effectFireworks = null;
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onProjectileHit(ProjectileHitEvent event) {
        if (event.getEntityType() != XEntityType.SNOWBALL.get()) return;

        if (onlyForEntities) {
            if (event.getHitEntity() == null) return;
            if (onlyForSpecificEntities && (asBlacklist == configuredTypes.contains(event.getHitEntity().getType()))) return;
        }

        if (onlyPlayers && !(event.getEntity().getShooter() instanceof Player)) return;

        final Snowball snowball = (Snowball) event.getEntity();

        if (SnowballFight.isServerFolia()) {
            SnowballFight.scheduling().entitySpecificScheduler(snowball).run(() -> detonateFirework(snowball), null);
        } else {
            detonateFirework(snowball);
        }
    }

    private void detonateFirework(final Snowball snowball) {
        Firework firework = snowball.getWorld().spawn(snowball.getLocation(), Firework.class);
        if (effectFireworks != null) {
            effectFireworks.add(firework.getUniqueId()); // Cache uuid to cancel damage/knockback by fireworks
        }
        FireworkMeta meta = firework.getFireworkMeta();
        meta.clearEffects();
        WrappedSnowball wrappedSnowball = SnowballFight.snowballTracker().get(snowball);
        meta.addEffect(FireworkEffect.builder()
                .withColor(wrappedSnowball.getPrimaryColor(), wrappedSnowball.getSecondaryColor())
                .with(effectTypes.get(Util.RANDOM.nextInt(effectTypes.size())))
                .flicker(flicker)
                .trail(trail)
                .build());
        firework.setFireworkMeta(meta);
        if (CAN_SET_SHOOTER) {
            firework.setShooter(snowball.getShooter()); // Copy over shooter for damage tracking
        }
        firework.detonate();
    }

    private static class DamageListener implements Listener {

        private final Set<UUID> effectFireworks;

        private DamageListener(Set<UUID> effectFireworks) {
            this.effectFireworks = effectFireworks;
        }

        @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
        private void onEntityDamageByEntity(org.bukkit.event.entity.EntityDamageByEntityEvent event) {
            if (effectFireworks.contains(event.getDamager().getUniqueId())) {
                event.setCancelled(true);
            }
        }
    }

    private static class KnockBackListener implements Listener {

        private final Set<UUID> effectFireworks;

        private KnockBackListener(Set<UUID> effectFireworks) {
            this.effectFireworks = effectFireworks;
        }

        @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
        private void onEntityKnockbackByEntity(com.destroystokyo.paper.event.entity.EntityKnockbackByEntityEvent event) {
            if (effectFireworks.contains(event.getHitBy().getUniqueId())) {
                event.setCancelled(true);
            }
        }
    }
}