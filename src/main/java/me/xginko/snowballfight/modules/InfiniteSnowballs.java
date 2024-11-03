package me.xginko.snowballfight.modules;

import com.cryptomorin.xseries.XMaterial;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class InfiniteSnowballs extends SnowballModule implements Listener {

    protected InfiniteSnowballs() {
        super("settings.infinite-snowballs", false,
                "\nIf enabled, will stop snowballs from being consumed for players.");
    }

    @Override
    public void enable() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void disable() {
        HandlerList.unregisterAll(this);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    private void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getMaterial() != XMaterial.SNOWBALL.parseMaterial()) return;

        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            event.setCancelled(true);
            event.getPlayer().launchProjectile(Snowball.class);
        }
    }
}