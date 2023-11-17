package me.xginko.snowballfight.modules;

import com.tcoded.folialib.FoliaLib;
import com.tcoded.folialib.impl.ServerImplementation;
import me.xginko.snowballfight.SnowballConfig;
import me.xginko.snowballfight.SnowballFight;
import me.xginko.snowballfight.events.SnowballHitEvent;
import me.xginko.snowballfight.models.WrappedSnowball;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.inventory.meta.FireworkMeta;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class FireworkOnHit implements SnowballModule, Listener {

    private final ServerImplementation scheduler;
    private final List<FireworkEffect.Type> types = new ArrayList<>();
    private final HashSet<EntityType> configuredTypes = new HashSet<>();
    private final boolean isFolia, flicker, trail, onlyForEntities, onlyForSpecificEntities, asBlacklist;

    protected FireworkOnHit() {
        shouldEnable();
        FoliaLib foliaLib = SnowballFight.getFoliaLib();
        this.isFolia = foliaLib.isFolia();
        this.scheduler = isFolia ? foliaLib.getImpl() : null;
        SnowballConfig config = SnowballFight.getConfiguration();
        config.master().addComment("settings.fireworks",
                "\nDetonate a firework when a snowball hits something for a cool effect.");
        this.trail = config.getBoolean("settings.fireworks.trail", true,
                "Whether the firework particles should leave trails.");
        this.flicker = config.getBoolean("settings.fireworks.flicker", false,
                "Whether the firework particles should flicker.");
        config.getList("settings.fireworks.types",
                List.of(FireworkEffect.Type.BALL.name(), FireworkEffect.Type.STAR.name()), """
                        FireworkEffect Types you wish to use. Has to be a valid enum from:\s
                        https://jd.papermc.io/paper/1.20/org/bukkit/FireworkEffect.Type.html"""
        ).forEach(effect -> {
            try {
                FireworkEffect.Type effectType = FireworkEffect.Type.valueOf(effect);
                this.types.add(effectType);
            } catch (IllegalArgumentException e) {
                SnowballFight.getLog().warning("FireworkEffect Type '"+effect+"' not recognized. " +
                        "Please use valid enums from: https://jd.papermc.io/paper/1.20/org/bukkit/FireworkEffect.Type.html");
            }
        });
        this.onlyForEntities = config.getBoolean("settings.fireworks.only-for-entities", false,
                "Enable if you only want explosions to happen when a snowball hits an entity.");
        this.onlyForSpecificEntities = config.getBoolean("settings.fireworks.only-for-specific-entities", false, """
                When enabled, snowballs will only explode for the configured entity types below.\s
                Needs only-for-entities to be set to true.""");
        this.asBlacklist = config.getBoolean("settings.fireworks.use-list-as-blacklist", false, """
                Setting this and only-for-specific-entities to true will mean there will only be an explosion\s
                if the hit entity is NOT on this list.""");
        config.getList("settings.fireworks.specific-entity-types",
                List.of(EntityType.PLAYER.name()),
                "Please use correct enums from: https://jd.papermc.io/paper/1.20/org/bukkit/entity/EntityType.html"
        ).forEach(configuredType -> {
            try {
                EntityType type = EntityType.valueOf(configuredType);
                this.configuredTypes.add(type);
            } catch (IllegalArgumentException e) {
                SnowballFight.getLog().warning("(Fireworks) Configured entity type '"+configuredType+"' not recognized. " +
                        "Please use correct values from: https://jd.papermc.io/paper/1.20/org/bukkit/entity/EntityType.html");
            }
        });
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
    private void onSnowballHit(SnowballHitEvent event) {
        final Entity hitEntity = event.getHitEntity();

        if (onlyForEntities) {
            if (hitEntity == null) return;
            if (onlyForSpecificEntities && (asBlacklist == configuredTypes.contains(event.getHitEntity().getType()))) return;
        }

        if (hitEntity != null) {
            if (isFolia) scheduler.runAtEntity(hitEntity, firework -> detonateFirework(hitEntity.getLocation(), event.getWrappedSnowball()));
            else detonateFirework(hitEntity.getLocation(), event.getWrappedSnowball());
            return;
        }

        final Block hitBlock = event.getHitBlock();

        if (hitBlock != null) {
            final BlockFace hitFace = event.getHitBlockFace();
            final Location fireworkLoc = hitFace != null ? hitBlock.getRelative(hitFace).getLocation().toCenterLocation() : hitBlock.getLocation().toCenterLocation();
            if (isFolia) scheduler.runAtLocation(fireworkLoc, firework -> detonateFirework(fireworkLoc, event.getWrappedSnowball()));
            else detonateFirework(hitBlock.getLocation(), event.getWrappedSnowball());
        }
    }

    private void detonateFirework(final Location explosionLoc, final WrappedSnowball snowball) {
        Firework firework = explosionLoc.getWorld().spawn(explosionLoc, Firework.class);
        FireworkMeta meta = firework.getFireworkMeta();
        meta.clearEffects();
        meta.addEffect(FireworkEffect.builder()
                .withColor(snowball.getPrimaryColor(), snowball.getSecondaryColor())
                .flicker(flicker)
                .trail(trail)
                .build());
        firework.setFireworkMeta(meta);
        firework.setShooter(snowball.snowball().getShooter()); // Copy over shooter for damage tracking
        firework.detonate();
    }
}