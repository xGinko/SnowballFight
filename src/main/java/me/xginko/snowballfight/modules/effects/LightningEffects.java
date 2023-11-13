package me.xginko.snowballfight.modules.effects;

import me.xginko.pumpkinpvpreloaded.PumpkinPVPConfig;
import me.xginko.pumpkinpvpreloaded.PumpkinPVPReloaded;
import me.xginko.pumpkinpvpreloaded.events.PostPumpkinExplodeEvent;
import me.xginko.pumpkinpvpreloaded.events.PostPumpkinHeadEntityExplodeEvent;
import me.xginko.pumpkinpvpreloaded.modules.PumpkinPVPModule;
import me.xginko.snowballfight.SnowballConfig;
import me.xginko.snowballfight.SnowballFight;
import me.xginko.snowballfight.modules.SnowballModule;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.Nullable;

import java.util.Random;
import java.util.UUID;

public class LightningEffects implements SnowballModule, Listener {

    private final SnowballFight plugin;
    private final boolean deal_damage;
    private final int spawn_amount, flashcount;
    private final double probability;

    public LightningEffects() {
        shouldEnable();
        this.plugin = SnowballFight.getInstance();
        SnowballConfig config = SnowballFight.getConfiguration();
        config.addComment("lightning-effects", "Will strike the closest player with lightning.");
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
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void disable() {
        HandlerList.unregisterAll(this);
    }

    @EventHandler(priority = EventPriority.LOW)
    private void onEvent() {
        if ((probability >= 1 || new Random().nextDouble() <= probability)) {
            strikeLightning();
        }
    }

    private void strikeLightning(@Nullable final UUID exploder, final Location explosionLoc) {
        Player closestPlayer = null;
        double distance = 100;
        for (Player player : explosionLoc.getNearbyPlayers(6, 6, 6)) {
            if (exploder != null && player.getUniqueId().equals(exploder)) continue;
            double currentDistance = explosionLoc.distance(player.getLocation());
            if (currentDistance < distance) {
                closestPlayer = player;
                distance = currentDistance;
            }
        }

        if (closestPlayer == null) return;

        final Location playerLoc = closestPlayer.getLocation();
        final World world = playerLoc.getWorld();
        closestPlayer.getScheduler().run(plugin, strike -> {
            for (int i = 0; i < spawn_amount; i++) {
                (deal_damage ? world.strikeLightning(playerLoc) : world.strikeLightningEffect(playerLoc)).setFlashCount(flashcount);
            }
        }, null);
    }
}