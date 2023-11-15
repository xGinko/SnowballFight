package me.xginko.snowballfight.modules.effects;

import com.tcoded.folialib.FoliaLib;
import com.tcoded.folialib.impl.ServerImplementation;
import me.xginko.snowballfight.SnowballConfig;
import me.xginko.snowballfight.SnowballFight;
import me.xginko.snowballfight.modules.SnowballModule;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class LevitateOnHit implements SnowballModule, Listener {

    private final ServerImplementation scheduler;
    private final int duration;
    private final boolean isFolia;

    public LevitateOnHit() {
        shouldEnable();
        FoliaLib foliaLib = SnowballFight.getFoliaLib();
        this.isFolia = foliaLib.isFolia();
        this.scheduler = isFolia ? foliaLib.getImpl() : null;
        SnowballConfig config = SnowballFight.getConfiguration();
        this.duration = config.getInt("levitation-on-hit.effect-duration-ticks", 40);
    }

    @Override
    public boolean shouldEnable() {
        return SnowballFight.getConfiguration().getBoolean("levitation-on-hit.enable", true);
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
    private void onProjectileHit(ProjectileHitEvent event) {
        if (!event.getEntityType().equals(EntityType.SNOWBALL)) return;

        if (event.getHitEntity() instanceof LivingEntity living) {
            if (isFolia) {
                scheduler.runAtEntity(living, levitate -> living.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, duration, 2, true)));
            } else {
                living.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, duration, 2, true));
            }
        }
    }
}