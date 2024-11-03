package me.xginko.snowballfight.modules;

import com.cryptomorin.xseries.XEntityType;
import com.cryptomorin.xseries.XPotion;
import me.xginko.snowballfight.SnowballFight;
import me.xginko.snowballfight.utils.Util;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.potion.PotionEffect;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class SlownessOnHit extends SnowballModule implements Listener {

    private final Set<EntityType> configuredTypes;
    private final PotionEffect slowness;
    private final double probability;
    private final boolean onlyForSpecificEntities, asBlacklist, onlyPlayers;

    protected SlownessOnHit() {
        super("settings.slowness", false,
                "\nApply slowness effect to entities hit by snowballs.");
        this.onlyPlayers = config.getBoolean(configPath + ".only-thrown-by-player", true,
                "If enabled will only work if the snowball was thrown by a player.");
        this.slowness = new PotionEffect(
                XPotion.SLOWNESS.getPotionEffectType(),
                config.getInt(configPath + ".duration-ticks", 40, "1 second = 20 ticks."),
                config.getInt(configPath + ".potion-amplifier", 2, "Vanilla amplifier can be up to 2.")
        );
        this.probability = config.getDouble(configPath + ".chance", 0.10,
                "Chance effect is applied on hit as double (100% = 1.00)");
        this.onlyForSpecificEntities = config.getBoolean(configPath + ".only-for-specific-entities", false,
                "When enabled, only configured entities will be slowed when hit by a snowball.");
        this.asBlacklist = config.getBoolean(configPath + ".use-list-as-blacklist", false,
                "All entities except the ones on this list will be slowed when hit by a snowball if set to true.");
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
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void disable() {
        HandlerList.unregisterAll(this);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onProjectileHit(ProjectileHitEvent event) {
        if (event.getEntityType() != XEntityType.SNOWBALL.get()) return;
        if (!Util.isLivingEntity(event.getHitEntity())) return;

        final LivingEntity living = (LivingEntity) event.getHitEntity();
        if (onlyForSpecificEntities && (asBlacklist == configuredTypes.contains(living.getType()))) return;
        if (probability < 1.0 && SnowballFight.getRandom().nextDouble() > probability) return;

        if (onlyPlayers && !(event.getEntity().getShooter() instanceof Player)) return;

        if (SnowballFight.isServerFolia()) {
            SnowballFight.scheduling().entitySpecificScheduler(living)
                    .run(() -> living.addPotionEffect(slowness), null);
        } else {
            living.addPotionEffect(slowness);
        }
    }
}