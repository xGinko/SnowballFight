package me.xginko.snowballfight.modules;

import com.cryptomorin.xseries.XEntityType;
import me.xginko.snowballfight.SnowballFight;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class RemoveArmorOnHit extends SnowballModule implements Listener {

    private final Set<Material> materials;
    private final boolean onlyPlayers;

    protected RemoveArmorOnHit() {
        super("settings.drop-armor", false,
                "\nWill remove and drop configured material in the armor slots if a player gets hit by a snowball.");
        this.onlyPlayers = config.getBoolean("settings.drop-armor.only-thrown-by-player", true,
                "If enabled will only work if the snowball was thrown by a player.");
        this.materials = config.getList("settings.drop-armor.materials", Collections.singletonList("ELYTRA"))
                .stream()
                .map(configuredType -> {
                    try {
                        return Material.valueOf(configuredType);
                    } catch (IllegalArgumentException e) {
                        warn("Material '" + configuredType + "' not recognized. " +
                                "Please use correct values from: https://jd.papermc.io/paper/1.20/org/bukkit/Material.html");
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(Material.class)));
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
        if (event.getHitEntity() == null || event.getHitEntity().getType() != XEntityType.PLAYER.get()) return;
        if (onlyPlayers && !(event.getEntity().getShooter() instanceof Player)) return;

        final Player player = (Player) event.getHitEntity();
        final PlayerInventory playerInventory = player.getInventory();
        ItemStack[] armorContents = playerInventory.getArmorContents();

        boolean changedSomething = false;

        for (int i = 0; i < armorContents.length; i++) {
            ItemStack armorItem = armorContents[i];
            if (armorItem != null && materials.contains(armorItem.getType())) {
                if (SnowballFight.isServerFolia()) {
                    SnowballFight.scheduling().entitySpecificScheduler(player)
                            .run(() -> player.getWorld().dropItemNaturally(player.getLocation(), armorItem), null);
                } else {
                    player.getWorld().dropItemNaturally(player.getLocation(), armorItem);
                }
                armorContents[i] = null;
                changedSomething = true;
            }
        }

        if (changedSomething) {
            playerInventory.setArmorContents(armorContents);
        }
    }
}