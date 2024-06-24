package me.xginko.snowballfight.modules;

import me.xginko.snowballfight.SnowballConfig;
import me.xginko.snowballfight.SnowballFight;
import me.xginko.snowballfight.utils.EntityUtil;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class LevitateOnHit implements SnowballModule, Listener {

    private final Set<EntityType> configuredTypes;
    private final PotionEffect levitation;
    private final boolean onlyForSpecificEntities, asBlacklist, onlyPlayers;

    protected LevitateOnHit() {
        shouldEnable();
        SnowballConfig config = SnowballFight.config();
        config.master().addComment("settings.levitation", "\nApply levitation effect on entities hit by snowballs.");
        this.levitation = new PotionEffect(
                PotionEffectType.LEVITATION,
                config.getInt("settings.levitation.duration-ticks", 6, "1 second = 20 ticks."),
                config.getInt("settings.levitation.potion-amplifier", 48, "Vanilla amplifier of levitation is 1.")
        );
        this.onlyPlayers = config.getBoolean("settings.levitation.only-thrown-by-player", true,
                "If enabled will only work if the snowball was thrown by a player.");
        this.onlyForSpecificEntities = config.getBoolean("settings.levitation.only-for-specific-entities", false,
                "When enabled, only configured entities will levitate when hit by a snowball.");
        this.asBlacklist = config.getBoolean("settings.levitation.use-list-as-blacklist", false,
                "All entities except the ones on this list will levitate when hit by a snowball if set to true.");
        this.configuredTypes = config.getList("settings.levitation.specific-entity-types", Collections.singletonList("PLAYER"),
                "Please use correct enums from: https://jd.papermc.io/paper/1.20/org/bukkit/entity/EntityType.html")
                .stream()
                .map(configuredType -> {
                    try {
                        return EntityType.valueOf(configuredType);
                    } catch (IllegalArgumentException e) {
                        SnowballFight.logger().warn("(Levitation) Configured entity type '{}' not recognized. " +
                                "Please use correct values from: https://jd.papermc.io/paper/1.20/org/bukkit/entity/EntityType.html", configuredType);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(EntityType.class)));
    }

    @Override
    public boolean shouldEnable() {
        return SnowballFight.config().getBoolean("settings.levitation.enable", false);
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
    private void onSnowballHit(ProjectileHitEvent event) {
        if (!event.getEntityType().equals(EntityType.SNOWBALL)) return;
        if (!EntityUtil.isLivingEntity(event.getHitEntity())) return;

        final LivingEntity living = (LivingEntity) event.getHitEntity();
        if (onlyForSpecificEntities && (asBlacklist == configuredTypes.contains(living.getType()))) return;

        if (onlyPlayers && !(event.getEntity().getShooter() instanceof Player)) return;

        if (SnowballFight.isServerFolia()) {
            SnowballFight.getScheduler().runAtEntity(living, levitate -> living.addPotionEffect(levitation));
        } else {
            living.addPotionEffect(levitation);
        }
    }
}