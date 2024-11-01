package me.xginko.snowballfight.modules;

import com.cryptomorin.xseries.XMaterial;
import me.xginko.snowballfight.SnowballConfig;
import me.xginko.snowballfight.SnowballFight;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class InfiniteSnowballs implements SnowballModule, Listener {

    protected InfiniteSnowballs() {
        shouldEnable();
        SnowballConfig config = SnowballFight.config();
        config.master().addComment("settings.infinite-snowballs",
                "\nIf enabled, will stop snowballs from being consumed for players.");
    }

    @Override
    public boolean shouldEnable() {
        return SnowballFight.config().getBoolean("settings.infinite-snowballs.enable", false);
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

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    private void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getMaterial() != XMaterial.SNOWBALL.parseMaterial()) return;

        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            event.setCancelled(true);
            event.getPlayer().launchProjectile(Snowball.class);
        }
    }
}