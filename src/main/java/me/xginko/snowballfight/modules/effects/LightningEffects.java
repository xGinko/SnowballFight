package me.xginko.snowballfight.modules.effects;

import com.tcoded.folialib.FoliaLib;
import com.tcoded.folialib.impl.ServerImplementation;
import me.xginko.snowballfight.SnowballConfig;
import me.xginko.snowballfight.SnowballFight;
import me.xginko.snowballfight.modules.SnowballModule;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;

import java.util.Random;

public class LightningEffects implements SnowballModule, Listener {

    private final ServerImplementation scheduler;
    private final boolean isFolia, deal_damage;
    private final int spawn_amount, flashcount;
    private final double probability;

    public LightningEffects() {
        shouldEnable();
        FoliaLib foliaLib = SnowballFight.getFoliaLib();
        this.isFolia = foliaLib.isFolia();
        this.scheduler = isFolia ? foliaLib.getImpl() : null;
        SnowballConfig config = SnowballFight.getConfiguration();
        config.master().addComment("lightning-effects", "Will strike the closest player with lightning.");
        this.deal_damage = config.getBoolean("lightning-effects.deal-damage", true);
        this.spawn_amount = config.getInt("lightning-effects.lightning-strikes", 2, "Amount of times to strike.");
        this.flashcount = config.getInt("lightning-effects.lightning-flash-count", 2, "Amount of times to flash after strike.");
        this.probability = config.getDouble("lightning-effects.lightning-chance", 0.1, "Percentage as double: 100% = 1.0");
    }

    @Override
    public boolean shouldEnable() {
        return SnowballFight.getConfiguration().getBoolean("lightning-effects.enable", false);
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

    @EventHandler(priority = EventPriority.LOW)
    private void onProjectileHit(ProjectileHitEvent event) {
        if (!event.getEntityType().equals(EntityType.SNOWBALL)) return;
        if (probability < 1.0 && new Random().nextDouble() > probability) return;

        final Entity hitEntity = event.getHitEntity();
        if (hitEntity != null) {
            if (isFolia) scheduler.runAtEntity(hitEntity, strike -> this.strikeLightning(hitEntity.getLocation()));
            else this.strikeLightning(hitEntity.getLocation());
            return;
        }

        final Block hitBlock = event.getHitBlock();
        if (hitBlock != null) {
            final Location hitBlockLoc = hitBlock.getLocation();
            if (isFolia) scheduler.runAtLocation(hitBlockLoc, strike -> this.strikeLightning(hitBlockLoc));
            else this.strikeLightning(hitBlockLoc);
        }
    }

    private void strikeLightning(final Location strikeLoc) {
        final World world = strikeLoc.getWorld();
        for (int i = 0; i < spawn_amount; i++) {
            (deal_damage ? world.strikeLightning(strikeLoc) : world.strikeLightningEffect(strikeLoc)).setFlashCount(flashcount);
        }
    }
}