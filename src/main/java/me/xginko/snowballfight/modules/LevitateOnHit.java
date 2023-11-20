package me.xginko.snowballfight.modules;

import com.tcoded.folialib.FoliaLib;
import com.tcoded.folialib.impl.ServerImplementation;
import me.xginko.snowballfight.SnowballConfig;
import me.xginko.snowballfight.SnowballFight;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashSet;
import java.util.List;

public class LevitateOnHit implements SnowballModule, Listener {

    private final ServerImplementation scheduler;
    private final HashSet<EntityType> configuredTypes = new HashSet<>();
    private final int duration, amplifier;
    private final boolean isFolia, onlyForSpecificEntities, asBlacklist;

    protected LevitateOnHit() {
        shouldEnable();
        FoliaLib foliaLib = SnowballFight.getFoliaLib();
        this.isFolia = foliaLib.isFolia();
        this.scheduler = isFolia ? foliaLib.getImpl() : null;
        SnowballConfig config = SnowballFight.getConfiguration();
        config.master().addComment("settings.levitation", "\nApply levitation effect on entities hit by snowballs.");
        this.duration = config.getInt("settings.levitation.duration-ticks", 6,
                "1 second = 20 ticks.");
        this.amplifier = config.getInt("settings.levitation.potion-amplifier", 48,
                "Vanilla amplifier of levitation is 1.");
        this.onlyForSpecificEntities = config.getBoolean("settings.levitation.only-for-specific-entities", false,
                "When enabled, only configured entities will levitate when hit by a snowball.");
        this.asBlacklist = config.getBoolean("settings.levitation.use-list-as-blacklist", false,
                "All entities except the ones on this list will levitate when hit by a snowball if set to true.");
        config.getList("settings.levitation.specific-entity-types",
                List.of(EntityType.PLAYER.name()),
                "Please use correct enums from: https://jd.papermc.io/paper/1.20/org/bukkit/entity/EntityType.html"
        ).forEach(configuredType -> {
            try {
                EntityType type = EntityType.valueOf(configuredType);
                this.configuredTypes.add(type);
            } catch (IllegalArgumentException e) {
                SnowballFight.getLog().warning("(Levitation) Configured entity type '"+configuredType+"' not recognized. " +
                        "Please use correct values from: https://jd.papermc.io/paper/1.20/org/bukkit/entity/EntityType.html");
            }
        });
    }

    @Override
    public boolean shouldEnable() {
        return SnowballFight.getConfiguration().getBoolean("settings.levitation.enable", false);
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
        if (!(event.getHitEntity() instanceof LivingEntity living)) return;
        if (onlyForSpecificEntities && (asBlacklist == configuredTypes.contains(living.getType()))) return;

        if (isFolia) {
            scheduler.runAtEntity(living, levitate -> living.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, duration, amplifier)));
        } else {
            living.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, duration, amplifier));
        }
    }
}