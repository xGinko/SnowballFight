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

public class LevitateOnHit extends SnowballModule implements Listener {

    private final Set<EntityType> configuredTypes;
    private final PotionEffect levitation;
    private final boolean onlyForSpecificEntities, asBlacklist, onlyPlayers;

    protected LevitateOnHit() {
        super("settings.levitation", false,
                "\nApply levitation effect on entities hit by snowballs.");
        this.levitation = XPotion.LEVITATION.buildPotionEffect(
                config.getInt(configPath + ".duration-ticks", 6, "1 second = 20 ticks."),
                config.getInt(configPath + ".potion-amplifier", 48, "Vanilla amplifier of levitation is 1.")
        );
        this.onlyPlayers = config.getBoolean(configPath + ".only-thrown-by-player", true,
                "If enabled will only work if the snowball was thrown by a player.");
        this.onlyForSpecificEntities = config.getBoolean(configPath + ".only-for-specific-entities", false,
                "When enabled, only configured entities will levitate when hit by a snowball.");
        this.asBlacklist = config.getBoolean(configPath + ".use-list-as-blacklist", false,
                "All entities except the ones on this list will levitate when hit by a snowball if set to true.");
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

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void onProjectileHit(ProjectileHitEvent event) {
        if (event.getEntityType() != XEntityType.SNOWBALL.get()) return;
        if (!Util.isLivingEntity(event.getHitEntity())) return;

        final LivingEntity living = (LivingEntity) event.getHitEntity();
        if (onlyForSpecificEntities && (asBlacklist == configuredTypes.contains(living.getType()))) return;

        if (onlyPlayers && !(event.getEntity().getShooter() instanceof Player)) return;

        if (SnowballFight.isServerFolia()) {
            SnowballFight.scheduling().entitySpecificScheduler(living)
                    .run(() -> living.addPotionEffect(levitation), null);
        } else {
            living.addPotionEffect(levitation);
        }
    }
}